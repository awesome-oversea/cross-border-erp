package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * FBA发货单行领域模型
 * <p>
 * 描述: FBA发货单的商品明细行，记录SKU、FNSKU和箱数。
 * </p>
 *
 * @author ERP系统
 */
public record FbaShipmentItem(
        String itemId,
        String tenantId,
        String shipmentId,
        String productId,
        String sellerSku,
        String fnsku,
        int quantity,
        int boxQuantity,
        Instant createdAt
) {}
