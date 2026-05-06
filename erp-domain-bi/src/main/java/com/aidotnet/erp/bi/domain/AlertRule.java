package com.aidotnet.erp.bi.domain;

import java.time.Instant;

public record AlertRule(String ruleId, String tenantId, String ruleName, String metricCode, String domain,
                        AlertCondition condition, String threshold, AlertSeverity severity, boolean enabled,
                        String notifyChannel, String notifyTargets, Instant createdAt, Instant updatedAt) {}
