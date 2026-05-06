package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 评价分析数据对象
 * <p>
 * 描述: 对应crm_review_analysis表，存储AI情感分析结果。
 *       按SKU维度聚合评价数据，包含正面/中性/负面评价统计。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_review_analysis")
public class ReviewAnalysisDO {

    /** 分析ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String analysisId;
    /** 租户ID */
    private String tenantId;
    /** 卖家SKU */
    private String sellerSku;
    /** 市场ID */
    private String marketplaceId;
    /** 平均评分 */
    private BigDecimal averageRating;
    /** 总评价数 */
    private Integer totalReviews;
    /** 正面评价数 */
    private Integer positiveCount;
    /** 中性评价数 */
    private Integer neutralCount;
    /** 负面评价数 */
    private Integer negativeCount;
    /** 情感分析摘要 */
    private String sentimentSummary;
    /** 分析时间 */
    private Instant analyzedAt;

    public ReviewAnalysisDO() {}

    public String getAnalysisId() { return analysisId; }
    public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    public Integer getPositiveCount() { return positiveCount; }
    public void setPositiveCount(Integer positiveCount) { this.positiveCount = positiveCount; }
    public Integer getNeutralCount() { return neutralCount; }
    public void setNeutralCount(Integer neutralCount) { this.neutralCount = neutralCount; }
    public Integer getNegativeCount() { return negativeCount; }
    public void setNegativeCount(Integer negativeCount) { this.negativeCount = negativeCount; }
    public String getSentimentSummary() { return sentimentSummary; }
    public void setSentimentSummary(String sentimentSummary) { this.sentimentSummary = sentimentSummary; }
    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }
}
