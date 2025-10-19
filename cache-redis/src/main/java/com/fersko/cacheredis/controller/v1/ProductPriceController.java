package com.fersko.cacheredis.controller.v1;

import com.fersko.cacheredis.dto.ProductPriceDto;
import com.fersko.cacheredis.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-prices")
@RequiredArgsConstructor
public class ProductPriceController {
    
    private final ProductPriceService productPriceService;
    
    @PostMapping
    public ResponseEntity<ProductPriceDto> createProductPrice(@RequestBody ProductPriceDto productPriceDto) {
        ProductPriceDto createdProductPrice = productPriceService.createProductPrice(productPriceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProductPrice);
    }
    
    @GetMapping("/product/{productId}/currency/{currency}")
    public ResponseEntity<ProductPriceDto> getProductPrice(@PathVariable UUID productId, @PathVariable String currency) {
        return productPriceService.getProductPrice(productId, currency)
                .map(price -> ResponseEntity.ok(price))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductPriceDto>> getProductPrices(@PathVariable UUID productId) {
        List<ProductPriceDto> prices = productPriceService.getProductPrices(productId);
        return ResponseEntity.ok(prices);
    }
    
    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<ProductPriceDto>> getPricesByCurrency(@PathVariable String currency) {
        List<ProductPriceDto> prices = productPriceService.getPricesByCurrency(currency);
        return ResponseEntity.ok(prices);
    }
    
    @PutMapping("/product/{productId}/currency/{currency}")
    public ResponseEntity<ProductPriceDto> updateProductPrice(
            @PathVariable UUID productId,
            @PathVariable String currency,
            @RequestBody ProductPriceDto productPriceDto) {
        try {
            ProductPriceDto updatedPrice = productPriceService.updateProductPrice(productId, currency, productPriceDto);
            return ResponseEntity.ok(updatedPrice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/product/{productId}/currency/{currency}")
    public ResponseEntity<Void> deleteProductPrice(@PathVariable UUID productId, @PathVariable String currency) {
        try {
            productPriceService.deleteProductPrice(productId, currency);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByProductIdAndCurrency(
            @RequestParam UUID productId,
            @RequestParam String currency) {
        boolean exists = productPriceService.existsByProductIdAndCurrency(productId, currency);
        return ResponseEntity.ok(exists);
    }
}
