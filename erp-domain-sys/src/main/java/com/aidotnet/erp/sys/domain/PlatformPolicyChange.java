package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record PlatformPolicyChange(String changeId, String platform, String policyArea,
                                   String changeTitle, String changeSummary,
                                   String impactLevel, String sourceUrl,
                                   Instant effectiveDate, Instant detectedAt) {

    public enum ImpactLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
