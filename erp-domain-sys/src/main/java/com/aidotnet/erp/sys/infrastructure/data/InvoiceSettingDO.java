package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 发票设置数据对象(InvoiceSettingDO)
 * <p>
 * 描述: 发票设置数据对象，对应sys_invoice_setting表。
 *       存储租户级发票相关配置，包括开票规则、税率设置、票面模板等。
 *       config字段存储JSON格式的详细配置，支持不同发票类型的差异化配置。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下settingType必须唯一
 *   2. enabled=false的配置项不参与发票业务处理
 *   3. config字段为JSON格式，结构因settingType不同而异
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_invoice_setting
 *   - 主键: setting_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, setting_type)
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_invoice_setting")
public class InvoiceSettingDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String settingId;
    private String tenantId;
    private String settingType;
    private String settingName;
    private String config;
    private boolean enabled;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public InvoiceSettingDO() {}

    public String getSettingId() { return settingId; }
    public void setSettingId(String settingId) { this.settingId = settingId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSettingType() { return settingType; }
    public void setSettingType(String settingType) { this.settingType = settingType; }
    public String getSettingName() { return settingName; }
    public void setSettingName(String settingName) { this.settingName = settingName; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
