package com.aidotnet.erp.wms.domain;

/**
 * 盘点单领域模型
 * <p>
 * 描述: 库存盘点单，支持全盘、抽盘和循环盘点三种类型。
 * </p>
 *
 * @author ERP系统
 */
public record StockCheckOrder(
        String checkOrderId,
        String tenantId,
        String warehouseId,
        CheckType checkType,
        CheckOrderStatus status,
        String checkedBy,
        String remark,
        java.time.Instant createdAt,
        java.time.Instant completedAt
) {
    /** 盘点类型 */
    public enum CheckType {
        /** 全盘 */
        FULL_CHECK,
        /** 抽盘 */
        SPOT_CHECK,
        /** 循环盘点 */
        CYCLE_CHECK
    }
    /** 盘点单状态 */
    public enum CheckOrderStatus {
        /** 待盘点 */
        PENDING,
        /** 盘点中 */
        IN_PROGRESS,
        /** 已完成 */
        COMPLETED,
        /** 已调整 */
        ADJUSTED
    }
}
