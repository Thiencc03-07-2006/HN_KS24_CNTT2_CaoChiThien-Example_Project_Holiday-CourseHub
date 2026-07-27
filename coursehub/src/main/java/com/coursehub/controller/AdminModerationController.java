package com.coursehub.controller;

import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.ReportResponse;
import com.coursehub.dto.response.UserProfileResponse;
import com.coursehub.service.AdminService;
import com.coursehub.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Moderation", description = "Quản trị báo cáo, điều phối người dùng và kiểm duyệt hệ thống (FR-21)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final ReportService reportService;
    private final AdminService adminService;

    // Report Management
    @GetMapping("/reports")
    @Operation(summary = "Xem toàn bộ báo cáo vi phạm")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports() {
        List<ReportResponse> responses = reportService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PatchMapping("/reports/{id}/status")
    @Operation(summary = "Cập nhật trạng thái báo cáo (RESOLVED, DISMISSED, UNDER_REVIEW...)")
    public ResponseEntity<ApiResponse<Void>> updateReportStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        reportService.updateReportStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái báo cáo thành công."));
    }

    // User Moderation
    @PutMapping("/users/{userId}/lock")
    @Operation(summary = "Khóa tài khoản người dùng")
    public ResponseEntity<ApiResponse<UserProfileResponse>> lockUser(@PathVariable UUID userId) {
        UserProfileResponse response = adminService.updateUserStatus(userId, "SOFT_LOCKED");
        return ResponseEntity.ok(ApiResponse.success("Đã khóa tài khoản thành công.", response));
    }

    @PutMapping("/users/{userId}/unlock")
    @Operation(summary = "Mở khóa tài khoản người dùng")
    public ResponseEntity<ApiResponse<UserProfileResponse>> unlockUser(@PathVariable UUID userId) {
        UserProfileResponse response = adminService.updateUserStatus(userId, "ACTIVE");
        return ResponseEntity.ok(ApiResponse.success("Mở khóa tài khoản thành công.", response));
    }

    @PutMapping("/users/{userId}/disable")
    @Operation(summary = "Vô hiệu hóa vĩnh viễn tài khoản người dùng (BANNED)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> disableAccount(@PathVariable UUID userId) {
        UserProfileResponse response = adminService.updateUserStatus(userId, "BANNED");
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa vĩnh viễn tài khoản thành công.", response));
    }
}
