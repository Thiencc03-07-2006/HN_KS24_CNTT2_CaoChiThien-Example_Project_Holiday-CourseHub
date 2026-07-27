package com.coursehub.repository;

import com.coursehub.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndIsRead(UUID recipientId, boolean isRead);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.id = :id AND n.recipient.id = :userId")
    int markAsRead(@Param("id") UUID id, @Param("userId") UUID userId);
}
