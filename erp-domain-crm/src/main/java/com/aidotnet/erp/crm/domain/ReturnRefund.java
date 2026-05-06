package com.aidotnet.erp.crm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 退货退款领域模型
 * <p>
 * 描述: CRM域售后退货退款单，记录退货原因、退款金额和退货状态。
 * </p>
 *
 * @author ERP系统
 */
public record ReturnRefund(
        String returnId,
        String tenantId,
        String orderId,
        String sellerSku,
        String customerId,
        int quantity,
        BigDecimal refundAmount,
        String currency,
        ReturnReason reason,
        ReturnStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 退货原因 */
    public enum ReturnReason {
        /** 产品缺陷 */
        DEFECTIVE,
        /** 发错货 */
        WRONG_ITEM,
        /** 与描述不符 */
        NOT_AS_DESCRIBED,
        /** 运输损坏 */
        DAMAGED_IN_TRANSIT,
        /** 改变主意 */
        CHANGE_OF_MIND,
        /** 其他 */
        OTHER
    }

    /** 退货状态 */
    public enum ReturnStatus {
        /** 已申请 */
        REQUESTED,
        /** 已批准 */
        APPROVED,
        /** 已收到退货 */
        RECEIVED,
        /** 已退款 */
        REFUNDED,
        /** 已拒绝 */
        REJECTED
    }
}
