package com.aidotnet.erp.fms.infrastructure.data;

import java.time.Instant;

public class InvoiceSettingDO {
    private String settingId;
    private String tenantId;
    private String storeId;
    private String marketplaceId;
    private String templateId;
    private String invoiceTitle;
    private String taxRegistrationNo;
    private boolean showUnitPrice;
    private boolean showTaxRate;
    private boolean showDiscount;
    private String remark;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public String getSettingId() { return settingId; }
    public void setSettingId(String settingId) { this.settingId = settingId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }
    public String getTaxRegistrationNo() { return taxRegistrationNo; }
    public void setTaxRegistrationNo(String taxRegistrationNo) { this.taxRegistrationNo = taxRegistrationNo; }
    public boolean isShowUnitPrice() { return showUnitPrice; }
    public void setShowUnitPrice(boolean showUnitPrice) { this.showUnitPrice = showUnitPrice; }
    public boolean isShowTaxRate() { return showTaxRate; }
    public void setShowTaxRate(boolean showTaxRate) { this.showTaxRate = showTaxRate; }
    public boolean isShowDiscount() { return showDiscount; }
    public void setShowDiscount(boolean showDiscount) { this.showDiscount = showDiscount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
