package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 审计日志数据对象
 * <p>
 * 描述: 对应iam_audit_log表，存储操作审计日志。
 *       审计日志只增不改，用于安全审计和问题排查。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_audit_log")
public class AuditLogDO {

    /** 审计日志唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String auditId;
    /** 租户ID */
    private String tenantId;
    /** 操作人 */
    private String actor;
    /** 操作类型，如 USER_CREATE、ROLE_UPDATE、LOGIN */
    private String action;
    /** 操作模块，如 iam、oms、fms */
    private String module;
    /** 操作目标 */
    private String target;
    /** 链路追踪ID */
    private String traceId;
    /** 操作是否成功 */
    private Boolean success;
    /** 操作发生时间 */
    private Instant occurredAt;

    public AuditLogDO() {
    }

    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
