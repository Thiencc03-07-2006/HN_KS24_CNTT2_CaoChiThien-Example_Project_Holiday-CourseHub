package com.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
    private UUID parentId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
}
