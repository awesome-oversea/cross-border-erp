package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 对账单领域模型
 * <p>
 * 描述: FMS域实体，记录财务对账结果。支持供应商对账、平台对账、物流对账等。
 *       对账差异记录在differenceItems中，需人工确认处理。
 * </p>
 *
 * @author ERP系统
 */
public record Reconciliation(
        /** 对账ID */
        String reconId,
        /** 租户ID */
        String tenantId,
        /** 对账类型(SUPPLIER/PLATFORM/CARRIER) */
        String type,
        /** 对账方ID */
        String partyId,
        /** 对账方名称 */
        String partyName,
        /** 对账周期 */
        String period,
        /** 应付金额 */
        BigDecimal payableAmount,
        /** 已付金额 */
        BigDecimal paidAmount,
        /** 余额 */
        BigDecimal balance,
        /** 状态 */
        String status,
        /** 对账明细(JSON) */
        String items,
        /** 差异明细(JSON) */
        String differenceItems,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt
) {}
