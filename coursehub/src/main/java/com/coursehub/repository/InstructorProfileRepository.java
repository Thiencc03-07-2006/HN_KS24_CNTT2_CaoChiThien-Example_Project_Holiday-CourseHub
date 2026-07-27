package com.coursehub.repository;

import com.coursehub.entity.InstructorProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorProfileRepository extends JpaRepository<InstructorProfileEntity, UUID> {
    Optional<InstructorProfileEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("UPDATE InstructorProfileEntity ip SET ip.totalStudents = :students, ip.totalCourses = :courses, ip.averageRating = :rating WHERE ip.user.id = :userId")
    void updateStats(@Param("userId") UUID userId, @Param("students") int students,
                     @Param("courses") int courses, @Param("rating") double rating);
}
