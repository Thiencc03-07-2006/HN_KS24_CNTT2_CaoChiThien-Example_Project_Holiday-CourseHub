package com.coursehub.service;

import com.coursehub.service.impl.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@coursehub.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    @DisplayName("sendOtpEmail_mockEmailTrue — logs without calling mailSender")
    void sendOtpEmail_mockEmailTrue() {
        ReflectionTestUtils.setField(emailService, "mockEmail", true);

        emailService.sendOtpEmail("test@example.com", "123456");

        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("sendOtpEmail_mockEmailFalse — creates message and sends it")
    void sendOtpEmail_mockEmailFalse() throws MessagingException {
        ReflectionTestUtils.setField(emailService, "mockEmail", false);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        emailService.sendOtpEmail("test@example.com", "123456");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendPasswordResetEmail_mockEmailTrue — logs without calling mailSender")
    void sendPasswordResetEmail_mockEmailTrue() {
        ReflectionTestUtils.setField(emailService, "mockEmail", true);

        emailService.sendPasswordResetEmail("test@example.com", "token-123");

        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("sendPasswordResetEmail_mockEmailFalse — creates message and sends it")
    void sendPasswordResetEmail_mockEmailFalse() throws MessagingException {
        ReflectionTestUtils.setField(emailService, "mockEmail", false);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        emailService.sendPasswordResetEmail("test@example.com", "token-123");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sendHtmlEmail_exception — catch MessagingException, no crash")
    void sendHtmlEmail_exception() throws MessagingException {
        ReflectionTestUtils.setField(emailService, "mockEmail", false);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Server down")).when(mailSender).send(any(MimeMessage.class));

        // Should handle exceptions internally
        emailService.sendHtmlEmail("test@example.com", "Subject", "Content");

        verify(mailSender).createMimeMessage();
    }
}
