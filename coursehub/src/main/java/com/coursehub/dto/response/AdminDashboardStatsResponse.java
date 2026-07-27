package com.coursehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsResponse {
    private long totalUsers;
    private long totalCourses;
    private long totalCategories;
    private long totalEnrollments;
    private List<UserProfileResponse> recentUsers;
    private List<CourseResponse> recentCourses;
    private long totalWishlist;
    private List<CourseResponse> top10FavoriteCourses;
    private long totalCourseReports;
    private long totalCommentReports;
    private long pendingReportsCount;
    private long resolvedReportsCount;
}
