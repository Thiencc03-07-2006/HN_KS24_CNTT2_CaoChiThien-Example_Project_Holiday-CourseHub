package com.coursehub.service.impl;

import com.coursehub.constant.AppConstants;
import com.coursehub.dto.response.InstructorReviewStatsResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.RatingSummaryResponse;
import com.coursehub.dto.response.ReviewResponse;
import com.coursehub.entity.CourseEntity;
import com.coursehub.entity.EnrollmentEntity;
import com.coursehub.entity.ReviewEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.ReviewService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;

    @Override
    @Transactional
    public ReviewEntity createOrUpdateReview(UUID userId, UUID courseId, int rating, String comment) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.hasRole(AppConstants.ROLE_STUDENT)) {
            throw new BadRequestException("ROLE_001", "Chỉ học viên mới được phép đánh giá khóa học.");
        }

        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BadRequestException("VALID_001", "Bạn phải đăng ký học khóa học này trước khi đánh giá."));

        // Check if student has completed at least one lesson
        long completedLessons = progressRepository.countByEnrollmentIdAndIsCompleted(enrollment.getId(), true);
        if (completedLessons == 0) {
            throw new BadRequestException("REVIEW_001", "Bạn phải hoàn thành ít nhất một bài học để đánh giá khóa học.");
        }

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("VALID_001", "Số sao đánh giá phải từ 1 đến 5.");
        }

        Optional<ReviewEntity> existingReviewOpt = reviewRepository.findByEnrollmentId(enrollment.getId());
        ReviewEntity review;

        if (existingReviewOpt.isPresent()) {
            review = existingReviewOpt.get();
            review.setRating(rating);
            review.setComment(comment);
            review.setUpdatedAt(LocalDateTime.now());
        } else {
            review = ReviewEntity.builder()
                    .enrollment(enrollment)
                    .rating(rating)
                    .comment(comment)
                    .isHidden(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        review = reviewRepository.save(review);
        recalculateCourseRating(courseId);
        return review;
    }

    @Override
    @Transactional
    public ReviewEntity updateReview(UUID userId, UUID reviewId, int rating, String comment) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getEnrollment().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa đánh giá này.");
        }

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("VALID_001", "Số sao đánh giá phải từ 1 đến 5.");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());

        review = reviewRepository.save(review);
        recalculateCourseRating(review.getEnrollment().getCourse().getId());
        return review;
    }

    @Override
    @Transactional
    public void deleteReview(UUID userId, UUID reviewId, boolean isAdmin) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!isAdmin && !review.getEnrollment().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa đánh giá này.");
        }

        UUID courseId = review.getEnrollment().getCourse().getId();
        reviewRepository.delete(review);
        recalculateCourseRating(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getCourseReviews(UUID courseId, int page, int size, String sort) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if (course.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        Sort sortObj;
        switch (sort.toLowerCase()) {
            case "oldest":
                sortObj = Sort.by("createdAt").ascending();
                break;
            case "highest":
                sortObj = Sort.by("rating").descending().and(Sort.by("createdAt").descending());
                break;
            case "lowest":
                sortObj = Sort.by("rating").ascending().and(Sort.by("createdAt").descending());
                break;
            case "newest":
            default:
                sortObj = Sort.by("createdAt").descending();
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<ReviewEntity> reviewPage = reviewRepository.findByCourseId(courseId, pageable);
        return PageResponse.from(reviewPage.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryResponse getRatingSummary(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
        long count = reviewRepository.countByCourseId(courseId);

        BigDecimal averageRating = BigDecimal.valueOf(avg != null ? avg : 0.0).setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(String.valueOf(i), 0L);
        }

        List<Object[]> distResults = reviewRepository.getRatingDistributionByCourseId(courseId);
        if (distResults != null) {
            for (Object[] row : distResults) {
                if (row != null && row.length == 2 && row[0] != null && row[1] != null) {
                    distribution.put(String.valueOf(row[0]), (Long) row[1]);
                }
            }
        }

        return RatingSummaryResponse.builder()
                .averageRating(averageRating)
                .totalReviews(count)
                .distribution(distribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsForAdmin(
            String keyword, UUID courseId, UUID userId, Integer rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<ReviewEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate commentLike = cb.like(cb.lower(root.get("comment")), pattern);
                Predicate userNameLike = cb.like(cb.lower(root.get("enrollment").get("user").get("fullName")), pattern);
                Predicate courseTitleLike = cb.like(cb.lower(root.get("enrollment").get("course").get("title")), pattern);
                predicates.add(cb.or(commentLike, userNameLike, courseTitleLike));
            }

            if (courseId != null) {
                predicates.add(cb.equal(root.get("enrollment").get("course").get("id"), courseId));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("enrollment").get("user").get("id"), userId));
            }

            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ReviewEntity> reviewPage = reviewRepository.findAll(spec, pageable);
        return PageResponse.from(reviewPage.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getInstructorReviews(UUID instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewEntity> reviewPage = reviewRepository.findByInstructorId(instructorId, pageable);
        return PageResponse.from(reviewPage.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorReviewStatsResponse getInstructorStats(UUID instructorId) {
        Double avg = reviewRepository.findAverageRatingByInstructorId(instructorId);
        long count = reviewRepository.countReviewsByInstructorId(instructorId);

        Map<String, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(String.valueOf(i), 0L);
        }

        List<Object[]> distResults = reviewRepository.getRatingDistributionByInstructorId(instructorId);
        if (distResults != null) {
            for (Object[] row : distResults) {
                if (row != null && row.length == 2 && row[0] != null && row[1] != null) {
                    distribution.put(String.valueOf(row[0]), (Long) row[1]);
                }
            }
        }

        return InstructorReviewStatsResponse.builder()
                .averageRating(avg != null ? avg : 0.0)
                .totalReviews(count)
                .distribution(distribution)
                .build();
    }

    private void recalculateCourseRating(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
        long count = reviewRepository.countByCourseId(courseId);

        course.setAverageRating(BigDecimal.valueOf(avg != null ? avg : 0.0).setScale(2, RoundingMode.HALF_UP));
        course.setTotalReviews((int) count);
        courseRepository.save(course);
    }

    private ReviewResponse mapToResponse(ReviewEntity review) {
        boolean isEdited = review.getUpdatedAt() != null && review.getCreatedAt() != null 
                && !review.getUpdatedAt().isEqual(review.getCreatedAt());

        return ReviewResponse.builder()
                .id(review.getId())
                .studentId(review.getEnrollment().getUser().getId())
                .studentName(review.getEnrollment().getUser().getFullName())
                .studentAvatar(review.getEnrollment().getUser().getAvatarUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .isEdited(isEdited)
                .courseId(review.getEnrollment().getCourse().getId())
                .courseTitle(review.getEnrollment().getCourse().getTitle())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
