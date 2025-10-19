package com.fersko.cacheredis.service;

import com.fersko.cacheredis.dto.CategoryDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    Optional<CategoryDto> getCategoryById(UUID id);

    Optional<CategoryDto> getCategoryBySlug(String slug);

    List<CategoryDto> getAllCategories();

    CategoryDto updateCategory(UUID id, CategoryDto categoryDto);

    void deleteCategory(UUID id);

    List<CategoryDto> searchCategories(String searchTerm);

    boolean existsBySlug(String slug);
}
