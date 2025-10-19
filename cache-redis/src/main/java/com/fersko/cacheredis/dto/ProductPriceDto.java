package com.fersko.cacheredis.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductPriceDto(
        UUID productId,
        String currency,
        BigDecimal amount,
        OffsetDateTime updatedAt
) {}
