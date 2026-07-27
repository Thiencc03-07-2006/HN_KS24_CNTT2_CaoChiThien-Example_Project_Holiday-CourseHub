package com.coursehub.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String studentAvatar;
    private int rating;
    private String comment;
    private boolean isEdited;
    private UUID courseId;
    private String courseTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

