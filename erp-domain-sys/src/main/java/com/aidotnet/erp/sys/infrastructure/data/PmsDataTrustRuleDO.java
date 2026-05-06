package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * PMS数据信任规则数据对象(PmsDataTrustRuleDO)
 * <p>
 * 描述: PMS数据信任规则数据对象，对应sys_pms_data_trust_rule表。
 *       定义各域数据信任级别和PMS写入权限，低信任数据不可覆盖高信任数据。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_pms_data_trust_rule")
public class PmsDataTrustRuleDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String ruleId;
    private String tenantId;
    private String domain;
    private String objectType;
    private String trustLevel;
    private String description;
    private String allowedActions;
    private Boolean canOverwriteErp;
    private Instant createdAt;
    private Instant updatedAt;

    public PmsDataTrustRuleDO() {}

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getTrustLevel() { return trustLevel; }
    public void setTrustLevel(String trustLevel) { this.trustLevel = trustLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAllowedActions() { return allowedActions; }
    public void setAllowedActions(String allowedActions) { this.allowedActions = allowedActions; }
    public Boolean getCanOverwriteErp() { return canOverwriteErp; }
    public void setCanOverwriteErp(Boolean canOverwriteErp) { this.canOverwriteErp = canOverwriteErp; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
