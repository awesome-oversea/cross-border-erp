package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 库存事务领域模型
 * <p>
 * 描述: 库存变动事务记录，记录每次库存变动前后的数量快照和关联单据。
 * </p>
 *
 * @author ERP系统
 */
public record InventoryTransaction(
        String transactionId,
        String tenantId,
        String warehouseId,
        String sellerSku,
        TransactionType transactionType,
        int quantity,
        int beforeOnHand,
        int beforeReserved,
        int afterOnHand,
        int afterReserved,
        String referenceType,
        String referenceId,
        String remark,
        Instant createdAt
) {
    /** 事务类型 */
    public enum TransactionType {
        /** 入库 */
        RECEIVE,
        /** 预占 */
        RESERVE,
        /** 释放预占 */
        RELEASE,
        /** 扣减(出库) */
        DEDUCT,
        /** 库存调整 */
        STOCK_ADJUST
    }
}
