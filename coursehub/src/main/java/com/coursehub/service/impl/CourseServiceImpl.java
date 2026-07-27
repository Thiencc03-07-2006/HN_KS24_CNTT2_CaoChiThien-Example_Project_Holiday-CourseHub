package com.coursehub.service.impl;

import com.coursehub.constant.AppConstants;
import com.coursehub.dto.request.CreateCourseRequest;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.ChapterResponse;
import com.coursehub.dto.response.LessonResponse;
import com.coursehub.dto.response.LearningCourseResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseStatus;
import com.coursehub.enums.NotificationType;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.CourseHubException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.CourseService;
import com.coursehub.service.NotificationService;
import com.coursehub.service.CloudinaryService;
import com.coursehub.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CourseApprovalHistoryRepository approvalHistoryRepository;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(UUID instructorId, CreateCourseRequest request) {
        UserEntity instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", instructorId));

        if (!instructor.hasRole(AppConstants.ROLE_INSTRUCTOR)) {
            throw new CourseHubException("AUTHZ_002", "Chỉ giảng viên mới có thể tạo khóa học.", HttpStatus.FORBIDDEN);
        }

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Build unique slug
        String baseSlug = AppUtils.toSlug(request.getTitle());
        String slug = courseRepository.existsBySlugAndDeletedAtIsNull(baseSlug) ? AppUtils.toUniqueSlug(request.getTitle()) : baseSlug;

        CourseEntity course = CourseEntity.builder()
                .instructor(instructor)
                .category(category)
                .title(request.getTitle())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .price(request.getPrice())
                .level(request.getLevel())
                .language(request.getLanguage() != null ? request.getLanguage() : "Vietnamese")
                .status(CourseStatus.DRAFT)
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        courseRepository.save(course);
        log.info("Course created: {} by instructor: {}", slug, instructorId);
        return mapToCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseBySlug(String slug, UUID currentUserId) {
        CourseEntity course = courseRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "slug", slug));
        validateCourseAccess(course, currentUserId);
        CourseResponse response = mapToCourseResponse(course);

        // Fetch curriculum
        List<ChapterResponse> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).stream()
            .map(chapter -> {
                List<LessonResponse> lessons = lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapter.getId()).stream()
                    .map(lesson -> {
                        LessonResourceEntity resource = lesson.getResource();
                        return LessonResponse.builder()
                                .id(lesson.getId())
                                .title(lesson.getTitle())
                                .orderIndex(lesson.getOrderIndex())
                                .lessonType(lesson.getLessonType())
                                .isPreview(lesson.getIsPreview())
                                .resourceUrl(lesson.getIsPreview() && resource != null ? resource.getResourceUrl() : null)
                                .durationSeconds(resource != null ? resource.getDurationSeconds() : null)
                                .isDownloadable(resource != null && resource.getIsDownloadable())
                                .videoStatus(resource != null && resource.getVideoStatus() != null ? resource.getVideoStatus().name() : "NONE")
                                .build();
                    }).collect(Collectors.toList());

                return ChapterResponse.builder()
                        .id(chapter.getId())
                        .title(chapter.getTitle())
                        .orderIndex(chapter.getOrderIndex())
                        .lessons(lessons)
                        .build();
            }).collect(Collectors.toList());

        response.setChapters(chapters);
        response.setTotalLessons(chapters.stream().mapToInt(c -> c.getLessons() != null ? c.getLessons().size() : 0).sum());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID courseId, UUID currentUserId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if (course.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        validateCourseAccess(course, currentUserId);
        return mapToCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getInstructorCourses(UUID instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CourseEntity> coursePage = courseRepository.findByInstructorIdAndDeletedAtIsNull(instructorId, pageable);
        return PageResponse.from(coursePage.map(this::mapToCourseResponse));
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(UUID instructorId, UUID courseId, CreateCourseRequest request) {
        CourseEntity course = findCourseOwnedBy(courseId, instructorId);

        if (!course.isEditable()) {
            throw new BadRequestException("VALID_001",
                    "Chỉ có thể chỉnh sửa khóa học ở trạng thái DRAFT, REJECTED, BLOCKED hoặc BLOCKED_EDITED.");
        }

        boolean wasBlocked = course.getStatus() == CourseStatus.BLOCKED;

        if (request.getTitle() != null && !request.getTitle().equals(course.getTitle())) {
            String baseSlug = AppUtils.toSlug(request.getTitle());
            String slug = courseRepository.existsBySlugAndDeletedAtIsNull(baseSlug) ? AppUtils.toUniqueSlug(request.getTitle()) : baseSlug;
            course.setTitle(request.getTitle());
            course.setSlug(slug);
        }
        if (request.getShortDescription() != null) course.setShortDescription(request.getShortDescription());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            course.setCategory(category);
        }
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getLanguage() != null) course.setLanguage(request.getLanguage());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());

        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(course);
        return mapToCourseResponse(course);
    }

    @Override
    @Transactional
    public String uploadThumbnail(UUID instructorId, UUID courseId, MultipartFile file) {
        if (!AppUtils.isAllowedContentType(file, AppConstants.ALLOWED_IMAGE_TYPES)) {
            throw new BadRequestException("VALID_003", "Định dạng ảnh không hợp lệ.");
        }
        if (!AppUtils.isWithinSizeLimit(file, AppConstants.MAX_THUMBNAIL_SIZE_BYTES)) {
            throw new BadRequestException("VALID_002", "Thumbnail không được vượt quá 5MB.");
        }

        CourseEntity course = findCourseOwnedBy(courseId, instructorId);

        try {
            String url = cloudinaryService.uploadFile(file, "coursehub/thumbnail");
            course.setThumbnailUrl(url);
            courseRepository.save(course);
            return url;
        } catch (Exception e) {
            throw new BadRequestException("SYS_001", "Lỗi upload thumbnail: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void submitForReview(UUID instructorId, UUID courseId) {
        CourseEntity course = findCourseOwnedBy(courseId, instructorId);

        if (course.getStatus() != CourseStatus.DRAFT && 
            course.getStatus() != CourseStatus.REJECTED && 
            course.getStatus() != CourseStatus.BLOCKED) {
            throw new BadRequestException("COURSE_003", 
                "Khóa học phải ở trạng thái DRAFT, REJECTED hoặc BLOCKED mới có thể gửi duyệt.");
        }
        validateCourseReadyForSubmit(course);

        if (course.getStatus() == CourseStatus.BLOCKED) {
            course.setStatus(CourseStatus.BLOCKED_EDITED);
        } else {
            course.setStatus(CourseStatus.PENDING_REVIEW);
            course.setRejectedReason(null);
            course.setRejectedBy(null);
            course.setRejectedAt(null);
        }
        courseRepository.save(course);

        saveApprovalHistory(course, instructorId, "SUBMIT", "Giảng viên gửi duyệt");
        log.info("Course {} submitted for review by instructor {}", courseId, instructorId);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID instructorId, UUID courseId) {
        CourseEntity course = findCourseOwnedBy(courseId, instructorId);
        if (!course.isEditable()) {
            throw new BadRequestException("VALID_001", "Chỉ có thể xóa khóa học ở trạng thái DRAFT hoặc REJECTED.");
        }
        course.setDeletedAt(java.time.LocalDateTime.now());
        course.setSlug(course.getSlug() + "-deleted-" + System.currentTimeMillis());
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void approveCourse(UUID adminId, UUID courseId, String note) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setStatus(CourseStatus.PUBLISHED);
        courseRepository.save(course);
        saveApprovalHistory(course, adminId, "APPROVE", note);

        notificationService.sendNotification(
                course.getInstructor().getId(),
                "Khóa học được duyệt",
                "Khóa học \"" + course.getTitle() + "\" đã được duyệt và đang hiển thị công khai.",
                NotificationType.COURSE_APPROVED,
                "/instructor/courses/" + courseId);
    }

    @Override
    @Transactional
    public void rejectCourse(UUID adminId, UUID courseId, String note) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setStatus(CourseStatus.REJECTED);
        course.setRejectedReason(note);
        course.setRejectedBy(adminId.toString());
        course.setRejectedAt(java.time.LocalDateTime.now());
        courseRepository.save(course);
        saveApprovalHistory(course, adminId, "REJECT", note);

        notificationService.sendNotification(
                course.getInstructor().getId(),
                "Khóa học bị từ chối",
                "Khóa học \"" + course.getTitle() + "\" bị từ chối. Lý do: " + (note != null ? note : "Không có"),
                NotificationType.COURSE_REJECTED,
                "/instructor/courses/" + courseId + "/edit");
    }

    @Override
    @Transactional
    public void blockCourse(UUID adminId, UUID courseId, String note) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setStatus(CourseStatus.BLOCKED);
        courseRepository.save(course);
        saveApprovalHistory(course, adminId, "BLOCK", note);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> searchCourses(String keyword, Long categoryId, String level,
                                                      BigDecimal minPrice, BigDecimal maxPrice, BigDecimal rating,
                                                      String language, String sortBy, int page, int size) {
        Pageable pageable = buildPageable(sortBy, page, size);

        Specification<CourseEntity> spec = buildSearchSpec(keyword, categoryId, level, minPrice, maxPrice, rating, language);
        Page<CourseEntity> result = courseRepository.findAll(spec, pageable);
        return PageResponse.from(result.map(this::mapToCourseResponse));
    }

    // ==================== PRIVATE HELPERS ====================

    private CourseEntity findCourseOwnedBy(UUID courseId, UUID instructorId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if (!course.isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        return course;
    }

    private void validateCourseAccess(CourseEntity course, UUID currentUserId) {
        if (course.getStatus() == CourseStatus.PUBLISHED) return;
        if (currentUserId != null && (course.isOwnedBy(currentUserId))) return;
        throw new ResourceNotFoundException("Course", "id", course.getId());
    }

    private void validateCourseReadyForSubmit(CourseEntity course) {
        if (course.getThumbnailUrl() == null || course.getThumbnailUrl().isBlank()) {
            throw new BadRequestException("COURSE_003", "Khóa học cần có thumbnail trước khi gửi duyệt.");
        }
        if (course.getChapters() == null || course.getChapters().isEmpty()) {
            throw new BadRequestException("COURSE_003", "Khóa học cần có ít nhất 1 chương và 1 bài học.");
        }
    }

    private void saveApprovalHistory(CourseEntity course, UUID actorId, String action, String note) {
        UserEntity actor = userRepository.getReferenceById(actorId);
        CourseApprovalHistoryEntity history = CourseApprovalHistoryEntity.builder()
                .course(course)
                .actor(actor)
                .action(action)
                .note(note)
                .build();
        approvalHistoryRepository.save(history);
    }

    private Specification<CourseEntity> buildSearchSpec(String keyword, Long categoryId, String level,
                                                        BigDecimal minPrice, BigDecimal maxPrice, BigDecimal rating, String language) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), CourseStatus.PUBLISHED));
            predicates.add(cb.isNull(root.get("deletedAt")));
            
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("shortDescription")), likePattern),
                        cb.like(cb.lower(root.get("instructor").get("fullName")), likePattern)
                ));
            }
            if (categoryId != null) predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            if (level != null && !level.isBlank()) predicates.add(cb.equal(root.get("level").as(String.class), level.toUpperCase()));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (rating != null) predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), rating));
            if (language != null && !language.isBlank()) predicates.add(cb.equal(root.get("language"), language));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(String sortBy, int page, int size) {
        int pageSize = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "newest") {
            case "rating" -> Sort.by("averageRating").descending();
            case "popular" -> Sort.by("totalReviews").descending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };
        return PageRequest.of(page, pageSize, sort);
    }

    public CourseResponse mapToCourseResponse(CourseEntity course) {
        InstructorProfileEntity instructorProfile = course.getInstructor().getInstructorProfile();
        CourseResponse.BlockedBySummary blockedBy = null;
        if (course.getBlockedBy() != null) {
            blockedBy = CourseResponse.BlockedBySummary.builder()
                    .id(course.getBlockedBy().getId())
                    .fullName(course.getBlockedBy().getFullName())
                    .email(course.getBlockedBy().getEmail())
                    .avatarUrl(course.getBlockedBy().getAvatarUrl())
                    .build();
        }

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
                .rejectReason(course.getRejectedReason())
                .rejectedAt(course.getRejectedAt())
                .blockedReason(course.getBlockedReason())
                .blockedBy(blockedBy)
                .blockedAt(course.getBlockedAt())
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
                .totalChapters(course.getChapters().size())
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseDetailPublic(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if (course.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        validateCourseAccess(course, null);

        CourseResponse response = mapToCourseResponse(course);

        // Fetch curriculum
        List<ChapterResponse> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
            .map(chapter -> {
                List<LessonResponse> lessons = lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapter.getId()).stream()
                    .map(lesson -> {
                        LessonResourceEntity resource = lesson.getResource();
                        return LessonResponse.builder()
                                .id(lesson.getId())
                                .title(lesson.getTitle())
                                .orderIndex(lesson.getOrderIndex())
                                .lessonType(lesson.getLessonType())
                                .isPreview(lesson.getIsPreview())
                                .resourceUrl(lesson.getIsPreview() && resource != null ? resource.getResourceUrl() : null)
                                .durationSeconds(resource != null ? resource.getDurationSeconds() : null)
                                .isDownloadable(resource != null && resource.getIsDownloadable())
                                .videoStatus(resource != null && resource.getVideoStatus() != null ? resource.getVideoStatus().name() : "NONE")
                                .build();
                    }).collect(Collectors.toList());

                return ChapterResponse.builder()
                        .id(chapter.getId())
                        .title(chapter.getTitle())
                        .orderIndex(chapter.getOrderIndex())
                        .lessons(lessons)
                        .build();
            }).collect(Collectors.toList());

        response.setChapters(chapters);
        response.setTotalLessons(chapters.stream().mapToInt(c -> c.getLessons() != null ? c.getLessons().size() : 0).sum());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LearningCourseResponse getLearningCourseContent(UUID userId, UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Check if user is enrolled
        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BadRequestException("VALID_001", "Bạn chưa đăng ký khóa học này."));

        CourseResponse courseResponse = mapToCourseResponse(course);

        // Load progress
        List<ProgressEntity> progressList = progressRepository.findByEnrollmentId(enrollment.getId());
        Set<UUID> completedLessonIds = progressList.stream()
                .filter(ProgressEntity::getIsCompleted)
                .map(p -> p.getLesson().getId())
                .collect(Collectors.toSet());

        List<ChapterResponse> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
            .map(chapter -> {
                List<LessonResponse> lessons = lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapter.getId()).stream()
                    .map(lesson -> {
                        LessonResourceEntity resource = lesson.getResource();
                        return LessonResponse.builder()
                                .id(lesson.getId())
                                .title(lesson.getTitle())
                                .orderIndex(lesson.getOrderIndex())
                                .lessonType(lesson.getLessonType())
                                .isPreview(lesson.getIsPreview())
                                .resourceUrl(resource != null ? resource.getResourceUrl() : null)
                                .durationSeconds(resource != null ? resource.getDurationSeconds() : null)
                                .textContent(resource != null ? resource.getTextContent() : null)
                                .isDownloadable(resource != null && resource.getIsDownloadable())
                                .videoStatus(resource != null && resource.getVideoStatus() != null ? resource.getVideoStatus().name() : "NONE")
                                .isCompleted(completedLessonIds.contains(lesson.getId()))
                                .build();
                    }).collect(Collectors.toList());

                return ChapterResponse.builder()
                        .id(chapter.getId())
                        .title(chapter.getTitle())
                        .orderIndex(chapter.getOrderIndex())
                        .lessons(lessons)
                        .build();
            }).collect(Collectors.toList());

        return LearningCourseResponse.builder()
                .course(courseResponse)
                .chapters(chapters)
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
