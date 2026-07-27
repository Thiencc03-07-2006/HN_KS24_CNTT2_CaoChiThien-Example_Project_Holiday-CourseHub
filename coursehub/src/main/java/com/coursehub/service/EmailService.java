package com.coursehub.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
    void sendPasswordResetEmail(String to, String token);
    void sendHtmlEmail(String to, String subject, String htmlContent);
}
