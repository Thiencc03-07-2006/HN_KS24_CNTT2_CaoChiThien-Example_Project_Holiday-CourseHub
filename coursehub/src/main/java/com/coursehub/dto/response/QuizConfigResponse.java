package com.coursehub.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizConfigResponse {
    private UUID id;
    private UUID lessonId;
    private String lessonTitle;
    private BigDecimal passingScore;
    private Integer timeLimit;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private boolean shuffleQuestions;
    private boolean shuffleAnswers;
    private boolean showCorrectAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
