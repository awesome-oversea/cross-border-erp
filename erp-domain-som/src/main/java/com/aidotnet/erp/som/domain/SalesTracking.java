package com.aidotnet.erp.som.domain;

import java.time.Instant;

/**
 * 销售追踪领域模型
 * <p>
 * 描述: 按店铺+SKU维度记录的销售数据，包括销量、收入和均价。
 *       用于销售分析和库存补货决策。
 * </p>
 *
 * @param trackingId   追踪记录唯一标识
 * @param tenantId     租户ID
 * @param storeId      关联店铺ID
 * @param sellerSku    卖家SKU编码
 * @param marketplaceId 市场站点ID
 * @param unitsSold    销售数量
 * @param revenue      销售收入
 * @param averagePrice 均价(自动计算)
 * @param periodStart  统计时段开始
 * @param periodEnd    统计时段结束
 * @param createdAt    创建时间
 * @author ERP系统
 */
public record SalesTracking(
        String trackingId,
        String tenantId,
        String storeId,
        String sellerSku,
        String marketplaceId,
        int unitsSold,
        java.math.BigDecimal revenue,
        java.math.BigDecimal averagePrice,
        Instant periodStart,
        Instant periodEnd,
        Instant createdAt
) {}
