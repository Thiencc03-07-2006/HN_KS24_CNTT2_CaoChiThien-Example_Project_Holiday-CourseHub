package com.coursehub.service;

import com.coursehub.dto.request.CreateChapterRequest;
import com.coursehub.dto.request.CreateLessonRequest;
import com.coursehub.dto.request.UpdateResourceRequest;
import com.coursehub.dto.response.ChapterResponse;
import com.coursehub.dto.response.LessonResponse;

import java.util.List;
import java.util.UUID;

public interface CurriculumService {
    List<ChapterResponse> getCurriculumByCourseId(UUID courseId, UUID currentUserId);
    
    ChapterResponse createChapter(UUID instructorId, UUID courseId, CreateChapterRequest request);
    ChapterResponse updateChapter(UUID instructorId, UUID courseId, UUID chapterId, CreateChapterRequest request);
    void deleteChapter(UUID instructorId, UUID courseId, UUID chapterId);

    LessonResponse createLesson(UUID instructorId, UUID courseId, UUID chapterId, CreateLessonRequest request);
    LessonResponse updateLesson(UUID instructorId, UUID courseId, UUID chapterId, UUID lessonId, CreateLessonRequest request);
    LessonResponse updateLessonResource(UUID instructorId, UUID courseId, UUID chapterId, UUID lessonId, UpdateResourceRequest request);
    void deleteLesson(UUID instructorId, UUID courseId, UUID chapterId, UUID lessonId);

    // New API support for cleaner REST paths
    List<ChapterResponse> getChaptersByCourseId(UUID instructorId, UUID courseId);
    ChapterResponse updateChapter(UUID instructorId, UUID chapterId, CreateChapterRequest request);
    void deleteChapter(UUID instructorId, UUID chapterId);
    List<LessonResponse> getLessonsByChapterId(UUID instructorId, UUID chapterId);
    LessonResponse createLesson(UUID instructorId, UUID chapterId, CreateLessonRequest request);
    LessonResponse updateLesson(UUID instructorId, UUID lessonId, CreateLessonRequest request);
    void deleteLesson(UUID instructorId, UUID lessonId);
}
