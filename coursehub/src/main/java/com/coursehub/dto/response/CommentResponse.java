package com.coursehub.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userAvatar;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;
}
