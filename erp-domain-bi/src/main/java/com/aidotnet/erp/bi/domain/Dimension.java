package com.aidotnet.erp.bi.domain;

import java.time.Instant;

public record Dimension(
        String dimensionId,
        String tenantId,
        String dimensionCode,
        String dimensionName,
        String dimensionType,
        String sourceField,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
