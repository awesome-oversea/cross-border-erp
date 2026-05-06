package com.aidotnet.erp.ads.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record AdCampaign(String campaignId, String tenantId, String platform, String campaignName,
                         BigDecimal dailyBudget, CampaignStatus status, boolean pmsOptimizationEnabled,
                         String pmsOptimizationScope, Instant createdAt, Instant updatedAt) {}
