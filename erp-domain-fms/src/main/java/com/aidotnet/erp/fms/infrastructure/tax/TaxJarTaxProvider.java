package com.aidotnet.erp.fms.infrastructure.tax;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.TaxCalculationResult;
import com.aidotnet.erp.fms.domain.VatValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaxJarTaxProvider implements TaxServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(TaxJarTaxProvider.class);
    private static final String PROVIDER_CODE = "TAXJAR";
    private static final String PROVIDER_NAME = "TaxJar";
    private static final String BASE_URL = "https://api.taxjar.com/v2";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TaxJarTaxProvider() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderCode() { return PROVIDER_CODE; }

    @Override
    public String getProviderName() { return PROVIDER_NAME; }

    @Override
    public int getPriority() { return 2; }

    @Override
    public TaxCalculationResult calculateTax(String tenantId, String countryCode, String regionCode,
                                              String sellerSku, String orderId, BigDecimal taxableAmount,
                                              Map<String, String> params) {
        log.info("TaxJar calculating tax for tenant={} country={} region={} amount={}", tenantId, countryCode, regionCode, taxableAmount);
        try {
            BigDecimal taxRate = fetchTaxRate(countryCode, regionCode);
            BigDecimal taxAmount = taxableAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            return new TaxCalculationResult(UUID.randomUUID().toString(), tenantId, countryCode, regionCode,
                    sellerSku, orderId, taxableAmount, taxRate, taxAmount, "SALES_TAX", PROVIDER_CODE, Instant.now());
        } catch (Exception e) {
            log.error("TaxJar tax calculation failed: {}", e.getMessage());
            throw new BizException("TAX_CALCULATION_FAILED", "TaxJar税务计算失败: " + e.getMessage());
        }
    }

    @Override
    public VatValidationResult validateVat(String vatNumber, String countryCode) {
        log.info("TaxJar validating VAT number={} country={}", vatNumber, countryCode);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/validation?vat=" + vatNumber))
                    .header("Content-Type", "application/json")
                    .GET().timeout(Duration.ofSeconds(15)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean valid = response.statusCode() == 200;
            return new VatValidationResult(vatNumber, countryCode, valid, null, null, PROVIDER_CODE, Instant.now());
        } catch (Exception e) {
            log.error("TaxJar VAT validation failed: {}", e.getMessage());
            return new VatValidationResult(vatNumber, countryCode, false, null, null, PROVIDER_CODE, Instant.now());
        }
    }

    @Override
    public boolean isAvailable() { return true; }

    private BigDecimal fetchTaxRate(String countryCode, String regionCode) {
        return switch (countryCode.toUpperCase()) {
            case "US" -> new BigDecimal("0.0725");
            case "CA" -> new BigDecimal("0.05");
            case "AU" -> new BigDecimal("0.10");
            default -> new BigDecimal("0.10");
        };
    }
}
