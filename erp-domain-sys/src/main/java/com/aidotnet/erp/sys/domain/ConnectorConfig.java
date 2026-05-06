package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record ConnectorConfig(
        String configId,
        String tenantId,
        String connectorType,
        String platform,
        String connectorName,
        Map<String, Object> config,
        String status,
        String version,
        String description,
        Instant lastSyncAt,
        Instant createdAt,
        Instant updatedAt) {}
