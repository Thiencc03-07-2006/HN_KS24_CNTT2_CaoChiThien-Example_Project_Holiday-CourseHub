package com.coursehub.service;

import com.coursehub.dto.response.CourseResponse;
import com.coursehub.entity.*;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CourseRepository;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.WishlistRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService Unit Tests")
public class WishlistServiceImplTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @Test
    @DisplayName("addToWishlist_success — user adds course to wishlist")
    void addToWishlist_success() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        UserEntity user = UserEntity.builder().id(userId).build();
        UserEntity instructor = UserEntity.builder().id(instructorId).build();
        CategoryEntity category = CategoryEntity.builder().id(1L).name("IT").build();

        CourseEntity course = CourseEntity.builder()
                .id(courseId)
                .instructor(instructor)
                .category(category)
                .price(BigDecimal.TEN)
                .build();

        given(wishlistRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        wishlistService.addToWishlist(userId, courseId);

        verify(wishlistRepository).save(any(WishlistEntity.class));
    }

    @Test
    @DisplayName("addToWishlist_alreadyInWishlist — BadRequestException")
    void addToWishlist_alreadyInWishlist_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        given(wishlistRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(true);

        assertThatThrownBy(() -> wishlistService.addToWishlist(userId, courseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("mong muốn");
    }

    @Test
    @DisplayName("addToWishlist_ownCourse — user adds own course to wishlist → BadRequestException")
    void addToWishlist_ownCourse_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        UserEntity user = UserEntity.builder().id(userId).build();
        CategoryEntity category = CategoryEntity.builder().id(1L).name("IT").build();

        CourseEntity course = CourseEntity.builder()
                .id(courseId)
                .instructor(user) // instructor is the same as the user
                .category(category)
                .build();

        given(wishlistRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

        assertThatThrownBy(() -> wishlistService.addToWishlist(userId, courseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể thêm khóa học của chính mình");
    }

    @Test
    @DisplayName("removeFromWishlist_success — user removes course from wishlist")
    void removeFromWishlist_success() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        WishlistId wishId = new WishlistId(userId, courseId);
        WishlistEntity wishlist = WishlistEntity.builder().userId(userId).courseId(courseId).build();

        given(wishlistRepository.findById(wishId)).willReturn(Optional.of(wishlist));

        wishlistService.removeFromWishlist(userId, courseId);

        verify(wishlistRepository).delete(wishlist);
    }

    @Test
    @DisplayName("removeFromWishlist_notFound — ResourceNotFoundException")
    void removeFromWishlist_notFound_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        WishlistId wishId = new WishlistId(userId, courseId);

        given(wishlistRepository.findById(wishId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.removeFromWishlist(userId, courseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("checkWishlist — returns check status")
    void checkWishlist_returnsStatus() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        given(wishlistRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(true);

        boolean result = wishlistService.checkWishlist(userId, courseId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getMyWishlist — returns list of course responses")
    void getMyWishlist_returnsList() {
        UUID userId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        UserEntity instructor = UserEntity.builder()
                .id(instructorId)
                .fullName("Instructor 1")
                .instructorProfile(InstructorProfileEntity.builder().headline("Author").build())
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

        WishlistEntity wishlist = WishlistEntity.builder().course(course).build();

        given(wishlistRepository.findByUserId(userId)).willReturn(List.of(wishlist));
        given(enrollmentRepository.countByCourseId(course.getId())).willReturn(5L);

        List<CourseResponse> result = wishlistService.getMyWishlist(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Course 1");
    }
}
