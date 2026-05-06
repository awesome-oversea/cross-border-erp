package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 仪表盘组件数据对象
 * <p>
 * 描述: 对应bi_dashboard_widget表，用于存储仪表盘组件的配置数据。
 *       组件是仪表盘的基本展示单元，包含图表、指标卡、表格等类型，
 *       通过排序号控制展示顺序，支持启用/禁用切换。
 * </p>
 * <p>
 * 业务规则:
 *   1. 组件编码(widgetCode)在租户内唯一
 *   2. 组件类型(widgetType)支持: kpi_card/chart/table/filter/text等
 *   3. 配置(config)以JSONB格式存储，包含数据源绑定、样式和交互配置
 *   4. 排序号(sortOrder)控制组件在仪表盘中的展示顺序(升序)
 *   5. 禁用的组件不参与仪表盘渲染
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.DashboardWidget
 */
@TableName("bi_dashboard_widget")
public class DashboardWidgetDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String widgetId;
    private String tenantId;
    private String widgetCode;
    private String widgetName;
    private String widgetType;
    private String config;
    private Integer sortOrder;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public DashboardWidgetDO() {}

    public String getWidgetId() { return widgetId; }
    public void setWidgetId(String widgetId) { this.widgetId = widgetId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWidgetCode() { return widgetCode; }
    public void setWidgetCode(String widgetCode) { this.widgetCode = widgetCode; }
    public String getWidgetName() { return widgetName; }
    public void setWidgetName(String widgetName) { this.widgetName = widgetName; }
    public String getWidgetType() { return widgetType; }
    public void setWidgetType(String widgetType) { this.widgetType = widgetType; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
