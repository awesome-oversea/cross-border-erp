package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * 发货异常领域模型
 * <p>
 * 描述: FBA发货过程中的异常记录，支持破损、丢失、退回、移除和共享五种异常类型。
 * </p>
 *
 * @author ERP系统
 */
public record ShipmentException(
        String exceptionId,
        String tenantId,
        String shipmentId,
        ExceptionType type,
        int qty,
        String status,
        String description,
        String resolvedBy,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
    /** 异常类型 */
    public enum ExceptionType {
        /** 破损 */
        DAMAGED,
        /** 丢失 */
        LOST,
        /** 退回 */
        RETURNED,
        /** 移除 */
        REMOVED,
        /** 共享库存 */
        SHARED
    }

    /** 异常状态 */
    public enum ExceptionStatus {
        /** 待处理 */
        OPEN,
        /** 处理中 */
        PROCESSING,
        /** 已解决 */
        RESOLVED
    }
}
