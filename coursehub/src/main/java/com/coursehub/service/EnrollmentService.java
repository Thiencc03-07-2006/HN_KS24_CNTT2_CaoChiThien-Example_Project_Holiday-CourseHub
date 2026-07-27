package com.coursehub.service;

import com.coursehub.dto.response.EnrollmentResponse;
import com.coursehub.dto.response.PageResponse;

import java.util.UUID;

public interface EnrollmentService {
    EnrollmentResponse enrollCourse(UUID userId, UUID courseId);
    PageResponse<EnrollmentResponse> getMyEnrollments(UUID userId, int page, int size);
    boolean isEnrolled(UUID userId, UUID courseId);
    EnrollmentResponse getEnrollmentDetails(UUID userId, UUID courseId);
    
    void completeLesson(UUID userId, UUID courseId, UUID lessonId);
    void updateEnrollmentProgress(UUID enrollmentId);
}
