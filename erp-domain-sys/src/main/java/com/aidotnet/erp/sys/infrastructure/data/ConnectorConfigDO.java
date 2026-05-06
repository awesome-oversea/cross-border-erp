package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 连接器配置数据对象(ConnectorConfigDO)
 * <p>
 * 描述: 连接器配置数据对象，对应sys_connector_config表。
 *       存储外部平台（Amazon/Shopify/ERP等）的连接器配置信息。
 *       支持多平台多类型连接器管理，通过connectorType和platform区分。
 *       与ConnectorSecretDO配合使用，密钥信息单独加密存储。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下(connectorType, platform)组合建议唯一
 *   2. status字段控制连接器状态: ACTIVE/INACTIVE/ERROR
 *   3. config字段为JSON格式，存储连接器特定配置参数
 *   4. lastSyncAt记录最近一次同步时间
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_connector_config
 *   - 主键: config_id (ASSIGN_ID策略)
 *   - 索引: (tenant_id, connector_type), (tenant_id, platform)
 * </p>
 *
 * @author ERP系统
 * @see ConnectorSecretDO
 * @see ConnectorCallLogDO
 */
@TableName("sys_connector_config")
public class ConnectorConfigDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String configId;
    private String tenantId;
    private String connectorType;
    private String platform;
    private String connectorName;
    private String config;
    private String status;
    private String version;
    private String description;
    private Instant lastSyncAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ConnectorConfigDO() {}

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getConnectorName() { return connectorName; }
    public void setConnectorName(String connectorName) { this.connectorName = connectorName; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
