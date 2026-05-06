package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record DocumentNumberRule(
        String ruleId,
        String tenantId,
        String ruleName,
        String documentType,
        String prefix,
        String dateFormat,
        int sequenceLength,
        long currentSequence,
        long step,
        boolean resetDaily,
        boolean resetMonthly,
        boolean resetYearly,
        Instant lastResetAt,
        Instant createdAt,
        Instant updatedAt
) {}
