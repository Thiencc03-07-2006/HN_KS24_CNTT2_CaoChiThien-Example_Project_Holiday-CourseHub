package com.coursehub.service;

import com.coursehub.dto.request.BlockCourseRequest;
import com.coursehub.dto.response.AdminCourseDetailResponse;
import com.coursehub.dto.response.AdminCourseResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseLevel;
import com.coursehub.enums.CourseStatus;
import com.coursehub.enums.LessonType;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.impl.AdminCourseServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCourseService Unit Tests")
public class AdminCourseServiceImplTest {

    @Mock private CourseRepository courseRepository;
    @Mock private ChapterRepository chapterRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseApprovalHistoryRepository courseApprovalHistoryRepository;

    @InjectMocks
    private AdminCourseServiceImpl adminCourseService;

    @Test
    @DisplayName("getCourses — returns paginated list of courses")
    void getCourses_success() {
        UUID instructorId = UUID.randomUUID();
        UserEntity instructor = UserEntity.builder().id(instructorId).fullName("Instructor Name").build();
        CategoryEntity category = CategoryEntity.builder().id(1L).name("IT").slug("it").build();

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("IT Course")
                .slug("it-course")
                .instructor(instructor)
                .category(category)
                .price(BigDecimal.TEN)
                .status(CourseStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .build();

        given(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(course)));
        given(enrollmentRepository.countByCourseId(any())).willReturn(10L);

        PageResponse<AdminCourseResponse> result = adminCourseService.getCourses(
                "ACTIVE", "Instructor Name", "it", "IT", 0, 10
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("IT Course");
        assertThat(result.getContent().get(0).getEnrollmentCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getCourseDetail_success — returns course metadata, chapters, and reviews")
    void getCourseDetail_success() {
        UUID courseId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();
        UserEntity instructor = UserEntity.builder().id(instructorId).fullName("Instructor 1").email("inst@test.com").build();
        CategoryEntity category = CategoryEntity.builder().id(1L).name("IT").slug("it").build();

        CourseEntity course = CourseEntity.builder()
                .id(courseId)
                .title("Course 1")
                .instructor(instructor)
                .category(category)
                .level(CourseLevel.BEGINNER)
                .status(CourseStatus.PUBLISHED)
                .build();

        UUID chapterId = UUID.randomUUID();
        ChapterEntity chapter = ChapterEntity.builder().id(chapterId).title("Chapter 1").orderIndex(1).build();
        LessonEntity lesson = LessonEntity.builder().id(UUID.randomUUID()).title("Lesson 1").orderIndex(1).lessonType(LessonType.VIDEO).build();

        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));
        given(chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).willReturn(List.of(chapter));
        given(lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapterId)).willReturn(List.of(lesson));
        given(reviewRepository.findByCourseId(courseId)).willReturn(Collections.emptyList());
        given(enrollmentRepository.countByCourseId(courseId)).willReturn(50L);

        AdminCourseDetailResponse result = adminCourseService.getCourseDetail(courseId);

        assertThat(result.getTitle()).isEqualTo("Course 1");
        assertThat(result.getEnrollmentCount()).isEqualTo(50L);
        assertThat(result.getChapters()).hasSize(1);
        assertThat(result.getChapters().get(0).getTitle()).isEqualTo("Chapter 1");
    }

    @Test
    @DisplayName("blockCourse — sets state to BLOCKED and records metadata")
    void blockCourse_success() {
        UUID courseId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        CourseEntity course = CourseEntity.builder().id(courseId).status(CourseStatus.PUBLISHED).build();
        UserEntity admin = UserEntity.builder().id(adminId).build();

        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));

        BlockCourseRequest req = new BlockCourseRequest();
        req.setReason("Violates terms");

        adminCourseService.blockCourse(courseId, req, adminId);

        assertThat(course.getStatus()).isEqualTo(CourseStatus.BLOCKED);
        assertThat(course.getBlockedReason()).isEqualTo("Violates terms");
        assertThat(course.getBlockedBy()).isEqualTo(admin);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("unblockCourse_success — sets status to PUBLISHED")
    void unblockCourse_success() {
        UUID courseId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        CourseEntity course = CourseEntity.builder().id(courseId).status(CourseStatus.BLOCKED).build();

        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        adminCourseService.unblockCourse(courseId, adminId);

        assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(course.getBlockedReason()).isNull();
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("unblockCourse_notBlocked — throws BadRequestException")
    void unblockCourse_notBlocked_throwsException() {
        UUID courseId = UUID.randomUUID();
        CourseEntity course = CourseEntity.builder().id(courseId).status(CourseStatus.PUBLISHED).build();

        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        assertThatThrownBy(() -> adminCourseService.unblockCourse(courseId, UUID.randomUUID()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không ở trạng thái bị chặn");
    }
}
