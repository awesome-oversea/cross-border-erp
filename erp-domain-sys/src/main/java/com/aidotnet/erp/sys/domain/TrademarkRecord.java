package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;

public record TrademarkRecord(
        String trademarkId,
        String tenantId,
        String trademarkName,
        String registrationNumber,
        String jurisdiction,
        List<String> niceClasses,
        String owner,
        String status,
        Instant registeredAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt) {}
