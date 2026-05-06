package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 应收账款领域模型
 * <p>
 * 描述: FMS域核心实体，记录应收账款。来源包括平台结算、客户付款等。
 *       状态流转: PENDING → PARTIALLY_PAID → PAID → OVERDUE → WRITTEN_OFF
 * </p>
 *
 * @author ERP系统
 * @see ReceivableStatus
 * @see PaymentRecord
 */
public record Receivable(
        /** 应收ID */
        String receivableId,
        /** 租户ID */
        String tenantId,
        /** 来源类型(SETTLEMENT/ORDER等) */
        String sourceType,
        /** 来源ID */
        String sourceId,
        /** 客户名称 */
        String customerName,
        /** 币种 */
        String currency,
        /** 应收金额 */
        BigDecimal amount,
        /** 已收金额 */
        BigDecimal paidAmount,
        /** 应收状态 */
        ReceivableStatus status,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt) {}
