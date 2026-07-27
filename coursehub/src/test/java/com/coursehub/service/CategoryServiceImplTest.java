package com.coursehub.service;

import com.coursehub.dto.request.CategoryRequest;
import com.coursehub.dto.response.CategoryResponse;
import com.coursehub.entity.CategoryEntity;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CategoryRepository;
import com.coursehub.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("getAllRootCategories — returns root categories response")
    void getAllRootCategories_success() {
        CategoryEntity c1 = CategoryEntity.builder().id(1L).name("IT").slug("it").build();
        given(categoryRepository.findAllRootCategories()).willReturn(List.of(c1));
        given(categoryRepository.countCoursesByCategoryId(1L)).willReturn(5L);

        List<CategoryResponse> result = categoryService.getAllRootCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("IT");
        assertThat(result.get(0).getCourseCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getSubCategories_success — valid parentId → returns subcategories")
    void getSubCategories_success() {
        CategoryEntity sub = CategoryEntity.builder().id(2L).name("Java").slug("java").build();
        given(categoryRepository.existsById(1L)).willReturn(true);
        given(categoryRepository.findByParentId(1L)).willReturn(List.of(sub));

        List<CategoryResponse> result = categoryService.getSubCategories(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("getSubCategories_notFound — invalid parentId → ResourceNotFoundException")
    void getSubCategories_notFound_throwsException() {
        given(categoryRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> categoryService.getSubCategories(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCategoryById_success — valid id → returns response")
    void getCategoryById_success() {
        CategoryEntity c = CategoryEntity.builder().id(1L).name("IT").slug("it").build();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(c));

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result.getName()).isEqualTo("IT");
    }

    @Test
    @DisplayName("getCategoryById_notFound — invalid id → ResourceNotFoundException")
    void getCategoryById_notFound_throwsException() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCategory_success — new category → saved")
    void createCategory_success() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Web Design");
        req.setSlug("web-design");

        given(categoryRepository.existsByName("Web Design")).willReturn(false);
        given(categoryRepository.existsBySlug("web-design")).willReturn(false);
        given(categoryRepository.save(any(CategoryEntity.class))).willAnswer(inv -> {
            CategoryEntity c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryResponse result = categoryService.createCategory(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Web Design");
    }

    @Test
    @DisplayName("createCategory_duplicateName — name exists → BadRequestException")
    void createCategory_duplicateName_throwsBadRequestException() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Web Design");

        given(categoryRepository.existsByName("Web Design")).willReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("updateCategory_success — valid update → saved")
    void updateCategory_success() {
        CategoryEntity existing = CategoryEntity.builder().id(1L).name("IT").slug("it").build();
        CategoryRequest req = new CategoryRequest();
        req.setName("Information Tech");
        req.setSlug("it-slug");

        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.existsByNameAndIdNot("Information Tech", 1L)).willReturn(false);
        given(categoryRepository.existsBySlugAndIdNot("it-slug", 1L)).willReturn(false);
        given(categoryRepository.save(any(CategoryEntity.class))).willAnswer(inv -> inv.getArgument(0));

        CategoryResponse result = categoryService.updateCategory(1L, req);

        assertThat(result.getName()).isEqualTo("Information Tech");
    }

    @Test
    @DisplayName("updateCategory_parentSelf — parentId equals self → BadRequestException")
    void updateCategory_parentSelf_throwsException() {
        CategoryEntity existing = CategoryEntity.builder().id(1L).name("IT").slug("it").build();
        CategoryRequest req = new CategoryRequest();
        req.setName("Information Tech");
        req.setParentId(1L);

        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.updateCategory(1L, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("deleteCategory_success — no subcategories, no courses → deleted")
    void deleteCategory_success() {
        CategoryEntity existing = CategoryEntity.builder().id(1L).name("IT").slug("it").build();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.existsByParentId(1L)).willReturn(false);
        given(categoryRepository.countCoursesByCategoryId(1L)).willReturn(0L);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteCategory_hasSubCategories — has child → BadRequestException")
    void deleteCategory_hasSubCategories_throwsException() {
        CategoryEntity existing = CategoryEntity.builder().id(1L).name("IT").slug("it").build();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.existsByParentId(1L)).willReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(BadRequestException.class);
    }
}
