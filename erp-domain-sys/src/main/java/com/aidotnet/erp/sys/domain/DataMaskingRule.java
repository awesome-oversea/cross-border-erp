package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record DataMaskingRule(
        String ruleId,
        String tenantId,
        String ruleCode,
        String ruleName,
        String fieldType,
        String maskPattern,
        String replaceChar,
        int keepPrefix,
        int keepSuffix,
        boolean enabled,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
