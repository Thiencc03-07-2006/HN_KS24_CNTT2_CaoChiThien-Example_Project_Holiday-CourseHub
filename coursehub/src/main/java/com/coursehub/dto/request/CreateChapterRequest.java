package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChapterRequest {
    @NotBlank(message = "Tên chương học không được để trống")
    @Size(min = 5, max = 100, message = "Tên chương học phải từ 5 đến 100 ký tự")
    private String title;

    private Integer orderIndex;
}
