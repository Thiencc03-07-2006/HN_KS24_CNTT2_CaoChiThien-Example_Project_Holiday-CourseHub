package com.coursehub.controller;

import com.coursehub.dto.request.CreateCommentRequest;
import com.coursehub.dto.request.UpdateCommentRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.CommentResponse;
import com.coursehub.entity.CommentEntity;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.CommentService;
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
@Tag(name = "Comments", description = "Thảo luận và Hỏi đáp trong bài học")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/lessons/{lessonId}/comments")
    @Operation(summary = "Đăng bình luận hoặc phản hồi mới")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentEntity comment = commentService.addComment(principal.getId(), lessonId, request.getParentId(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng bình luận thành công.", mapToResponse(comment)));
    }

    @GetMapping("/lessons/{lessonId}/comments")
    @Operation(summary = "Lấy danh sách bình luận thảo luận của bài học")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable UUID lessonId) {
        List<CommentEntity> comments = commentService.getLessonComments(lessonId);
        List<CommentResponse> responses = comments.stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Chỉnh sửa bình luận")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentEntity comment = commentService.updateComment(principal.getId(), commentId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Chỉnh sửa bình luận thành công.", mapToResponse(comment)));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Xóa bình luận")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID commentId) {
        commentService.deleteComment(principal.getId(), commentId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công."));
    }

    @PutMapping("/admin/comments/{commentId}/hide")
    @Operation(summary = "Ẩn bình luận vi phạm (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> hideComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID commentId) {
        commentService.hideComment(principal.getId(), commentId);
        return ResponseEntity.ok(ApiResponse.success("Ẩn bình luận thành công."));
    }

    private CommentResponse mapToResponse(CommentEntity comment) {
        List<CommentResponse> replies = null;
        if (comment.getReplies() != null) {
            replies = comment.getReplies().stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .userAvatar(comment.getUser().getAvatarUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
