package com.coursehub.repository;

import com.coursehub.entity.FavoriteEntity;
import com.coursehub.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, FavoriteId> {
    @org.springframework.data.jpa.repository.Query("SELECT f FROM FavoriteEntity f " +
           "JOIN FETCH f.course c " +
           "JOIN FETCH c.instructor i " +
           "LEFT JOIN FETCH i.instructorProfile " +
           "JOIN FETCH c.category cat " +
           "WHERE f.userId = :userId AND c.deletedAt IS NULL")
    List<FavoriteEntity> findByUserId(@org.springframework.data.repository.query.Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(f) > 0 FROM FavoriteEntity f WHERE f.userId = :userId AND f.courseId = :courseId AND f.course.deletedAt IS NULL")
    boolean existsByUserIdAndCourseId(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("courseId") UUID courseId);
}
