package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 商标记录数据对象(TrademarkRecordDO)
 * <p>
 * 描述: 商标记录数据对象，对应sys_trademark_record表。
 *       存储商标注册信息，用于品牌保护和侵权检测。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_trademark_record")
public class TrademarkRecordDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String trademarkId;
    private String tenantId;
    private String trademarkName;
    private String registrationNumber;
    private String jurisdiction;
    private String niceClasses;
    private String owner;
    private String status;
    private Instant registeredAt;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public TrademarkRecordDO() {}

    public String getTrademarkId() { return trademarkId; }
    public void setTrademarkId(String trademarkId) { this.trademarkId = trademarkId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTrademarkName() { return trademarkName; }
    public void setTrademarkName(String trademarkName) { this.trademarkName = trademarkName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getJurisdiction() { return jurisdiction; }
    public void setJurisdiction(String jurisdiction) { this.jurisdiction = jurisdiction; }
    public String getNiceClasses() { return niceClasses; }
    public void setNiceClasses(String niceClasses) { this.niceClasses = niceClasses; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
