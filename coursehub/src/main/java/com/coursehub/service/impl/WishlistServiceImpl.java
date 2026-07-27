package com.coursehub.service.impl;

import com.coursehub.dto.response.CourseResponse;
import com.coursehub.entity.*;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CourseRepository;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.repository.WishlistRepository;
import com.coursehub.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public void addToWishlist(UUID userId, UUID courseId) {
        if (wishlistRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BadRequestException("VALID_001", "Khóa học đã có sẵn trong danh sách mong muốn của bạn.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        if (course.getInstructor().getId().equals(userId)) {
            throw new BadRequestException("VALID_002", "Bạn không thể thêm khóa học của chính mình vào danh sách mong muốn.");
        }

        WishlistEntity wishlist = WishlistEntity.builder()
                .userId(userId)
                .courseId(courseId)
                .user(user)
                .course(course)
                .build();

        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(UUID userId, UUID courseId) {
        WishlistId id = new WishlistId(userId, courseId);
        WishlistEntity wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist", "userId & courseId", courseId));
        wishlistRepository.delete(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkWishlist(UUID userId, UUID courseId) {
        return wishlistRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getMyWishlist(UUID userId) {
        List<WishlistEntity> wishlist = wishlistRepository.findByUserId(userId);
        return wishlist.stream()
                .map(w -> mapToCourseResponse(w.getCourse()))
                .collect(Collectors.toList());
    }

    private CourseResponse mapToCourseResponse(CourseEntity course) {
        InstructorProfileEntity instructorProfile = course.getInstructor().getInstructorProfile();
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
                .totalStudents(enrollmentRepository.countByCourseId(course.getId()))
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
                .totalChapters(course.getChapters() != null ? course.getChapters().size() : 0)
                .build();
    }
}
