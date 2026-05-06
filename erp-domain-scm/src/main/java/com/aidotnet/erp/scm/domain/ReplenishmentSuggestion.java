package com.aidotnet.erp.scm.domain;

import java.time.Instant;

/**
 * 补货建议领域模型
 * <p>
 * 描述: 基于库存和销售数据自动生成的补货建议，包含当前库存、日均销量、
 *       安全库存和建议采购数量。
 * </p>
 *
 * @author ERP系统
 */
public record ReplenishmentSuggestion(
        String suggestionId,
        String tenantId,
        String sellerSku,
        String warehouseId,
        int currentStock,
        int avgDailySales,
        int leadTimeDays,
        int safetyStock,
        int suggestedQuantity,
        SuggestionPriority priority,
        SuggestionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 建议优先级 */
    public enum SuggestionPriority {
        /** 紧急 - 库存即将耗尽 */
        URGENT,
        /** 高 - 库存低于安全线 */
        HIGH,
        /** 中 - 需要关注 */
        MEDIUM,
        /** 低 - 常规补货 */
        LOW
    }

    /** 建议状态 */
    public enum SuggestionStatus {
        /** 待处理 */
        PENDING,
        /** 已采纳 */
        ACCEPTED,
        /** 已拒绝 */
        REJECTED,
        /** 已过期 */
        EXPIRED
    }
}
