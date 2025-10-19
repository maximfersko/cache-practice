package com.fersko.cacheredis.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryDto(
        UUID productId,
        Long warehouseId,
        Integer quantity,
        OffsetDateTime updatedAt
) {}
