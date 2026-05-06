package com.aidotnet.erp.fms.infrastructure.tax;

import com.aidotnet.erp.fms.domain.TaxCalculationResult;
import com.aidotnet.erp.fms.domain.VatValidationResult;
import java.math.BigDecimal;
import java.util.Map;

public interface TaxServiceProvider {

    String getProviderCode();

    String getProviderName();

    int getPriority();

    TaxCalculationResult calculateTax(String tenantId, String countryCode, String regionCode,
                                       String sellerSku, String orderId, BigDecimal taxableAmount,
                                       Map<String, String> params);

    VatValidationResult validateVat(String vatNumber, String countryCode);

    boolean isAvailable();
}
