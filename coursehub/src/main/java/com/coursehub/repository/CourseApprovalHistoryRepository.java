package com.coursehub.repository;

import com.coursehub.entity.CourseApprovalHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseApprovalHistoryRepository extends JpaRepository<CourseApprovalHistoryEntity, UUID> {
    List<CourseApprovalHistoryEntity> findByCourseIdOrderByCreatedAtDesc(UUID courseId);
}
