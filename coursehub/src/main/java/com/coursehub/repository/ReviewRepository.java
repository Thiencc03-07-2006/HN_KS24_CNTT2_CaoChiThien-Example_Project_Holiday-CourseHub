package com.coursehub.repository;

import com.coursehub.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>, JpaSpecificationExecutor<ReviewEntity> {

    Optional<ReviewEntity> findByEnrollmentId(UUID enrollmentId);

    boolean existsByEnrollmentId(UUID enrollmentId);

    @Query("SELECT r FROM ReviewEntity r WHERE r.enrollment.course.id = :courseId AND r.isHidden = false")
    Page<ReviewEntity> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT r FROM ReviewEntity r WHERE r.enrollment.course.id = :courseId AND r.isHidden = false")
    List<ReviewEntity> findByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.enrollment.course.id = :courseId AND r.isHidden = false")
    Double findAverageRatingByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.enrollment.course.id = :courseId AND r.isHidden = false")
    long countByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM ReviewEntity r JOIN r.enrollment e JOIN e.course c WHERE c.instructor.id = :instructorId AND r.isHidden = false")
    Double findAverageRatingByInstructorId(@Param("instructorId") UUID instructorId);

    @Query("SELECT COUNT(r) FROM ReviewEntity r JOIN r.enrollment e JOIN e.course c WHERE c.instructor.id = :instructorId AND r.isHidden = false")
    long countReviewsByInstructorId(@Param("instructorId") UUID instructorId);

    @Query("SELECT r.rating, COUNT(r) FROM ReviewEntity r WHERE r.enrollment.course.id = :courseId AND r.isHidden = false GROUP BY r.rating")
    List<Object[]> getRatingDistributionByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT r FROM ReviewEntity r JOIN r.enrollment e JOIN e.course c WHERE c.instructor.id = :instructorId AND r.isHidden = false")
    Page<ReviewEntity> findByInstructorId(@Param("instructorId") UUID instructorId, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM ReviewEntity r JOIN r.enrollment e JOIN e.course c WHERE c.instructor.id = :instructorId AND r.isHidden = false GROUP BY r.rating")
    List<Object[]> getRatingDistributionByInstructorId(@Param("instructorId") UUID instructorId);
}

