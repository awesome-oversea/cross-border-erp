package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record BusinessRuleVersion(
        String versionId, String tenantId, String ruleId, String ruleType,
        String ruleName, int version, String contentJson, String changeDescription,
        String changedBy, Instant createdAt) {}
