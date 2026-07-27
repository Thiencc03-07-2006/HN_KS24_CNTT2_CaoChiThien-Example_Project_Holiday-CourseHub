package com.coursehub.repository;

import com.coursehub.entity.LessonResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonResourceRepository extends JpaRepository<LessonResourceEntity, UUID> {
    Optional<LessonResourceEntity> findByLessonId(UUID lessonId);
}
