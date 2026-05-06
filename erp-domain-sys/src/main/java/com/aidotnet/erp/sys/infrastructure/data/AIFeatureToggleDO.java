package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * AI功能开关数据对象(AIFeatureToggleDO)
 * <p>
 * 描述: AI功能开关数据对象，对应sys_ai_feature_toggle表。
 *       控制各子域AI功能的灰度发布和开关状态，支持按租户和域独立配置。
 *       configJson字段存储功能特定的配置参数，如模型选择、阈值设置等。
 *       与PMS集成，控制PMS智能推荐在各域的启用范围。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下featureCode必须唯一
 *   2. domain字段标识功能所属子域(ADS/BI/OMS等)
 *   3. enabled=false时该AI功能不参与业务处理
 *   4. configJson为JSON格式，结构因featureCode不同而异
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_ai_feature_toggle
 *   - 主键: toggle_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, feature_code)
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_ai_feature_toggle")
public class AIFeatureToggleDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String toggleId;
    private String tenantId;
    private String featureCode;
    private String featureName;
    private String domain;
    private boolean enabled;
    private String description;
    private String configJson;
    private Instant createdAt;
    private Instant updatedAt;

    public AIFeatureToggleDO() {}

    public String getToggleId() { return toggleId; }
    public void setToggleId(String toggleId) { this.toggleId = toggleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFeatureCode() { return featureCode; }
    public void setFeatureCode(String featureCode) { this.featureCode = featureCode; }
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
