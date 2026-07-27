package com.coursehub.service;

import com.coursehub.entity.NotificationEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.enums.NotificationType;
import com.coursehub.repository.NotificationRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("sendNotification_success — saves notification entity")
    void sendNotification_success() {
        UUID recipientId = UUID.randomUUID();
        UserEntity recipient = UserEntity.builder().id(recipientId).build();

        given(userRepository.getReferenceById(recipientId)).willReturn(recipient);

        notificationService.sendNotification(
                recipientId,
                "New Course",
                "A new course has been published",
                NotificationType.SYSTEM,
                "/courses"
        );

        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    @DisplayName("sendNotification_exception — when exception thrown, logs it and does not propagate")
    void sendNotification_exception() {
        UUID recipientId = UUID.randomUUID();

        given(userRepository.getReferenceById(recipientId)).willThrow(new RuntimeException("DB Error"));

        // Should not throw exception
        notificationService.sendNotification(
                recipientId,
                "New Course",
                "A new course has been published",
                NotificationType.SYSTEM,
                "/courses"
        );

        verify(notificationRepository, never()).save(any());
    }
}
