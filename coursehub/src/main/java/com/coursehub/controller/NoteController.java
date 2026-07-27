package com.coursehub.controller;

import com.coursehub.dto.request.CreateNoteRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.NoteResponse;
import com.coursehub.entity.NoteEntity;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.NoteService;
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
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Quản lý ghi chú cá nhân của học viên")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(summary = "Tạo ghi chú mới")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteEntity note = noteService.createNote(principal.getId(), request.getLessonId(), request.getContent(), request.getTimestampSeconds());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lưu ghi chú thành công.", mapToResponse(note)));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Lấy danh sách ghi chú của tôi trong khóa học này")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getMyNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        List<NoteEntity> notes = noteService.getMyNotesForCourse(principal.getId(), courseId);
        List<NoteResponse> responses = notes.stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Xóa ghi chú")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId) {
        noteService.deleteNote(principal.getId(), noteId);
        return ResponseEntity.ok(ApiResponse.success("Xóa ghi chú thành công."));
    }

    private NoteResponse mapToResponse(NoteEntity note) {
        return NoteResponse.builder()
                .id(note.getId())
                .content(note.getContent())
                .timestampSeconds(note.getTimestampSeconds())
                .createdAt(note.getCreatedAt())
                .lessonId(note.getLesson() != null ? note.getLesson().getId() : null)
                .lessonTitle(note.getLesson() != null ? note.getLesson().getTitle() : null)
                .build();
    }
}

