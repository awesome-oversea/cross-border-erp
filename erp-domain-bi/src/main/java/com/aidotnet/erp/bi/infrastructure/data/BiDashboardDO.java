package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * BI仪表盘数据对象
 * <p>
 * 描述: 对应bi_dashboard表，用于存储BI仪表盘的配置数据。
 *       仪表盘是BI域的核心展示载体，包含布局配置、类型和所有者信息，
 *       支持多种仪表盘类型(经营驾驶舱/销售分析/库存监控等)。
 * </p>
 * <p>
 * 业务规则:
 *   1. 仪表盘名称(dashboardName)在租户内建议唯一
 *   2. 配置(config)以JSONB格式存储，包含布局、组件引用等
 *   3. 仪表盘类型(dashboardType)支持: cockpit/sales/inventory/finance/custom等
 *   4. 所有者(owner)关联IAM域用户，控制仪表盘编辑权限
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.BiDashboard
 */
@TableName("bi_dashboard")
public class BiDashboardDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String dashboardId;
    private String tenantId;
    private String dashboardName;
    private String dashboardType;
    private String config;
    private String owner;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public BiDashboardDO() {}

    public String getDashboardId() { return dashboardId; }
    public void setDashboardId(String dashboardId) { this.dashboardId = dashboardId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDashboardName() { return dashboardName; }
    public void setDashboardName(String dashboardName) { this.dashboardName = dashboardName; }
    public String getDashboardType() { return dashboardType; }
    public void setDashboardType(String dashboardType) { this.dashboardType = dashboardType; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
