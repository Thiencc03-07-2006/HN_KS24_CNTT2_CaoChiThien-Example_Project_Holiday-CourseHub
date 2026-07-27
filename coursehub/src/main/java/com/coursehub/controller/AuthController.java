package com.coursehub.controller;

import com.coursehub.constant.AppConstants;
import com.coursehub.dto.request.*;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.AuthResponse;
import com.coursehub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Authentication API Controller — FR-01, FR-27
 * Endpoints: register, verify-otp, login, logout, refresh-token, forgot-password, reset-password
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Xác thực và phân quyền người dùng")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    // ==================== REGISTER ====================

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới với email và mật khẩu. Hệ thống gửi OTP 6 số về email để xác thực.")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để nhận mã OTP xác thực."));
    }

    // ==================== VERIFY OTP ====================

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực OTP", description = "Nhập mã OTP 6 số để kích hoạt tài khoản.")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt tài khoản thành công."));
    }

    // ==================== LOGIN ====================

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email/mật khẩu. Trả về accessToken. Refresh Token được set vào HTTP-Only cookie.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        // Hide refreshToken from response body (it's in cookie)
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công.", authResponse));
    }

    // ==================== REFRESH TOKEN ====================

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới Access Token", description = "Sử dụng Refresh Token từ cookie hoặc body để lấy Access Token mới (RTR - Rotate).")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request,
                                                                   HttpServletResponse response,
                                                                   @RequestBody(required = false) RefreshTokenRequest bodyRequest) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null && bodyRequest != null) {
            refreshToken = bodyRequest.getRefreshToken();
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("AUTH_005", "Không tìm thấy Refresh Token. Vui lòng đăng nhập lại."));
        }

        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công.", authResponse));
    }

    // ==================== LOGOUT ====================

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Thu hồi Refresh Token và xóa cookie. Cần đăng nhập lại để dùng tiếp.")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        authService.logout(refreshToken);
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công."));
    }

    // ==================== FORGOT PASSWORD ====================

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi link khôi phục mật khẩu về email (hiệu lực 15 phút). Không tiết lộ email có tồn tại hay không.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi link đặt lại mật khẩu. Vui lòng kiểm tra hộp thư."));
    }

    // ==================== RESET PASSWORD ====================

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt mật khẩu mới bằng token nhận được qua email.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."));
    }

    // ==================== OAUTH2 MOCK LOGIN ====================

    @PostMapping("/oauth2/mock")
    @Operation(summary = "Đăng nhập OAuth2 giả lập (GOOGLE) (FR-28)")
    public ResponseEntity<ApiResponse<AuthResponse>> oauth2MockLogin(
            @Valid @RequestBody OAuth2MockRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.mockLogin(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập Google thành công.", authResponse));
    }

    // ==================== HELPERS ====================

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null) return;
        Cookie cookie = new Cookie(AppConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);      // Set true in production (HTTPS)
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(AppConstants.REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> AppConstants.REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
