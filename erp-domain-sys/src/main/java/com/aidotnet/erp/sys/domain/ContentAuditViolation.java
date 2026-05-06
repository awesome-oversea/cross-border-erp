package com.aidotnet.erp.sys.domain;

public record ContentAuditViolation(
        String ruleId,
        String ruleType,
        String category,
        String matchedContent,
        int severity,
        String action,
        String replacement,
        int position) {}
