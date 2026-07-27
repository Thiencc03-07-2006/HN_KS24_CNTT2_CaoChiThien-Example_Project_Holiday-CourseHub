package com.coursehub.service;

import com.coursehub.dto.request.CreateCourseRequest;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.dto.response.LearningCourseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

public interface CourseService {
    CourseResponse createCourse(UUID instructorId, CreateCourseRequest request);
    CourseResponse getCourseBySlug(String slug, UUID currentUserId);
    CourseResponse getCourseById(UUID courseId, UUID currentUserId);
    PageResponse<CourseResponse> getInstructorCourses(UUID instructorId, int page, int size);
    CourseResponse updateCourse(UUID instructorId, UUID courseId, CreateCourseRequest request);
    String uploadThumbnail(UUID instructorId, UUID courseId, MultipartFile file);
    void submitForReview(UUID instructorId, UUID courseId);
    void deleteCourse(UUID instructorId, UUID courseId);

    // Admin actions
    void approveCourse(UUID adminId, UUID courseId, String note);
    void rejectCourse(UUID adminId, UUID courseId, String note);
    void blockCourse(UUID adminId, UUID courseId, String note);

    // Public
    PageResponse<CourseResponse> searchCourses(String keyword, Long categoryId, String level,
                                               BigDecimal minPrice, BigDecimal maxPrice, BigDecimal rating,
                                               String language, String sortBy, int page, int size);

    CourseResponse getCourseDetailPublic(UUID courseId);
    LearningCourseResponse getLearningCourseContent(UUID userId, UUID courseId);
}
