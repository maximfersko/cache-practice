package com.fersko.cacheredis.mappers;

import com.fersko.cacheredis.dto.CategoryDto;
import com.fersko.cacheredis.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    CategoryDto toDto(Category category);
    
    Category toEntity(CategoryDto categoryDto);
}
