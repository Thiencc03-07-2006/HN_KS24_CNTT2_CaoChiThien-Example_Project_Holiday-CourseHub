package com.coursehub.entity;

import com.coursehub.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports",
        indexes = {
                @Index(name = "idx_reports_reporter_id", columnList = "reporter_id"),
                @Index(name = "idx_reports_reportable", columnList = "reportable_type, reportable_id"),
                @Index(name = "idx_reports_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    @Column(name = "reportable_type", nullable = false, length = 50)
    private String reportableType;

    @Column(name = "reportable_id", nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID reportableId;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "admin_note", length = 255)
    private String adminNote;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
