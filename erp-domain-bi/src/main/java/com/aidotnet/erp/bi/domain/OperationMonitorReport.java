package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * BI 运营监控读模型
 * <p>
 * 聚合 SOM/WMS/CRM 等多域只读信号，形成面向运营、定价、客服的统一监控视图。
 * 该模型不回写业务域，仅承担 BI 读取与预警归并职责。
 * </p>
 */
public record OperationMonitorReport(
        Summary summary,
        List<ActiveRule> activeRules,
        List<ListingMonitorItem> listings,
        Instant generatedAt
) {

    public record Summary(
            int monitoredListingCount,
            int abnormalListingCount,
            int criticalListingCount,
            int salesAnomalyCount,
            int priceAnomalyCount,
            int buyboxAnomalyCount,
            int inventoryAnomalyCount,
            int reviewAnomalyCount,
            int pendingAlertCount,
            int enabledRuleCount
    ) {}

    public record ActiveRule(
            String ruleId,
            String ruleName,
            String metricCode,
            String threshold,
            String condition,
            String severity,
            String notifyChannel,
            String notifyTargets
    ) {}

    public record ListingMonitorItem(
            String listingId,
            String storeId,
            String title,
            String platform,
            String marketplace,
            String sellerSku,
            BigDecimal listingPrice,
            int orders,
            BigDecimal priceGapRate,
            BigDecimal buyboxWinnerPrice,
            BigDecimal ourPrice,
            int inventoryAvailable,
            double averageRating,
            int negativeReviewCount,
            int hijackerCount,
            String highestSeverity,
            List<String> anomalyTypes,
            List<Anomaly> anomalies
    ) {}

    public record Anomaly(
            String anomalyType,
            String metricCode,
            String severity,
            String ruleId,
            String ruleName,
            String message,
            BigDecimal currentValue,
            BigDecimal thresholdValue
    ) {}
}
