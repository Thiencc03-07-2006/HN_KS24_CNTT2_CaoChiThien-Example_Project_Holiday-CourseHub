package com.coursehub.repository;

import com.coursehub.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    boolean existsByNameAndIdNot(String name, Long excludeId);
    boolean existsBySlugAndIdNot(String slug, Long excludeId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.parent IS NULL ORDER BY c.name ASC")
    List<CategoryEntity> findAllRootCategories();

    @Query("SELECT c FROM CategoryEntity c WHERE c.parent.id = :parentId ORDER BY c.name ASC")
    List<CategoryEntity> findByParentId(Long parentId);

    boolean existsByParentId(Long parentId);

    @Query("SELECT COUNT(c) FROM CourseEntity c WHERE c.category.id = :categoryId AND c.deletedAt IS NULL")
    long countCoursesByCategoryId(Long categoryId);
}
