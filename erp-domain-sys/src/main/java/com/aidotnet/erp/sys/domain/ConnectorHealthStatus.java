package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record ConnectorHealthStatus(
        String configId,
        String tenantId,
        String connectorType,
        String platform,
        String status,
        long lastSuccessAt,
        long lastFailureAt,
        double successRate,
        double avgLatencyMs,
        int consecutiveFailures,
        Instant checkedAt) {}
