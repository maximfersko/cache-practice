package com.fersko.cacheredis.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String sku,
        String name,
        String description,
        UUID categoryId,
        String categoryName,
        OffsetDateTime updatedAt
) {}
