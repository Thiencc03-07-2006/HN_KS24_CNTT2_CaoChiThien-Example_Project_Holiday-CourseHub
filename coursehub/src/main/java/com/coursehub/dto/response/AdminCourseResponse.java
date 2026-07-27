package com.coursehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseResponse {
    private UUID id;
    private String title;
    private String thumbnail;
    private String instructor;
    private String status;
    private LocalDateTime createdAt;
    private Long enrollmentCount;
    private BigDecimal rating;
    private String blockedReason;
    private String rejectReason;
}
