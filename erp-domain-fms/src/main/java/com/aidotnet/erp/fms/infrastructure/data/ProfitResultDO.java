package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class ProfitResultDO {
    private String resultId;
    private String tenantId;
    private String dimensionType;
    private String dimensionId;
    private String sellerSku;
    private String orderId;
    private String storeId;
    private String marketplaceId;
    private BigDecimal revenue;
    private BigDecimal productCost;
    private BigDecimal shippingCost;
    private BigDecimal fbaFee;
    private BigDecimal commission;
    private BigDecimal advertisingCost;
    private BigDecimal returnCost;
    private BigDecimal storageFee;
    private BigDecimal packagingCost;
    private BigDecimal customDuty;
    private BigDecimal otherCost;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal grossMargin;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal amountInBaseCurrency;
    private String costDetailsJson;
    private Instant calculatedAt;

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDimensionType() { return dimensionType; }
    public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }
    public String getDimensionId() { return dimensionId; }
    public void setDimensionId(String dimensionId) { this.dimensionId = dimensionId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public BigDecimal getProductCost() { return productCost; }
    public void setProductCost(BigDecimal productCost) { this.productCost = productCost; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
    public BigDecimal getFbaFee() { return fbaFee; }
    public void setFbaFee(BigDecimal fbaFee) { this.fbaFee = fbaFee; }
    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }
    public BigDecimal getAdvertisingCost() { return advertisingCost; }
    public void setAdvertisingCost(BigDecimal advertisingCost) { this.advertisingCost = advertisingCost; }
    public BigDecimal getReturnCost() { return returnCost; }
    public void setReturnCost(BigDecimal returnCost) { this.returnCost = returnCost; }
    public BigDecimal getStorageFee() { return storageFee; }
    public void setStorageFee(BigDecimal storageFee) { this.storageFee = storageFee; }
    public BigDecimal getPackagingCost() { return packagingCost; }
    public void setPackagingCost(BigDecimal packagingCost) { this.packagingCost = packagingCost; }
    public BigDecimal getCustomDuty() { return customDuty; }
    public void setCustomDuty(BigDecimal customDuty) { this.customDuty = customDuty; }
    public BigDecimal getOtherCost() { return otherCost; }
    public void setOtherCost(BigDecimal otherCost) { this.otherCost = otherCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }
    public BigDecimal getGrossMargin() { return grossMargin; }
    public void setGrossMargin(BigDecimal grossMargin) { this.grossMargin = grossMargin; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public BigDecimal getAmountInBaseCurrency() { return amountInBaseCurrency; }
    public void setAmountInBaseCurrency(BigDecimal amountInBaseCurrency) { this.amountInBaseCurrency = amountInBaseCurrency; }
    public String getCostDetailsJson() { return costDetailsJson; }
    public void setCostDetailsJson(String costDetailsJson) { this.costDetailsJson = costDetailsJson; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
