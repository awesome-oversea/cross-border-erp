package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 用户仪表盘数据对象
 * <p>
 * 描述: 对应dashboard_user_dashboard表，用于存储用户自定义仪表盘布局配置。
 *       每个用户可拥有多个仪表盘，其中仅一个可设为默认。
 * </p>
 * <p>
 * 业务规则:
 *   1. 每个用户只能有一个默认仪表盘(isDefault=true)
 *   2. layoutConfig存储布局配置JSON，定义组件排列方式
 *   3. widgets存储组件列表JSON，定义仪表盘包含的组件
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.UserDashboard
 */
@TableName("dashboard_user_dashboard")
public class UserDashboardDO {

    /** 仪表盘唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String dashboardId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 用户ID，关联IAM域用户 */
    private String userId;

    /** 布局配置JSON，定义组件排列方式 */
    private String layoutConfig;

    /** 组件列表JSON，定义仪表盘包含的组件ID和位置 */
    private String widgets;

    /** 是否为默认仪表盘，每用户仅一个默认 */
    private boolean isDefault;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public UserDashboardDO() {}

    public String getDashboardId() { return dashboardId; }
    public void setDashboardId(String dashboardId) { this.dashboardId = dashboardId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLayoutConfig() { return layoutConfig; }
    public void setLayoutConfig(String layoutConfig) { this.layoutConfig = layoutConfig; }
    public String getWidgets() { return widgets; }
    public void setWidgets(String widgets) { this.widgets = widgets; }
    public boolean isIsDefault() { return isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
