package com.coursehub.repository;

import com.coursehub.entity.CourseEntity;
import com.coursehub.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID>, JpaSpecificationExecutor<CourseEntity> {

    Optional<CourseEntity> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    Page<CourseEntity> findByInstructorIdAndDeletedAtIsNull(UUID instructorId, Pageable pageable);

    Page<CourseEntity> findByStatusAndDeletedAtIsNull(CourseStatus status, Pageable pageable);

    long countByInstructorIdAndDeletedAtIsNull(UUID instructorId);

    long countByInstructorIdAndStatusAndDeletedAtIsNull(UUID instructorId, CourseStatus status);

    long countByStatus(CourseStatus status);

    @Query("SELECT c FROM CourseEntity c WHERE c.instructor.id = :instructorId AND c.status = :status AND c.deletedAt IS NULL")
    List<CourseEntity> findByInstructorIdAndStatus(@Param("instructorId") UUID instructorId, @Param("status") CourseStatus status);

    @Query(value = "SELECT c.* FROM courses c WHERE c.deleted_at IS NULL AND c.status = 'PUBLISHED' " +
            "AND MATCH(c.title, c.short_description) AGAINST (:keyword IN BOOLEAN MODE) " +
            "ORDER BY MATCH(c.title, c.short_description) AGAINST (:keyword IN BOOLEAN MODE) DESC",
            countQuery = "SELECT count(*) FROM courses c WHERE c.deleted_at IS NULL AND c.status = 'PUBLISHED' " +
                    "AND MATCH(c.title, c.short_description) AGAINST (:keyword IN BOOLEAN MODE)",
            nativeQuery = true)
    Page<CourseEntity> fullTextSearch(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE CourseEntity c SET c.averageRating = :rating, c.totalReviews = :totalReviews WHERE c.id = :courseId")
    void updateRating(@Param("courseId") UUID courseId, @Param("rating") java.math.BigDecimal rating, @Param("totalReviews") int totalReviews);

    // Stats
    @Query("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.course.instructor.id = :instructorId")
    long countTotalStudentsByInstructor(@Param("instructorId") UUID instructorId);
}
