package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Listing表现数据领域模型
 * <p>
 * 描述: 记录Listing在特定时段内的广告和销售表现数据，
 *       用于分析Listing运营效果和优化决策。
 * </p>
 *
 * @param performanceId  表现记录唯一标识
 * @param tenantId       租户ID
 * @param listingId      关联Listing ID
 * @param storeId        关联店铺ID
 * @param platform       平台
 * @param marketplace    市场站点
 * @param impressions    曝光量
 * @param clicks         点击量
 * @param ctr            点击率(Click-Through Rate)
 * @param spend          广告花费
 * @param sales          广告销售额
 * @param acos           广告成本销售比(Advertising Cost of Sales)
 * @param orders         订单量
 * @param conversionRate 转化率
 * @param periodStart    统计时段开始
 * @param periodEnd      统计时段结束
 * @param createdAt      创建时间
 * @author ERP系统
 */
public record ListingPerformance(String performanceId, String tenantId, String listingId,
                                 String storeId, String platform, String marketplace,
                                 int impressions, int clicks, BigDecimal ctr,
                                 BigDecimal spend, BigDecimal sales, BigDecimal acos,
                                 int orders, BigDecimal conversionRate,
                                 Instant periodStart, Instant periodEnd, Instant createdAt) {}
