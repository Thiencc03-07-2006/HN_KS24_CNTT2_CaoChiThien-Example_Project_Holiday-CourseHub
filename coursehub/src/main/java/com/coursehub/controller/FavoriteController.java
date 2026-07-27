package com.coursehub.controller;

import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Quản lý danh sách khóa học yêu thích")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{courseId}")
    @Operation(summary = "Thêm khóa học vào danh sách yêu thích")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        favoriteService.addFavorite(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm khóa học vào danh sách yêu thích."));
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Xóa khóa học khỏi danh sách yêu thích")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        favoriteService.removeFavorite(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khóa học khỏi danh sách yêu thích."));
    }

    @GetMapping("/{courseId}/check")
    @Operation(summary = "Kiểm tra xem khóa học đã được yêu thích chưa")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(favoriteService.checkFavorite(principal.getId(), courseId)));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách khóa học yêu thích của tôi")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyFavorites(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(favoriteService.getMyFavorites(principal.getId())));
    }
}
