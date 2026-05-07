package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Buybox采集与监控领域模型
 * <p>
 * 描述: 采集Amazon等平台Listing的Buybox信息(价格/卖家/跟卖者)，
 *       支持自动调价策略以提升Buybox获得率。
 * </p>
 * <p>
 * 业务规则:
 *   1. 定期采集Buybox信息，记录当前持有者和价格
 *   2. 自动调价策略根据Buybox价格和利润规则调整售价
 *   3. Buybox丢失时生成运营监控告警
 * </p>
 *
 * @author ERP系统
 */
public record BuyboxMonitor(
        String monitorId,
        String tenantId,
        String listingId,
        String platform,
        String marketplace,
        /** 当前持有Buybox的卖家名称 */
        String winnerName,
        /** 当前Buybox价格 */
        BigDecimal winnerPrice,
        /** 我方当前售价 */
        BigDecimal ourPrice,
        /** 是否有调价建议 */
        boolean priceAdjustSuggested,
        /** 建议调价金额 */
        BigDecimal suggestedPrice,
        /** 跟卖者数量 */
        int hijackerCount,
        /** 采集时间 */
        Instant collectedAt,
        Instant createdAt
) {}
