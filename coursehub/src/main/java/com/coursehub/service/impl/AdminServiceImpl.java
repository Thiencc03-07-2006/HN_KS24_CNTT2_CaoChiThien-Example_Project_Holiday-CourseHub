package com.coursehub.service.impl;

import com.coursehub.dto.response.*;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseStatus;
import com.coursehub.enums.UserStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.AdminService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final WishlistRepository wishlistRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count((root, query, cb) -> cb.isNull(root.get("deletedAt")));
        long totalCourses = courseRepository.count((root, query, cb) -> cb.isNull(root.get("deletedAt")));
        long totalCategories = categoryRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        List<UserEntity> recentUserEntities = userRepository.findAll(
                (root, query, cb) -> cb.isNull(root.get("deletedAt")),
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        ).getContent();

        List<CourseEntity> recentCourseEntities = courseRepository.findAll(
                (root, query, cb) -> cb.isNull(root.get("deletedAt")),
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        ).getContent();

        List<UserProfileResponse> recentUsers = recentUserEntities.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());

        List<CourseResponse> recentCourses = recentCourseEntities.stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());

        // Wishlist & Reports Stats
        long totalWishlist = wishlistRepository.count();
        List<CourseEntity> topFavoriteEntities = wishlistRepository.findTopFavoriteCourses(PageRequest.of(0, 10));
        List<CourseResponse> top10FavoriteCourses = topFavoriteEntities.stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());

        long totalCourseReports = reportRepository.countByReportableType("COURSE");
        long totalCommentReports = reportRepository.countByReportableType("COMMENT") + reportRepository.countByReportableType("REVIEW");
        long pendingReportsCount = reportRepository.countByStatus(com.coursehub.enums.ReportStatus.PENDING);
        long resolvedReportsCount = reportRepository.countByStatus(com.coursehub.enums.ReportStatus.RESOLVED);

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .totalCategories(totalCategories)
                .totalEnrollments(totalEnrollments)
                .recentUsers(recentUsers)
                .recentCourses(recentCourses)
                .totalWishlist(totalWishlist)
                .top10FavoriteCourses(top10FavoriteCourses)
                .totalCourseReports(totalCourseReports)
                .totalCommentReports(totalCommentReports)
                .pendingReportsCount(pendingReportsCount)
                .resolvedReportsCount(resolvedReportsCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserProfileResponse> searchUsers(String keyword, String status, String role, int page, int size) {
        Specification<UserEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), likePattern),
                        cb.like(cb.lower(root.get("fullName")), likePattern)
                ));
            }

            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), UserStatus.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid user status filter: {}", status);
                }
            }

            if (role != null && !role.isBlank()) {
                Join<UserEntity, RoleEntity> userRoleJoin = root.join("roles");
                predicates.add(cb.equal(userRoleJoin.get("name"), role.toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<UserEntity> result = userRepository.findAll(
                spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return PageResponse.from(result.map(this::mapToProfileResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserDetail(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserStatus(UUID userId, String status) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getDeletedAt() != null) {
            throw new BadRequestException("VALID_001", "Không thể cập nhật trạng thái của tài khoản đã xóa.");
        }

        try {
            UserStatus newStatus = UserStatus.valueOf(status.toUpperCase());
            user.setStatus(newStatus);
            userRepository.save(user);

            if (newStatus == UserStatus.BANNED) {
                // Revoke all refresh tokens to log out the banned user
                refreshTokenRepository.revokeAllByUserId(userId);
                log.info("Banned user {} and revoked all their active sessions", userId);
            }

            return mapToProfileResponse(user);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("VALID_001", "Trạng thái người dùng không hợp lệ: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesForReview(String keyword, String status, int page, int size) {
        Specification<CourseEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), likePattern));
            }

            if (status != null && !status.isBlank()) {
                if ("BLOCKED".equalsIgnoreCase(status)) {
                    predicates.add(root.get("status").in(CourseStatus.BLOCKED, CourseStatus.BLOCKED_EDITED));
                } else {
                    try {
                        predicates.add(cb.equal(root.get("status"), CourseStatus.valueOf(status.toUpperCase())));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid course status filter: {}", status);
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CourseEntity> result = courseRepository.findAll(
                spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        return PageResponse.from(result.map(this::mapToCourseResponse));
    }

    private UserProfileResponse mapToProfileResponse(UserEntity user) {
        InstructorProfileResponse instructorResponse = null;
        if (user.getInstructorProfile() != null) {
            instructorResponse = InstructorProfileResponse.builder()
                    .id(user.getInstructorProfile().getId())
                    .headline(user.getInstructorProfile().getHeadline())
                    .detailedBio(user.getInstructorProfile().getDetailedBio())
                    .websiteUrl(user.getInstructorProfile().getWebsiteUrl())
                    .linkedinUrl(user.getInstructorProfile().getLinkedinUrl())
                    .build();
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

    private CourseResponse mapToCourseResponse(CourseEntity course) {
        var instructorProfile = course.getInstructor().getInstructorProfile();
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnailUrl(course.getThumbnailUrl())
                .promoVideoUrl(course.getPromoVideoUrl())
                .level(course.getLevel())
                .language(course.getLanguage())
                .status(course.getStatus())
                .averageRating(course.getAverageRating())
                .totalReviews(course.getTotalReviews())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .instructor(CourseResponse.InstructorSummary.builder()
                        .id(course.getInstructor().getId())
                        .fullName(course.getInstructor().getFullName())
                        .avatarUrl(course.getInstructor().getAvatarUrl())
                        .headline(instructorProfile != null ? instructorProfile.getHeadline() : null)
                        .build())
                .category(CourseResponse.CategorySummary.builder()
                        .id(course.getCategory().getId())
                        .name(course.getCategory().getName())
                        .slug(course.getCategory().getSlug())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatisticsResponse getSystemStatistics() {
        long totalUsers = userRepository.count((root, query, cb) -> cb.isNull(root.get("deletedAt")));
        long totalStudents = userRepository.countUsersByRoleName("ROLE_STUDENT");
        long totalInstructor = userRepository.countUsersByRoleName("ROLE_INSTRUCTOR");
        long totalCourses = courseRepository.count((root, query, cb) -> cb.isNull(root.get("deletedAt")));
        long totalEnrollments = enrollmentRepository.count();

        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        List<EnrollmentEntity> enrollments = enrollmentRepository.findAll();
        for (EnrollmentEntity enrollment : enrollments) {
            if (enrollment.getCourse() != null && enrollment.getCourse().getPrice() != null) {
                totalRevenue = totalRevenue.add(enrollment.getCourse().getPrice());
            }
        }

        return SystemStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalInstructor(totalInstructor)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse addUserRole(UUID userId, String roleName) {
        if ("ROLE_ADMIN".equalsIgnoreCase(roleName)) {
            throw new org.springframework.security.access.AccessDeniedException("Không được phép thêm ROLE_ADMIN");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getDeletedAt() != null) {
            throw new BadRequestException("VALID_001", "Không thể chỉnh sửa vai trò của người dùng đã bị xóa.");
        }

        String cleanRoleName = roleName.toUpperCase().trim();

        if (user.hasRole(cleanRoleName)) {
            throw new BadRequestException("VALID_001", "Người dùng đã có vai trò này.");
        }

        RoleEntity role = roleRepository.findByName(cleanRoleName)
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .name(cleanRoleName)
                                .description("Tự động tạo cho vai trò: " + cleanRoleName)
                                .build()
                ));

        user.getRoles().add(role);

        // If adding ROLE_INSTRUCTOR, also ensure they have an InstructorProfileEntity
        if ("ROLE_INSTRUCTOR".equals(cleanRoleName) && user.getInstructorProfile() == null) {
            InstructorProfileEntity profile = InstructorProfileEntity.builder()
                    .user(user)
                    .headline("Giảng viên tại CourseHub")
                    .detailedBio("Chưa cập nhật tiểu sử giảng viên.")
                    .build();
            user.setInstructorProfile(profile);
        }

        userRepository.save(user);
        log.info("Admin added role {} to user {}", cleanRoleName, userId);
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse removeUserRole(UUID userId, String roleName) {
        if ("ROLE_ADMIN".equalsIgnoreCase(roleName)) {
            throw new org.springframework.security.access.AccessDeniedException("Không được phép xóa ROLE_ADMIN");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getDeletedAt() != null) {
            throw new BadRequestException("VALID_001", "Không thể chỉnh sửa vai trò của người dùng đã bị xóa.");
        }

        String cleanRoleName = roleName.toUpperCase().trim();

        if (!user.hasRole(cleanRoleName)) {
            throw new BadRequestException("VALID_001", "Người dùng không có vai trò này.");
        }

        user.getRoles().removeIf(r -> r.getName().equals(cleanRoleName));
        userRepository.save(user);
        log.info("Admin removed role {} from user {}", cleanRoleName, userId);
        return mapToProfileResponse(user);
    }
}
