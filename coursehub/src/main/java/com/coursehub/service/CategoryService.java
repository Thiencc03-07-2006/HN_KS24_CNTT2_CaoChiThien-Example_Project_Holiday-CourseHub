package com.coursehub.service;

import com.coursehub.dto.request.CategoryRequest;
import com.coursehub.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllRootCategories();
    List<CategoryResponse> getSubCategories(Long parentId);
    List<CategoryResponse> getAllCategoriesTree();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
