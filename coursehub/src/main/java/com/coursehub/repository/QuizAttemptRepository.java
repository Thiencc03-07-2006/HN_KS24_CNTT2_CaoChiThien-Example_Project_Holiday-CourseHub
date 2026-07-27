package com.coursehub.repository;

import com.coursehub.entity.QuizAttemptEntity;
import com.coursehub.enums.QuizAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, UUID> {
    List<QuizAttemptEntity> findByEnrollmentIdAndLessonIdOrderByStartedAtDesc(UUID enrollmentId, UUID lessonId);
    long countByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);
    boolean existsByEnrollmentIdAndLessonIdAndStatus(UUID enrollmentId, UUID lessonId, QuizAttemptStatus status);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM QuizAttemptEntity q WHERE q.lesson.id = :lessonId")
    void deleteByLessonId(@org.springframework.data.repository.query.Param("lessonId") java.util.UUID lessonId);
}
