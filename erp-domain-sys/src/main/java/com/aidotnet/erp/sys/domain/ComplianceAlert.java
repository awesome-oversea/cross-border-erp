package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record ComplianceAlert(String alertId, String tenantId, String platform, String ruleId,
                              String alertType, String title, String description,
                              String severity, String status, String referenceType,
                              String referenceId, Instant detectedAt, Instant resolvedAt) {

    public enum AlertStatus {
        OPEN, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, DISMISSED
    }
}
