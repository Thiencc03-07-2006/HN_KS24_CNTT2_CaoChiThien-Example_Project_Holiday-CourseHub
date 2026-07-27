package com.coursehub.service;

import com.coursehub.dto.request.BlockCourseRequest;
import com.coursehub.dto.response.AdminCourseDetailResponse;
import com.coursehub.dto.response.AdminCourseResponse;
import com.coursehub.dto.response.PageResponse;

import java.util.UUID;

public interface AdminCourseService {
    PageResponse<AdminCourseResponse> getCourses(String status, String instructor, String category, String keyword, int page, int size);
    AdminCourseDetailResponse getCourseDetail(UUID courseId);
    void blockCourse(UUID courseId, BlockCourseRequest request, UUID adminId);
    void unblockCourse(UUID courseId, UUID adminId);
}
