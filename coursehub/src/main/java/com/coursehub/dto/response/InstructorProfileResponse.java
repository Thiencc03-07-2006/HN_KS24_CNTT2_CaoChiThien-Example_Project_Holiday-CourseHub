package com.coursehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorProfileResponse {
    private UUID id;
    private String headline;
    private String detailedBio;
    private String websiteUrl;
    private String linkedinUrl;
    private Integer totalStudents;
    private Integer totalCourses;
    private Double averageRating;
    // Payout details omitted from response for security (admin only)
}
