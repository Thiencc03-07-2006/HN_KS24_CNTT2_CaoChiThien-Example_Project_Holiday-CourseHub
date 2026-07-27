package com.coursehub.service;

import com.coursehub.dto.request.UpdateProfileRequest;
import com.coursehub.dto.request.BecomeInstructorRequest;
import com.coursehub.dto.response.UserProfileResponse;
import com.coursehub.entity.RoleEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.entity.InstructorProfileEntity;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.InstructorProfileRepository;
import com.coursehub.repository.RoleRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 * Tests: getMyProfile, updateMyProfile flows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private InstructorProfileRepository instructorProfileRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserServiceImpl userService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_EMAIL = "user@example.com";
    private static final String TEST_FULL_NAME = "Nguyen Van A";
    private static final String TEST_PHONE = "0901234567";

    private UserEntity buildUser() {
        return UserEntity.builder()
                .id(USER_ID)
                .email(TEST_EMAIL)
                .fullName(TEST_FULL_NAME)
                .phoneNumber(TEST_PHONE)
                .roles(new java.util.HashSet<>(Set.of(RoleEntity.builder().id(1L).name("ROLE_STUDENT").build())))
                .build();
    }

    // ==================== GET PROFILE ====================

    @Test
    @DisplayName("getMyProfile_success — valid userId → returns UserProfileResponse")
    void getMyProfile_success() {
        // Given
        UserEntity user = buildUser();
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

        // When
        UserProfileResponse response = userService.getMyProfile(USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getFullName()).isEqualTo(TEST_FULL_NAME);
        assertThat(response.getPhoneNumber()).isEqualTo(TEST_PHONE);
    }

    @Test
    @DisplayName("getMyProfile_userNotFound — invalid userId → ResourceNotFoundException")
    void getMyProfile_userNotFound_throwsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.getMyProfile(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== UPDATE PROFILE ====================

    @Test
    @DisplayName("updateMyProfile_success — valid request → user updated and response returned")
    void updateMyProfile_success() {
        // Given
        UserEntity user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Nguyen Van B");
        request.setPhoneNumber("0912345678");

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(userRepository.existsByPhoneNumberAndIdNot("0912345678", USER_ID)).willReturn(false);
        given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        UserProfileResponse response = userService.updateMyProfile(USER_ID, request);

        // Then
        assertThat(response.getFullName()).isEqualTo("Nguyen Van B");
        assertThat(response.getPhoneNumber()).isEqualTo("0912345678");
        verify(userRepository).save(argThat(u ->
                u.getFullName().equals("Nguyen Van B")
                        && u.getPhoneNumber().equals("0912345678")
        ));
    }

    @Test
    @DisplayName("updateMyProfile_duplicatePhone — phone taken by another user → BadRequestException VALID_001")
    void updateMyProfile_duplicatePhone_throwsBadRequestException() {
        // Given
        UserEntity user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhoneNumber("0999888777"); // different from current

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(userRepository.existsByPhoneNumberAndIdNot("0999888777", USER_ID)).willReturn(true);

        // When / Then
        assertThatThrownBy(() -> userService.updateMyProfile(USER_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số điện thoại đã được sử dụng");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMyProfile_samePhone — same phone as current user → no conflict check needed, updated ok")
    void updateMyProfile_samePhone_noConflict() {
        // Given
        UserEntity user = buildUser(); // phoneNumber = TEST_PHONE
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhoneNumber(TEST_PHONE); // same as current

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        UserProfileResponse response = userService.updateMyProfile(USER_ID, request);

        // Then
        // existsByPhoneNumberAndIdNot NOT called when phone unchanged
        verify(userRepository, never()).existsByPhoneNumberAndIdNot(any(), any());
        assertThat(response.getPhoneNumber()).isEqualTo(TEST_PHONE);
    }

    @Test
    @DisplayName("updateMyProfile_partialUpdate — only fullName provided → only fullName changed")
    void updateMyProfile_partialUpdate_onlyUpdatesProvidedFields() {
        // Given
        UserEntity user = buildUser();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        // phoneNumber and bio left null

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        UserProfileResponse response = userService.updateMyProfile(USER_ID, request);

        // Then
        assertThat(response.getFullName()).isEqualTo("Updated Name");
        assertThat(response.getPhoneNumber()).isEqualTo(TEST_PHONE); // unchanged
    }

    // ==================== AVATAR UPLOAD ====================

    @Test
    @DisplayName("uploadAvatar_success — valid image → avatar url returned")
    void uploadAvatar_success() {
        UserEntity user = buildUser();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1000L);

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(cloudinaryService.uploadFile(file, "coursehub/avatar")).willReturn("http://avatar.url");
        given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));

        String url = userService.uploadAvatar(USER_ID, file);

        assertThat(url).isEqualTo("http://avatar.url");
        verify(userRepository).save(argThat(u -> u.getAvatarUrl().equals("http://avatar.url")));
    }

    @Test
    @DisplayName("uploadAvatar_invalidContentType — non-image file → BadRequestException")
    void uploadAvatar_invalidContentType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("text/plain");

        assertThatThrownBy(() -> userService.uploadAvatar(USER_ID, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Định dạng ảnh không hợp lệ");
    }

    @Test
    @DisplayName("uploadAvatar_exceedSize — large file → BadRequestException")
    void uploadAvatar_exceedSize_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(3 * 1024 * 1024L); // 3MB > 2MB

        assertThatThrownBy(() -> userService.uploadAvatar(USER_ID, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("vượt quá 2MB");
    }

    // ==================== BECOME INSTRUCTOR ====================

    @Test
    @DisplayName("becomeInstructor_success — user profile created and role updated")
    void becomeInstructor_success() {
        UserEntity user = buildUser();
        BecomeInstructorRequest request = new BecomeInstructorRequest();
        request.setHeadline("Java Expert");
        request.setDetailedBio("Coding for 10 years");

        RoleEntity instructorRole = RoleEntity.builder().id(2L).name("ROLE_INSTRUCTOR").build();

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(instructorProfileRepository.existsByUserId(USER_ID)).willReturn(false);
        given(roleRepository.findByName("ROLE_INSTRUCTOR")).willReturn(Optional.of(instructorRole));

        userService.becomeInstructor(USER_ID, request);

        verify(userRepository).save(argThat(u -> u.getRoles().contains(instructorRole)));
        verify(instructorProfileRepository).save(any(InstructorProfileEntity.class));
    }

    @Test
    @DisplayName("becomeInstructor_alreadyInstructor — profile exists → BadRequestException")
    void becomeInstructor_alreadyInstructor_throwsException() {
        BecomeInstructorRequest request = new BecomeInstructorRequest();
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(buildUser()));
        given(instructorProfileRepository.existsByUserId(USER_ID)).willReturn(true);

        assertThatThrownBy(() -> userService.becomeInstructor(USER_ID, request))
                .isInstanceOf(BadRequestException.class);
    }
}
