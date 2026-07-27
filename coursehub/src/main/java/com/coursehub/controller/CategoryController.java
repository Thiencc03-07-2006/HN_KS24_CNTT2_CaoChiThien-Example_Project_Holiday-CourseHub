package com.coursehub.controller;

import com.coursehub.dto.request.CategoryRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.CategoryResponse;
import com.coursehub.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Quản lý danh mục khóa học")
public class CategoryController {

    private final CategoryService categoryService;

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/categories")
    @Operation(summary = "Lấy cây danh mục khóa học")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesTree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategoriesTree()));
    }

    @GetMapping("/categories/roots")
    @Operation(summary = "Lấy danh mục cha gốc")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllRootCategories()));
    }

    @GetMapping("/categories/{id}/subcategories")
    @Operation(summary = "Lấy danh mục con của một danh mục cha")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubCategories(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getSubCategories(id)));
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Xem chi tiết một danh mục")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id)));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @PostMapping("/admin/categories")
    @Operation(summary = "Tạo danh mục mới (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo danh mục thành công.", response));
    }

    @PutMapping("/admin/categories/{id}")
    @Operation(summary = "Cập nhật danh mục (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công.", response));
    }

    @DeleteMapping("/admin/categories/{id}")
    @Operation(summary = "Xóa danh mục (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công."));
    }
}
