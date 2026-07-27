package com.coursehub.controller;

import com.coursehub.dto.request.BecomeInstructorRequest;
import com.coursehub.dto.request.UpdateProfileRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.UserProfileResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Quản lý hồ sơ người dùng và giảng viên")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    // ==================== User Profile ====================

    @GetMapping("/users/me")
    @Operation(summary = "Xem hồ sơ của tôi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(principal.getId())));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Cập nhật hồ sơ của tôi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công.",
                userService.updateMyProfile(principal.getId(), request)));
    }

    @PostMapping(value = "/users/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ảnh đại diện (tối đa 2MB, JPG/PNG)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(principal.getId(), file);
        return ResponseEntity.ok(ApiResponse.success("Upload ảnh đại diện thành công.", url));
    }

    // ==================== Instructor Profile ====================

    @PostMapping("/instructor/register")
    @Operation(summary = "Đăng ký trở thành giảng viên (FR-03)")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> becomeInstructor(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BecomeInstructorRequest request) {
        userService.becomeInstructor(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký giảng viên thành công. Bạn có thể bắt đầu tạo khóa học."));
    }

    @PutMapping("/instructor/profile")
    @Operation(summary = "Cập nhật hồ sơ giảng viên")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> updateInstructorProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BecomeInstructorRequest request) {
        userService.updateInstructorProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ giảng viên thành công."));
    }

    // ==================== Public Instructor Profile ====================

    @GetMapping("/instructors/public/{instructorId}")
    @Operation(summary = "Xem hồ sơ công khai giảng viên (FR-31)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getInstructorPublicProfile(
            @PathVariable UUID instructorId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getInstructorPublicProfile(instructorId)));
    }
}
