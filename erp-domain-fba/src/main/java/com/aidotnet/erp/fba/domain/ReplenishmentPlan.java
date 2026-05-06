package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * FBA补货计划领域模型
 * <p>
 * 描述: FBA补货计划，记录建议补货数量、源仓库和目的FBA仓。
 * </p>
 *
 * @author ERP系统
 */
public record ReplenishmentPlan(
        String planId,
        String tenantId,
        String sellerSku,
        String destinationFc,
        int suggestedQuantity,
        String sourceWarehouseId,
        PlanStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 补货计划状态 */
    public enum PlanStatus {
        /** 草稿 */
        DRAFT,
        /** 已提交 */
        SUBMITTED,
        /** 在途 */
        IN_TRANSIT,
        /** 已完成 */
        COMPLETED,
        /** 已取消 */
        CANCELLED
    }
}
