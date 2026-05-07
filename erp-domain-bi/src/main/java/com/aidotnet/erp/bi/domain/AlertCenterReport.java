package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * BI 经营预警中心读模型。
 * <p>
 * 描述: 面向运营、财务、仓储、物流统一输出经营异常视图，
 * 聚合 KPI 异常、利润偏差、库存/价格/评价异常、FBA 物流异常等信号。
 * 该模型只做跨域只读聚合，不回写业务域。
 * </p>
 */
public record AlertCenterReport(
        Summary summary,
        List<AlertItem> alerts,
        Instant generatedAt
) {

    public record Summary(
            int totalAlertCount,
            int openAlertCount,
            int acknowledgedAlertCount,
            int resolvedAlertCount,
            int criticalAlertCount,
            int warningAlertCount,
            Map<String, Integer> categoryCounts,
            Map<String, Integer> sourceDomainCounts
    ) {}

    public record AlertItem(
            String alertId,
            String category,
            String sourceDomain,
            String severity,
            String status,
            String businessStatus,
            String title,
            String message,
            String metricCode,
            BigDecimal metricValue,
            BigDecimal thresholdValue,
            String listingId,
            String shipmentId,
            String dimensionType,
            String dimensionId,
            String storeId,
            String sellerSku,
            Instant detectedAt
    ) {}
}
