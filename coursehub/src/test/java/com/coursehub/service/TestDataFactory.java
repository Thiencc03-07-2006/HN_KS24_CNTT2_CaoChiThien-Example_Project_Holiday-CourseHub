package com.coursehub.service;

import com.coursehub.entity.*;
import com.coursehub.enums.*;
import com.coursehub.dto.request.*;
import com.coursehub.dto.response.*;
import com.coursehub.constant.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class TestDataFactory {

    public static RoleEntity createRole(Long id, String name) {
        return RoleEntity.builder()
                .id(id)
                .name(name)
                .build();
    }

    public static UserEntity createUser(UUID id, String email, String roleName) {
        RoleEntity role = createRole(roleName.equals(AppConstants.ROLE_STUDENT) ? 1L : 2L, roleName);
        return UserEntity.builder()
                .id(id)
                .email(email)
                .fullName("Test " + roleName)
                .passwordHash("hashedpassword")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CategoryEntity createCategory(Long id, String name) {
        return CategoryEntity.builder()
                .id(id)
                .name(name)
                .slug(name.toLowerCase().replace(" ", "-"))
                .build();
    }

    public static CourseEntity createCourse(UUID id, UserEntity instructor, CategoryEntity category) {
        return CourseEntity.builder()
                .id(id)
                .title("Test Course")
                .slug("test-course")
                .shortDescription("Short description of test course")
                .description("Detailed description of test course")
                .instructor(instructor)
                .category(category)
                .price(BigDecimal.valueOf(100.0))
                .status(CourseStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static ChapterEntity createChapter(UUID id, CourseEntity course) {
        return ChapterEntity.builder()
                .id(id)
                .title("Test Chapter")
                .course(course)
                .orderIndex(1)
                .build();
    }

    public static LessonEntity createLesson(UUID id, ChapterEntity chapter) {
        return LessonEntity.builder()
                .id(id)
                .title("Test Lesson")
                .chapter(chapter)
                .orderIndex(1)
                .lessonType(LessonType.VIDEO)
                .build();
    }
}
