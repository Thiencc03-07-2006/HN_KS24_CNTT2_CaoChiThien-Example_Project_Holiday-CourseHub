package com.coursehub.repository;

import com.coursehub.entity.EnrollmentEntity;
import com.coursehub.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {

    @Query("SELECT e FROM EnrollmentEntity e JOIN FETCH e.course WHERE e.course.instructor.id = :instructorId ORDER BY e.enrollmentDate ASC")
    List<EnrollmentEntity> findEnrollmentsByInstructorId(@Param("instructorId") UUID instructorId);

    @Query("SELECT e FROM EnrollmentEntity e WHERE e.user.id = :userId AND e.course.id = :courseId AND e.course.deletedAt IS NULL")
    Optional<EnrollmentEntity> findByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT COUNT(e) > 0 FROM EnrollmentEntity e WHERE e.user.id = :userId AND e.course.id = :courseId AND e.course.deletedAt IS NULL")
    boolean existsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT e FROM EnrollmentEntity e WHERE e.user.id = :userId AND e.status = :status AND e.course.deletedAt IS NULL")
    Page<EnrollmentEntity> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") EnrollmentStatus status, Pageable pageable);

    @Query("SELECT e FROM EnrollmentEntity e WHERE e.user.id = :userId AND e.course.deletedAt IS NULL")
    Page<EnrollmentEntity> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    long countByCourseId(UUID courseId);

    long countByCourseIdAndStatus(UUID courseId, EnrollmentStatus status);

    @Modifying
    @Query("UPDATE EnrollmentEntity e SET e.progressPercent = :progress, e.status = CASE WHEN :progress >= 100 THEN 'COMPLETED' ELSE e.status END WHERE e.id = :id")
    void updateProgress(@Param("id") UUID id, @Param("progress") BigDecimal progress);

    @Query("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.course.instructor.id = :instructorId")
    long countTotalStudentsByInstructorId(@Param("instructorId") UUID instructorId);

    @Query("SELECT COALESCE(SUM(c.price), 0.0) FROM EnrollmentEntity e JOIN e.course c WHERE c.instructor.id = :instructorId")
    BigDecimal sumRevenueByInstructorId(@Param("instructorId") UUID instructorId);
}
