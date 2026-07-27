package com.coursehub.service;

import com.coursehub.dto.request.BecomeInstructorRequest;
import com.coursehub.dto.request.UpdateProfileRequest;
import com.coursehub.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    UserProfileResponse getMyProfile(UUID userId);
    UserProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request);
    String uploadAvatar(UUID userId, MultipartFile file);
    void becomeInstructor(UUID userId, BecomeInstructorRequest request);
    void updateInstructorProfile(UUID userId, BecomeInstructorRequest request);
    UserProfileResponse getInstructorPublicProfile(UUID instructorId);
}
