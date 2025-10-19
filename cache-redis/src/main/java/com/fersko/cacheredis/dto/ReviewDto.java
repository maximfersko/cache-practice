package com.fersko.cacheredis.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID productId,
        Short rating,
        String text,
        OffsetDateTime updatedAt
) {}
