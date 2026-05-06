package com.aidotnet.erp.ads.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record AdKeyword(
        String keywordId,
        String tenantId,
        String groupId,
        String keywordText,
        String matchType,
        BigDecimal bid,
        AdKeywordStatus status,
        int impressions,
        int clicks,
        BigDecimal spend,
        BigDecimal sales,
        BigDecimal acos,
        Instant createdAt,
        Instant updatedAt
) {
    public enum MatchType { BROAD, PHRASE, EXACT }
    public enum AdKeywordStatus { ACTIVE, PAUSED, ARCHIVED }
}
