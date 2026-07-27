package com.coursehub.entity;

import com.coursehub.enums.CourseLevel;
import com.coursehub.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "courses",
        indexes = {
                @Index(name = "idx_courses_instructor_id", columnList = "instructor_id"),
                @Index(name = "idx_courses_category_id", columnList = "category_id"),
                @Index(name = "idx_courses_status", columnList = "status"),
                @Index(name = "idx_courses_slug", columnList = "slug")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private UserEntity instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String description = "";

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "promo_video_url", length = 500)
    private String promoVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    @Builder.Default
    private CourseLevel level = CourseLevel.BEGINNER;

    @Column(name = "language", nullable = false, length = 50)
    @Builder.Default
    private String language = "Vietnamese";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "blocked_reason", length = 500)
    private String blockedReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by")
    private UserEntity blockedBy;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    @Column(name = "rejected_by", length = 36)
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    // Relationships
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<ChapterEntity> chapters = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return this.instructor != null && this.instructor.getId().equals(userId);
    }

    public boolean isEditable() {
        return status == CourseStatus.DRAFT || status == CourseStatus.REJECTED || status == CourseStatus.BLOCKED || status == CourseStatus.BLOCKED_EDITED;
    }
}
