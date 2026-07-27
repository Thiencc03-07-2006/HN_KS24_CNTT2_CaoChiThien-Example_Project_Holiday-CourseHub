package com.coursehub.service.impl;

import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.InstructorDashboardStatsResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseStatus;
import com.coursehub.repository.*;
import com.coursehub.service.InstructorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorDashboardServiceImpl implements InstructorDashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;

    @Override
    @Transactional(readOnly = true)
    public InstructorDashboardStatsResponse getStats(UUID instructorId) {
        long totalCourses = courseRepository.countByInstructorIdAndDeletedAtIsNull(instructorId);
        long publishedCourses = courseRepository.countByInstructorIdAndStatusAndDeletedAtIsNull(instructorId, CourseStatus.PUBLISHED);
        long totalStudents = enrollmentRepository.countTotalStudentsByInstructorId(instructorId);
        
        BigDecimal totalRevenue = enrollmentRepository.sumRevenueByInstructorId(instructorId);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Double avg = reviewRepository.findAverageRatingByInstructorId(instructorId);
        double averageRating = (avg != null) ? avg : 0.0;

        long totalReviews = reviewRepository.countReviewsByInstructorId(instructorId);

        // Wishlist Stats
        long totalWishlist = wishlistRepository.countByCourseInstructorId(instructorId);

        // Favorites Stats
        long totalFavorites = wishlistRepository.countByCourseInstructorId(instructorId);

        // Enrollment Timeline
        List<EnrollmentEntity> enrollments = enrollmentRepository.findEnrollmentsByInstructorId(instructorId);
        List<InstructorDashboardStatsResponse.EnrollmentTimelineData> enrollmentTimeline = enrollments.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEnrollmentDate().toLocalDate().toString(),
                        Collectors.groupingBy(
                                e -> e.getCourse().getTitle(),
                                Collectors.counting()
                        )
                ))
                .entrySet().stream()
                .flatMap(dateEntry -> dateEntry.getValue().entrySet().stream()
                        .map(courseEntry -> InstructorDashboardStatsResponse.EnrollmentTimelineData.builder()
                                .date(dateEntry.getKey())
                                .courseName(courseEntry.getKey())
                                .enrollmentCount(courseEntry.getValue())
                                .build()
                        )
                )
                .sorted(java.util.Comparator
                        .comparing(InstructorDashboardStatsResponse.EnrollmentTimelineData::getDate)
                        .thenComparing(InstructorDashboardStatsResponse.EnrollmentTimelineData::getCourseName)
                )
                .collect(Collectors.toList());

        // Top Favorite Courses
        List<Object[]> topFavoriteObjects = wishlistRepository.findTopFavoriteCoursesWithCountByInstructorId(instructorId, PageRequest.of(0, 5));
        List<InstructorDashboardStatsResponse.FavoriteCourseData> topFavoriteCourses = topFavoriteObjects.stream()
                .map(obj -> InstructorDashboardStatsResponse.FavoriteCourseData.builder()
                        .course(mapToCourseResponse((CourseEntity) obj[0]))
                        .favoriteCount((Long) obj[1])
                        .build())
                .collect(Collectors.toList());

        return InstructorDashboardStatsResponse.builder()
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .totalStudents(totalStudents)
                .totalRevenue(totalRevenue)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .totalWishlist(totalWishlist)
                .totalFavorites(totalFavorites)
                .enrollmentTimeline(enrollmentTimeline)
                .topFavoriteCourses(topFavoriteCourses)
                .build();
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
