package com.coursehub.service.impl;

import com.coursehub.constant.AppConstants;
import com.coursehub.dto.request.BecomeInstructorRequest;
import com.coursehub.dto.request.UpdateProfileRequest;
import com.coursehub.dto.response.InstructorProfileResponse;
import com.coursehub.dto.response.UserProfileResponse;
import com.coursehub.entity.InstructorProfileEntity;
import com.coursehub.entity.RoleEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.enums.UserStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.CourseHubException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.InstructorProfileRepository;
import com.coursehub.repository.RoleRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.CloudinaryService;
import com.coursehub.service.UserService;
import com.coursehub.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstructorProfileRepository instructorProfileRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UUID userId) {
        UserEntity user = findUserById(userId);
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        UserEntity user = findUserById(userId);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            if (!request.getPhoneNumber().equals(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
                throw new BadRequestException("VALID_001", "Số điện thoại đã được sử dụng bởi tài khoản khác.");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userRepository.save(user);
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(UUID userId, MultipartFile file) {
        if (!AppUtils.isAllowedContentType(file, AppConstants.ALLOWED_IMAGE_TYPES)) {
            throw new BadRequestException("VALID_003", "Định dạng ảnh không hợp lệ. Chỉ hỗ trợ JPG, PNG.");
        }
        if (!AppUtils.isWithinSizeLimit(file, AppConstants.MAX_AVATAR_SIZE_BYTES)) {
            throw new BadRequestException("VALID_002", "Ảnh đại diện không được vượt quá 2MB.");
        }

        UserEntity user = findUserById(userId);

        try {
            String url = cloudinaryService.uploadFile(file, "coursehub/avatar");
            user.setAvatarUrl(url);
            userRepository.save(user);
            log.info("Avatar uploaded for user {}: {}", userId, url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage());
            throw new BadRequestException("SYS_001", "Lỗi upload ảnh đại diện. Vui lòng thử lại.");
        }
    }

    @Override
    @Transactional
    public void becomeInstructor(UUID userId, BecomeInstructorRequest request) {
        UserEntity user = findUserById(userId);

        if (instructorProfileRepository.existsByUserId(userId)) {
            throw new BadRequestException("VALID_001", "Bạn đã là giảng viên.");
        }

        RoleEntity instructorRole = roleRepository.findByName(AppConstants.ROLE_INSTRUCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", AppConstants.ROLE_INSTRUCTOR));

        user.getRoles().add(instructorRole);
        userRepository.save(user);

        InstructorProfileEntity profile = InstructorProfileEntity.builder()
                .user(user)
                .headline(request.getHeadline())
                .detailedBio(request.getDetailedBio())
                .websiteUrl(request.getWebsiteUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .build();

        instructorProfileRepository.save(profile);
        log.info("User {} became instructor", userId);
    }

    @Override
    @Transactional
    public void updateInstructorProfile(UUID userId, BecomeInstructorRequest request) {
        InstructorProfileEntity profile = instructorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor Profile", "userId", userId));

        if (request.getHeadline() != null) profile.setHeadline(request.getHeadline());
        if (request.getDetailedBio() != null) profile.setDetailedBio(request.getDetailedBio());
        if (request.getWebsiteUrl() != null) profile.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());

        instructorProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getInstructorPublicProfile(UUID instructorId) {
        UserEntity user = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor", "id", instructorId));

        if (user.getStatus() == UserStatus.BANNED || user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Instructor", "id", instructorId);
        }

        if (!user.hasRole(AppConstants.ROLE_INSTRUCTOR)) {
            throw new ResourceNotFoundException("Instructor", "id", instructorId);
        }

        return mapToPublicProfileResponse(user);
    }

    // ==================== PRIVATE HELPERS ====================

    private UserEntity findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserProfileResponse mapToProfileResponse(UserEntity user) {
        InstructorProfileResponse instructorResponse = null;
        if (user.getInstructorProfile() != null) {
            instructorResponse = mapInstructorProfile(user.getInstructorProfile());
        }
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .instructorProfile(instructorResponse)
                .build();
    }

    private UserProfileResponse mapToPublicProfileResponse(UserEntity user) {
        InstructorProfileResponse instructorResponse = null;
        if (user.getInstructorProfile() != null) {
            instructorResponse = mapInstructorProfile(user.getInstructorProfile());
        }
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .instructorProfile(instructorResponse)
                .build();
    }

    private InstructorProfileResponse mapInstructorProfile(InstructorProfileEntity profile) {
        return InstructorProfileResponse.builder()
                .id(profile.getId())
                .headline(profile.getHeadline())
                .detailedBio(profile.getDetailedBio())
                .websiteUrl(profile.getWebsiteUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .totalStudents(profile.getTotalStudents())
                .totalCourses(profile.getTotalCourses())
                .averageRating(profile.getAverageRating())
                .build();
    }
}
