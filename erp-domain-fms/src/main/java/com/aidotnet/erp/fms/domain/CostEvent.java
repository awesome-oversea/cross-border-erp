package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 成本事件领域模型
 * <p>
 * 描述: FMS域核心实体，记录各类成本事件。成本事件是成本归集引擎的输入，
 *       按SKU+市场维度归集，支持多维度成本分析。
 * </p>
 * <p>
 * 成本类型:
 *   PRODUCT_COST - 产品成本(采购价)
 *   SHIPPING_COST - 物流运费
 *   FBA_FEE - FBA仓储/配送费
 *   COMMISSION - 平台佣金
 *   ADVERTISING - 广告费用
 *   RETURN_COST - 退货成本
 *   STORAGE_FEE - 仓储费
 *   OTHER - 其他费用
 * </p>
 *
 * @author ERP系统
 */
public record CostEvent(
        /** 成本事件ID */
        String costEventId,
        /** 租户ID */
        String tenantId,
        /** 成本类型 */
        String costType,
        /** 来源类型(ORDER/PURCHASE/SHIPMENT等) */
        String sourceType,
        /** 来源ID(订单号/采购单号/运单号) */
        String sourceId,
        /** 卖家SKU */
        String sellerSku,
        /** 店铺ID */
        String storeId,
        /** 渠道编码 */
        String channelCode,
        /** 市场ID */
        String marketplaceId,
        /** 币种 */
        String currency,
        /** 金额 */
        BigDecimal amount,
        /** 发生时间 */
        Instant occurredAt,
        /** 创建时间 */
        Instant createdAt
) {
    /** 成本类型枚举 */
    public enum CostType {
        /** 产品成本(采购价) */
        PRODUCT_COST,
        /** 物流运费 */
        SHIPPING_COST,
        /** FBA仓储/配送费 */
        FBA_FEE,
        /** 平台佣金 */
        COMMISSION,
        /** 广告费用 */
        ADVERTISING,
        /** 退货成本 */
        RETURN_COST,
        /** 仓储费 */
        STORAGE_FEE,
        /** 其他费用 */
        OTHER
    }
}
