package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.CategoryDto;
import com.fersko.cacheredis.entity.Category;
import com.fersko.cacheredis.mappers.CategoryMapper;
import com.fersko.cacheredis.repository.CategoryRepository;
import com.fersko.cacheredis.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(categoryMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }
    
    @Override
    public CategoryDto updateCategory(UUID id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        existingCategory.setName(categoryDto.name());
        existingCategory.setSlug(categoryDto.slug());
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }
    
    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategories(String searchTerm) {
        return categoryRepository.findBySearchTerm(searchTerm)
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }
}
