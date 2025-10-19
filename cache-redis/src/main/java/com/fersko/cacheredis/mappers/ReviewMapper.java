package com.fersko.cacheredis.mappers;

import com.fersko.cacheredis.dto.ReviewDto;
import com.fersko.cacheredis.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    
    @Mapping(target = "productId", source = "product.id")
    ReviewDto toDto(Review review);
    
    @Mapping(target = "product", ignore = true)
    Review toEntity(ReviewDto reviewDto);
}
