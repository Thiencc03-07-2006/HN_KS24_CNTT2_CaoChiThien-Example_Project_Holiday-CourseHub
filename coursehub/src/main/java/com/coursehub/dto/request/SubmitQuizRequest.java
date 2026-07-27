package com.coursehub.dto.request;

import lombok.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitQuizRequest {
    private Map<UUID, List<UUID>> selectedAnswers;
}
