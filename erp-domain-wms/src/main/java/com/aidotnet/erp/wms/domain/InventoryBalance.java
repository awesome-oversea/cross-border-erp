package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 库存余额领域模型
 * <p>
 * 描述: 按租户+仓库+SKU维度的库存余额快照，包含在手、预占、在途、冻结数量。
 * </p>
 *
 * @author ERP系统
 */
public record InventoryBalance(String tenantId, String warehouseId, String sellerSku,
                               int onHand, int reserved, int inTransit, int frozen,
                               Instant updatedAt) {

    /** 计算可用库存 = 在手 - 预占 - 冻结 */
    public int getAvailable() {
        return onHand - reserved - frozen;
    }

    /** 计算总库存 = 在手 + 在途 */
    public int getTotal() {
        return onHand + inTransit;
    }
}
