package com.aidotnet.erp.tms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("tms_carrier")
public class CarrierDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String carrierId;
    private String tenantId;
    private String code;
    private String name;
    private String countryCode;
    private String type;
    private String status;
    private String contactPerson;
    private String phone;
    private Boolean apiEnabled;
    private Boolean featured;
    private String authorizationStatus;
    private Instant authorizationValidUntil;
    private Instant lastAuthorizedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public CarrierDO() {}

    public String getCarrierId() { return carrierId; }
    public void setCarrierId(String carrierId) { this.carrierId = carrierId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getApiEnabled() { return apiEnabled; }
    public void setApiEnabled(Boolean apiEnabled) { this.apiEnabled = apiEnabled; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    public String getAuthorizationStatus() { return authorizationStatus; }
    public void setAuthorizationStatus(String authorizationStatus) { this.authorizationStatus = authorizationStatus; }
    public Instant getAuthorizationValidUntil() { return authorizationValidUntil; }
    public void setAuthorizationValidUntil(Instant authorizationValidUntil) { this.authorizationValidUntil = authorizationValidUntil; }
    public Instant getLastAuthorizedAt() { return lastAuthorizedAt; }
    public void setLastAuthorizedAt(Instant lastAuthorizedAt) { this.lastAuthorizedAt = lastAuthorizedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
