package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.ProductDto;
import com.fersko.cacheredis.entity.Category;
import com.fersko.cacheredis.entity.Product;
import com.fersko.cacheredis.mappers.ProductMapper;
import com.fersko.cacheredis.repository.CategoryRepository;
import com.fersko.cacheredis.repository.ProductRepository;
import com.fersko.cacheredis.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        
        if (productDto.categoryId() != null) {
            Category category = categoryRepository.findById(productDto.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDto.categoryId()));
            product.setCategory(category);
        }
        
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDto> getProductById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDto> getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategoryId(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }
    
    @Override
    public ProductDto updateProduct(UUID id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        existingProduct.setSku(productDto.sku());
        existingProduct.setName(productDto.name());
        existingProduct.setDescription(productDto.description());
        
        if (productDto.categoryId() != null) {
            Category category = categoryRepository.findById(productDto.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDto.categoryId()));
            existingProduct.setCategory(category);
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }
    
    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String searchTerm) {
        return productRepository.findBySearchTerm(searchTerm)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }
}
