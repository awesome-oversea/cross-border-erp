package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrencyRateSyncLog(
        String syncId,
        String tenantId,
        String source,
        String status,
        int totalRates,
        int successCount,
        int failCount,
        Instant startedAt,
        Instant completedAt,
        String errorMessage
) {}
