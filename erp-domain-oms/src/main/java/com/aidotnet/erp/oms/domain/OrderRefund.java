package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单退款领域模型
 * <p>
 * 描述: 订单退款记录，支持全额退款和部分退款。
 * </p>
 * <p>
 * 状态流转: REQUESTED → APPROVED → COMPLETED 或 REQUESTED → REJECTED
 * </p>
 *
 * @author ERP系统
 */
public record OrderRefund(
        String refundId,
        String tenantId,
        String orderId,
        String reason,
        BigDecimal refundAmount,
        RefundType refundType,
        RefundStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 退款类型 */
    public enum RefundType {
        /** 全额退款 */
        FULL,
        /** 部分退款 */
        PARTIAL
    }

    /** 退款状态 */
    public enum RefundStatus {
        /** 已申请 */
        REQUESTED,
        /** 已批准 */
        APPROVED,
        /** 已拒绝 */
        REJECTED,
        /** 已完成 */
        COMPLETED
    }
}
