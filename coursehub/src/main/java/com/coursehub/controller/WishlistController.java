package com.coursehub.controller;

import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.WishlistService;
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
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Quản lý danh sách khóa học mong muốn mua sau")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{courseId}")
    @Operation(summary = "Thêm khóa học vào danh sách mong muốn")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        wishlistService.addToWishlist(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm khóa học vào danh sách mong muốn."));
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Xóa khóa học khỏi danh sách mong muốn")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        wishlistService.removeFromWishlist(principal.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khóa học khỏi danh sách mong muốn."));
    }

    @GetMapping({"/check/{courseId}", "/{courseId}/check"})
    @Operation(summary = "Kiểm tra xem khóa học đã có trong danh sách mong muốn chưa")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.checkWishlist(principal.getId(), courseId)));
    }


    @GetMapping
    @Operation(summary = "Lấy danh sách khóa học mong muốn của tôi")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyWishlist(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getMyWishlist(principal.getId())));
    }
}
