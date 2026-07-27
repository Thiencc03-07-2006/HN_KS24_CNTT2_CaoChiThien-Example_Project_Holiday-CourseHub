package com.coursehub.dto.response;

import com.coursehub.enums.EnrollmentStatus;
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
public class EnrollmentResponse {
    private UUID id;
    private CourseResponse course;
    private LocalDateTime enrollmentDate;
    private BigDecimal progressPercent;
    private EnrollmentStatus status;
}
