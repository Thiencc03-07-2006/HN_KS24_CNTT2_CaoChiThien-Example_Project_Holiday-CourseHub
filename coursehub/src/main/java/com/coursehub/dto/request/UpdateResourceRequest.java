package com.coursehub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateResourceRequest {
    private String resourceUrl;
    private Integer durationSeconds;
    private String textContent;

    @JsonProperty("isDownloadable")
    private boolean isDownloadable;
}
