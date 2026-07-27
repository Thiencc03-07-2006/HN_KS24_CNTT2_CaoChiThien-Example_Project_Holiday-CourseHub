package com.coursehub.repository;

import com.coursehub.entity.ReportEntity;
import com.coursehub.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

    boolean existsByReporterIdAndReportableTypeAndReportableId(UUID reporterId, String reportableType, UUID reportableId);

    boolean existsByReporterIdAndReportableTypeAndReportableIdAndStatus(UUID reporterId, String reportableType, UUID reportableId, ReportStatus status);

    long countByReportableTypeAndReportableId(String reportableType, UUID reportableId);

    Page<ReportEntity> findByStatus(ReportStatus status, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT r.reporter.id) FROM ReportEntity r WHERE r.reportableType = :type AND r.reportableId = :id")
    long countDistinctReportersByTarget(@Param("type") String type, @Param("id") UUID id);

    @Query("SELECT r FROM ReportEntity r WHERE r.status IN ('PENDING', 'AUTO_ESCALATED') ORDER BY r.status DESC, r.createdAt ASC")
    Page<ReportEntity> findPendingReports(Pageable pageable);

    @Query("SELECT r FROM ReportEntity r JOIN FETCH r.reporter WHERE r.reportableType = :type")
    List<ReportEntity> findByReportableType(@Param("type") String type);

    @Query("SELECT r FROM ReportEntity r JOIN FETCH r.reporter WHERE r.reportableType IN ('COMMENT', 'REVIEW')")
    List<ReportEntity> findCommentAndReviewReports();

    @Query("SELECT r FROM ReportEntity r JOIN FETCH r.reporter WHERE r.reporter.id = :reporterId AND r.reportableType = :type")
    List<ReportEntity> findByReporterIdAndReportableType(@Param("reporterId") UUID reporterId, @Param("type") String type);

    long countByStatus(ReportStatus status);

    long countByReportableType(String reportableType);
}
