package com.coursehub.repository;

import com.coursehub.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
    List<NoteEntity> findByUserIdAndLessonIdOrderByTimestampSecondsAsc(UUID userId, UUID lessonId);
    Optional<NoteEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT n FROM NoteEntity n JOIN FETCH n.lesson l WHERE n.user.id = :userId AND l.chapter.course.id = :courseId ORDER BY n.createdAt DESC")
    List<NoteEntity> findByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
}

