package com.aidotnet.erp.tms.domain;

import java.time.Instant;
import java.util.List;

/**
 * 批量发货领域模型
 * <p>
 * 描述: 批量发货单，将多个发货单合并提交给承运商。
 * </p>
 *
 * @author ERP系统
 */
public record ShippingBatch(
        String batchId,
        String tenantId,
        String carrierId,
        List<String> shipmentIds,
        ShippingBatchStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 批量发货状态 */
    public enum ShippingBatchStatus {
        /** 草稿 */
        DRAFT,
        /** 已提交 */
        SUBMITTED,
        /** 在途 */
        IN_TRANSIT,
        /** 已完成 */
        COMPLETED,
        /** 已取消 */
        CANCELLED
    }
}
