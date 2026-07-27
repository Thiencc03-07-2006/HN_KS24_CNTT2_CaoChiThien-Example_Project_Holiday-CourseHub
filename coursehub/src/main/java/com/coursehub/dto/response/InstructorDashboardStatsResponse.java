package com.coursehub.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorDashboardStatsResponse {
    private long totalCourses;
    private long publishedCourses;
    private long totalStudents;
    private BigDecimal totalRevenue;
    private double averageRating;
    private long totalReviews;
    private long totalWishlist;
    private long totalFavorites;
    private java.util.List<EnrollmentTimelineData> enrollmentTimeline;
    private java.util.List<FavoriteCourseData> topFavoriteCourses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrollmentTimelineData {
        private String date;
        private String courseName;
        private long enrollmentCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FavoriteCourseData {
        private CourseResponse course;
        private long favoriteCount;
    }
}
