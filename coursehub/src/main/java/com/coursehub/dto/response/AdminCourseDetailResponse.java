package com.coursehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseDetailResponse {
    private UUID id;
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private String thumbnailUrl;
    private String promoVideoUrl;
    private String level;
    private String language;
    private String status;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Long enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Block details
    private String blockedReason;
    private BlockedByInfo blockedBy;
    private LocalDateTime blockedAt;

    // Reject details
    private String rejectReason;
    private String rejectedBy;
    private LocalDateTime rejectedAt;

    private InstructorInfo instructor;
    private CategoryInfo category;
    private List<ChapterDetail> chapters;
    private List<ReviewResponse> reviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorInfo {
        private UUID id;
        private String fullName;
        private String avatarUrl;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterDetail {
        private UUID id;
        private String title;
        private Integer orderIndex;
        private List<LessonDetail> lessons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonDetail {
        private UUID id;
        private String title;
        private Integer orderIndex;
        private String lessonType;
        private Boolean isPreview;
        private String resourceUrl;
        private Integer durationSeconds;
        private String textContent;
        private Boolean isDownloadable;
        private List<QuestionResponse> questions; // loaded if quiz
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockedByInfo {
        private UUID id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}
