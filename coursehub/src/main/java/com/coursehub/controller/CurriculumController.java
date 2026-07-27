package com.coursehub.controller;

import com.coursehub.dto.request.CreateChapterRequest;
import com.coursehub.dto.request.CreateLessonRequest;
import com.coursehub.dto.request.UpdateResourceRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.ChapterResponse;
import com.coursehub.dto.response.LessonResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.CurriculumService;
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
@Tag(name = "Course Curriculum", description = "Quản lý chương và bài học (Curriculum)")
public class CurriculumController {

    private final CurriculumService curriculumService;

    // ==================== PUBLIC endpoints ====================

    @GetMapping("/courses/public/{courseId}/curriculum")
    @Operation(summary = "Xem cây giáo trình (Public/Student)")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getCurriculumPublic(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.getCurriculumByCourseId(courseId, currentUserId)));
    }

    // ==================== INSTRUCTOR endpoints ====================

    @GetMapping("/instructor/courses/{courseId}/curriculum")
    @Operation(summary = "Xem giáo trình phục vụ chỉnh sửa (Instructor)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getCurriculumAsInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.getCurriculumByCourseId(courseId, principal.getId())));
    }

    @PostMapping("/instructor/courses/{courseId}/chapters")
    @Operation(summary = "Tạo chương học mới")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ChapterResponse>> createChapter(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateChapterRequest request) {
        ChapterResponse response = curriculumService.createChapter(principal.getId(), courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chương học thành công.", response));
    }

    @PutMapping("/instructor/courses/{courseId}/chapters/{chapterId}")
    @Operation(summary = "Cập nhật tên chương học")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ChapterResponse>> updateChapter(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID chapterId,
            @Valid @RequestBody CreateChapterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương học thành công.",
                curriculumService.updateChapter(principal.getId(), courseId, chapterId, request)));
    }

    @DeleteMapping("/instructor/courses/{courseId}/chapters/{chapterId}")
    @Operation(summary = "Xóa chương học")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID chapterId) {
        curriculumService.deleteChapter(principal.getId(), courseId, chapterId);
        return ResponseEntity.ok(ApiResponse.success("Xóa chương học thành công."));
    }

    @PostMapping("/instructor/courses/{courseId}/chapters/{chapterId}/lessons")
    @Operation(summary = "Tạo bài học mới")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID chapterId,
            @Valid @RequestBody CreateLessonRequest request) {
        LessonResponse response = curriculumService.createLesson(principal.getId(), courseId, chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài học thành công.", response));
    }

    @PutMapping("/instructor/courses/{courseId}/chapters/{chapterId}/lessons/{lessonId}")
    @Operation(summary = "Cập nhật thông tin cơ bản bài học")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID chapterId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài học thành công.",
                curriculumService.updateLesson(principal.getId(), courseId, chapterId, lessonId, request)));
    }

    @PutMapping("/instructor/courses/{courseId}/chapters/{chapterId}/lessons/{lessonId}/resource")
    @Operation(summary = "Cập nhật tài nguyên bài học (video url, văn bản, file đính kèm...)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLessonResource(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID chapterId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateResourceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài nguyên thành công.",
                curriculumService.updateLessonResource(principal.getId(), courseId, chapterId, lessonId, request)));
    }

    @DeleteMapping("/instructor/courses/{courseId}/chapters/{chapterId}/lessons/{lessonId}")
    @Operation(summary = "Xóa bài học")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID chapterId,
            @PathVariable UUID lessonId) {
        curriculumService.deleteLesson(principal.getId(), courseId, chapterId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài học thành công."));
    }

    // ==================== NEW REST endpoints to support frontend ====================

    @GetMapping("/instructor/courses/{courseId}/chapters")
    @Operation(summary = "Xem danh sách chương học của khóa học (Instructor)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getChapters(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.getChaptersByCourseId(principal.getId(), courseId)));
    }

    @PutMapping("/instructor/chapters/{chapterId}")
    @Operation(summary = "Cập nhật tên chương học (Không cần courseId)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ChapterResponse>> updateChapterWithoutCourseId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID chapterId,
            @Valid @RequestBody CreateChapterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương học thành công.",
                curriculumService.updateChapter(principal.getId(), chapterId, request)));
    }

    @DeleteMapping("/instructor/chapters/{chapterId}")
    @Operation(summary = "Xóa chương học (Không cần courseId)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteChapterWithoutCourseId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID chapterId) {
        curriculumService.deleteChapter(principal.getId(), chapterId);
        return ResponseEntity.ok(ApiResponse.success("Xóa chương học thành công."));
    }

    @GetMapping("/instructor/chapters/{chapterId}/lessons")
    @Operation(summary = "Xem danh sách bài học của chương học (Instructor)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessons(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID chapterId) {
        return ResponseEntity.ok(ApiResponse.success(
                curriculumService.getLessonsByChapterId(principal.getId(), chapterId)));
    }

    @PostMapping("/instructor/chapters/{chapterId}/lessons")
    @Operation(summary = "Tạo bài học mới (Không cần courseId)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<LessonResponse>> createLessonWithoutCourseId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID chapterId,
            @Valid @RequestBody CreateLessonRequest request) {
        LessonResponse response = curriculumService.createLesson(principal.getId(), chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài học thành công.", response));
    }

    @PutMapping("/instructor/lessons/{lessonId}")
    @Operation(summary = "Cập nhật thông tin cơ bản bài học (Không cần courseId/chapterId)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLessonWithoutCourseId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài học thành công.",
                curriculumService.updateLesson(principal.getId(), lessonId, request)));
    }

    @DeleteMapping("/instructor/lessons/{lessonId}")
    @Operation(summary = "Xóa bài học (Không cần courseId/chapterId)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteLessonWithoutCourseId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID lessonId) {
        curriculumService.deleteLesson(principal.getId(), lessonId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài học thành công."));
    }
}
