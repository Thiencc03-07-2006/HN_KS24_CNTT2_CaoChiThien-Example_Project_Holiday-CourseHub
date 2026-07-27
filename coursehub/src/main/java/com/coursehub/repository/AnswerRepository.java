package com.coursehub.repository;

import com.coursehub.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, UUID> {
    List<AnswerEntity> findByQuestionIdOrderByOrderIndexAsc(UUID questionId);
}
