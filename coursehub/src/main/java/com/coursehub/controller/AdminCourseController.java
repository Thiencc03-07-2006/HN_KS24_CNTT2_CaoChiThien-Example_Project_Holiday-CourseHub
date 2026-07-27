package com.coursehub.controller;

import com.coursehub.dto.request.BlockCourseRequest;
import com.coursehub.dto.response.AdminCourseDetailResponse;
import com.coursehub.dto.response.AdminCourseResponse;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.AdminCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@Tag(name = "Admin Course Management", description = "Quản lý khóa học dành cho Admin")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @GetMapping
    @Operation(summary = "Lấy danh sách khóa học có bộ lọc")
    public ResponseEntity<ApiResponse<PageResponse<AdminCourseResponse>>> getCourses(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String instructor,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminCourseService.getCourses(status, instructor, category, keyword, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết khóa học")
    public ResponseEntity<ApiResponse<AdminCourseDetailResponse>> getCourseDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminCourseService.getCourseDetail(id)));
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Chặn khóa học vi phạm")
    public ResponseEntity<ApiResponse<Void>> blockCourse(
            @PathVariable UUID id,
            @RequestBody @Valid BlockCourseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminCourseService.blockCourse(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Chặn khóa học thành công."));
    }

    @PutMapping("/{id}/unblock")
    @Operation(summary = "Bỏ chặn khóa học")
    public ResponseEntity<ApiResponse<Void>> unblockCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminCourseService.unblockCourse(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Bỏ chặn khóa học thành công."));
    }
}
