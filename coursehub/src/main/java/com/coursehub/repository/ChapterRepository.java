package com.coursehub.repository;

import com.coursehub.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID> {
    List<ChapterEntity> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
    long countByCourseId(UUID courseId);

    @Query("SELECT COALESCE(MAX(c.orderIndex), 0) FROM ChapterEntity c WHERE c.course.id = :courseId")
    int findMaxOrderIndexByCourseId(@Param("courseId") UUID courseId);

    @Modifying
    @Query("UPDATE ChapterEntity c SET c.orderIndex = :orderIndex WHERE c.id = :id")
    void updateOrderIndex(@Param("id") UUID id, @Param("orderIndex") int orderIndex);
}
