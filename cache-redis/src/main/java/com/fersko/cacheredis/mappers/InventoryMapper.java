package com.fersko.cacheredis.mappers;

import com.fersko.cacheredis.dto.InventoryDto;
import com.fersko.cacheredis.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    
    @Mapping(target = "productId", source = "id.productId")
    InventoryDto toDto(Inventory inventory);
    
    @Mapping(target = "id.productId", source = "productId")
    @Mapping(target = "id.warehouseId", source = "warehouseId")
    @Mapping(target = "product", ignore = true)
    Inventory toEntity(InventoryDto inventoryDto);
}
