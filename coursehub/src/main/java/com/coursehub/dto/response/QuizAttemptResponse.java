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
public class QuizAttemptResponse {
    private UUID id;
    private UUID enrollmentId;
    private UUID lessonId;
    private BigDecimal score;
    private String status;
    private String answersSnapshot;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
