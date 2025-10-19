package com.fersko.cacheredis.service;

import com.fersko.cacheredis.dto.ProductPriceDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductPriceService {

    ProductPriceDto createProductPrice(ProductPriceDto productPriceDto);

    Optional<ProductPriceDto> getProductPrice(UUID productId, String currency);

    List<ProductPriceDto> getProductPrices(UUID productId);

    List<ProductPriceDto> getPricesByCurrency(String currency);

    ProductPriceDto updateProductPrice(UUID productId, String currency, ProductPriceDto productPriceDto);

    void deleteProductPrice(UUID productId, String currency);

    boolean existsByProductIdAndCurrency(UUID productId, String currency);
}
