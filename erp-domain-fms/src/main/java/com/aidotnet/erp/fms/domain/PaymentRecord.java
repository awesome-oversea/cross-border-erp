package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 付款记录领域模型
 * <p>
 * 描述: FMS域实体，记录应收账款的收款/付款信息。
 *       一个应收可对应多笔付款(分期收款场景)。
 * </p>
 *
 * @author ERP系统
 * @see Receivable
 */
public record PaymentRecord(
        /** 付款ID */
        String paymentId,
        /** 租户ID */
        String tenantId,
        /** 关联应收ID */
        String receivableId,
        /** 付款金额 */
        BigDecimal amount,
        /** 付款方式(BANK_TRANSFER/ONLINE/PAYPAL等) */
        String paymentMethod,
        /** 付款时间 */
        Instant paidAt) {}
