package com.aidotnet.erp.fba.domain;

import java.time.Instant;
import java.util.List;

/**
 * 移除订单领域模型
 * <p>
 * 描述: FBA库存移除订单，支持退回、销毁和清仓三种移除类型。
 * </p>
 *
 * @author ERP系统
 */
public record RemovalOrder(
        String removalId,
        String tenantId,
        String fbaSku,
        int quantity,
        RemovalType removalType,
        RemovalStatus status,
        String returnAddressId,
        String reason,
        List<RemovalItem> items,
        Instant createdAt,
        Instant updatedAt
) {
    /** 移除类型 */
    public enum RemovalType {
        /** 退回 */
        RETURN,
        /** 销毁 */
        DISPOSE,
        /** 清仓 */
        LIQUIDATE
    }
    /** 移除状态 */
    public enum RemovalStatus {
        /** 待处理 */
        PENDING,
        /** 处理中 */
        PROCESSING,
        /** 已完成 */
        COMPLETED,
        /** 已取消 */
        CANCELLED
    }
}
