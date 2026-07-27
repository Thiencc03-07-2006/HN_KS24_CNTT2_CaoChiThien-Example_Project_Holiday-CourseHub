package com.coursehub.service.impl;

import com.coursehub.dto.request.CategoryRequest;
import com.coursehub.dto.response.CategoryResponse;
import com.coursehub.entity.CategoryEntity;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CategoryRepository;
import com.coursehub.service.CategoryService;
import com.coursehub.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllRootCategories() {
        return categoryRepository.findAllRootCategories().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Category", "id", parentId);
        }
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesTree() {
        List<CategoryEntity> roots = categoryRepository.findAllRootCategories();
        return roots.stream().map(this::mapToTreeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("VALID_001", "Tên danh mục đã tồn tại.");
        }

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = AppUtils.toSlug(request.getName());
        }
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        CategoryEntity parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "parentId", request.getParentId()));
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(request.getName())
                .slug(slug)
                .icon(request.getIcon())
                .description(request.getDescription())
                .parent(parent)
                .build();

        category = categoryRepository.save(category);
        log.info("Category created successfully: {}", category.getName());
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BadRequestException("VALID_001", "Tên danh mục đã tồn tại.");
        }

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = AppUtils.toSlug(request.getName());
        }
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        CategoryEntity parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("VALID_001", "Danh mục cha không thể là chính nó.");
            }
            if (isDescendantOf(request.getParentId(), id)) {
                throw new BadRequestException("VALID_001", "Danh mục cha mới không được là danh mục con của danh mục hiện tại.");
            }
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "parentId", request.getParentId()));
        }

        category.setName(request.getName());
        category.setSlug(slug);
        category.setIcon(request.getIcon());
        category.setDescription(request.getDescription());
        category.setParent(parent);

        category = categoryRepository.save(category);
        log.info("Category updated successfully: {}", category.getName());
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (categoryRepository.existsByParentId(id)) {
            throw new BadRequestException("VALID_001", "Không thể xóa danh mục có danh mục con.");
        }

        if (categoryRepository.countCoursesByCategoryId(id) > 0) {
            throw new BadRequestException("VALID_001", "Không thể xóa danh mục đang có khóa học.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", id);
    }

    private boolean isDescendantOf(Long parentId, Long childId) {
        if (parentId == null || childId == null) return false;
        if (parentId.equals(childId)) return true;
        CategoryEntity parent = categoryRepository.findById(parentId).orElse(null);
        if (parent == null || parent.getParent() == null) return false;
        return isDescendantOf(parent.getParent().getId(), childId);
    }

    private CategoryResponse mapToResponse(CategoryEntity category) {
        long courseCount = categoryRepository.countCoursesByCategoryId(category.getId());
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .icon(category.getIcon())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .courseCount(courseCount)
                .build();
    }

    private CategoryResponse mapToTreeResponse(CategoryEntity category) {
        List<CategoryResponse> children = category.getChildren().stream()
                .map(this::mapToTreeResponse)
                .collect(Collectors.toList());

        long courseCount = categoryRepository.countCoursesByCategoryId(category.getId());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .icon(category.getIcon())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(children)
                .courseCount(courseCount)
                .build();
    }
}
