package com.coursehub.controller;

import com.coursehub.dto.response.*;
import com.coursehub.entity.CourseEntity;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CourseRepository;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.AdminService;
import com.coursehub.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Quản lý dành cho Quản trị viên")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final CourseService courseService;
    private final CourseRepository courseRepository;

    // ==================== DASHBOARD STATS ====================

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Lấy số liệu thống kê Dashboard Admin")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    @Operation(summary = "Danh sách và tìm kiếm người dùng")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.searchUsers(keyword, status, role, page, size)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Xem chi tiết thông tin người dùng")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserDetail(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserDetail(userId)));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Bật / Tắt trạng thái hoạt động của người dùng (Enable/Disable)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam String status) {
        UserProfileResponse response = adminService.updateUserStatus(userId, status);
        String msg = "ACTIVE".equalsIgnoreCase(status) ? "Mở khóa tài khoản thành công." : "Tài khóa đã bị khóa.";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Thêm vai trò cho người dùng")
    public ResponseEntity<ApiResponse<UserProfileResponse>> addUserRole(
            @PathVariable UUID userId,
            @RequestParam String role) {
        UserProfileResponse response = adminService.addUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success("Thêm vai trò thành công.", response));
    }

    @DeleteMapping("/users/{userId}/roles/{role}")
    @Operation(summary = "Xóa vai trò khỏi người dùng")
    public ResponseEntity<ApiResponse<UserProfileResponse>> removeUserRole(
            @PathVariable UUID userId,
            @PathVariable String role) {
        UserProfileResponse response = adminService.removeUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success("Xóa vai trò thành công.", response));
    }

    // ==================== COURSE MANAGEMENT ====================

    @PutMapping("/courses/{courseId}/approve")
    @Operation(summary = "Duyệt khóa học")
    public ResponseEntity<ApiResponse<Void>> approveCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @RequestParam(required = false, defaultValue = "Duyệt thành công") String note) {
        courseService.approveCourse(principal.getId(), courseId, note);
        return ResponseEntity.ok(ApiResponse.success("Duyệt khóa học thành công. Khóa học đã được xuất bản công khai."));
    }

    @PutMapping("/courses/{courseId}/reject")
    @Operation(summary = "Từ chối duyệt khóa học")
    public ResponseEntity<ApiResponse<Void>> rejectCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @RequestParam(required = false, defaultValue = "Nội dung chưa đạt yêu cầu") String note) {
        courseService.rejectCourse(principal.getId(), courseId, note);
        return ResponseEntity.ok(ApiResponse.success("Từ chối duyệt khóa học thành công."));
    }

    @DeleteMapping("/courses/{courseId}")
    @Operation(summary = "Xóa khóa học (Admin soft-delete)")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(ApiResponse.success("Xóa khóa học thành công."));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê chi tiết hệ thống cho Admin (FR-26)")
    public ResponseEntity<ApiResponse<SystemStatisticsResponse>> getSystemStatistics() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSystemStatistics()));
    }
}
