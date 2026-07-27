package com.coursehub.controller;

import com.coursehub.dto.request.CreateReviewRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.RatingSummaryResponse;
import com.coursehub.dto.response.ReviewResponse;
import com.coursehub.entity.ReviewEntity;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Quản lý đánh giá và xếp hạng khóa học")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/courses/{courseId}/reviews")
    @Operation(summary = "Đánh giá khóa học hoặc cập nhật nếu đã tồn tại")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewEntity review = reviewService.createOrUpdateReview(principal.getId(), courseId, request.getRating(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đánh giá khóa học thành công.", mapToResponse(review)));
    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "Chỉnh sửa đánh giá")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID reviewId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewEntity review = reviewService.updateReview(principal.getId(), reviewId, request.getRating(), request.getComment());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công.", mapToResponse(review)));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "Xóa đánh giá (chủ sở hữu hoặc admin)")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID reviewId) {
        boolean isAdmin = principal.hasRole("ROLE_ADMIN");
        reviewService.deleteReview(principal.getId(), reviewId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công."));
    }

    @GetMapping("/courses/{courseId}/reviews")
    @Operation(summary = "Lấy danh sách đánh giá của khóa học (phân trang và sắp xếp)")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getCourseReviews(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        PageResponse<ReviewResponse> responses = reviewService.getCourseReviews(courseId, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/courses/{courseId}/rating-summary")
    @Operation(summary = "Lấy thống kê đánh giá của khóa học")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getRatingSummary(@PathVariable UUID courseId) {
        RatingSummaryResponse summary = reviewService.getRatingSummary(courseId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/admin/reviews")
    @Operation(summary = "Danh sách đánh giá cho Admin (tìm kiếm và lọc)")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsForAdmin(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReviewResponse> responses = reviewService.getReviewsForAdmin(keyword, courseId, userId, rating, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    private ReviewResponse mapToResponse(ReviewEntity review) {
        boolean isEdited = review.getUpdatedAt() != null && review.getCreatedAt() != null 
                && !review.getUpdatedAt().isEqual(review.getCreatedAt());

        return ReviewResponse.builder()
                .id(review.getId())
                .studentId(review.getEnrollment().getUser().getId())
                .studentName(review.getEnrollment().getUser().getFullName())
                .studentAvatar(review.getEnrollment().getUser().getAvatarUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .isEdited(isEdited)
                .courseId(review.getEnrollment().getCourse().getId())
                .courseTitle(review.getEnrollment().getCourse().getTitle())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
