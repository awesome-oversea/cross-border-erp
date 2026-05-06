package com.aidotnet.erp.pdm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("pdm_product_development")
public class ProductDevelopmentDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String devId;
    private String tenantId;
    private String spuId;
    private String proposalId;
    private String stage;
    private String stageNote;
    private String developer;
    private String editor;
    private String designer;
    private int priority;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ProductDevelopmentDO() {}

    public String getDevId() { return devId; }
    public void setDevId(String devId) { this.devId = devId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpuId() { return spuId; }
    public void setSpuId(String spuId) { this.spuId = spuId; }
    public String getProposalId() { return proposalId; }
    public void setProposalId(String proposalId) { this.proposalId = proposalId; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStageNote() { return stageNote; }
    public void setStageNote(String stageNote) { this.stageNote = stageNote; }
    public String getDeveloper() { return developer; }
    public void setDeveloper(String developer) { this.developer = developer; }
    public String getEditor() { return editor; }
    public void setEditor(String editor) { this.editor = editor; }
    public String getDesigner() { return designer; }
    public void setDesigner(String designer) { this.designer = designer; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
