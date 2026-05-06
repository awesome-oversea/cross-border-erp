package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 采购跟踪领域模型
 * <p>
 * 描述: 采购订单行的收货跟踪记录，记录已收/待收/损坏/退货数量和成本差异。
 * </p>
 *
 * @author ERP系统
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
        Instant updatedAt
) {
    /** 是否收货完成(已收+损坏+退货 >= 订购量) */
    public boolean isComplete() {
        return receivedQuantity + damagedQuantity + returnedQuantity >= orderedQuantity;
    }

    /** 计算收货差异(已收-订购) */
    public int getVariance() {
        return receivedQuantity - orderedQuantity;
    }
}
