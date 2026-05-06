package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 快捷入口数据对象
 * <p>
 * 描述: 对应dashboard_quick_entry表，用于存储用户自定义的快捷功能入口。
 *       快捷入口提供常用功能的快速访问通道，支持自定义排序和分类。
 * </p>
 * <p>
 * 业务规则:
 *   1. entryCode在租户+用户下唯一，由UNIQUE约束保证
 *   2. sortOrder定义排序顺序，数值越小越靠前
 *   3. category定义入口分类，如: order/inventory/finance
 *   4. icon存储图标标识，前端根据标识渲染对应图标
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.QuickEntry
 */
@TableName("dashboard_quick_entry")
public class QuickEntryDO {

    /** 入口唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String entryId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 用户ID，关联IAM域用户 */
    private String userId;

    /** 入口编码，租户+用户内唯一 */
    private String entryCode;

    /** 入口名称，用于前端展示 */
    private String entryName;

    /** 图标标识，前端根据标识渲染图标 */
    private String icon;

    /** 跳转链接 */
    private String url;

    /** 入口分类，如: order/inventory/finance */
    private String category;

    /** 排序顺序，数值越小越靠前 */
    private int sortOrder;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public QuickEntryDO() {}

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEntryCode() { return entryCode; }
    public void setEntryCode(String entryCode) { this.entryCode = entryCode; }
    public String getEntryName() { return entryName; }
    public void setEntryName(String entryName) { this.entryName = entryName; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
