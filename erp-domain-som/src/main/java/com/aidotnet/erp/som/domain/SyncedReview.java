package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 平台Reviews同步记录
 * <p>
 * 描述: 从Amazon/eBay/Shopify等平台同步的客户评价/Reviews。
 *       通过Reviews发现产品问题，记录质量投诉并归类，
 *       与PDM产品质量问题联动。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一平台+平台ReviewID唯一
 *   2. 低星级(<3)Review自动生成质量问题记录
 *   3. Review通过Listing关联到具体产品(SPU/SKU)
 * </p>
 *
 * @author ERP系统
 */
public record SyncedReview(
        String reviewId,
        String tenantId,
        String listingId,
        String productId,
        String platform,
        String marketplace,
        String platformReviewId,
        String reviewerName,
        int rating,
        String title,
        String content,
        /** 是否有质量问题标记 */
        boolean qualityIssue,
        /** 质量问题归类: QUALITY/SHIPPING/DESCRIPTION/OTHER */
        String issueCategory,
        Instant reviewDate,
        Instant syncedAt,
        Instant createdAt
) {}
