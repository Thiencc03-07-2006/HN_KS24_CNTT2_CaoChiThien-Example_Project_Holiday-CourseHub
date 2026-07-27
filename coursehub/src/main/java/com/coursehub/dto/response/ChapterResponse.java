package com.coursehub.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterResponse {
    private UUID id;
    private String title;
    private int orderIndex;
    private List<LessonResponse> lessons;
}
