package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 店铺指标领域模型
 * <p>
 * 描述: 店铺维度运营指标，包括营收、订单量、客单价、退货率和评分。
 *       用于店铺健康度评估和运营决策。
 * </p>
 *
 * @param metricsId          指标记录唯一标识
 * @param tenantId           租户ID
 * @param storeId            关联店铺ID
 * @param marketplaceId      市场站点ID
 * @param totalRevenue       总营收
 * @param totalOrders        总订单量
 * @param averageOrderValue  客单价(Average Order Value，自动计算)
 * @param returnRate         退货率
 * @param feedbackScore      买家评分
 * @param periodStart        统计时段开始
 * @param periodEnd          统计时段结束
 * @param createdAt          创建时间
 * @author ERP系统
 */
public record StoreMetrics(
        String metricsId,
        String tenantId,
        String storeId,
        String marketplaceId,
        BigDecimal totalRevenue,
        BigDecimal totalOrders,
        BigDecimal averageOrderValue,
        BigDecimal returnRate,
        BigDecimal feedbackScore,
        Instant periodStart,
        Instant periodEnd,
        Instant createdAt
) {}
