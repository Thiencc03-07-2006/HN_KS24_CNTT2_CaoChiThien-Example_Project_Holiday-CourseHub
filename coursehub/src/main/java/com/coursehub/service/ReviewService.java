package com.coursehub.service;

import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.RatingSummaryResponse;
import com.coursehub.dto.response.ReviewResponse;
import com.coursehub.dto.response.InstructorReviewStatsResponse;
import com.coursehub.entity.ReviewEntity;

import java.util.UUID;

public interface ReviewService {
    ReviewEntity createOrUpdateReview(UUID userId, UUID courseId, int rating, String comment);
    ReviewEntity updateReview(UUID userId, UUID reviewId, int rating, String comment);
    void deleteReview(UUID userId, UUID reviewId, boolean isAdmin);
    PageResponse<ReviewResponse> getCourseReviews(UUID courseId, int page, int size, String sort);
    RatingSummaryResponse getRatingSummary(UUID courseId);
    PageResponse<ReviewResponse> getReviewsForAdmin(String keyword, UUID courseId, UUID userId, Integer rating, int page, int size);
    PageResponse<ReviewResponse> getInstructorReviews(UUID instructorId, int page, int size);
    InstructorReviewStatsResponse getInstructorStats(UUID instructorId);
}

