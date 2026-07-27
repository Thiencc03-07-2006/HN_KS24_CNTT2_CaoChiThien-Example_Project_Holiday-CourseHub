package com.coursehub.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponse {
    private UUID id;
    private String content;
    private int orderIndex;
    private Boolean isCorrect;
}
