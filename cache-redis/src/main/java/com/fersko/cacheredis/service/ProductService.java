package com.fersko.cacheredis.service;

import com.fersko.cacheredis.dto.ProductDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProductService {

    ProductDto createProduct(ProductDto productDto);

    Optional<ProductDto> getProductById(UUID id);

    Optional<ProductDto> getProductBySku(String sku);

    List<ProductDto> getAllProducts();

    List<ProductDto> getProductsByCategoryId(UUID categoryId);

    ProductDto updateProduct(UUID id, ProductDto productDto);

    void deleteProduct(UUID id);

    List<ProductDto> searchProducts(String searchTerm);

    boolean existsBySku(String sku);
}
