package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 连接器密钥数据对象(ConnectorSecretDO)
 * <p>
 * 描述: 连接器密钥数据对象，对应sys_connector_secret表。
 *       存储外部平台连接器的加密密钥信息，支持密钥轮换。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_connector_secret")
public class ConnectorSecretDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String secretId;
    private String tenantId;
    private String configId;
    private String keyType;
    private String encryptedValue;
    private String maskedPreview;
    private String lastRotatedBy;
    private Instant lastRotatedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ConnectorSecretDO() {}

    public String getSecretId() { return secretId; }
    public void setSecretId(String secretId) { this.secretId = secretId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getKeyType() { return keyType; }
    public void setKeyType(String keyType) { this.keyType = keyType; }
    public String getEncryptedValue() { return encryptedValue; }
    public void setEncryptedValue(String encryptedValue) { this.encryptedValue = encryptedValue; }
    public String getMaskedPreview() { return maskedPreview; }
    public void setMaskedPreview(String maskedPreview) { this.maskedPreview = maskedPreview; }
    public String getLastRotatedBy() { return lastRotatedBy; }
    public void setLastRotatedBy(String lastRotatedBy) { this.lastRotatedBy = lastRotatedBy; }
    public Instant getLastRotatedAt() { return lastRotatedAt; }
    public void setLastRotatedAt(Instant lastRotatedAt) { this.lastRotatedAt = lastRotatedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
