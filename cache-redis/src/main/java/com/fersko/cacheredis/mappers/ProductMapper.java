package com.fersko.cacheredis.mappers;

import com.fersko.cacheredis.dto.ProductDto;
import com.fersko.cacheredis.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductDto toDto(Product product);
    
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductDto productDto);
}
