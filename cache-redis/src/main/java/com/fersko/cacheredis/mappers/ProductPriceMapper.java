package com.fersko.cacheredis.mappers;

import com.fersko.cacheredis.dto.ProductPriceDto;
import com.fersko.cacheredis.entity.ProductPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductPriceMapper {
    
    @Mapping(target = "productId", source = "id.productId")
    ProductPriceDto toDto(ProductPrice productPrice);
    
    @Mapping(target = "id.productId", source = "productId")
    @Mapping(target = "id.currency", source = "currency")
    @Mapping(target = "product", ignore = true)
    ProductPrice toEntity(ProductPriceDto productPriceDto);
}
