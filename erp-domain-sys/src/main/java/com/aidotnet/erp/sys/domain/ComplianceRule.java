package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record ComplianceRule(String ruleId, String tenantId, String platform, String ruleType,
                             String ruleName, String description, String severity,
                             boolean enabled, Instant createdAt, Instant updatedAt) {

    public enum RuleType {
        LISTING_POLICY, PRICING_POLICY, VAT_COMPLIANCE, PRODUCT_SAFETY, DATA_PRIVACY, ADVERTISING
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
