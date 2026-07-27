package com.coursehub.service.impl;

import com.coursehub.dto.request.CreateChapterRequest;
import com.coursehub.dto.request.CreateLessonRequest;
import com.coursehub.dto.request.UpdateResourceRequest;
import com.coursehub.dto.response.ChapterResponse;
import com.coursehub.dto.response.LessonResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.LessonType;
import com.coursehub.enums.VideoStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.CourseHubException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final LessonResourceRepository lessonResourceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getCurriculumByCourseId(UUID courseId, UUID currentUserId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        boolean hasFullAccess = false;
        if (currentUserId != null) {
            boolean isInstructor = course.isOwnedBy(currentUserId);
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId);
            hasFullAccess = isInstructor || isEnrolled;
        }

        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        final boolean finalHasFullAccess = hasFullAccess;

        return chapters.stream().map(chapter -> {
            List<LessonEntity> lessons = lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapter.getId());
            List<LessonResponse> lessonResponses = lessons.stream().map(lesson -> {
                LessonResourceEntity resource = lesson.getResource();
                boolean showContent = finalHasFullAccess || lesson.getIsPreview();

                return LessonResponse.builder()
                        .id(lesson.getId())
                        .title(lesson.getTitle())
                        .orderIndex(lesson.getOrderIndex())
                        .lessonType(lesson.getLessonType())
                        .isPreview(lesson.getIsPreview())
                        .resourceUrl(showContent && resource != null ? resource.getResourceUrl() : null)
                        .durationSeconds(resource != null ? resource.getDurationSeconds() : null)
                        .textContent(showContent && resource != null ? resource.getTextContent() : null)
                        .isDownloadable(resource != null && resource.getIsDownloadable())
                        .videoStatus(resource != null && resource.getVideoStatus() != null ? resource.getVideoStatus().name() : "NONE")
                        .build();
            }).collect(Collectors.toList());

            return ChapterResponse.builder()
                    .id(chapter.getId())
                    .title(chapter.getTitle())
                    .orderIndex(chapter.getOrderIndex())
                    .lessons(lessonResponses)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChapterResponse createChapter(UUID instructorId, UUID courseId, CreateChapterRequest request) {
        CourseEntity course = getOwnedCourse(courseId, instructorId);
        int maxIndex = chapterRepository.findMaxOrderIndexByCourseId(courseId);

        ChapterEntity chapter = ChapterEntity.builder()
                .course(course)
                .title(request.getTitle())
                .orderIndex(maxIndex + 1)
                .build();

        chapter = chapterRepository.save(chapter);
        return ChapterResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .orderIndex(chapter.getOrderIndex())
                .lessons(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional
    public ChapterResponse updateChapter(UUID instructorId, UUID courseId, UUID chapterId, CreateChapterRequest request) {
        getOwnedCourse(courseId, instructorId);
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new BadRequestException("VALID_001", "Chương học không thuộc khóa học này.");
        }

        chapter.setTitle(request.getTitle());
        chapter = chapterRepository.save(chapter);

        return ChapterResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .orderIndex(chapter.getOrderIndex())
                .build();
    }

    @Override
    @Transactional
    public void deleteChapter(UUID instructorId, UUID courseId, UUID chapterId) {
        getOwnedCourse(courseId, instructorId);
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new BadRequestException("VALID_001", "Chương học không thuộc khóa học này.");
        }

        chapterRepository.delete(chapter);
    }

    @Override
    @Transactional
    public LessonResponse createLesson(UUID instructorId, UUID courseId, UUID chapterId, CreateLessonRequest request) {
        getOwnedCourse(courseId, instructorId);
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));

        if (!chapter.getCourse().getId().equals(courseId)) {
            throw new BadRequestException("VALID_001", "Chương học không thuộc khóa học này.");
        }

        int maxIndex = lessonRepository.findMaxOrderIndexByChapterId(chapterId);

        LessonEntity lesson = LessonEntity.builder()
                .chapter(chapter)
                .title(request.getTitle())
                .lessonType(request.getLessonType())
                .isPreview(request.isPreview())
                .orderIndex(maxIndex + 1)
                .build();

        lesson = lessonRepository.save(lesson);

        // Auto create lesson resource record
        LessonResourceEntity resource = LessonResourceEntity.builder()
                .lesson(lesson)
                .isDownloadable(false)
                .videoStatus(request.getLessonType() == LessonType.VIDEO ? VideoStatus.NONE : VideoStatus.NONE)
                .build();
        lessonResourceRepository.save(resource);
        lesson.setResource(resource);

        return mapToLessonResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(UUID instructorId, UUID courseId, UUID chapterId, UUID lessonId, CreateLessonRequest request) {
        getOwnedCourse(courseId, instructorId);
        LessonEntity lesson = getLessonInChapterAndCourse(courseId, chapterId, lessonId);

        lesson.setTitle(request.getTitle());
        lesson.setIsPreview(request.isPreview());
        lesson = lessonRepository.save(lesson);

        return mapToLessonResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLessonResource(UUID instructorId, UUID courseId, UUID chapterId, UUID lessonId, UpdateResourceRequest request) {
        getOwnedCourse(courseId, instructorId);
        LessonEntity lesson = getLessonInChapterAndCourse(courseId, chapterId, lessonId);

        LessonResourceEntity resource = lesson.getResource();
        if (resource == null) {
            resource = LessonResourceEntity.builder().lesson(lesson).build();
        }

        if (request.getResourceUrl() != null) resource.setResourceUrl(request.getResourceUrl());
        if (request.getDurationSeconds() != null) resource.setDurationSeconds(request.getDurationSeconds());
        if (request.getTextContent() != null) resource.setTextContent(request.getTextContent());
        resource.setIsDownloadable(request.isDownloadable());
        
        if (lesson.getLessonType() == LessonType.VIDEO && request.getResourceUrl() != null) {
            resource.setVideoStatus(VideoStatus.READY); // Mock ready for video file uploads
        }

        lessonResourceRepository.save(resource);
        lesson.setResource(resource);

        return mapToLessonResponse(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID instructorId, UUID courseId, UUID chapterId, UUID lessonId) {
        getOwnedCourse(courseId, instructorId);
        LessonEntity lesson = getLessonInChapterAndCourse(courseId, chapterId, lessonId);
        quizAttemptRepository.deleteByLessonId(lessonId);
        progressRepository.deleteByLessonId(lessonId);
        lessonRepository.delete(lesson);
    }

    private CourseEntity getOwnedCourse(UUID courseId, UUID instructorId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if (!course.isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        return course;
    }

    private LessonEntity getLessonInChapterAndCourse(UUID courseId, UUID chapterId, UUID lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        
        if (!lesson.getChapter().getId().equals(chapterId) || 
            !lesson.getChapter().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("VALID_001", "Bài học không thuộc chương học hoặc khóa học này.");
        }
        return lesson;
    }

    private LessonResponse mapToLessonResponse(LessonEntity lesson) {
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
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByCourseId(UUID instructorId, UUID courseId) {
        getOwnedCourse(courseId, instructorId);
        return getCurriculumByCourseId(courseId, instructorId);
    }

    @Override
    @Transactional
    public ChapterResponse updateChapter(UUID instructorId, UUID chapterId, CreateChapterRequest request) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));
        if (!chapter.getCourse().isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        chapter.setTitle(request.getTitle());
        if (request.getOrderIndex() != null && request.getOrderIndex() > 0) {
            chapter.setOrderIndex(request.getOrderIndex());
        }
        chapter = chapterRepository.save(chapter);
        return ChapterResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .orderIndex(chapter.getOrderIndex())
                .build();
    }

    @Override
    @Transactional
    public void deleteChapter(UUID instructorId, UUID chapterId) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));
        if (!chapter.getCourse().isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        chapterRepository.delete(chapter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByChapterId(UUID instructorId, UUID chapterId) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));
        if (!chapter.getCourse().isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        List<LessonEntity> lessons = lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapterId);
        return lessons.stream().map(this::mapToLessonResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LessonResponse createLesson(UUID instructorId, UUID chapterId, CreateLessonRequest request) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", chapterId));
        if (!chapter.getCourse().isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        int maxIndex = lessonRepository.findMaxOrderIndexByChapterId(chapterId);
        LessonEntity lesson = LessonEntity.builder()
                .chapter(chapter)
                .title(request.getTitle())
                .lessonType(request.getLessonType())
                .isPreview(request.isPreview())
                .orderIndex(maxIndex + 1)
                .build();
        lesson = lessonRepository.save(lesson);

        LessonResourceEntity resource = LessonResourceEntity.builder()
                .lesson(lesson)
                .isDownloadable(false)
                .videoStatus(VideoStatus.NONE)
                .build();
        lessonResourceRepository.save(resource);
        lesson.setResource(resource);

        return mapToLessonResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(UUID instructorId, UUID lessonId, CreateLessonRequest request) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getChapter().getCourse().isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        lesson.setTitle(request.getTitle());
        lesson.setIsPreview(request.isPreview());
        if (request.getLessonType() != null) {
            lesson.setLessonType(request.getLessonType());
        }
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID instructorId, UUID lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getChapter().getCourse().isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        quizAttemptRepository.deleteByLessonId(lessonId);
        progressRepository.deleteByLessonId(lessonId);
        lessonRepository.delete(lesson);
    }
}
