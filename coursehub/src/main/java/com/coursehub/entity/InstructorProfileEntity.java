package com.coursehub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_profiles",
        indexes = @Index(name = "idx_instructor_profiles_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorProfileEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "headline", nullable = false, length = 80)
    private String headline;

    @Column(name = "detailed_bio", nullable = false, columnDefinition = "TEXT")
    private String detailedBio;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "payout_bank_name", length = 100)
    private String payoutBankName;

    @Column(name = "payout_account_number", length = 50)
    private String payoutAccountNumber;

    @Column(name = "payout_account_name", length = 100)
    private String payoutAccountName;

    @Column(name = "total_students", nullable = false)
    @Builder.Default
    private Integer totalStudents = 0;

    @Column(name = "total_courses", nullable = false)
    @Builder.Default
    private Integer totalCourses = 0;

    @Column(name = "average_rating", nullable = false)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
