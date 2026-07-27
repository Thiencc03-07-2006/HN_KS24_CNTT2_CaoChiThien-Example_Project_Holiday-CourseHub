package com.coursehub.service.impl;

import com.coursehub.dto.request.BlockCourseRequest;
import com.coursehub.dto.response.*;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseStatus;
import com.coursehub.enums.LessonType;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.AdminCourseService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseApprovalHistoryRepository courseApprovalHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminCourseResponse> getCourses(String status, String instructor, String category, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<CourseEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (status != null && !status.isBlank()) {
                if ("BLOCKED".equalsIgnoreCase(status)) {
                    predicates.add(root.get("status").in(CourseStatus.BLOCKED, CourseStatus.BLOCKED_EDITED));
                } else {
                    CourseStatus dbStatus = mapApiStatusToDb(status);
                    if (dbStatus != null) {
                        predicates.add(cb.equal(root.get("status"), dbStatus));
                    } else {
                        try {
                            predicates.add(cb.equal(root.get("status"), CourseStatus.valueOf(status.toUpperCase())));
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid status filter: {}", status);
                        }
                    }
                }
            }

            if (instructor != null && !instructor.isBlank()) {
                Join<CourseEntity, UserEntity> instructorJoin = root.join("instructor");
                predicates.add(cb.or(
                        cb.like(cb.lower(instructorJoin.get("fullName")), "%" + instructor.toLowerCase() + "%"),
                        cb.equal(instructorJoin.get("id").as(String.class), instructor)
                ));
            }

            if (category != null && !category.isBlank()) {
                Join<CourseEntity, CategoryEntity> categoryJoin = root.join("category");
                predicates.add(cb.or(
                        cb.like(cb.lower(categoryJoin.get("name")), "%" + category.toLowerCase() + "%"),
                        cb.equal(categoryJoin.get("slug"), category),
                        cb.equal(categoryJoin.get("id").as(String.class), category)
                ));
            }

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("shortDescription")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CourseEntity> result = courseRepository.findAll(spec, pageable);
        return PageResponse.from(result.map(this::mapToAdminCourseResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCourseDetailResponse getCourseDetail(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Get instructor details
        AdminCourseDetailResponse.InstructorInfo instructor = AdminCourseDetailResponse.InstructorInfo.builder()
                .id(course.getInstructor().getId())
                .fullName(course.getInstructor().getFullName())
                .avatarUrl(course.getInstructor().getAvatarUrl())
                .email(course.getInstructor().getEmail())
                .build();

        // Get category details
        AdminCourseDetailResponse.CategoryInfo category = AdminCourseDetailResponse.CategoryInfo.builder()
                .id(course.getCategory().getId())
                .name(course.getCategory().getName())
                .slug(course.getCategory().getSlug())
                .build();

        // Get Chapters & Lessons
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<AdminCourseDetailResponse.ChapterDetail> chapterDetails = chapters.stream().map(ch -> {
            List<LessonEntity> lessons = lessonRepository.findByChapterIdOrderByOrderIndexAsc(ch.getId());
            List<AdminCourseDetailResponse.LessonDetail> lessonDetails = lessons.stream().map(l -> {
                LessonResourceEntity res = l.getResource();

                // If it is a quiz, load its questions and answers
                List<QuestionResponse> questions = null;
                if (l.getLessonType() == LessonType.QUIZ) {
                    questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(l.getId()).stream()
                            .map(q -> {
                                List<AnswerResponse> answers = new ArrayList<>();
                                if (q.getAnswers() != null) {
                                    answers = q.getAnswers().stream()
                                            .map(a -> AnswerResponse.builder()
                                                    .id(a.getId())
                                                    .content(a.getContent())
                                                    .orderIndex(a.getOrderIndex())
                                                    .isCorrect(a.getIsCorrect())
                                                    .build())
                                            .collect(Collectors.toList());
                                }
                                return QuestionResponse.builder()
                                        .id(q.getId())
                                        .content(q.getContent())
                                        .questionType(q.getQuestionType())
                                        .points(q.getPoints())
                                        .orderIndex(q.getOrderIndex())
                                        .explanation(q.getExplanation())
                                        .answers(answers)
                                        .build();
                            }).collect(Collectors.toList());
                }

                return AdminCourseDetailResponse.LessonDetail.builder()
                        .id(l.getId())
                        .title(l.getTitle())
                        .orderIndex(l.getOrderIndex())
                        .lessonType(l.getLessonType().name())
                        .isPreview(l.getIsPreview())
                        .resourceUrl(res != null ? res.getResourceUrl() : null)
                        .durationSeconds(res != null ? res.getDurationSeconds() : null)
                        .textContent(res != null ? res.getTextContent() : null)
                        .isDownloadable(res != null && res.getIsDownloadable())
                        .questions(questions)
                        .build();
            }).collect(Collectors.toList());

            return AdminCourseDetailResponse.ChapterDetail.builder()
                    .id(ch.getId())
                    .title(ch.getTitle())
                    .orderIndex(ch.getOrderIndex())
                    .lessons(lessonDetails)
                    .build();
        }).collect(Collectors.toList());

        // Get reviews
        List<ReviewEntity> reviews = reviewRepository.findByCourseId(courseId);
        List<ReviewResponse> reviewResponses = reviews.stream().map(r -> ReviewResponse.builder()
                .id(r.getId())
                .studentId(r.getEnrollment().getUser().getId())
                .studentName(r.getEnrollment().getUser().getFullName())
                .studentAvatar(r.getEnrollment().getUser().getAvatarUrl())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build()).collect(Collectors.toList());

        // Get rejection details if REJECTED
        String rejectReason = null;
        String rejectedBy = null;
        LocalDateTime rejectedAt = null;
        if (course.getStatus() == CourseStatus.REJECTED) {
            List<CourseApprovalHistoryEntity> historyList = courseApprovalHistoryRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
            for (CourseApprovalHistoryEntity h : historyList) {
                if ("REJECT".equals(h.getAction())) {
                    rejectReason = h.getNote();
                    rejectedBy = h.getActor() != null ? h.getActor().getFullName() : null;
                    rejectedAt = h.getCreatedAt();
                    break;
                }
            }
        }

        AdminCourseDetailResponse.BlockedByInfo blockedByInfo = null;
        if (course.getBlockedBy() != null) {
            blockedByInfo = AdminCourseDetailResponse.BlockedByInfo.builder()
                    .id(course.getBlockedBy().getId())
                    .fullName(course.getBlockedBy().getFullName())
                    .email(course.getBlockedBy().getEmail())
                    .avatarUrl(course.getBlockedBy().getAvatarUrl())
                    .build();
        }

        return AdminCourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnailUrl(course.getThumbnailUrl())
                .promoVideoUrl(course.getPromoVideoUrl())
                .level(course.getLevel().name())
                .language(course.getLanguage())
                .status(mapDbStatusToApi(course.getStatus()))
                .averageRating(course.getAverageRating())
                .totalReviews(course.getTotalReviews())
                .enrollmentCount(enrollmentRepository.countByCourseId(course.getId()))
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .blockedReason(course.getBlockedReason())
                .blockedBy(blockedByInfo)
                .blockedAt(course.getBlockedAt())
                .rejectReason(rejectReason)
                .rejectedBy(rejectedBy)
                .rejectedAt(rejectedAt)
                .instructor(instructor)
                .category(category)
                .chapters(chapterDetails)
                .reviews(reviewResponses)
                .build();
    }

    @Override
    @Transactional
    public void blockCourse(UUID courseId, BlockCourseRequest request, UUID adminId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        course.setStatus(CourseStatus.BLOCKED);
        course.setBlockedReason(request.getReason());
        course.setBlockedBy(admin);
        course.setBlockedAt(LocalDateTime.now());

        courseRepository.save(course);
        log.info("Course {} blocked by admin {}", courseId, adminId);
    }

    @Override
    @Transactional
    public void unblockCourse(UUID courseId, UUID adminId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getStatus() != CourseStatus.BLOCKED && course.getStatus() != CourseStatus.BLOCKED_EDITED) {
            throw new BadRequestException("VALID_001", "Khóa học không ở trạng thái bị chặn.");
        }

        course.setStatus(CourseStatus.PUBLISHED); // return to active (PUBLISHED) state
        course.setBlockedReason(null);
        course.setBlockedBy(null);
        course.setBlockedAt(null);

        courseRepository.save(course);
        log.info("Course {} unblocked by admin {}", courseId, adminId);
    }

    private CourseStatus mapApiStatusToDb(String status) {
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return CourseStatus.PUBLISHED;
            case "PENDING":
                return CourseStatus.PENDING_REVIEW;
            case "DRAFT":
                return CourseStatus.DRAFT;
            case "BLOCKED":
                return CourseStatus.BLOCKED;
            case "BLOCKED_EDITED":
                return CourseStatus.BLOCKED_EDITED;
            case "REJECTED":
                return CourseStatus.REJECTED;
            default:
                return null;
        }
    }

    private String mapDbStatusToApi(CourseStatus status) {
        if (status == null) return null;
        switch (status) {
            case PUBLISHED:
                return "ACTIVE";
            case PENDING_REVIEW:
                return "PENDING";
            case DRAFT:
                return "DRAFT";
            case BLOCKED:
                return "BLOCKED";
            case BLOCKED_EDITED:
                return "BLOCKED_EDITED";
            default:
                return status.name();
        }
    }

    private AdminCourseResponse mapToAdminCourseResponse(CourseEntity course) {
        return AdminCourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .thumbnail(course.getThumbnailUrl())
                .instructor(course.getInstructor().getFullName())
                .status(mapDbStatusToApi(course.getStatus()))
                .createdAt(course.getCreatedAt())
                .enrollmentCount(enrollmentRepository.countByCourseId(course.getId()))
                .rating(course.getAverageRating())
                .blockedReason(course.getBlockedReason())
                .rejectReason(course.getRejectedReason())
                .build();
    }
}
