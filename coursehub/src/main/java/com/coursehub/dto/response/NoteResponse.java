package com.coursehub.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {
    private UUID id;
    private String content;
    private Integer timestampSeconds;
    private LocalDateTime createdAt;
    private UUID lessonId;
    private String lessonTitle;
}

