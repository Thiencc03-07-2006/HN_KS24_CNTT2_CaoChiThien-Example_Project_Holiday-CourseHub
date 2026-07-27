package com.coursehub.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private UUID id;
    private String reporterName;
    private String reportableType;
    private UUID reportableId;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private String adminNote;
    private LocalDateTime updatedAt;
    private String targetTitle;
    private UUID reporterId;
}
