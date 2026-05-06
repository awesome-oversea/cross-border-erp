package com.aidotnet.erp.ads.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record KeywordBid(
        String bidId,
        String tenantId,
        String campaignId,
        String keyword,
        BigDecimal bidAmount,
        BigDecimal maxBid,
        BidStrategy strategy,
        Instant createdAt,
        Instant updatedAt
) {
    public enum BidStrategy {
        MANUAL,
        AUTO_DOWN,
        AUTO_UP_DOWN
    }
}
