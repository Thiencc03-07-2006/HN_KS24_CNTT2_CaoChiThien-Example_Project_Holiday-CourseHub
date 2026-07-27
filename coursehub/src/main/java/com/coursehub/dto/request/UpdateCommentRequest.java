package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentRequest {
    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
}
