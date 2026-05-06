package com.aidotnet.erp.ads.domain;

import java.time.Instant;
import java.util.List;

public record SearchTermAnalysis(
        String analysisId,
        String tenantId,
        String campaignId,
        String searchTerm,
        int impressions,
        int clicks,
        double ctr,
        double acos,
        int orders,
        SearchTermPerformance performance,
        List<String> suggestedKeywords,
        Instant analyzedAt
) {
    public enum SearchTermPerformance {
        HIGH_CONVERTING,
        LOW_CONVERTING,
        HIGH_SPEND_LOW_RETURN,
        IRRELEVANT
    }
}
