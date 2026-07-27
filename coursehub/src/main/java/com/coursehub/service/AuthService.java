package com.coursehub.service;

import com.coursehub.dto.request.*;
import com.coursehub.dto.response.AuthResponse;

public interface AuthService {
    void register(RegisterRequest request);
    void verifyOtp(VerifyOtpRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshAccessToken(String refreshToken);
    void logout(String refreshToken);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    AuthResponse mockLogin(OAuth2MockRequest request);
}
