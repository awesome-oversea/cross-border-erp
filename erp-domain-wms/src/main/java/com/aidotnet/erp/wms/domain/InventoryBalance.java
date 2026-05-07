package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 库存余额领域模型
 * <p>
 * 描述: 按租户+仓库+SKU维度的库存余额快照，管理五类库存状态:
 * </p>
 * <p>
 * 五类库存状态:
 *   1. onHand(在手) - 仓库实际持有的库存，入库确认后增加
 *   2. reserved(预占) - 已分配未出库，订单分配后预占
 *   3. available(可售) - 可供销售，计算公式: onHand - reserved - frozen
 *   4. inTransit(在途) - 运输中库存，调拨在途/FBA在途
 *   5. frozen(冻结/不良) - 不可销售，质检不良品、待检冻结
 * </p>
 * <p>
 * 库存事务记录: 每次库存变化基于业务来源记录流水(InventoryTransaction)，
 * 确保库存变动可追溯、可审计。
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
