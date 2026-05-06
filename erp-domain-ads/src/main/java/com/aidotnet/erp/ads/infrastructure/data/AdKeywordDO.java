package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 广告关键词持久化对象，承载广告组下关键词的投放配置与效果数据。
 * <p>
 * 对应数据库表: ads_keyword
 * </p>
 * <p>
 * 业务说明:
 *   广告关键词隶属于广告组，管理关键词的匹配方式、出价和效果数据。
 *   支持广泛匹配(BROAD)、短语匹配(PHRASE)和精确匹配(EXACT)三种匹配类型。
 *   效果数据包括曝光、点击、花费、销售额和ACOS，用于关键词优化决策。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.AdKeyword
 */
@TableName("ads_keyword")
public class AdKeywordDO {

    /** 关键词唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String keywordId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 关联广告组ID。 */
    private String groupId;

    /** 关键词文本。 */
    private String keywordText;

    /** 匹配类型: BROAD(广泛匹配)/PHRASE(短语匹配)/EXACT(精确匹配)。 */
    private String matchType;

    /** 关键词出价金额。 */
    private BigDecimal bid;

    /** 关键词状态: ACTIVE(启用)/PAUSED(暂停)/ARCHIVED(归档)。 */
    private String status;

    /** 累计曝光量。 */
    private int impressions;

    /** 累计点击量。 */
    private int clicks;

    /** 累计花费金额。 */
    private BigDecimal spend;

    /** 累计销售额。 */
    private BigDecimal sales;

    /** 广告成本销售比(ACOS)。 */
    private BigDecimal acos;

    /** 创建时间。 */
    private Instant createdAt;

    /** 更新时间。 */
    private Instant updatedAt;

    public AdKeywordDO() {}

    public String getKeywordId() { return keywordId; }
    public void setKeywordId(String keywordId) { this.keywordId = keywordId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getKeywordText() { return keywordText; }
    public void setKeywordText(String keywordText) { this.keywordText = keywordText; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public BigDecimal getBid() { return bid; }
    public void setBid(BigDecimal bid) { this.bid = bid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getImpressions() { return impressions; }
    public void setImpressions(int impressions) { this.impressions = impressions; }
    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }
    public BigDecimal getSpend() { return spend; }
    public void setSpend(BigDecimal spend) { this.spend = spend; }
    public BigDecimal getSales() { return sales; }
    public void setSales(BigDecimal sales) { this.sales = sales; }
    public BigDecimal getAcos() { return acos; }
    public void setAcos(BigDecimal acos) { this.acos = acos; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
