package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;

public record PmsDataTrustRule(
        String ruleId,
        String tenantId,
        String domain,
        String objectType,
        DataTrustLevel trustLevel,
        String description,
        List<String> allowedActions,
        boolean canOverwriteErp,
        Instant createdAt,
        Instant updatedAt) {}
