package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * 补货购物车项领域模型
 * <p>
 * 描述: 用户补货购物车中的商品项，记录SKU、数量和仓库。
 * </p>
 *
 * @author ERP系统
 */
public record RestockCartItem(
        String cartItemId,
        String tenantId,
        String userId,
        String productId,
        String sellerSku,
        int qty,
        String warehouseId,
        Instant createdAt
) {}
