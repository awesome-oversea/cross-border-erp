package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 加工单领域模型
 * <p>
 * 描述: 原材料加工单，记录原材料投入、产出SKU和加工成本。
 *       支持组装、分装、贴标等加工类型。
 * </p>
 *
 * @author ERP系统
 */
public record ProcessingOrder(
        String processId,
        String tenantId,
        String name,
        List<RawMaterial> rawMaterials,
        String outputSku,
        int outputQuantity,
        BigDecimal totalCost,
        String currency,
        ProcessingOrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 加工单状态 */
    public enum ProcessingOrderStatus {
        /** 草稿 */
        DRAFT,
        /** 已提交 */
        SUBMITTED,
        /** 加工中 */
        IN_PROGRESS,
        /** 已完成 */
        COMPLETED,
        /** 已取消 */
        CANCELLED
    }

    /** 原材料行 */
    public record RawMaterial(
            /** 原材料SKU */
            String sellerSku,
            /** 消耗数量 */
            int quantity,
            /** 单位成本 */
            BigDecimal unitCost
    ) {}
}
