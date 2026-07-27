package com.coursehub.controller;

import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.EnrollmentResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Quản lý đăng ký và tiến trình khóa học")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/course/{courseId}")
    @Operation(summary = "Đăng ký tham gia khóa học (Đăng ký học)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        EnrollmentResponse response = enrollmentService.enrollCourse(principal.getId(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký khóa học thành công.", response));
    }

    @PostMapping("/{courseId}")
    @Operation(summary = "Đăng ký tham gia khóa học (Đăng ký học) - Direct Path")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollCourseDirect(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return enrollCourse(principal, courseId);
    }

    @GetMapping("/check/{courseId}")
    @Operation(summary = "Kiểm tra trạng thái đăng ký của tôi - Direct Path")
    public ResponseEntity<ApiResponse<Boolean>> isEnrolledDirect(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.isEnrolled(principal.getId(), courseId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Danh sách khóa học đã đăng ký của tôi (My Courses)")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getMyEnrollments(principal.getId(), page, size)));
    }

    @GetMapping("/course/{courseId}/check")
    @Operation(summary = "Kiểm tra trạng thái đăng ký của tôi với khóa học này")
    public ResponseEntity<ApiResponse<Boolean>> isEnrolled(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.isEnrolled(principal.getId(), courseId)));
    }

    @GetMapping("/course/{courseId}/details")
    @Operation(summary = "Xem chi tiết tiến trình đăng ký của tôi với khóa học này")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getEnrollmentDetails(principal.getId(), courseId)));
    }
}
