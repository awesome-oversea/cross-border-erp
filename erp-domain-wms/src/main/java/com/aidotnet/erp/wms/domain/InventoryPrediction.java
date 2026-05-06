package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 库存预测领域模型
 * <p>
 * 描述: 基于历史销量和AI算法的库存预测，包含7天/30天预测需求、库存天数和预警等级。
 * </p>
 *
 * @author ERP系统
 */
public record InventoryPrediction(
        String predictionId,
        String tenantId,
        String warehouseId,
        String sellerSku,
        int currentStock,
        int predictedDemand7d,
        int predictedDemand30d,
        int daysOfStock,
        PredictionLevel level,
        Instant predictedAt
) {
    /** 预警等级 */
    public enum PredictionLevel {
        /** 充足 */
        SUFFICIENT,
        /** 预警 */
        WARNING,
        /** 紧急 */
        CRITICAL,
        /** 缺货 */
        OUT_OF_STOCK
    }
}
