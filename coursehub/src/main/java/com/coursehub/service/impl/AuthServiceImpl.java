package com.coursehub.service.impl;

import com.coursehub.constant.AppConstants;
import com.coursehub.dto.request.*;
import com.coursehub.dto.response.AuthResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.UserStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.CourseHubException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.security.JwtUtils;
import com.coursehub.service.AuthService;
import com.coursehub.service.EmailService;
import com.coursehub.service.TokenCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final TokenCleanupService tokenCleanupService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("AUTH_001", "Email đã được đăng ký trên hệ thống.");
        }

        RoleEntity studentRole = roleRepository.findByName(AppConstants.ROLE_STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", AppConstants.ROLE_STUDENT));

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(UserStatus.PENDING_VERIFICATION)
                .roles(Set.of(studentRole))
                .build();

        userRepository.save(user);

        // Generate and send OTP
        String otp = generateOtp();
        saveOtpToRedis(request.getEmail(), otp);
        emailService.sendOtpEmail(request.getEmail(), otp);
        log.info("User registered and OTP sent to: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("VALID_001", "Tài khoản đã được xác thực trước đó.");
        }

        String redisOtpKey = AppConstants.REDIS_OTP_PREFIX + request.getEmail();
        String redisAttemptsKey = AppConstants.REDIS_OTP_ATTEMPTS_PREFIX + request.getEmail();

        String savedOtp = redisTemplate.opsForValue().get(redisOtpKey);
        if (savedOtp == null) {
            throw new BadRequestException("AUTH_003", "Mã OTP kích hoạt đã hết hiệu lực hoặc chưa được gửi.");
        }

        if (!savedOtp.equals(request.getOtpCode())) {
            Long attempts = redisTemplate.opsForValue().increment(redisAttemptsKey);
            if (attempts != null && attempts >= AppConstants.OTP_MAX_ATTEMPTS) {
                redisTemplate.delete(redisOtpKey);
                redisTemplate.delete(redisAttemptsKey);
                throw new BadRequestException("AUTH_003", "Mã OTP đã bị khóa do nhập sai quá nhiều lần. Vui lòng yêu cầu mã OTP mới.");
            }
            throw new BadRequestException("AUTH_004", "Mã OTP không chính xác. Bạn còn " + (AppConstants.OTP_MAX_ATTEMPTS - attempts) + " lần thử.");
        }

        // OTP matches
        redisTemplate.delete(redisOtpKey);
        redisTemplate.delete(redisAttemptsKey);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User email verified successfully: {}", request.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String lockKey = AppConstants.REDIS_ACCOUNT_LOCKED_PREFIX + request.getEmail();
        String failKey = AppConstants.REDIS_LOGIN_FAIL_PREFIX + request.getEmail();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new LockedException("Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần.");
        }

        UserEntity user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không chính xác."));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new CourseHubException("AUTHZ_001", "Tài khoản đã bị khóa do vi phạm quy chế.", HttpStatus.FORBIDDEN);
        }

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("VALID_001", "Tài khoản chưa được kích hoạt OTP.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            Long attempts = redisTemplate.opsForValue().increment(failKey);
            if (attempts != null && attempts >= AppConstants.LOGIN_MAX_ATTEMPTS) {
                redisTemplate.opsForValue().set(lockKey, "locked", AppConstants.LOGIN_LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                redisTemplate.delete(failKey);
                throw new LockedException("Tài khoản bị khóa tạm thời 15 phút.");
            }
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác.");
        }

        // Login success, clear login fail records
        redisTemplate.delete(failKey);
        redisTemplate.delete(lockKey);

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenString = UUID.randomUUID().toString();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .build();
        
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user, accessToken, refreshTokenString);
    }

    @Override
    @Transactional(noRollbackFor = { CourseHubException.class })
    public AuthResponse refreshAccessToken(String refreshToken) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenWithLock(refreshToken)
                .orElseThrow(() -> new CourseHubException("AUTH_005", "Refresh token không hợp lệ hoặc không tồn tại.", HttpStatus.UNAUTHORIZED));

        if (!tokenEntity.isValid()) {
            if (tokenEntity.getRevoked()) {
                // RTR Breach: Revoke all tokens for this user!
                refreshTokenRepository.revokeAllByUserId(tokenEntity.getUser().getId());
                throw new CourseHubException("AUTH_006", "Phiên đăng nhập bị thu hồi do phát hiện rủi ro bảo mật (RTR).", HttpStatus.UNAUTHORIZED);
            }
            throw new CourseHubException("AUTH_005", "Refresh token đã hết hạn.", HttpStatus.UNAUTHORIZED);
        }

        // Rotate Refresh Token
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        UserEntity user = tokenEntity.getUser();
        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshTokenString = UUID.randomUUID().toString();

        RefreshTokenEntity newRefreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(newRefreshTokenString)
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .build();

        refreshTokenRepository.save(newRefreshToken);

        // Async cleanup of revoked/expired tokens for user via TokenCleanupService
        tokenCleanupService.cleanupExpiredAndRevokedTokens(user.getId());

        return buildAuthResponse(user, newAccessToken, newRefreshTokenString);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.revokeByToken(refreshToken);
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Return 200 regardless of email existence to prevent user enumeration
        userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.invalidateAllByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                    .user(user)
                    .token(token)
                    .expiryDate(LocalDateTime.now().plusMinutes(AppConstants.PASSWORD_RESET_TTL_MINUTES))
                    .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            log.info("Password reset token generated and sent to: {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("VALID_001", "Mật khẩu xác nhận không khớp.");
        }

        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("AUTH_008", "Token đặt lại mật khẩu không hợp lệ."));

        if (tokenEntity.getUsed()) {
            throw new BadRequestException("AUTH_010", "Token đặt lại mật khẩu đã được sử dụng.");
        }

        if (tokenEntity.isExpired()) {
            throw new BadRequestException("AUTH_009", "Token đặt lại mật khẩu đã hết hạn.");
        }

        UserEntity user = tokenEntity.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);

        // Force logout all devices by revoking all refresh tokens
        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("Password reset successful and all sessions revoked for user: {}", user.getEmail());
    }

    private String generateOtp() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000);
        return String.valueOf(otp);
    }

    private void saveOtpToRedis(String email, String otp) {
        String key = AppConstants.REDIS_OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, AppConstants.OTP_TTL_MINUTES, TimeUnit.MINUTES);
    }

    private AuthResponse buildAuthResponse(UserEntity user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtUtils.getAccessTokenExpirationMs())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .roles(user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet()))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse mockLogin(OAuth2MockRequest request) {
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseGet(() -> {
                    RoleEntity studentRole = roleRepository.findByName(AppConstants.ROLE_STUDENT)
                            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", AppConstants.ROLE_STUDENT));
                    UserEntity newUser = UserEntity.builder()
                            .email(request.getEmail())
                            .fullName(request.getName())
                            .status(UserStatus.ACTIVE)
                            .roles(Set.of(studentRole))
                            .build();
                    return userRepository.save(newUser);
                });

        if (user.getStatus() == UserStatus.BANNED) {
            throw new CourseHubException("AUTHZ_001", "Tài khoản đã bị khóa do vi phạm quy chế.", HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenString = UUID.randomUUID().toString();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .build();
        
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user, accessToken, refreshTokenString);
    }
}
