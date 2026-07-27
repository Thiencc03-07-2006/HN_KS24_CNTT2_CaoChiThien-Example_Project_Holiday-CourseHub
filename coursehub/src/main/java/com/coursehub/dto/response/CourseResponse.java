package com.coursehub.dto.response;

import com.coursehub.enums.CourseLevel;
import com.coursehub.enums.CourseStatus;
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
public class CourseResponse {
    private UUID id;
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private String thumbnailUrl;
    private String promoVideoUrl;
    private CourseLevel level;
    private String language;
    private CourseStatus status;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Long totalStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String rejectReason;
    private LocalDateTime rejectedAt;
    private String blockedReason;
    private BlockedBySummary blockedBy;
    private LocalDateTime blockedAt;


    // Nested objects
    private InstructorSummary instructor;
    private CategorySummary category;
    private List<ChapterResponse> chapters;

    // Curriculum summary
    private Integer totalChapters;
    private Integer totalLessons;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InstructorSummary {
        private UUID id;
        private String fullName;
        private String avatarUrl;
        private String headline;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategorySummary {
        private Long id;
        private String name;
        private String slug;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BlockedBySummary {
        private UUID id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}
