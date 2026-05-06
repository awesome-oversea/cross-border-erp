package com.aidotnet.erp.common.forex;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fms/api/v1/forex")
public class ForexController {

    private final ForexService forexService;

    public ForexController(ForexService forexService) {
        this.forexService = forexService;
    }

    @GetMapping("/rates/{from}/{to}")
    public Result<Map<String, Object>> getRate(@PathVariable String from, @PathVariable String to) {
        var rateOpt = forexService.getRate(from, to);
        if (rateOpt.isEmpty()) {
            return Result.ok(Map.of("from", from, "to", to, "rate", "N/A"));
        }
        return Result.ok(Map.of("from", from, "to", to, "rate", rateOpt.get()));
    }

    @GetMapping("/rates/{from}/{to}/snapshot")
    public Result<Map<String, Object>> getSnapshotRate(
            @PathVariable String from, @PathVariable String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var rateOpt = forexService.getSnapshotRate(from, to, date);
        return Result.ok(Map.of("from", from, "to", to, "rate", rateOpt.orElse(null), "date", date));
    }

    @GetMapping("/history")
    public Result<List<ForexService.HistoricalRate>> getHistory(
            @RequestParam String from, @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(forexService.getHistory(from, to, startDate, endDate));
    }

    @PostMapping("/convert")
    public Result<Map<String, BigDecimal>> batchConvert(@RequestBody Map<String, Object> request) {
        String from = (String) request.get("from");
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> amounts = (Map<String, BigDecimal>) request.get("amounts");
        return Result.ok(forexService.batchConvert(from, amounts));
    }

    @PostMapping("/gain-loss/calculate")
    public Result<ForexService.GainLossResult> calculateGainLoss(@RequestBody Map<String, Object> request) {
        ForexService.GainLossResult result = forexService.calculateGainLoss(
                (String) request.get("baseCurrency"),
                (String) request.get("transactionCurrency"),
                new BigDecimal(request.get("transactionAmount").toString()),
                new BigDecimal(request.get("transactionRate").toString()),
                new BigDecimal(request.get("settlementRate").toString())
        );
        return Result.ok(result);
    }

    @GetMapping("/risk-alert")
    public Result<List<ForexService.RiskAlert>> getRiskAlerts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return Result.ok(forexService.getRiskAlerts(from, to));
    }

    @PostMapping("/rates/sync")
    public Result<Map<String, Object>> syncRates() {
        int count = forexService.syncRatesFromSources();
        return Result.ok(Map.of("syncedCount", count, "status", "COMPLETED"));
    }
}
