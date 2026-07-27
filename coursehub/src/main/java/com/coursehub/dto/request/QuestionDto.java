package com.coursehub.dto.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
    private String content;
    private String questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
    private BigDecimal points;
    private Integer orderIndex;
    private String explanation;
    private List<AnswerDto> answers;
}
