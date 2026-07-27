package com.coursehub.controller;

import com.coursehub.dto.request.QuestionDto;
import com.coursehub.dto.request.QuizConfigDto;
import com.coursehub.dto.request.SubmitQuizRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.QuestionResponse;
import com.coursehub.dto.response.QuizAttemptResponse;
import com.coursehub.entity.QuizAttemptEntity;
import com.coursehub.dto.response.QuizConfigResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.QuizService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Quiz curriculum", description = "Quản lý bài trắc nghiệm và kết quả bài thi")
public class QuizController {

    private final QuizService quizService;

    // ==================== INSTRUCTOR Quiz Settings ====================

    @GetMapping("/instructor/courses/{courseId}/lessons/{lessonId}/quiz/config")
    @Operation(summary = "Lấy cấu hình bài trắc nghiệm dành cho giảng viên")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<QuizConfigResponse>> getConfigForInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizConfig(lessonId)));
    }

    @GetMapping("/instructor/courses/{courseId}/lessons/{lessonId}/quiz/questions")
    @Operation(summary = "Lấy toàn bộ câu hỏi và đáp án cho giảng viên")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId) {
        return ResponseEntity.ok(ApiResponse.success(
                quizService.getQuizQuestionsForInstructor(principal.getId(), courseId, lessonId)));
    }

    @PostMapping("/instructor/courses/{courseId}/lessons/{lessonId}/quiz/config")
    @Operation(summary = "Lưu cấu hình bài trắc nghiệm")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<QuizConfigResponse>> saveConfig(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @RequestBody QuizConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Lưu cấu hình thành công.",
                quizService.saveQuizConfig(principal.getId(), courseId, lessonId, dto)));
    }

    @PostMapping("/instructor/courses/{courseId}/lessons/{lessonId}/quiz/questions")
    @Operation(summary = "Thêm câu hỏi mới vào bài trắc nghiệm")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody QuestionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm câu hỏi thành công.",
                        quizService.addQuestion(principal.getId(), courseId, lessonId, dto)));
    }

    @PutMapping("/instructor/courses/{courseId}/lessons/{lessonId}/quiz/questions/{questionId}")
    @Operation(summary = "Cập nhật câu hỏi và danh sách đáp án")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật câu hỏi thành công.",
                quizService.updateQuestion(principal.getId(), courseId, lessonId, questionId, dto)));
    }

    @DeleteMapping("/instructor/courses/{courseId}/lessons/{lessonId}/quiz/questions/{questionId}")
    @Operation(summary = "Xóa câu hỏi")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @PathVariable UUID questionId) {
        quizService.deleteQuestion(principal.getId(), courseId, lessonId, questionId);
        return ResponseEntity.ok(ApiResponse.success("Xóa câu hỏi thành công."));
    }

    // ==================== STUDENT Quiz Taking ====================

    @GetMapping("/quiz/{lessonId}/config")
    @Operation(summary = "Lấy cấu hình bài trắc nghiệm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizConfigResponse>> getQuizConfigForStudent(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizConfig(lessonId)));
    }

    @GetMapping("/quiz/{lessonId}/questions")
    @Operation(summary = "Lấy câu hỏi thi (Đáp án sẽ bị ẩn isCorrect để chống hack)")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForStudent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID lessonId) {
        return ResponseEntity.ok(ApiResponse.success(
                quizService.getQuizQuestionsForStudent(principal.getId(), lessonId)));
    }

    @PostMapping("/quiz/{lessonId}/attempts")
    @Operation(summary = "Bắt đầu làm bài thi trắc nghiệm")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> startAttempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID lessonId) {
        QuizAttemptEntity attempt = quizService.startQuizAttempt(principal.getId(), lessonId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bắt đầu làm bài trắc nghiệm.", mapAttemptToResponse(attempt)));
    }

    @PostMapping("/quiz/{lessonId}/attempts/{attemptId}/submit")
    @Operation(summary = "Nộp bài thi trắc nghiệm (Hệ thống chấm điểm tự động)")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitAttempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID lessonId,
            @PathVariable UUID attemptId,
            @RequestBody SubmitQuizRequest request) {
        QuizAttemptEntity attempt = quizService.submitQuizAttempt(principal.getId(), lessonId, attemptId, request);
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thi thành công.", mapAttemptToResponse(attempt)));
    }

    // ==================== HELPERS ====================



    private QuizAttemptResponse mapAttemptToResponse(QuizAttemptEntity a) {
        return QuizAttemptResponse.builder()
                .id(a.getId())
                .enrollmentId(a.getEnrollment().getId())
                .lessonId(a.getLesson().getId())
                .score(a.getScore())
                .status(a.getStatus().name())
                .answersSnapshot(a.getAnswersSnapshot())
                .startedAt(a.getStartedAt())
                .submittedAt(a.getSubmittedAt())
                .build();
    }
}
