package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 连接器调用日志数据对象(ConnectorCallLogDO)
 * <p>
 * 描述: 连接器调用日志数据对象，对应sys_connector_call_log表。
 *       记录每次外部连接器API调用的详情，用于监控和问题排查。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_connector_call_log")
public class ConnectorCallLogDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String logId;
    private String tenantId;
    private String configId;
    private String connectorType;
    private String platform;
    private String endpoint;
    private String method;
    private String traceId;
    private Integer statusCode;
    private Long durationMs;
    private Boolean success;
    private String errorMessage;
    private Instant calledAt;

    public ConnectorCallLogDO() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCalledAt() { return calledAt; }
    public void setCalledAt(Instant calledAt) { this.calledAt = calledAt; }
}
