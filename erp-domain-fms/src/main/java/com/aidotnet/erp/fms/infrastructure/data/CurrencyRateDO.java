package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class CurrencyRateDO {
    private String rateId;
    private String tenantId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private String source;
    private LocalDate rateDate;
    private Instant syncedAt;

    public String getRateId() { return rateId; }
    public void setRateId(String rateId) { this.rateId = rateId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDate getRateDate() { return rateDate; }
    public void setRateDate(LocalDate rateDate) { this.rateDate = rateDate; }
    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }
}
