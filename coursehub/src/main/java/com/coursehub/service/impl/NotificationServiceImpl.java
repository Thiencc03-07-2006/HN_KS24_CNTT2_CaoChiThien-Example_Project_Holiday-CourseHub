package com.coursehub.service.impl;

import com.coursehub.entity.NotificationEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.enums.NotificationType;
import com.coursehub.repository.NotificationRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendNotification(UUID recipientId, String title, String content,
                                 NotificationType type, String targetUrl) {
        try {
            UserEntity recipient = userRepository.getReferenceById(recipientId);
            NotificationEntity notification = NotificationEntity.builder()
                    .recipient(recipient)
                    .title(title)
                    .content(content)
                    .notificationType(type)
                    .targetUrl(targetUrl)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.debug("Notification sent to user {}: {}", recipientId, title);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", recipientId, e.getMessage());
        }
    }
}
