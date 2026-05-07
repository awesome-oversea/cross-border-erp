package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 利润核算结果领域模型
 * <p>
 * 描述: FMS域核心实体，利润核算引擎的输出结果。支持多维度利润分析:
 *       按订单/SKU/店铺/市场/产品/组合维度计算利润。
 *       毛利 = 收入 - 总成本; 毛利率 = 毛利 / 收入
 * </p>
 * <p>
 * 成本构成: 产品成本 + 物流运费 + FBA费用 + 平台佣金 + 广告费 + 退货成本
 *          + 仓储费 + 包装费 + 关税 + 其他费用 = 总成本
 * </p>
 * <p>
 * 汇率处理: 支持多币种利润核算，exchangeRate为结算汇率，
 *           amountInBaseCurrency为本币金额(按基准币种折算)
 * </p>
 *
 * @author ERP系统
 * @see ProfitCalculationEngine
 */
public record ProfitResult(
        /** 结果ID */
        String resultId,
        /** 租户ID */
        String tenantId,
        /** 维度类型 */
        String dimensionType,
        /** 维度ID */
        String dimensionId,
        /** 卖家SKU */
        String sellerSku,
        /** 订单ID */
        String orderId,
        /** 店铺ID */
        String storeId,
        /** 市场ID */
        String marketplaceId,
        /** 收入 */
        BigDecimal revenue,
        /** 产品成本 */
        BigDecimal productCost,
        /** 物流运费 */
        BigDecimal shippingCost,
        /** FBA费用 */
        BigDecimal fbaFee,
        /** 平台佣金 */
        BigDecimal commission,
        /** 广告费 */
        BigDecimal advertisingCost,
        /** 退货成本 */
        BigDecimal returnCost,
        /** 仓储费 */
        BigDecimal storageFee,
        /** 包装费 */
        BigDecimal packagingCost,
        /** 关税 */
        BigDecimal customDuty,
        /** 其他费用 */
        BigDecimal otherCost,
        /** 总成本 */
        BigDecimal totalCost,
        /** 毛利 = 收入 - 总成本 */
        BigDecimal grossProfit,
        /** 毛利率 = 毛利 / 收入 */
        BigDecimal grossMargin,
        /** 币种 */
        String currency,
        /** 结算汇率 */
        BigDecimal exchangeRate,
        /** 本币金额 */
        BigDecimal amountInBaseCurrency,
        /** 计算时间 */
        Instant calculatedAt,
        /** 成本明细(扩展字段) */
        Map<String, BigDecimal> costDetails
) {
    /** 利润分析维度类型 */
    public enum DimensionType {
        /** 按订单 */
        ORDER,
        /** 按SKU */
        SKU,
        /** 按店铺 */
        STORE,
        /** 按渠道 */
        CHANNEL,
        /** 按市场 */
        MARKETPLACE,
        /** 按产品 */
        PRODUCT,
        /** 组合维度 */
        COMBINED
    }
}
