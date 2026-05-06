package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 评价领域模型
 * <p>
 * 描述: 跨境电商平台评价，包含评分、情感分析和回复状态。
 * </p>
 *
 * @author ERP系统
 */
public record Review(
        String reviewId,
        String tenantId,
        String customerId,
        String productId,
        String orderId,
        String platform,
        int rating,
        String title,
        String text,
        String sentiment,
        boolean verified,
        boolean responded,
        Instant reviewDate,
        Instant createdAt,
        Instant updatedAt
) {
    /** 情感倾向 */
    public enum Sentiment { /** 正面 */ POSITIVE, /** 中性 */ NEUTRAL, /** 负面 */ NEGATIVE }
}
