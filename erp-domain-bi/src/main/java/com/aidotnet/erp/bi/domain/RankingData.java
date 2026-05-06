package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RankingData(
        String rankingId,
        String tenantId,
        String rankingType,
        String dimension,
        List<RankingItem> items,
        Instant generatedAt
) {
    public record RankingItem(String rankKey, String label, BigDecimal value, int rank) {}
}
