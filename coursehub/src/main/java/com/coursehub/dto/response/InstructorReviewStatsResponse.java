package com.coursehub.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorReviewStatsResponse {
    private double averageRating;
    private long totalReviews;
    private Map<String, Long> distribution;
}
