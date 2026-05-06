package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 广告组持久化对象，承载广告活动的分组信息与平台同步标识。
 * <p>
 * 对应数据库表: ads_ad_group
 * </p>
 * <p>
 * 业务说明:
 *   广告组是广告活动下的二级分组，用于组织和管理同一活动内的不同广告投放单元。
 *   每个广告组可设置独立的默认出价，并关联平台侧的广告组ID(platformGroupId)实现同步。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.AdGroup
 */
@TableName("ads_ad_group")
public class AdGroupDO {

    /** 广告组唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String groupId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 关联广告活动ID。 */
    private String campaignId;

    /** 平台侧广告组ID，用于与外部广告平台同步。 */
    private String platformGroupId;

    /** 广告组名称。 */
    private String name;

    /** 默认出价金额。 */
    private BigDecimal bid;

    /** 广告组状态: ACTIVE(启用)/PAUSED(暂停)/ARCHIVED(归档)。 */
    private String status;

    /** 创建时间。 */
    private Instant createdAt;

    /** 更新时间。 */
    private Instant updatedAt;

    public AdGroupDO() {}

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getPlatformGroupId() { return platformGroupId; }
    public void setPlatformGroupId(String platformGroupId) { this.platformGroupId = platformGroupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getBid() { return bid; }
    public void setBid(BigDecimal bid) { this.bid = bid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
