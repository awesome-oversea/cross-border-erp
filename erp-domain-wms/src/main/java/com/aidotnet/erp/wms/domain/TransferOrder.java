package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 调拨单领域模型
 * <p>
 * 描述: 仓库间调拨单，记录源仓库、目标仓库和调拨状态。
 * </p>
 *
 * @author ERP系统
 */
public record TransferOrder(
        String transferId,
        String tenantId,
        String fromWarehouseId,
        String toWarehouseId,
        TransferStatus status,
        String remark,
        Instant createdAt,
        Instant updatedAt
) {
    /** 调拨状态 */
    public enum TransferStatus {
        /** 待调拨 */
        PENDING,
        /** 在途 */
        IN_TRANSIT,
        /** 已接收 */
        RECEIVED,
        /** 已取消 */
        CANCELLED
    }
}
