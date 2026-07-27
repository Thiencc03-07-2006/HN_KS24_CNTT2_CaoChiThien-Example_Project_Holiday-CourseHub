package com.coursehub.service;

import com.coursehub.enums.NotificationType;

import java.util.UUID;

public interface NotificationService {
    void sendNotification(UUID recipientId, String title, String content,
                          NotificationType type, String targetUrl);
}
