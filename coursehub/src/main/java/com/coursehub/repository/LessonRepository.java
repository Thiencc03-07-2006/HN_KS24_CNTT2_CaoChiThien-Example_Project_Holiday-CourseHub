package com.coursehub.repository;

import com.coursehub.entity.LessonEntity;
import com.coursehub.enums.LessonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, UUID> {
    List<LessonEntity> findByChapterIdOrderByOrderIndexAsc(UUID chapterId);
    long countByChapterId(UUID chapterId);

    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM LessonEntity l WHERE l.chapter.id = :chapterId")
    int findMaxOrderIndexByChapterId(@Param("chapterId") UUID chapterId);

    @Query("SELECT COUNT(l) FROM LessonEntity l JOIN l.chapter c WHERE c.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT l FROM LessonEntity l JOIN l.chapter c WHERE c.course.id = :courseId AND l.isPreview = true")
    List<LessonEntity> findPreviewLessonsByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(l) FROM LessonEntity l JOIN l.chapter c WHERE c.course.id = :courseId AND l.lessonType = :type")
    long countByCourseIdAndType(@Param("courseId") UUID courseId, @Param("type") LessonType type);
}
