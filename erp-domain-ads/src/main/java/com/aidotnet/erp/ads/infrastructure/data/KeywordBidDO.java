package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 关键词竞价持久化对象，承载广告关键词的出价配置与竞价策略。
 * <p>
 * 对应数据库表: ads_keyword_bid
 * </p>
 * <p>
 * 业务说明:
 *   关键词竞价管理广告活动中每个关键词的出价金额和竞价策略。
 *   支持手动出价(MANUAL)、自动降价(AUTO_DOWN)和自动升降价(AUTO_UP_DOWN)三种策略。
 *   maxBid字段用于限制自动策略的最高出价，防止超支。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.KeywordBid
 */
@TableName("ads_keyword_bid")
public class KeywordBidDO {

    /** 竞价记录唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String bidId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 关联广告活动ID。 */
    private String campaignId;

    /** 竞价关键词文本。 */
    private String keyword;

    /** 当前出价金额。 */
    private BigDecimal bidAmount;

    /** 最高出价限额，用于自动竞价策略的上限控制。 */
    private BigDecimal maxBid;

    /** 竞价策略: MANUAL(手动)/AUTO_DOWN(自动降价)/AUTO_UP_DOWN(自动升降价)。 */
    private String strategy;

    /** 创建时间。 */
    private Instant createdAt;

    /** 更新时间。 */
    private Instant updatedAt;

    public KeywordBidDO() {}

    public String getBidId() { return bidId; }
    public void setBidId(String bidId) { this.bidId = bidId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    public BigDecimal getMaxBid() { return maxBid; }
    public void setMaxBid(BigDecimal maxBid) { this.maxBid = maxBid; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
