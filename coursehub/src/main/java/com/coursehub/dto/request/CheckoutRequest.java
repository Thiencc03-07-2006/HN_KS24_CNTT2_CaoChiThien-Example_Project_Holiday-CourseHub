package com.coursehub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    @NotNull(message = "Course ID không được để trống.")
    private UUID courseId;
}
