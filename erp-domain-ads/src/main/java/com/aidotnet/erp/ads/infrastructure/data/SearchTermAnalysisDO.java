package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 搜索词分析持久化对象，承载广告搜索词的表现数据与优化建议。
 * <p>
 * 对应数据库表: ads_search_term_analysis
 * </p>
 * <p>
 * 业务说明:
 *   搜索词分析记录用户在广告平台上的搜索词表现数据，
 *   根据CTR、ACOS、订单量等指标自动分类搜索词表现等级，
 *   并生成关键词优化建议(精确匹配/短语匹配/否定关键词)。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.SearchTermAnalysis
 */
@TableName("ads_search_term_analysis")
public class SearchTermAnalysisDO {

    /** 分析记录唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String analysisId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 关联广告活动ID。 */
    private String campaignId;

    /** 搜索词文本。 */
    private String searchTerm;

    /** 曝光量。 */
    private Integer impressions;

    /** 点击量。 */
    private Integer clicks;

    /** 点击率(CTR)，点击量/曝光量。 */
    private Double ctr;

    /** 广告成本销售比(ACOS)，广告花费/销售额。 */
    private Double acos;

    /** 订单量。 */
    private Integer orders;

    /** 表现等级: HIGH_CONVERTING(高转化)/LOW_CONVERTING(低转化)/HIGH_SPEND_LOW_RETURN(高花费低回报)/IRRELEVANT(不相关)。 */
    private String performance;

    /** 推荐关键词JSON数组，如 ["keyword exact", "neg:keyword"]。 */
    private String suggestedKeywords;

    /** 分析时间。 */
    private Instant analyzedAt;

    public SearchTermAnalysisDO() {}

    public String getAnalysisId() { return analysisId; }
    public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    public Integer getImpressions() { return impressions; }
    public void setImpressions(Integer impressions) { this.impressions = impressions; }
    public Integer getClicks() { return clicks; }
    public void setClicks(Integer clicks) { this.clicks = clicks; }
    public Double getCtr() { return ctr; }
    public void setCtr(Double ctr) { this.ctr = ctr; }
    public Double getAcos() { return acos; }
    public void setAcos(Double acos) { this.acos = acos; }
    public Integer getOrders() { return orders; }
    public void setOrders(Integer orders) { this.orders = orders; }
    public String getPerformance() { return performance; }
    public void setPerformance(String performance) { this.performance = performance; }
    public String getSuggestedKeywords() { return suggestedKeywords; }
    public void setSuggestedKeywords(String suggestedKeywords) { this.suggestedKeywords = suggestedKeywords; }
    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }
}
