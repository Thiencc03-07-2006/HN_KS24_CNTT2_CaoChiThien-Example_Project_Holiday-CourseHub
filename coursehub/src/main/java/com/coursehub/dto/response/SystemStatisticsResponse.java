package com.coursehub.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatisticsResponse {
    private long totalUsers;
    private long totalStudents;
    private long totalInstructor;
    private long totalCourses;
    private long totalEnrollments;
    private BigDecimal totalRevenue;
}
