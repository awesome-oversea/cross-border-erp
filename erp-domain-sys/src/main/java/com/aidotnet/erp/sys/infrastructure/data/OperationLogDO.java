package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 操作日志数据对象(OperationLogDO)
 * <p>
 * 描述: 操作日志数据对象，对应sys_operation_log表。
 *       记录用户在系统中的关键操作行为，支持操作审计和行为追溯。
 *       通过traceId关联分布式链路追踪，便于跨服务问题排查。
 * </p>
 * <p>
 * 业务规则:
 *   1. 操作日志为只读数据，不允许修改和删除
 *   2. traceId用于关联同一请求在多个服务间的调用链路
 *   3. targetObjectType+targetObjectId定位操作目标对象
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_operation_log
 *   - 主键: log_id (ASSIGN_ID策略)
 *   - 索引: (tenant_id, module), (tenant_id, user_id), (trace_id)
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_operation_log")
public class OperationLogDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String logId;
    private String tenantId;
    private String userId;
    private String username;
    private String module;
    private String action;
    private String targetObjectType;
    private String targetObjectId;
    private String detail;
    private String ipAddress;
    private String userAgent;
    private String traceId;
    private Instant operatedAt;

    public OperationLogDO() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetObjectType() { return targetObjectType; }
    public void setTargetObjectType(String targetObjectType) { this.targetObjectType = targetObjectType; }
    public String getTargetObjectId() { return targetObjectId; }
    public void setTargetObjectId(String targetObjectId) { this.targetObjectId = targetObjectId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Instant getOperatedAt() { return operatedAt; }
    public void setOperatedAt(Instant operatedAt) { this.operatedAt = operatedAt; }
}
