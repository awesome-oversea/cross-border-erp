package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;

public record ContentAuditRule(
        String ruleId,
        String tenantId,
        String ruleType,
        String category,
        String keyword,
        String keywordPattern,
        int severity,
        String action,
        String replacement,
        String description,
        boolean enabled,
        List<String> applicablePlatforms,
        Instant createdAt,
        Instant updatedAt) {}
