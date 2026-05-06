package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.forex.ForexService;
import com.aidotnet.erp.fms.domain.CurrencyRate;
import com.aidotnet.erp.fms.domain.CurrencyRateSyncLog;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 货币汇率服务
 * <p>
 * 描述: FMS域业务中台(5.2)，管理实时汇率获取与缓存。
 *       支持多汇率源(API/手动)，汇率同步日志(CurrencyRateSyncLog)，
 *       外汇交易记录(ForexTransaction)和汇率风险预警(ForexRiskAlert)。
 * </p>
 * <p>
 * 汇率获取策略:
 *   1. 优先从缓存获取(Redis TTL=1h)
 *   2. 缓存未命中则调用外部汇率API
 *   3. 支持手动设置汇率(覆盖API汇率)
 * </p>
 *
 * @author ERP系统
 * @see ForexRate
 * @see CurrencyRate
 * @see ForexRiskAlert
 */
@Service
public class CurrencyExchangeService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyExchangeService.class);
    private static final int SCALE = 6;

    private final FmsExtStore extStore;
    private final ForexService forexService;

    public CurrencyExchangeService(FmsExtStore extStore, ForexService forexService) {
        this.extStore = extStore;
        this.forexService = forexService;
    }

    @Transactional
    public CurrencyRate saveRate(String tenantId, SaveRateCommand command) {
        CurrencyRate rate = new CurrencyRate(
                UUID.randomUUID().toString(), tenantId, command.fromCurrency(), command.toCurrency(),
                command.rate(), command.source(), command.rateDate() != null ? command.rateDate() : LocalDate.now(),
                Instant.now());
        forexService.updateRate(command.fromCurrency(), command.toCurrency(), command.rate(), command.source());
        return extStore.saveCurrencyRate(rate);
    }

    public Optional<BigDecimal> getRate(String tenantId, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return Optional.of(BigDecimal.ONE);
        Optional<BigDecimal> cachedRate = forexService.getRate(fromCurrency, toCurrency);
        if (cachedRate.isPresent()) return cachedRate;
        List<CurrencyRate> rates = extStore.listCurrencyRates(tenantId, fromCurrency, toCurrency);
        if (!rates.isEmpty()) {
            return Optional.of(rates.get(0).rate());
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> getSnapshotRate(String tenantId, String fromCurrency, String toCurrency, LocalDate date) {
        if (fromCurrency.equals(toCurrency)) return Optional.of(BigDecimal.ONE);
        Optional<BigDecimal> cachedRate = forexService.getSnapshotRate(fromCurrency, toCurrency, date);
        if (cachedRate.isPresent()) return cachedRate;
        List<CurrencyRate> rates = extStore.listCurrencyRatesByDate(tenantId, fromCurrency, toCurrency, date);
        if (!rates.isEmpty()) {
            return Optional.of(rates.get(0).rate());
        }
        return getRate(tenantId, fromCurrency, toCurrency);
    }

    public BigDecimal convert(String tenantId, String fromCurrency, String toCurrency, BigDecimal amount) {
        if (fromCurrency.equals(toCurrency)) return amount;
        Optional<BigDecimal> rateOpt = getRate(tenantId, fromCurrency, toCurrency);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No exchange rate available: " + fromCurrency + " -> " + toCurrency);
        }
        return amount.multiply(rateOpt.get()).setScale(2, RoundingMode.HALF_UP);
    }

    public List<CurrencyRate> listRates(String tenantId, String fromCurrency, String toCurrency) {
        return extStore.listCurrencyRates(tenantId, fromCurrency, toCurrency);
    }

    public List<CurrencyRate> listRateHistory(String tenantId, String fromCurrency, String toCurrency,
                                               LocalDate startDate, LocalDate endDate) {
        return extStore.listCurrencyRateHistory(tenantId, fromCurrency, toCurrency, startDate, endDate);
    }

    @Transactional
    public CurrencyRateSyncLog syncRates(String tenantId, String source) {
        Instant startedAt = Instant.now();
        int successCount = 0;
        int failCount = 0;
        try {
            int count = forexService.syncRatesFromSources();
            successCount = count;
            CurrencyRateSyncLog syncLog = new CurrencyRateSyncLog(
                    UUID.randomUUID().toString(), tenantId, source, "SUCCESS",
                    successCount + failCount, successCount, failCount, startedAt, Instant.now(), null);
            return extStore.saveCurrencyRateSyncLog(syncLog);
        } catch (Exception e) {
            log.error("Currency rate sync failed", e);
            CurrencyRateSyncLog syncLog = new CurrencyRateSyncLog(
                    UUID.randomUUID().toString(), tenantId, source, "FAILED",
                    0, 0, 0, startedAt, Instant.now(), e.getMessage());
            return extStore.saveCurrencyRateSyncLog(syncLog);
        }
    }

    public List<CurrencyRateSyncLog> listSyncLogs(String tenantId) {
        return extStore.listCurrencyRateSyncLogs(tenantId);
    }

    public ForexService.GainLossResult calculateGainLoss(String tenantId, String baseCurrency,
                                                          String transactionCurrency, BigDecimal transactionAmount,
                                                          BigDecimal transactionRate, BigDecimal settlementRate) {
        return forexService.calculateGainLoss(baseCurrency, transactionCurrency, transactionAmount, transactionRate, settlementRate);
    }

    public record SaveRateCommand(String fromCurrency, String toCurrency, BigDecimal rate, String source, LocalDate rateDate) {}
}
