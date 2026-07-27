package com.coursehub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {
    @Min(value = 1, message = "Đánh giá thấp nhất là 1 sao")
    @Max(value = 5, message = "Đánh giá cao nhất là 5 sao")
    private int rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(min = 5, max = 1000, message = "Nội dung đánh giá phải từ 5 đến 1000 ký tự")
    private String comment;
}

