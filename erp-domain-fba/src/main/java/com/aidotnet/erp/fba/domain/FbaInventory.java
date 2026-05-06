package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * FBA库存领域模型
 * <p>
 * 描述: 亚马逊FBA仓库库存，记录FNSKU、库存数量和库龄天数。
 * </p>
 *
 * @author ERP系统
 */
public record FbaInventory(
        String inventoryId,
        String tenantId,
        String warehouseId,
        String productId,
        String sellerSku,
        String fnsku,
        int quantity,
        String storeId,
        String siteCode,
        int inventoryAgeDays,
        Instant lastUpdated
) {}
