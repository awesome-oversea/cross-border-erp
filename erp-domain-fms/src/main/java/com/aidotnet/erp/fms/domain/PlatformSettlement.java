package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 平台结算单领域模型
 * <p>
 * 描述: FMS域实体，记录电商平台(Amazon/Shopify等)的结算单。
 *       结算单关联多个平台账单(PlatformBill)，支持结算对账。
 *       包含汇率转换信息(forexStatus/forexRate)用于多币种结算。
 * </p>
 *
 * @author ERP系统
 * @see PlatformBill
 * @see Reconciliation
 */
public record PlatformSettlement(
        /** 结算ID */
        String settlementId,
        /** 租户ID */
        String tenantId,
        /** 平台(Amazon/Shopify等) */
        String platform,
        /** 店铺 */
        String store,
        /** 结算类型 */
        String settlementType,
        /** 结算金额 */
        BigDecimal amount,
        /** 已对账金额 */
        BigDecimal reconciledAmount,
        /** 关联账单数 */
        int linkedBillCount,
        /** 实际到账金额 */
        BigDecimal receivedAmount,
        /** 币种 */
        String currency,
        /** 结算日期 */
        LocalDate settlementDate,
        /** 结算状态 */
        String status,
        /** 提现状态 */
        String withdrawalStatus,
        /** 提现参考号 */
        String withdrawalReference,
        /** 汇兑状态 */
        String forexStatus,
        /** 汇率 */
        BigDecimal forexRate,
        /** 到账时间 */
        Instant receivedAt,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt
) {}
