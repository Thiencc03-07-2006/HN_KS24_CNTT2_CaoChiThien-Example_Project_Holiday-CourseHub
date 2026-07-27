package com.coursehub.controller;

import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.InstructorDashboardStatsResponse;
import com.coursehub.dto.response.InstructorReviewStatsResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.ReviewResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.InstructorDashboardService;
import com.coursehub.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructor")
@RequiredArgsConstructor
@Tag(name = "Instructor Dashboard", description = "Số liệu thống kê dành cho Giảng viên")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorController {

    private final InstructorDashboardService instructorDashboardService;
    private final ReviewService reviewService;

    @GetMapping("/dashboard")
    @Operation(summary = "Lấy số liệu thống kê Dashboard Giảng viên")
    public ResponseEntity<ApiResponse<InstructorDashboardStatsResponse>> getStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(instructorDashboardService.getStats(principal.getId())));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Danh sách đánh giá các khóa học của tôi")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getInstructorReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReviewResponse> responses = reviewService.getInstructorReviews(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/reviews/stats")
    @Operation(summary = "Thống kê đánh giá của tôi (số sao và phân bố)")
    public ResponseEntity<ApiResponse<InstructorReviewStatsResponse>> getInstructorReviewStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        InstructorReviewStatsResponse stats = reviewService.getInstructorStats(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
