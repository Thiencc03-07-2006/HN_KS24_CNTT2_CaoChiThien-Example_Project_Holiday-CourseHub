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
@Tag(name = "Course Reports", description = "Quản lý báo cáo vi phạm nội dung khóa học")
@SecurityRequirement(name = "Bearer Authentication")
public class CourseReportController {

    private final ReportService reportService;

    @PostMapping("/courses/{courseId}/report")
    @Operation(summary = "Báo cáo khóa học vi phạm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponse>> reportCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.reportCourse(
                courseId,
                request.getReason(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo khóa học vi phạm thành công.", response));
    }

    @PostMapping("/reports")
    @Operation(summary = "Báo cáo vi phạm chung (Khóa học, Đánh giá, Bình luận)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponse>> reportGeneral(
            @Valid @RequestBody CreateReportRequest request) {
        if (request.getReportableId() == null) {
            throw new com.coursehub.exception.BadRequestException("REPORT_004", "ID đối tượng bị báo cáo không được để trống");
        }
        if (request.getReportableType() == null || request.getReportableType().isBlank()) {
            throw new com.coursehub.exception.BadRequestException("REPORT_005", "Loại đối tượng bị báo cáo không được để trống");
        }

        ReportResponse response;
        String type = request.getReportableType().toUpperCase();
        switch (type) {
            case "COURSE":
                response = reportService.reportCourse(request.getReportableId(), request.getReason(), request.getDescription());
                break;
            case "REVIEW":
                response = reportService.reportReview(request.getReportableId(), request.getReason(), request.getDescription());
                break;
            case "COMMENT":
                response = reportService.reportComment(request.getReportableId(), request.getReason(), request.getDescription());
                break;
            default:
                throw new com.coursehub.exception.BadRequestException("REPORT_006", "Loại đối tượng báo cáo không hợp lệ: " + request.getReportableType());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo vi phạm thành công.", response));
    }

    @GetMapping("/reports/courses/my")
    @Operation(summary = "Lấy danh sách các báo cáo khóa học của tôi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyCourseReports(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ReportResponse> responses = reportService.getMyCourseReports(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/admin/reports/courses")
    @Operation(summary = "Lấy danh sách báo cáo khóa học của hệ thống (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getAdminCourseReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAdminCourseReports()));
    }

    @GetMapping("/admin/reports/courses/{id}")
    @Operation(summary = "Lấy chi tiết báo cáo khóa học (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getAdminCourseReport(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAdminCourseReport(id)));
    }

    @PutMapping("/admin/reports/courses/{id}/status")
    @Operation(summary = "Cập nhật trạng thái xử lý báo cáo khóa học (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> updateCourseReportStatus(
            @PathVariable UUID id,
            @RequestBody UpdateReportStatusRequest request) {
        ReportResponse response = reportService.updateCourseReportStatus(id, request.getStatus(), request.getAdminNote());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái báo cáo thành công.", response));
    }

    @DeleteMapping("/admin/reports/courses/{id}")
    @Operation(summary = "Xóa báo cáo khóa học khỏi hệ thống (Admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourseReport(@PathVariable UUID id) {
        reportService.deleteCourseReport(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa báo cáo khóa học thành công."));
    }
}
