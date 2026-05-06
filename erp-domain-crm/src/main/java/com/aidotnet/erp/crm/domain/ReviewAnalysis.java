package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 评价分析领域模型
 * <p>
 * 描述: 按SKU+店铺维度的评价汇总分析，包含平均评分、情感分布和摘要。
 * </p>
 *
 * @author ERP系统
 */
public record ReviewAnalysis(
        String analysisId,
        String tenantId,
        String sellerSku,
        String marketplaceId,
        double averageRating,
        int totalReviews,
        int positiveCount,
        int neutralCount,
        int negativeCount,
        String sentimentSummary,
        Instant analyzedAt
) {}
