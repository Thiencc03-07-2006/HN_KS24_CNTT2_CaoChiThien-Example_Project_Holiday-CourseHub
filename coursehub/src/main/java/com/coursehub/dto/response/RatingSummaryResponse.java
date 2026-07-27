package com.coursehub.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummaryResponse {
    private BigDecimal averageRating;
    private long totalReviews;
    private Map<String, Long> distribution;
}
