package com.aidotnet.erp.common.forex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ForexService {

    private static final Logger log = LoggerFactory.getLogger(ForexService.class);
    private static final int SCALE = 6;
    private final Map<String, ExchangeRate> rates = new ConcurrentHashMap<>();
    private final Map<String, List<HistoricalRate>> historicalRates = new ConcurrentHashMap<>();
    private final Map<String, List<RiskAlert>> riskAlerts = new ConcurrentHashMap<>();
    private BigDecimal volatilityThreshold = new BigDecimal("0.05");

    public void updateRate(String from, String to, BigDecimal rate, String source) {
        String key = rateKey(from, to);
        ExchangeRate previous = rates.get(key);
        rates.put(key, new ExchangeRate(from, to, rate, source, LocalDate.now()));

        String histKey = historyKey(from, to);
        historicalRates.computeIfAbsent(histKey, k -> new ArrayList<>())
                .add(new HistoricalRate(from, to, rate, source, LocalDate.now()));

        if (previous != null) {
            BigDecimal change = rate.subtract(previous.rate()).divide(previous.rate(), SCALE, RoundingMode.HALF_UP).abs();
            if (change.compareTo(volatilityThreshold) > 0) {
                String alertId = "FX-ALERT-" + System.currentTimeMillis();
                RiskAlert alert = new RiskAlert(alertId, from, to, previous.rate(), rate,
                        change.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                        "HIGH", "Exchange rate volatility exceeds threshold: " + change.multiply(BigDecimal.valueOf(100)) + "%",
                        LocalDate.now());
                riskAlerts.computeIfAbsent(rateKey(from, to), k -> new ArrayList<>()).add(alert);
                log.warn("Forex risk alert: {} -> {} changed by {}%", from, to, change.multiply(BigDecimal.valueOf(100)));
            }
        }
        log.info("Updated exchange rate: {} -> {} = {} (source={})", from, to, rate, source);
    }

    public Optional<BigDecimal> getRate(String from, String to) {
        if (from.equals(to)) {
            return Optional.of(BigDecimal.ONE);
        }
        String key = rateKey(from, to);
        ExchangeRate rate = rates.get(key);
        if (rate != null) {
            return Optional.of(rate.rate());
        }
        String reverseKey = rateKey(to, from);
        ExchangeRate reverseRate = rates.get(reverseKey);
        if (reverseRate != null) {
            return Optional.of(BigDecimal.ONE.divide(reverseRate.rate(), SCALE, RoundingMode.HALF_UP));
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> getSnapshotRate(String from, String to, LocalDate date) {
        String histKey = historyKey(from, to);
        List<HistoricalRate> history = historicalRates.get(histKey);
        if (history != null) {
            Optional<HistoricalRate> snapshot = history.stream()
                    .filter(h -> !h.date().isAfter(date))
                    .reduce((first, second) -> second);
            if (snapshot.isPresent()) {
                return Optional.of(snapshot.get().rate());
            }
        }
        return getRate(from, to);
    }

    public List<HistoricalRate> getHistory(String from, String to, LocalDate startDate, LocalDate endDate) {
        String histKey = historyKey(from, to);
        List<HistoricalRate> history = historicalRates.getOrDefault(histKey, List.of());
        return history.stream()
                .filter(h -> !h.date().isBefore(startDate) && !h.date().isAfter(endDate))
                .toList();
    }

    public BigDecimal convert(String from, String to, BigDecimal amount) {
        Optional<BigDecimal> rateOpt = getRate(from, to);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No exchange rate available: " + from + " -> " + to);
        }
        return amount.multiply(rateOpt.get()).setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, BigDecimal> batchConvert(String from, Map<String, BigDecimal> amounts) {
        Map<String, BigDecimal> results = new ConcurrentHashMap<>();
        amounts.forEach((to, amount) -> results.put(to, convert(from, to, amount)));
        return results;
    }

    public GainLossResult calculateGainLoss(String baseCurrency, String transactionCurrency,
                                              BigDecimal transactionAmount, BigDecimal transactionRate,
                                              BigDecimal settlementRate) {
        BigDecimal transactionInBase = transactionAmount.multiply(transactionRate);
        BigDecimal settlementInBase = transactionAmount.multiply(settlementRate);
        BigDecimal gainLoss = settlementInBase.subtract(transactionInBase).setScale(2, RoundingMode.HALF_UP);
        String direction = gainLoss.compareTo(BigDecimal.ZERO) > 0 ? "GAIN" : gainLoss.compareTo(BigDecimal.ZERO) < 0 ? "LOSS" : "BREAKEVEN";
        return new GainLossResult(baseCurrency, transactionCurrency, transactionAmount,
                transactionRate, settlementRate, transactionInBase, settlementInBase, gainLoss, direction);
    }

    public List<RiskAlert> getRiskAlerts(String from, String to) {
        if (from != null && to != null) {
            return riskAlerts.getOrDefault(rateKey(from, to), List.of());
        }
        return riskAlerts.values().stream().flatMap(List::stream).toList();
    }

    public void setVolatilityThreshold(BigDecimal threshold) {
        this.volatilityThreshold = threshold;
        log.info("Forex volatility threshold updated to {}%", threshold.multiply(BigDecimal.valueOf(100)));
    }

    public int syncRatesFromSources() {
        int count = 0;
        log.info("Forex rate sync initiated from external sources");
        return count;
    }

    private String rateKey(String from, String to) {
        return from + "_" + to;
    }

    private String historyKey(String from, String to) {
        return from + "_" + to;
    }

    public record ExchangeRate(String from, String to, BigDecimal rate, String source, LocalDate date) {}
    public record HistoricalRate(String from, String to, BigDecimal rate, String source, LocalDate date) {}
    public record RiskAlert(String alertId, String fromCurrency, String toCurrency,
                             BigDecimal previousRate, BigDecimal currentRate,
                             BigDecimal changePercent, String level,
                             String message, LocalDate date) {}
    public record GainLossResult(String baseCurrency, String transactionCurrency,
                                  BigDecimal transactionAmount, BigDecimal transactionRate,
                                  BigDecimal settlementRate, BigDecimal transactionInBase,
                                  BigDecimal settlementInBase, BigDecimal gainLoss,
                                  String direction) {}
}
