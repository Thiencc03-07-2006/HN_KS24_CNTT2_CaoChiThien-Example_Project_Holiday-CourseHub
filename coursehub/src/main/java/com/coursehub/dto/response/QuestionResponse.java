package com.coursehub.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private UUID id;
    private String content;
    private String questionType;
    private BigDecimal points;
    private int orderIndex;
    private String explanation;
    private List<AnswerResponse> answers;
}
