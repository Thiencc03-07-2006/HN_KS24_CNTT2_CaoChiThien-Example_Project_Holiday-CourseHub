package com.coursehub.service;

import com.coursehub.dto.response.InstructorDashboardStatsResponse;

import java.util.UUID;

public interface InstructorDashboardService {
    InstructorDashboardStatsResponse getStats(UUID instructorId);
}
