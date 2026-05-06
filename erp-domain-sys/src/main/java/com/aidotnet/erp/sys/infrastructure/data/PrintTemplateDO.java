package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 打印模板数据对象(PrintTemplateDO)
 * <p>
 * 描述: 打印模板数据对象，对应sys_print_template表。
 *       存储各类单据的打印模板配置，支持变量渲染。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_print_template")
public class PrintTemplateDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String templateId;
    private String tenantId;
    private String templateCode;
    private String templateName;
    private String templateType;
    private String content;
    private String paperSize;
    private String orientation;
    private String variables;
    private Boolean enabled;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public PrintTemplateDO() {}

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }
    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
