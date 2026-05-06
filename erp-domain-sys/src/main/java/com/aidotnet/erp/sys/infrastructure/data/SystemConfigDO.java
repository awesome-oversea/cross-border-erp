package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 系统配置数据对象(SystemConfigDO)
 * <p>
 * 描述: 系统配置表的数据库映射对象，对应sys_config表。
 *       存储租户级系统参数配置，如平台开关、业务参数、功能配置等。
 *       每个租户拥有独立的配置空间，通过config_key实现配置项唯一约束。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下config_key必须唯一
 *   2. enabled=false的配置项在业务查询时自动过滤
 *   3. config_value最大支持1024字符，超长配置建议使用JSONB字段
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_config
 *   - 主键: config_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, config_key)
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.sys.domain.SystemConfig
 */
@TableName("sys_config")
public class SystemConfigDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String configId;

    private String tenantId;

    private String configKey;

    private String configValue;

    private String description;

    private Boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    public SystemConfigDO() {
    }

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
