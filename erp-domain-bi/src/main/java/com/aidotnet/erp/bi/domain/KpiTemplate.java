package com.aidotnet.erp.bi.domain;

import java.time.Instant;

public record KpiTemplate(
        String templateId,
        String tenantId,
        String templateCode,
        String templateName,
        String category,
        String defaultUnit,
        String defaultTargetFormula,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
