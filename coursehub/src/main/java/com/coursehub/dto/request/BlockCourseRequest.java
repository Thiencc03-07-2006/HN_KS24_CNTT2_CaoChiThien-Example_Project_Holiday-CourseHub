package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockCourseRequest {
    @NotBlank(message = "Lý do chặn không được để trống")
    private String reason;
}
