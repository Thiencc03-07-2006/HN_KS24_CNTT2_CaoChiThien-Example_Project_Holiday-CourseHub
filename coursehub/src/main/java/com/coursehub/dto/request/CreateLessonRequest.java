package com.coursehub.dto.request;

import com.coursehub.enums.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLessonRequest {
    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(min = 5, max = 150, message = "Tiêu đề bài học phải từ 5 đến 150 ký tự")
    private String title;

    @NotNull(message = "Loại bài học không được để trống")
    private LessonType lessonType;

    @JsonProperty("isPreview")
    private boolean isPreview;
}
