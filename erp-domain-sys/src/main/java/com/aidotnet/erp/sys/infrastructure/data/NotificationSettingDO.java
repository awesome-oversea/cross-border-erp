package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 通知渠道设置数据对象(NotificationSettingDO)
 * <p>
 * 描述: 通知渠道设置数据对象，对应sys_notification_setting表。
 *       定义各通知渠道的启用状态和路由规则，控制通知消息的投递策略。
 *       rules字段存储JSON格式的路由规则，如触发事件、接收人过滤等。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下channel必须唯一
 *   2. enabled=false的渠道不参与通知投递
 *   3. rules字段为JSON格式，定义通知触发和路由条件
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_notification_setting
 *   - 主键: setting_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, channel)
 * </p>
 *
 * @author ERP系统
 * @see NotificationTemplateDO
 */
@TableName("sys_notification_setting")
public class NotificationSettingDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String settingId;
    private String tenantId;
    private String channel;
    private String channelName;
    private String rules;
    private boolean enabled;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public NotificationSettingDO() {}

    public String getSettingId() { return settingId; }
    public void setSettingId(String settingId) { this.settingId = settingId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
