package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 平台账单领域模型
 * <p>
 * 描述: FMS域实体，记录电商平台的各种费用账单明细。
 *       包括佣金、FBA费、广告费、仓储费等。按SKU+市场维度归集。
 *       关联结算单(settlementId)实现账单与结算的匹配。
 * </p>
 *
 * @author ERP系统
 * @see PlatformSettlement
 */
public record PlatformBill(
        /** 账单ID */
        String billId,
        /** 租户ID */
        String tenantId,
        /** 平台 */
        String platform,
        /** 店铺 */
        String store,
        /** 账单类型(COMMISSION/FBA_FEE/ADVERTISING等) */
        String billType,
        /** 账单周期 */
        String period,
        /** 卖家SKU */
        String sellerSku,
        /** 市场ID */
        String marketplaceId,
        /** 来源类型 */
        String sourceType,
        /** 来源ID */
        String sourceId,
        /** 币种 */
        String currency,
        /** 金额 */
        BigDecimal amount,
        /** 原始数据(JSON) */
        String rawData,
        /** 状态 */
        String status,
        /** 关联结算单ID */
        String settlementId,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt
) {}
