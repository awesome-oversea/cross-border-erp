package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 采购跟单领域模型
 * <p>
 * 描述: 按采购单行跟踪供应商履约过程，汇总下单、收货、待收、不良、退回与成本偏差。
 *       该模型是“采购执行视角”的业务快照，用于支撑可视化跟单、异常闭环与收货完成判断。
 * </p>
 */
public record PurchaseTracking(
        String trackingId,
        String tenantId,
        String poId,
        String lineId,
        String sellerSku,
        int orderedQuantity,
        int receivedQuantity,
        int pendingQuantity,
        int damagedQuantity,
        int returnedQuantity,
        BigDecimal orderedUnitCost,
        BigDecimal actualUnitCost,
        PurchaseTrackingStatus status,
        Instant lastReceivedAt,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * 是否已完成跟单。
     * 业务规则:
     * 1. 已人工按异常结案时，直接视为完成；
     * 2. 收货 + 不良 + 退回数量覆盖订购数量时，视为完成。
     */
    public boolean isComplete() {
        return status == PurchaseTrackingStatus.CLOSED_WITH_EXCEPTION
                || receivedQuantity + damagedQuantity + returnedQuantity >= orderedQuantity;
    }

    /** 收货差异 = 实收数量 - 订购数量，用于超收/少收差异分析。 */
    public int getVariance() {
        return receivedQuantity - orderedQuantity;
    }
}
