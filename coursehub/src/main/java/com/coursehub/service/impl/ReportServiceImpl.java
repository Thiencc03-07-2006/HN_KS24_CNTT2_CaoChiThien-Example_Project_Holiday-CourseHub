package com.coursehub.service.impl;

import com.coursehub.dto.response.ReportResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.ReportStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadRequestException("REPORT_007", "Vui lòng đăng nhập để thực hiện báo cáo.");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
    }

    @Override
    @Transactional
    public ReportResponse reportCourse(UUID courseId, String reason, String description) {
        UserEntity reporter = getCurrentUser();
        UUID reporterId = reporter.getId();

        if (reportRepository.existsByReporterIdAndReportableTypeAndReportableIdAndStatus(reporterId, "COURSE", courseId, ReportStatus.PENDING)) {
            throw new BadRequestException("REPORT_001", "Bạn đã có báo cáo đang chờ xử lý cho khóa học này.");
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getInstructor().getId().equals(reporterId)) {
            throw new BadRequestException("REPORT_002", "Bạn không thể báo cáo khóa học của chính mình.");
        }

        ReportEntity report = ReportEntity.builder()
                .reporter(reporter)
                .reportableType("COURSE")
                .reportableId(courseId)
                .reason(reason)
                .description(description)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse reportReview(UUID reviewId, String reason, String description) {
        UserEntity reporter = getCurrentUser();
        UUID reporterId = reporter.getId();

        if (reportRepository.existsByReporterIdAndReportableTypeAndReportableIdAndStatus(reporterId, "REVIEW", reviewId, ReportStatus.PENDING)) {
            throw new BadRequestException("REPORT_001", "Bạn đã có báo cáo đang chờ xử lý cho đánh giá này.");
        }

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        UUID studentId = review.getEnrollment().getUser().getId();
        if (studentId.equals(reporterId)) {
            throw new BadRequestException("REPORT_002", "Bạn không thể báo cáo đánh giá của chính mình.");
        }

        ReportEntity report = ReportEntity.builder()
                .reporter(reporter)
                .reportableType("REVIEW")
                .reportableId(reviewId)
                .reason(reason)
                .description(description)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse reportComment(UUID commentId, String reason, String description) {
        UserEntity reporter = getCurrentUser();
        UUID reporterId = reporter.getId();

        if (reportRepository.existsByReporterIdAndReportableTypeAndReportableIdAndStatus(reporterId, "COMMENT", commentId, ReportStatus.PENDING)) {
            throw new BadRequestException("REPORT_001", "Bạn đã có báo cáo đang chờ xử lý cho bình luận này.");
        }

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (comment.getUser().getId().equals(reporterId)) {
            throw new BadRequestException("REPORT_002", "Bạn không thể báo cáo bình luận của chính mình.");
        }

        ReportEntity report = ReportEntity.builder()
                .reporter(reporter)
                .reportableType("COMMENT")
                .reportableId(commentId)
                .reason(reason)
                .description(description)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getMyCourseReports(UUID reporterId) {
        return reportRepository.findByReporterIdAndReportableType(reporterId, "COURSE").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getAdminCourseReports() {
        return reportRepository.findByReportableType("COURSE").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getAdminCourseReport(UUID id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        if (!"COURSE".equals(report.getReportableType())) {
            throw new BadRequestException("REPORT_003", "Báo cáo này không thuộc loại khóa học.");
        }
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse updateCourseReportStatus(UUID id, String status, String adminNote) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        if (!"COURSE".equals(report.getReportableType())) {
            throw new BadRequestException("REPORT_003", "Báo cáo này không thuộc loại khóa học.");
        }
        report.setStatus(ReportStatus.valueOf(status.toUpperCase()));
        report.setAdminNote(adminNote);
        report.setUpdatedAt(LocalDateTime.now());
        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public void deleteCourseReport(UUID id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        if (!"COURSE".equals(report.getReportableType())) {
            throw new BadRequestException("REPORT_003", "Báo cáo này không thuộc loại khóa học.");
        }
        reportRepository.delete(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getAdminCommentAndReviewReports() {
        return reportRepository.findCommentAndReviewReports().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getAdminCommentOrReviewReport(UUID id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        if (!"COMMENT".equals(report.getReportableType()) && !"REVIEW".equals(report.getReportableType())) {
            throw new BadRequestException("REPORT_003", "Báo cáo này không thuộc loại bình luận hoặc đánh giá.");
        }
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse updateCommentOrReviewReportStatus(UUID id, String status, String adminNote) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        if (!"COMMENT".equals(report.getReportableType()) && !"REVIEW".equals(report.getReportableType())) {
            throw new BadRequestException("REPORT_003", "Báo cáo này không thuộc loại bình luận hoặc đánh giá.");
        }
        
        ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
        report.setStatus(reportStatus);
        report.setAdminNote(adminNote);
        report.setUpdatedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        // Auto-hide target comment/review if the report status becomes RESOLVED
        if (reportStatus == ReportStatus.RESOLVED) {
            if ("COMMENT".equals(report.getReportableType())) {
                commentRepository.findById(report.getReportableId()).ifPresent(c -> {
                    c.setIsHidden(true);
                    commentRepository.save(c);
                });
            } else if ("REVIEW".equals(report.getReportableType())) {
                reviewRepository.findById(report.getReportableId()).ifPresent(r -> {
                    r.setIsHidden(true);
                    reviewRepository.save(r);
                });
            }
        }

        return mapToResponse(report);
    }

    @Override
    @Transactional
    public void deleteCommentOrReviewReport(UUID id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        if (!"COMMENT".equals(report.getReportableType()) && !"REVIEW".equals(report.getReportableType())) {
            throw new BadRequestException("REPORT_003", "Báo cáo này không thuộc loại bình luận hoặc đánh giá.");
        }
        reportRepository.delete(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportResponse updateReportStatus(UUID reportId, String status) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
        report.setStatus(reportStatus);
        report.setUpdatedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        // Auto-hide target comment/review if the report status becomes RESOLVED
        if (reportStatus == ReportStatus.RESOLVED) {
            if ("COMMENT".equals(report.getReportableType())) {
                commentRepository.findById(report.getReportableId()).ifPresent(c -> {
                    c.setIsHidden(true);
                    commentRepository.save(c);
                });
            } else if ("REVIEW".equals(report.getReportableType())) {
                reviewRepository.findById(report.getReportableId()).ifPresent(r -> {
                    r.setIsHidden(true);
                    reviewRepository.save(r);
                });
            }
        }

        return mapToResponse(report);
    }

    private ReportResponse mapToResponse(ReportEntity report) {
        String targetTitle = "Nội dung đã bị xóa hoặc không tìm thấy";
        try {
            if ("COURSE".equals(report.getReportableType())) {
                targetTitle = courseRepository.findById(report.getReportableId())
                        .map(CourseEntity::getTitle)
                        .orElse(targetTitle);
            } else if ("COMMENT".equals(report.getReportableType())) {
                targetTitle = commentRepository.findById(report.getReportableId())
                        .map(CommentEntity::getContent)
                        .orElse(targetTitle);
            } else if ("REVIEW".equals(report.getReportableType())) {
                targetTitle = reviewRepository.findById(report.getReportableId())
                        .map(ReviewEntity::getComment)
                        .orElse(targetTitle);
            }
        } catch (Exception e) {
            // fallback
        }

        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getFullName())
                .reportableType(report.getReportableType())
                .reportableId(report.getReportableId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .adminNote(report.getAdminNote())
                .targetTitle(targetTitle)
                .build();
    }
}
