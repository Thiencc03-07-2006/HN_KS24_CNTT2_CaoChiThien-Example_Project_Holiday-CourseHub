package com.coursehub.service.impl;

import com.coursehub.dto.response.CourseResponse;
import com.coursehub.entity.*;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CourseRepository;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.repository.FavoriteRepository;
import com.coursehub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public void addFavorite(UUID userId, UUID courseId) {
        if (favoriteRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BadRequestException("VALID_001", "Bạn đã lưu khóa học này vào danh sách yêu thích rồi.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        if (course.getInstructor().getId().equals(userId)) {
            throw new BadRequestException("VALID_002", "Bạn không thể thêm khóa học của chính mình vào danh sách yêu thích.");
        }

        FavoriteEntity favorite = FavoriteEntity.builder()
                .userId(userId)
                .courseId(courseId)
                .user(user)
                .course(course)
                .build();

        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(UUID userId, UUID courseId) {
        FavoriteId id = new FavoriteId(userId, courseId);
        FavoriteEntity favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite", "userId & courseId", courseId));
        favoriteRepository.delete(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkFavorite(UUID userId, UUID courseId) {
        return favoriteRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getMyFavorites(UUID userId) {
        List<FavoriteEntity> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(fav -> mapToCourseResponse(fav.getCourse()))
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
