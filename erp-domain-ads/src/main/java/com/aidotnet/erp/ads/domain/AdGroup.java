package com.aidotnet.erp.ads.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record AdGroup(
        String groupId,
        String tenantId,
        String campaignId,
        String platformGroupId,
        String name,
        BigDecimal bid,
        AdGroupStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public enum AdGroupStatus { ACTIVE, PAUSED, ARCHIVED }
}
