package com.fersko.cacheredis.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryDto(
        UUID id,
        String name,
        String slug,
        OffsetDateTime createdAt
) {}
