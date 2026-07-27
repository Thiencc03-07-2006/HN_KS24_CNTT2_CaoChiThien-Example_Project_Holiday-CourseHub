package com.coursehub.repository;

import com.coursehub.entity.WishlistEntity;
import com.coursehub.entity.WishlistId;
import com.coursehub.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, WishlistId> {
    @org.springframework.data.jpa.repository.Query("SELECT w FROM WishlistEntity w " +
           "JOIN FETCH w.course c " +
           "JOIN FETCH c.instructor i " +
           "LEFT JOIN FETCH i.instructorProfile " +
           "JOIN FETCH c.category cat " +
           "WHERE w.userId = :userId AND c.deletedAt IS NULL")
    List<WishlistEntity> findByUserId(@org.springframework.data.repository.query.Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(w) > 0 FROM WishlistEntity w WHERE w.userId = :userId AND w.courseId = :courseId AND w.course.deletedAt IS NULL")
    boolean existsByUserIdAndCourseId(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("courseId") UUID courseId);

    @Query("SELECT COUNT(w) FROM WishlistEntity w WHERE w.course.instructor.id = :instructorId AND w.course.deletedAt IS NULL")
    long countByCourseInstructorId(@Param("instructorId") UUID instructorId);

    @Query("SELECT w.course, COUNT(w.course) FROM WishlistEntity w " +
           "WHERE w.course.instructor.id = :instructorId AND w.course.deletedAt IS NULL " +
           "GROUP BY w.course " +
           "ORDER BY COUNT(w.course) DESC")
    List<Object[]> findTopFavoriteCoursesWithCountByInstructorId(@Param("instructorId") UUID instructorId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w.course FROM WishlistEntity w " +
           "WHERE w.course.instructor.id = :instructorId AND w.course.deletedAt IS NULL " +
           "GROUP BY w.course " +
           "ORDER BY COUNT(w.course) DESC")
    List<CourseEntity> findTopFavoriteCoursesByInstructorId(@Param("instructorId") UUID instructorId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w.course FROM WishlistEntity w " +
           "GROUP BY w.course " +
           "ORDER BY COUNT(w.course) DESC")
    List<CourseEntity> findTopFavoriteCourses(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w.createdAt FROM WishlistEntity w WHERE w.course.instructor.id = :instructorId ORDER BY w.createdAt ASC")
    List<java.time.LocalDateTime> findWishlistCreatedDatesByInstructorId(@Param("instructorId") UUID instructorId);
}
