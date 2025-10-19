package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.ProductPriceDto;
import com.fersko.cacheredis.entity.Product;
import com.fersko.cacheredis.entity.ProductPrice;
import com.fersko.cacheredis.mappers.ProductPriceMapper;
import com.fersko.cacheredis.repository.ProductPriceRepository;
import com.fersko.cacheredis.repository.ProductRepository;
import com.fersko.cacheredis.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductPriceServiceImpl implements ProductPriceService {
    
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final ProductPriceMapper productPriceMapper;
    
    @Override
    public ProductPriceDto createProductPrice(ProductPriceDto productPriceDto) {
        ProductPrice productPrice = productPriceMapper.toEntity(productPriceDto);
        
        Product product = productRepository.findById(productPriceDto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productPriceDto.productId()));
        productPrice.setProduct(product);
        
        ProductPrice savedProductPrice = productPriceRepository.save(productPrice);
        return productPriceMapper.toDto(savedProductPrice);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductPriceDto> getProductPrice(UUID productId, String currency) {
        return productPriceRepository.findByProductIdAndCurrency(productId, currency)
                .map(productPriceMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductPriceDto> getProductPrices(UUID productId) {
        return productPriceRepository.findByProductId(productId)
                .stream()
                .map(productPriceMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductPriceDto> getPricesByCurrency(String currency) {
        return productPriceRepository.findByCurrency(currency)
                .stream()
                .map(productPriceMapper::toDto)
                .toList();
    }
    
    @Override
    public ProductPriceDto updateProductPrice(UUID productId, String currency, ProductPriceDto productPriceDto) {
        ProductPrice existingProductPrice = productPriceRepository.findByProductIdAndCurrency(productId, currency)
                .orElseThrow(() -> new RuntimeException("Product price not found for product: " + productId + " and currency: " + currency));
        
        existingProductPrice.setAmount(productPriceDto.amount());
        
        ProductPrice updatedProductPrice = productPriceRepository.save(existingProductPrice);
        return productPriceMapper.toDto(updatedProductPrice);
    }
    
    @Override
    public void deleteProductPrice(UUID productId, String currency) {
        ProductPrice productPrice = productPriceRepository.findByProductIdAndCurrency(productId, currency)
                .orElseThrow(() -> new RuntimeException("Product price not found for product: " + productId + " and currency: " + currency));
        
        productPriceRepository.delete(productPrice);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductIdAndCurrency(UUID productId, String currency) {
        return productPriceRepository.existsByProductIdAndCurrency(productId, currency);
    }
}
