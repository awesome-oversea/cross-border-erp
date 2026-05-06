package com.aidotnet.erp.fms.infrastructure.data;

import java.time.Instant;

public class VatComplianceStatusDO {
    private String statusId;
    private String tenantId;
    private String countryCode;
    private String vatNumber;
    private String registrationStatus;
    private String filingStatus;
    private Instant nextFilingDate;
    private Instant registrationDate;
    private Instant expiryDate;
    private String detailsJson;
    private Instant updatedAt;

    public String getStatusId() { return statusId; }
    public void setStatusId(String statusId) { this.statusId = statusId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public String getFilingStatus() { return filingStatus; }
    public void setFilingStatus(String filingStatus) { this.filingStatus = filingStatus; }
    public Instant getNextFilingDate() { return nextFilingDate; }
    public void setNextFilingDate(Instant nextFilingDate) { this.nextFilingDate = nextFilingDate; }
    public Instant getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Instant registrationDate) { this.registrationDate = registrationDate; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
