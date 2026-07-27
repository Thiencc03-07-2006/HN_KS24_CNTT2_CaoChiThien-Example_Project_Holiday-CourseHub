package com.coursehub.repository;

import com.coursehub.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    @Query(value = "SELECT c FROM CommentEntity c JOIN FETCH c.user WHERE c.lesson.id = :lessonId AND c.parent IS NULL AND c.isHidden = false ORDER BY c.createdAt ASC",
           countQuery = "SELECT count(c) FROM CommentEntity c WHERE c.lesson.id = :lessonId AND c.parent IS NULL AND c.isHidden = false")
    Page<CommentEntity> findRootCommentsByLessonId(@Param("lessonId") UUID lessonId, Pageable pageable);

    @Query("SELECT c FROM CommentEntity c JOIN FETCH c.user WHERE c.parent.id = :parentId AND c.isHidden = false ORDER BY c.createdAt ASC")
    List<CommentEntity> findRepliesByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM CommentEntity c JOIN FETCH c.user WHERE c.lesson.id = :lessonId AND c.isHidden = false ORDER BY c.createdAt ASC")
    List<CommentEntity> findByLessonIdAndIsHiddenFalseOrderByCreatedAtAsc(@Param("lessonId") UUID lessonId);

    long countByLessonId(UUID lessonId);
}
