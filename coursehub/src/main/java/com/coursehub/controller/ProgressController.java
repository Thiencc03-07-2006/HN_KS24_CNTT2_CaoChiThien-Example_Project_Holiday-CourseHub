package com.coursehub.controller;

import com.coursehub.dto.response.ApiResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Progress Tracking", description = "Quản lý tiến độ học tập bài học")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class ProgressController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/api/v1/enrollments/course/{courseId}/lessons/{lessonId}/complete")
    @Operation(summary = "Đánh dấu bài học đã hoàn thành (Video, Document, Text)")
    public ResponseEntity<ApiResponse<Void>> completeLesson(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId) {
        enrollmentService.completeLesson(principal.getId(), courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Đã hoàn thành bài học."));
    }

    @PostMapping("/api/v1/progress")
    @Operation(summary = "Cập nhật tiến độ bài học (Path mới)")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody java.util.Map<String, String> payload) {
        UUID courseId = UUID.fromString(payload.get("courseId"));
        UUID lessonId = UUID.fromString(payload.get("lessonId"));
        enrollmentService.completeLesson(principal.getId(), courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tiến trình hoàn thành bài học thành công."));
    }
}
