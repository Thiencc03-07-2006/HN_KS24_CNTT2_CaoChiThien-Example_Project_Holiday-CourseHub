package com.coursehub.service;

import com.coursehub.dto.response.AdminDashboardStatsResponse;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.UserProfileResponse;

import java.util.UUID;

public interface AdminService {
    AdminDashboardStatsResponse getDashboardStats();
    PageResponse<UserProfileResponse> searchUsers(String keyword, String status, String role, int page, int size);
    UserProfileResponse getUserDetail(UUID userId);
    UserProfileResponse updateUserStatus(UUID userId, String status);
    UserProfileResponse addUserRole(UUID userId, String roleName);
    UserProfileResponse removeUserRole(UUID userId, String roleName);
    PageResponse<CourseResponse> getCoursesForReview(String keyword, String status, int page, int size);
    com.coursehub.dto.response.SystemStatisticsResponse getSystemStatistics();
}
