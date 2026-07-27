package com.coursehub.service;

import com.coursehub.dto.response.ReportResponse;
import java.util.List;
import java.util.UUID;

public interface ReportService {
    ReportResponse reportCourse(UUID courseId, String reason, String description);
    ReportResponse reportReview(UUID reviewId, String reason, String description);
    ReportResponse reportComment(UUID commentId, String reason, String description);

    List<ReportResponse> getMyCourseReports(UUID reporterId);

    List<ReportResponse> getAdminCourseReports();
    ReportResponse getAdminCourseReport(UUID id);
    ReportResponse updateCourseReportStatus(UUID id, String status, String adminNote);
    void deleteCourseReport(UUID id);

    List<ReportResponse> getAdminCommentAndReviewReports();
    ReportResponse getAdminCommentOrReviewReport(UUID id);
    ReportResponse updateCommentOrReviewReportStatus(UUID id, String status, String adminNote);
    void deleteCommentOrReviewReport(UUID id);

    List<ReportResponse> getAllReports();
    ReportResponse updateReportStatus(UUID reportId, String status);
}
