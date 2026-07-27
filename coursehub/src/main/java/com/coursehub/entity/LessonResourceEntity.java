package com.coursehub.entity;

import com.coursehub.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;
import java.util.UUID;

@Entity
@Table(name = "lesson_resources",
        indexes = @Index(name = "idx_lesson_resources_lesson_id", columnList = "lesson_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResourceEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private LessonEntity lesson;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "is_downloadable", nullable = false)
    @Builder.Default
    private Boolean isDownloadable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", length = 20)
    @Builder.Default
    private VideoStatus videoStatus = VideoStatus.NONE;
}
