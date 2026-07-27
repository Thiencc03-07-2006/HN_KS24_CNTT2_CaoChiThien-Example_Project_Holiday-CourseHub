package com.coursehub.repository;

import com.coursehub.entity.QuestionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {
    @EntityGraph(attributePaths = {"answers"})
    List<QuestionEntity> findByQuizIdOrderByOrderIndexAsc(UUID quizId);
}
