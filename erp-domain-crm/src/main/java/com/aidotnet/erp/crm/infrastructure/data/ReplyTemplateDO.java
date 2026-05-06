package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 回复模板数据对象
 * <p>
 * 描述: 对应crm_reply_template表，存储客服回复模板。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_reply_template")
public class ReplyTemplateDO {

    /** 模板ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String templateId;
    /** 租户ID */
    private String tenantId;
    /** 模板名称 */
    private String name;
    /** 模板分类 */
    private String category;
    /** 模板内容 */
    private String content;
    /** 语言 */
    private String language;
    /** 是否启用 */
    private boolean enabled;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public ReplyTemplateDO() {}

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
