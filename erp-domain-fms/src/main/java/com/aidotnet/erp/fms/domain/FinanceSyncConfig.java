package com.aidotnet.erp.fms.domain;

import java.time.Instant;
import java.util.Map;

public record FinanceSyncConfig(
        String configId,
        String tenantId,
        String financeSystem,
        String apiUrl,
        String apiKey,
        String apiSecret,
        String accountSet,
        boolean enabled,
        Map<String, String> mappingRules,
        Instant lastSyncAt,
        Instant createdAt,
        Instant updatedAt) {}
