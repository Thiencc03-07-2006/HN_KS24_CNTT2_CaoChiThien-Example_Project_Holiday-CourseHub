package com.coursehub.repository;

import com.coursehub.entity.QuizConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizConfigRepository extends JpaRepository<QuizConfigEntity, UUID> {
    Optional<QuizConfigEntity> findByLessonId(UUID lessonId);
}
