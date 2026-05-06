package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 通知模板数据对象(NotificationTemplateDO)
 * <p>
 * 描述: 通知模板数据对象，对应sys_notification_template表。
 *       存储各渠道（邮件/短信/站内信/Webhook等）的通知模板配置。
 *       支持变量渲染，模板内容中使用${variableName}占位符。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下templateCode必须唯一
 *   2. variables字段存储JSON格式的变量定义列表
 *   3. enabled=false的模板不参与通知发送
 *   4. channel字段与NotificationSettingDO的channel对应
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_notification_template
 *   - 主键: template_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, template_code)
 * </p>
 *
 * @author ERP系统
 * @see NotificationSettingDO
 */
@TableName("sys_notification_template")
public class NotificationTemplateDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String templateId;
    private String tenantId;
    private String templateCode;
    private String templateName;
    private String channel;
    private String subject;
    private String content;
    private String variables;
    private boolean enabled;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public NotificationTemplateDO() {}

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
