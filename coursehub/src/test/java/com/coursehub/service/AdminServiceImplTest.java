package com.coursehub.service;

import com.coursehub.dto.response.AdminDashboardStatsResponse;
import com.coursehub.dto.response.UserProfileResponse;
import com.coursehub.entity.RoleEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.enums.UserStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Unit Tests")
public class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private ReportRepository reportRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("getDashboardStats — returns metrics stats successfully")
    void getDashboardStats_success() {
        given(userRepository.count(any(Specification.class))).willReturn(10L);
        given(courseRepository.count(any(Specification.class))).willReturn(5L);
        given(categoryRepository.count()).willReturn(3L);
        given(enrollmentRepository.count()).willReturn(15L);

        given(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));
        given(courseRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));
        given(wishlistRepository.count()).willReturn(8L);
        given(wishlistRepository.findTopFavoriteCourses(any())).willReturn(Collections.emptyList());
        given(reportRepository.countByReportableType("COURSE")).willReturn(2L);
        given(reportRepository.countByReportableType("COMMENT")).willReturn(1L);
        given(reportRepository.countByReportableType("REVIEW")).willReturn(1L);
        given(reportRepository.countByStatus(any())).willReturn(2L);

        AdminDashboardStatsResponse stats = adminService.getDashboardStats();

        assertThat(stats.getTotalUsers()).isEqualTo(10L);
        assertThat(stats.getTotalCourses()).isEqualTo(5L);
        assertThat(stats.getTotalEnrollments()).isEqualTo(15L);
    }

    @Test
    @DisplayName("getUserDetail_success — returns profile")
    void getUserDetail_success() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("admin@test.com")
                .status(UserStatus.ACTIVE)
                .roles(Collections.emptySet())
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UserProfileResponse response = adminService.getUserDetail(userId);

        assertThat(response.getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("getUserDetail_deleted — throws ResourceNotFoundException")
    void getUserDetail_deleted_throwsException() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .deletedAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.getUserDetail(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateUserStatus_success — updates user status and revokes tokens if banned")
    void updateUserStatus_banned() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("user@test.com")
                .status(UserStatus.ACTIVE)
                .roles(Collections.emptySet())
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UserProfileResponse response = adminService.updateUserStatus(userId, "BANNED");

        assertThat(response.getStatus()).isEqualTo("BANNED");
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    @DisplayName("addUserRole_adminRole — throws AccessDeniedException")
    void addUserRole_adminRole_throwsException() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> adminService.addUserRole(userId, "ROLE_ADMIN"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("addUserRole_success — adds role to user")
    void addUserRole_success() {
        UUID userId = UUID.randomUUID();
        RoleEntity studentRole = RoleEntity.builder().name("ROLE_STUDENT").build();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(List.of(studentRole)))
                .status(UserStatus.ACTIVE)
                .build();

        RoleEntity instructorRole = RoleEntity.builder().name("ROLE_INSTRUCTOR").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(roleRepository.findByName("ROLE_INSTRUCTOR")).willReturn(Optional.of(instructorRole));

        UserProfileResponse response = adminService.addUserRole(userId, "ROLE_INSTRUCTOR");

        assertThat(response.getRoles()).contains("ROLE_INSTRUCTOR");
    }
}
