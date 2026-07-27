package com.coursehub.service;

import com.coursehub.constant.AppConstants;
import com.coursehub.dto.request.LoginRequest;
import com.coursehub.dto.request.RegisterRequest;
import com.coursehub.dto.response.AuthResponse;
import com.coursehub.entity.RefreshTokenEntity;
import com.coursehub.entity.RoleEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.enums.UserStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.CourseHubException;
import com.coursehub.repository.*;
import com.coursehub.security.JwtUtils;
import com.coursehub.service.TokenCleanupService;
import com.coursehub.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Tests: register, login, refreshToken, logout flows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private EmailService emailService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private TokenCleanupService tokenCleanupService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test@12345";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String MOCK_ACCESS_TOKEN = "mock.access.token";
    private static final long REFRESH_TOKEN_EXP_MS = 604_800_000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", REFRESH_TOKEN_EXP_MS);
        // Default: valueOps always available
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ==================== REGISTER ====================

    @Test
    @DisplayName("register_success — new email + valid password → user saved, OTP sent")
    void register_success() {
        // Given
        RegisterRequest req = new RegisterRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);
        req.setFullName(TEST_FULL_NAME);

        RoleEntity studentRole = RoleEntity.builder().id(1L).name(AppConstants.ROLE_STUDENT).build();

        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(roleRepository.findByName(AppConstants.ROLE_STUDENT)).willReturn(Optional.of(studentRole));
        given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn("$2a$10$hashed");

        // When
        authService.register(req);

        // Then
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals(TEST_EMAIL)
                        && u.getStatus() == UserStatus.PENDING_VERIFICATION
                        && u.getRoles().contains(studentRole)
        ));
        verify(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());
        verify(valueOps).set(
                startsWith(AppConstants.REDIS_OTP_PREFIX),
                anyString(),
                eq((long) AppConstants.OTP_TTL_MINUTES),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("register_duplicateEmail — existing email → BadRequestException AUTH_001")
    void register_duplicateEmail_throwsBadRequestException() {
        // Given
        RegisterRequest req = new RegisterRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);
        req.setFullName(TEST_FULL_NAME);

        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email đã được đăng ký");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    // ==================== LOGIN ====================

    @Test
    @DisplayName("login_success — valid credentials → returns accessToken + refreshToken")
    void login_success() {
        // Given
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        RoleEntity studentRole = RoleEntity.builder().id(1L).name(AppConstants.ROLE_STUDENT).build();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$hashed")
                .fullName(TEST_FULL_NAME)
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build();

        String lockKey = AppConstants.REDIS_ACCOUNT_LOCKED_PREFIX + TEST_EMAIL;
        given(redisTemplate.hasKey(lockKey)).willReturn(false);
        given(userRepository.findByEmailWithRoles(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, "$2a$10$hashed")).willReturn(true);
        given(jwtUtils.generateAccessToken(any(UUID.class), eq(TEST_EMAIL))).willReturn(MOCK_ACCESS_TOKEN);
        given(jwtUtils.getAccessTokenExpirationMs()).willReturn(900_000L);
        given(refreshTokenRepository.save(any(RefreshTokenEntity.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        AuthResponse response = authService.login(req);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(MOCK_ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getUser().getEmail()).isEqualTo(TEST_EMAIL);
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("login_wrongPassword — invalid password → BadCredentialsException, attempts incremented")
    void login_wrongPassword_incrementsAttempts() {
        // Given
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword("WrongPassword@1");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$hashed")
                .status(UserStatus.ACTIVE)
                .build();

        String lockKey = AppConstants.REDIS_ACCOUNT_LOCKED_PREFIX + TEST_EMAIL;
        String failKey = AppConstants.REDIS_LOGIN_FAIL_PREFIX + TEST_EMAIL;

        given(redisTemplate.hasKey(lockKey)).willReturn(false);
        given(userRepository.findByEmailWithRoles(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("WrongPassword@1", "$2a$10$hashed")).willReturn(false);
        given(valueOps.increment(failKey)).willReturn(1L); // first failed attempt

        // When / Then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(valueOps).increment(failKey);
    }

    @Test
    @DisplayName("login_accountLocked — Redis lock key exists → LockedException")
    void login_accountLocked_throwsLockedException() {
        // Given
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        String lockKey = AppConstants.REDIS_ACCOUNT_LOCKED_PREFIX + TEST_EMAIL;
        given(redisTemplate.hasKey(lockKey)).willReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(LockedException.class);

        verify(userRepository, never()).findByEmailWithRoles(any());
    }

    @Test
    @DisplayName("login_maxAttemptsReached — 5th failed attempt → account locked for 15 min")
    void login_maxAttemptsReached_locksAccount() {
        // Given
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword("WrongPwd@1");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .passwordHash("$2a$10$hashed")
                .status(UserStatus.ACTIVE)
                .build();

        String lockKey = AppConstants.REDIS_ACCOUNT_LOCKED_PREFIX + TEST_EMAIL;
        String failKey = AppConstants.REDIS_LOGIN_FAIL_PREFIX + TEST_EMAIL;

        given(redisTemplate.hasKey(lockKey)).willReturn(false);
        given(userRepository.findByEmailWithRoles(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("WrongPwd@1", "$2a$10$hashed")).willReturn(false);
        given(valueOps.increment(failKey)).willReturn((long) AppConstants.LOGIN_MAX_ATTEMPTS); // 5th attempt

        // When / Then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(LockedException.class)
                .hasMessageContaining("khóa");

        verify(valueOps).set(
                eq(lockKey), eq("locked"),
                eq((long) AppConstants.LOGIN_LOCK_DURATION_MINUTES),
                eq(TimeUnit.MINUTES)
        );
        verify(redisTemplate).delete(failKey);
    }

    // ==================== REFRESH TOKEN ====================

    @Test
    @DisplayName("refreshToken_valid — valid non-revoked token → old token revoked, new token issued")
    void refreshToken_valid_rotatesToken() {
        // Given
        String oldTokenStr = UUID.randomUUID().toString();
        RoleEntity role = RoleEntity.builder().id(1L).name(AppConstants.ROLE_STUDENT).build();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .fullName(TEST_FULL_NAME)
                .roles(Set.of(role))
                .build();

        RefreshTokenEntity oldToken = RefreshTokenEntity.builder()
                .id(1L)
                .user(user)
                .token(oldTokenStr)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByTokenWithLock(oldTokenStr)).willReturn(Optional.of(oldToken));
        given(refreshTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(jwtUtils.generateAccessToken(any(), any())).willReturn(MOCK_ACCESS_TOKEN);
        given(jwtUtils.getAccessTokenExpirationMs()).willReturn(900_000L);

        // When
        AuthResponse response = authService.refreshAccessToken(oldTokenStr);

        // Then
        assertThat(response.getAccessToken()).isEqualTo(MOCK_ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isNotEqualTo(oldTokenStr); // new token different
        assertThat(oldToken.getRevoked()).isTrue(); // old token revoked
    }

    @Test
    @DisplayName("refreshToken_revoked — reuse of revoked token → all tokens revoked, CourseHubException AUTH_006")
    void refreshToken_revokedToken_throwsSecurityException() {
        // Given
        String revokedTokenStr = UUID.randomUUID().toString();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .build();

        RefreshTokenEntity revokedToken = RefreshTokenEntity.builder()
                .id(1L)
                .user(user)
                .token(revokedTokenStr)
                .revoked(true)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByTokenWithLock(revokedTokenStr)).willReturn(Optional.of(revokedToken));

        // When / Then
        assertThatThrownBy(() -> authService.refreshAccessToken(revokedTokenStr))
                .isInstanceOf(CourseHubException.class)
                .hasMessageContaining("RTR");

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    @Test
    @DisplayName("refreshToken_expired — expired token → CourseHubException AUTH_005")
    void refreshToken_expired_throwsException() {
        // Given
        String expiredTokenStr = UUID.randomUUID().toString();
        UserEntity user = UserEntity.builder().id(UUID.randomUUID()).email(TEST_EMAIL).build();

        RefreshTokenEntity expiredToken = RefreshTokenEntity.builder()
                .id(2L)
                .user(user)
                .token(expiredTokenStr)
                .revoked(false)
                .expiryDate(LocalDateTime.now().minusDays(1)) // expired
                .build();

        given(refreshTokenRepository.findByTokenWithLock(expiredTokenStr)).willReturn(Optional.of(expiredToken));

        // When / Then
        assertThatThrownBy(() -> authService.refreshAccessToken(expiredTokenStr))
                .isInstanceOf(CourseHubException.class)
                .hasMessageContaining("hết hạn");

        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    // ==================== LOGOUT ====================

    @Test
    @DisplayName("logout_withToken — valid refreshToken provided → token revoked")
    void logout_withToken_revokesToken() {
        // Given
        String token = UUID.randomUUID().toString();

        // When
        authService.logout(token);

        // Then
        verify(refreshTokenRepository).revokeByToken(token);
    }

    @Test
    @DisplayName("logout_withoutToken — null token → no action taken")
    void logout_withoutToken_noAction() {
        // When
        authService.logout(null);

        // Then
        verify(refreshTokenRepository, never()).revokeByToken(any());
    }
}
