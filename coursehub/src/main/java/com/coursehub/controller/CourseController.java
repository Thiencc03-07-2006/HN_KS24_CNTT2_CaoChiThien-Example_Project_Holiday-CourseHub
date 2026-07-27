package com.coursehub.controller;

import com.coursehub.dto.request.CreateCourseRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Quản lý khóa học")
public class CourseController {

    private final CourseService courseService;

    // ==================== PUBLIC endpoints ====================

    @GetMapping({"/api/v1/courses/public/search", "/api/v1/courses/search"})
    @Operation(summary = "Tìm kiếm và lọc khóa học (FR-24, FR-25)")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal rating,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.searchCourses(keyword, categoryId, level, minPrice, maxPrice, rating, language, sortBy, page, size)));
    }

    @GetMapping("/api/v1/courses/public/{slug}")
    @Operation(summary = "Xem chi tiết khóa học theo slug (public)")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseBySlug(slug, currentUserId)));
    }

    @GetMapping("/api/v1/courses/{courseId}")
    @Operation(summary = "Xem chi tiết khóa học theo ID (public)")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseByIdPublic(
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseDetailPublic(courseId)));
    }

    @GetMapping("/api/v1/learning/courses/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lấy nội dung học của khóa học (đối với học viên đã đăng ký)")
    public ResponseEntity<ApiResponse<com.coursehub.dto.response.LearningCourseResponse>> getLearningCourseContent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getLearningCourseContent(principal.getId(), courseId)));
    }

    // ==================== INSTRUCTOR endpoints ====================

    @PostMapping("/api/v1/instructor/courses")
    @Operation(summary = "Tạo khóa học mới (FR-04)", description = "Instructor tạo khóa học ở trạng thái DRAFT")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.createCourse(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo khóa học thành công.", response));
    }

    @GetMapping("/api/v1/instructor/courses")
    @Operation(summary = "Danh sách khóa học của giảng viên")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getMyCoursesAsInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getInstructorCourses(principal.getId(), page, size)));
    }

    @GetMapping("/api/v1/instructor/courses/{courseId}")
    @Operation(summary = "Chi tiết khóa học của giảng viên")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseAsInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(courseId, principal.getId())));
    }

    @PutMapping("/api/v1/instructor/courses/{courseId}")
    @Operation(summary = "Cập nhật thông tin khóa học (DRAFT/REJECTED)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khóa học thành công.",
                courseService.updateCourse(principal.getId(), courseId, request)));
    }

    @PostMapping(value = "/api/v1/instructor/courses/{courseId}/thumbnail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ảnh bìa khóa học")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<String>> uploadThumbnail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @RequestParam("file") MultipartFile file) {
        String url = courseService.uploadThumbnail(principal.getId(), courseId, file);
        return ResponseEntity.ok(ApiResponse.success("Upload thumbnail thành công.", url));
    }

    @PostMapping("/api/v1/instructor/courses/{courseId}/submit")
    @Operation(summary = "Gửi khóa học để Admin duyệt (FR-04)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> submitForReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        courseService.submitForReview(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi khóa học cho Admin xét duyệt."));
    }

    @PutMapping("/api/v1/instructor/courses/{courseId}/resubmit")
    @Operation(summary = "Gửi lại khóa học sau khi bị từ chối")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> resubmitForReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        courseService.submitForReview(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi lại khóa học cho Admin xét duyệt."));
    }

    @DeleteMapping("/api/v1/instructor/courses/{courseId}")
    @Operation(summary = "Xóa khóa học (chỉ DRAFT/REJECTED)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        courseService.deleteCourse(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Xóa khóa học thành công."));
    }
}
