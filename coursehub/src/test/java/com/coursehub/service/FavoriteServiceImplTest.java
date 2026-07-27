package com.coursehub.service;

import com.coursehub.dto.response.CourseResponse;
import com.coursehub.entity.*;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CourseRepository;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.FavoriteRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteService Unit Tests")
public class FavoriteServiceImplTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Test
    @DisplayName("addFavorite_success — user enlists a valid course")
    void addFavorite_success() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        UserEntity user = UserEntity.builder().id(userId).build();
        UserEntity instructor = UserEntity.builder().id(instructorId).build();
        CategoryEntity category = CategoryEntity.builder().id(1L).name("Java").build();

        CourseEntity course = CourseEntity.builder()
                .id(courseId)
                .instructor(instructor)
                .category(category)
                .price(BigDecimal.TEN)
                .build();

        given(favoriteRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        favoriteService.addFavorite(userId, courseId);

        verify(favoriteRepository).save(any(FavoriteEntity.class));
    }

    @Test
    @DisplayName("addFavorite_alreadyFavorite — returns BadRequestException")
    void addFavorite_alreadyFavorite_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        given(favoriteRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(true);

        assertThatThrownBy(() -> favoriteService.addFavorite(userId, courseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã lưu khóa học này");
    }

    @Test
    @DisplayName("addFavorite_selfFavorite — user favorites own course → BadRequestException")
    void addFavorite_selfFavorite_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        UserEntity user = UserEntity.builder().id(userId).build();
        CategoryEntity category = CategoryEntity.builder().id(1L).name("Java").build();

        CourseEntity course = CourseEntity.builder()
                .id(courseId)
                .instructor(user) // instructor is the same as the user
                .category(category)
                .build();

        given(favoriteRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        assertThatThrownBy(() -> favoriteService.addFavorite(userId, courseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể thêm khóa học của chính mình");
    }

    @Test
    @DisplayName("removeFavorite_success — user removes course from favorites")
    void removeFavorite_success() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        FavoriteId favId = new FavoriteId(userId, courseId);
        FavoriteEntity fav = FavoriteEntity.builder().userId(userId).courseId(courseId).build();

        given(favoriteRepository.findById(favId)).willReturn(Optional.of(fav));

        favoriteService.removeFavorite(userId, courseId);

        verify(favoriteRepository).delete(fav);
    }

    @Test
    @DisplayName("removeFavorite_notFound — ResourceNotFoundException")
    void removeFavorite_notFound_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        FavoriteId favId = new FavoriteId(userId, courseId);

        given(favoriteRepository.findById(favId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.removeFavorite(userId, courseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("checkFavorite — returns check status")
    void checkFavorite_returnsStatus() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        given(favoriteRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(true);

        boolean result = favoriteService.checkFavorite(userId, courseId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getMyFavorites — returns list of course responses")
    void getMyFavorites_returnsList() {
        UUID userId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        UserEntity instructor = UserEntity.builder()
                .id(instructorId)
                .fullName("Instructor 1")
                .instructorProfile(InstructorProfileEntity.builder().headline("Guru").build())
                .build();

        CategoryEntity category = CategoryEntity.builder().id(1L).name("IT").slug("it").build();

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("Course 1")
                .slug("course-1")
                .instructor(instructor)
                .category(category)
                .price(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        FavoriteEntity fav = FavoriteEntity.builder().course(course).build();

        given(favoriteRepository.findByUserId(userId)).willReturn(List.of(fav));
        given(enrollmentRepository.countByCourseId(course.getId())).willReturn(10L);

        List<CourseResponse> result = favoriteService.getMyFavorites(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Course 1");
        assertThat(result.get(0).getTotalStudents()).isEqualTo(10L);
    }
}
