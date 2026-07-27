package com.coursehub.repository;

import com.coursehub.entity.ProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<ProgressEntity, UUID> {

    Optional<ProgressEntity> findByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);

    List<ProgressEntity> findByEnrollmentId(UUID enrollmentId);

    long countByEnrollmentIdAndIsCompleted(UUID enrollmentId, boolean isCompleted);

    long countByEnrollmentId(UUID enrollmentId);

    @Query("SELECT COUNT(p) FROM ProgressEntity p WHERE p.enrollment.id = :enrollmentId AND p.isCompleted = true")
    long countCompletedLessons(@Param("enrollmentId") UUID enrollmentId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM ProgressEntity p WHERE p.lesson.id = :lessonId")
    void deleteByLessonId(@org.springframework.data.repository.query.Param("lessonId") java.util.UUID lessonId);
}
