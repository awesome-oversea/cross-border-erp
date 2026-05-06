package com.aidotnet.erp.fms.infrastructure.tax;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.TaxCalculationResult;
import com.aidotnet.erp.fms.domain.VatValidationResult;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaxServiceFacade {

    private static final Logger log = LoggerFactory.getLogger(TaxServiceFacade.class);
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final List<TaxServiceProvider> providers;
    private final ConcurrentHashMap<String, CachedTaxRate> rateCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedVatValidation> vatCache = new ConcurrentHashMap<>();

    public TaxServiceFacade(List<TaxServiceProvider> providers) {
        this.providers = providers.stream().sorted(Comparator.comparingInt(TaxServiceProvider::getPriority)).toList();
        log.info("TaxServiceFacade initialized with {} providers: {}", providers.size(),
                providers.stream().map(TaxServiceProvider::getProviderCode).toList());
    }

    public TaxCalculationResult calculateTax(String tenantId, String countryCode, String regionCode,
                                              String sellerSku, String orderId, BigDecimal taxableAmount,
                                              Map<String, String> params) {
        String cacheKey = countryCode + ":" + regionCode + ":" + (params != null ? params.getOrDefault("tax_code", "") : "");
        CachedTaxRate cached = rateCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            BigDecimal taxAmount = taxableAmount.multiply(cached.rate()).setScale(2, java.math.RoundingMode.HALF_UP);
            return new TaxCalculationResult(java.util.UUID.randomUUID().toString(), tenantId, countryCode, regionCode,
                    sellerSku, orderId, taxableAmount, cached.rate(), taxAmount, "VAT/GST", cached.provider(), Instant.now());
        }

        for (TaxServiceProvider provider : providers) {
            if (!provider.isAvailable()) {
                log.warn("Tax provider {} unavailable, skipping", provider.getProviderCode());
                continue;
            }
            try {
                TaxCalculationResult result = provider.calculateTax(tenantId, countryCode, regionCode,
                        sellerSku, orderId, taxableAmount, params);
                rateCache.put(cacheKey, new CachedTaxRate(result.taxRate(), result.serviceProvider(), Instant.now()));
                return result;
            } catch (Exception e) {
                log.warn("Tax provider {} calculation failed, trying next: {}", provider.getProviderCode(), e.getMessage());
            }
        }

        if (cached != null) {
            log.warn("All tax providers failed, using stale cached rate from {}", cached.provider());
            BigDecimal taxAmount = taxableAmount.multiply(cached.rate()).setScale(2, java.math.RoundingMode.HALF_UP);
            return new TaxCalculationResult(java.util.UUID.randomUUID().toString(), tenantId, countryCode, regionCode,
                    sellerSku, orderId, taxableAmount, cached.rate(), taxAmount, "VAT/GST", cached.provider() + "_STALE", Instant.now());
        }

        throw new BizException("TAX_SERVICE_UNAVAILABLE", "所有税务服务不可用，且无缓存数据");
    }

    public VatValidationResult validateVat(String vatNumber, String countryCode) {
        String cacheKey = vatNumber + ":" + countryCode;
        CachedVatValidation cached = vatCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return new VatValidationResult(vatNumber, countryCode, cached.valid(), cached.name(), cached.address(), cached.provider(), Instant.now());
        }

        for (TaxServiceProvider provider : providers) {
            if (!provider.isAvailable()) continue;
            try {
                VatValidationResult result = provider.validateVat(vatNumber, countryCode);
                vatCache.put(cacheKey, new CachedVatValidation(result.valid(), result.name(), result.address(), result.serviceProvider(), Instant.now()));
                return result;
            } catch (Exception e) {
                log.warn("VAT validation by {} failed, trying next: {}", provider.getProviderCode(), e.getMessage());
            }
        }

        throw new BizException("VAT_VALIDATION_UNAVAILABLE", "所有VAT验证服务不可用");
    }

    private record CachedTaxRate(BigDecimal rate, String provider, Instant cachedAt) {
        boolean isValid() { return Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0; }
    }

    private record CachedVatValidation(boolean valid, String name, String address, String provider, Instant cachedAt) {
        boolean isValid() { return Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0; }
    }
}
