package com.aidotnet.erp.ads.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CampaignPerformance(
        String performanceId,
        String tenantId,
        String campaignId,
        BigDecimal spend,
        int impressions,
        int clicks,
        BigDecimal ctr,
        int orders,
        BigDecimal acos,
        BigDecimal roas,
        Instant periodStart,
        Instant periodEnd,
        Instant createdAt
) {}
