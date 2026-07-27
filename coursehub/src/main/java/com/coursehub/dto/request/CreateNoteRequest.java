package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNoteRequest {
    @NotNull(message = "courseId không được để trống")
    private UUID courseId;

    @NotNull(message = "lessonId không được để trống")
    private UUID lessonId;

    @NotBlank(message = "Nội dung ghi chú không được để trống")
    private String content;

    private Integer timestampSeconds;
}

