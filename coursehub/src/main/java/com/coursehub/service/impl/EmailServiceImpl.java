package com.coursehub.service.impl;

import com.coursehub.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service implementation.
 * In dev mode (app.dev.mock-email=true), emails are logged to console instead of sent.
 * This allows local development without a real SMTP server.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@coursehub.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Mock email flag — set to true in dev profile to skip SMTP and log OTP/token to console.
     * Controlled via: app.dev.mock-email=true in application-dev.yml
     */
    @Value("${app.dev.mock-email:false}")
    private boolean mockEmail;

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        if (mockEmail) {
            log.warn("============================================================");
            log.warn("[DEV MODE] OTP EMAIL MOCK — Không gửi email thật");
            log.warn("[DEV MODE] To: {}", to);
            log.warn("[DEV MODE] OTP Code: {}", otp);
            log.warn("[DEV MODE] Dùng OTP này để xác thực tài khoản qua API verify-otp");
            log.warn("============================================================");
            return;
        }

        String subject = "CourseHub - Mã OTP xác thực tài khoản";
        String htmlContent = "<h3>Chào mừng bạn đến với CourseHub!</h3>"
                + "<p>Mã OTP để xác thực đăng ký tài khoản của bạn là: <strong>" + otp + "</strong></p>"
                + "<p>Mã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>";
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        if (mockEmail) {
            log.warn("============================================================");
            log.warn("[DEV MODE] PASSWORD RESET EMAIL MOCK — Không gửi email thật");
            log.warn("[DEV MODE] To: {}", to);
            log.warn("[DEV MODE] Reset Link: {}", resetLink);
            log.warn("============================================================");
            return;
        }

        String subject = "CourseHub - Yêu cầu khôi phục mật khẩu";
        String htmlContent = "<h3>Yêu cầu khôi phục mật khẩu</h3>"
                + "<p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>"
                + "<p>Vui lòng click vào đường link dưới đây để khôi phục mật khẩu (có hiệu lực trong 15 phút):</p>"
                + "<p><a href=\"" + resetLink + "\">Đặt lại mật khẩu mới</a></p>"
                + "<p>Nếu bạn không gửi yêu cầu này, vui lòng bỏ qua email này.</p>";
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mockEmail) {
            log.warn("[DEV MODE] HTML EMAIL MOCK — To: {}, Subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send email due to general mail sender error: {}", e.getMessage());
        }
    }
}
