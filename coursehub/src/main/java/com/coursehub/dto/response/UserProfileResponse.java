package com.coursehub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String bio;
    private String status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private InstructorProfileResponse instructorProfile;
}
