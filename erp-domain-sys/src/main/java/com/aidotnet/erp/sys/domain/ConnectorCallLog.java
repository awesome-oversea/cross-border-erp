package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record ConnectorCallLog(
        String logId,
        String tenantId,
        String configId,
        String connectorType,
        String platform,
        String endpoint,
        String method,
        String traceId,
        int statusCode,
        long durationMs,
        boolean success,
        String errorMessage,
        Instant calledAt) {}
