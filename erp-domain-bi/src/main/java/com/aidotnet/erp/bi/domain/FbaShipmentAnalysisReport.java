package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * FBA货件分析读模型。
 * <p>
 * 描述: 面向 BI 看板输出 FBA 货件时效、异常和成本聚合结果，避免控制器拼装临时 Map。
 * </p>
 */
public record FbaShipmentAnalysisReport(
        Summary summary,
        List<ShipmentAnalysisItem> shipments,
        Instant generatedAt
) {
    public record Summary(
            int shipmentCount,
            int closedShipmentCount,
            int openShipmentCount,
            int inTransitShipmentCount,
            int exceptionShipmentCount,
            int exceptionCount,
            int totalPlannedQuantity,
            int totalReceivedQuantity,
            BigDecimal averageReceiveRate,
            BigDecimal averageTransitHours,
            BigDecimal totalShippingCost,
            BigDecimal totalFbaFee,
            BigDecimal totalLogisticsCost,
            String currency
    ) {}

    public record ShipmentAnalysisItem(
            String shipmentId,
            String amazonShipmentId,
            String destinationFc,
            String carrier,
            String trackingNo,
            String status,
            int plannedQuantity,
            int receivedQuantity,
            BigDecimal receiveRate,
            int cartonCount,
            BigDecimal totalWeight,
            BigDecimal shippingCost,
            BigDecimal fbaFee,
            BigDecimal totalCost,
            int exceptionCount,
            int openExceptionCount,
            List<String> exceptionTypes,
            BigDecimal processingHours,
            BigDecimal transitHours,
            Instant createdAt,
            Instant packedAt,
            Instant shippedAt,
            Instant updatedAt
    ) {}
}
