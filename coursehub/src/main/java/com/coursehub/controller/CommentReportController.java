package com.coursehub.controller;

import com.coursehub.dto.request.CreateReportRequest;
import com.coursehub.dto.request.UpdateReportStatusRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.ReportResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.ReportService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comment Reports", description = "Quản lý báo cáo vi phạm nội dung bình luận & đánh giá")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentReportController {

    private final ReportService reportService;

    @PostMapping("/reviews/{reviewId}/report")
    @Operation(summary = "Báo cáo đánh giá vi phạm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponse>> reportReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.reportReview(
                reviewId,
                request.getReason(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo đánh giá vi phạm thành công.", response));
    }

    @PostMapping("/comments/{commentId}/report")
    @Operation(summary = "Báo cáo bình luận vi phạm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponse>> reportComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.reportComment(
                commentId,
                request.getReason(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo bình luận vi phạm thành công.", response));
    }

    @GetMapping("/admin/reports/comments")
    @Operation(summary = "Lấy danh sách báo cáo bình luận & đánh giá của hệ thống (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getAdminCommentAndReviewReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAdminCommentAndReviewReports()));
    }

    @GetMapping("/admin/reports/comments/{id}")
    @Operation(summary = "Lấy chi tiết báo cáo bình luận & đánh giá (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getAdminCommentOrReviewReport(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAdminCommentOrReviewReport(id)));
    }

    @PutMapping("/admin/reports/comments/{id}/status")
    @Operation(summary = "Cập nhật trạng thái xử lý báo cáo bình luận & đánh giá (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> updateCommentOrReviewReportStatus(
            @PathVariable UUID id,
            @RequestBody UpdateReportStatusRequest request) {
        ReportResponse response = reportService.updateCommentOrReviewReportStatus(id, request.getStatus(), request.getAdminNote());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái báo cáo thành công.", response));
    }

    @DeleteMapping("/admin/reports/comments/{id}")
    @Operation(summary = "Xóa báo cáo bình luận & đánh giá khỏi hệ thống (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCommentOrReviewReport(@PathVariable UUID id) {
        reportService.deleteCommentOrReviewReport(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa báo cáo thành công."));
    }
}
