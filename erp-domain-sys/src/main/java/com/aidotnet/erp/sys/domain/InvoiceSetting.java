package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record InvoiceSetting(
        String settingId,
        String tenantId,
        String settingType,
        String settingName,
        Map<String, Object> config,
        boolean enabled,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
