package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 仪表盘组件数据对象
 * <p>
 * 描述: 对应dashboard_widget表，用于存储工作台可配置的组件定义。
 *       组件是仪表盘的基本展示单元，支持多种数据源和刷新频率配置。
 * </p>
 * <p>
 * 业务规则:
 *   1. 组件类型(type): chart/table/stat/list/calendar等
 *   2. 组件状态(status): ACTIVE(启用)/INACTIVE(停用)
 *   3. config存储组件配置JSONB，如图表类型、维度、指标等
 *   4. refreshRateSeconds定义自动刷新间隔，单位秒
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.DashboardWidget
 */
@TableName("dashboard_widget")
public class DashboardWidgetDO {

    /** 组件唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String widgetId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 组件类型: chart/table/stat/list/calendar */
    private String type;

    /** 组件名称，用于前端展示 */
    private String name;

    /** 数据源标识，关联BI域报表或外部API */
    private String dataSource;

    /** 自动刷新间隔(秒)，默认300秒(5分钟) */
    private int refreshRateSeconds;

    /** 组件配置JSONB，存储图表类型、维度、指标等 */
    private String config;

    /** 组件状态: ACTIVE/INACTIVE */
    private String status;

    /** 组件分类，如: sales/inventory/finance */
    private String category;

    /** 组件描述 */
    private String description;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public DashboardWidgetDO() {}

    public String getWidgetId() { return widgetId; }
    public void setWidgetId(String widgetId) { this.widgetId = widgetId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public int getRefreshRateSeconds() { return refreshRateSeconds; }
    public void setRefreshRateSeconds(int refreshRateSeconds) { this.refreshRateSeconds = refreshRateSeconds; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
