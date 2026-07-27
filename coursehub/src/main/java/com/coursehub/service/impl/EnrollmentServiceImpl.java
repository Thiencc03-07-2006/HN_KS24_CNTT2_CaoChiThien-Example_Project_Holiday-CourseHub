package com.coursehub.service.impl;

import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.EnrollmentResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseStatus;
import com.coursehub.enums.EnrollmentStatus;
import com.coursehub.enums.LessonType;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;

    public EnrollmentServiceImpl(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            ProgressRepository progressRepository,
            LessonRepository lessonRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
        this.lessonRepository = lessonRepository;
    }

    @Override
    @Transactional
    public EnrollmentResponse enrollCourse(UUID userId, UUID courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BadRequestException("VALID_001", "Bạn đã đăng ký khóa học này rồi.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("VALID_001", "Khóa học chưa được xuất bản.");
        }

        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("VALID_001", "Khóa học này là khóa học trả phí. Vui lòng thanh toán trước khi đăng ký.");
        }

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .user(user)
                .course(course)
                .enrollmentDate(LocalDateTime.now())
                .progressPercent(BigDecimal.ZERO)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} enrolled in course {}", userId, courseId);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getMyEnrollments(UUID userId, int page, int size) {
        Page<EnrollmentEntity> result = enrollmentRepository.findByUserId(
                userId, PageRequest.of(page, size, Sort.by("enrollmentDate").descending()));
        return PageResponse.from(result.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(UUID userId, UUID courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentDetails(UUID userId, UUID courseId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "userId & courseId", courseId));
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public void completeLesson(UUID userId, UUID courseId, UUID lessonId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "userId & courseId", courseId));

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        if (lesson.getLessonType() == LessonType.QUIZ) {
            throw new BadRequestException("VALID_001", "Bài học trắc nghiệm phải vượt qua Quiz mới được đánh dấu hoàn thành.");
        }

        ProgressEntity progress = progressRepository.findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElseGet(() -> ProgressEntity.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .build());

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        updateEnrollmentProgress(enrollment.getId());
    }

    @Override
    @Transactional
    public void updateEnrollmentProgress(UUID enrollmentId) {
        EnrollmentEntity enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        long totalLessons = lessonRepository.countByCourseId(enrollment.getCourse().getId());
        if (totalLessons == 0) {
            enrollment.setProgressPercent(BigDecimal.ZERO);
            enrollmentRepository.save(enrollment);
            return;
        }

        long completedLessons = progressRepository.countCompletedLessons(enrollmentId);

        BigDecimal progress = BigDecimal.valueOf(completedLessons)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);

        enrollment.setProgressPercent(progress);
        if (progress.compareTo(new BigDecimal("100.0")) >= 0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        }
        enrollmentRepository.save(enrollment);
    }

    private EnrollmentResponse mapToResponse(EnrollmentEntity enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .progressPercent(enrollment.getProgressPercent())
                .status(enrollment.getStatus())
                .course(mapToCourseResponse(enrollment.getCourse()))
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
}
