package com.coursehub.dto.response;

import com.coursehub.enums.LessonType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {
    private UUID id;
    private String title;
    private int orderIndex;
    private LessonType lessonType;

    @JsonProperty("isPreview")
    private boolean isPreview;
    private String resourceUrl;
    private Integer durationSeconds;
    private String textContent;

    @JsonProperty("isDownloadable")
    private boolean isDownloadable;
    private String videoStatus;
    private Boolean isCompleted;
}
