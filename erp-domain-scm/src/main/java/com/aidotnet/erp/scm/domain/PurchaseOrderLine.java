package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 采购订单行领域模型
 * <p>
 * 描述: 采购订单中的单个商品行，记录SKU、数量、单价和收货数量。
 * </p>
 *
 * @author ERP系统
 */
public record PurchaseOrderLine(
        String lineId,
        String productId,
        String sellerSku,
        int quantity,
        int receivedQuantity,
        BigDecimal unitCost,
        BigDecimal totalPrice,
        Instant expectedDate
) {
    /** 计算行总价，totalPrice为空时按单价×数量计算 */
    public BigDecimal lineTotal() {
        return totalPrice != null ? totalPrice : unitCost.multiply(BigDecimal.valueOf(quantity));
    }
}
