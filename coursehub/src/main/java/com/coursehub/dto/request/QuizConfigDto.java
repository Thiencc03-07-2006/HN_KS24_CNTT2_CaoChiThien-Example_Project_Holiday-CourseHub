package com.coursehub.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizConfigDto {
    private Integer timeLimitMinutes;
    private BigDecimal passingScore;
    private Integer maxAttempts;
    private boolean shuffleQuestions;
    private boolean shuffleAnswers;
}
