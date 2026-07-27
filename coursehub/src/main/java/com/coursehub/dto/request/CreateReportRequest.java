package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReportRequest {
    private java.util.UUID reportableId;

    private String reportableType;

    @NotBlank(message = "Lý do báo cáo không được để trống")
    private String reason;

    private String description;
}
