package com.aidotnet.erp.common.profit;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fms/api/v1/profit-engine")
public class ProfitEngineController {

    private final ProfitEngineService profitEngineService;

    public ProfitEngineController(ProfitEngineService profitEngineService) {
        this.profitEngineService = profitEngineService;
    }

    @PostMapping("/calculate")
    public Result<ProfitEngineService.ProfitResult> calculate(@RequestBody Map<String, Object> request) {
        ProfitEngineService.ProfitResult result = profitEngineService.calculateOrderProfit(
                (String) request.get("orderId"),
                (String) request.getOrDefault("sku", null),
                (String) request.getOrDefault("storeId", null),
                (String) request.getOrDefault("channel", null),
                (String) request.getOrDefault("market", null),
                new BigDecimal(request.get("netRevenue").toString()),
                new BigDecimal(request.getOrDefault("purchaseCost", "0").toString()),
                new BigDecimal(request.getOrDefault("headTransport", "0").toString()),
                new BigDecimal(request.getOrDefault("platformFee", "0").toString()),
                new BigDecimal(request.getOrDefault("advertisingCost", "0").toString()),
                new BigDecimal(request.getOrDefault("paymentFee", "0").toString()),
                new BigDecimal(request.getOrDefault("tailTransport", "0").toString()),
                new BigDecimal(request.getOrDefault("taxCost", "0").toString()),
                new BigDecimal(request.getOrDefault("otherCost", "0").toString())
        );
        return Result.ok(result);
    }

    @GetMapping("/order/{id}")
    public Result<ProfitEngineService.ProfitResult> getOrderProfit(@PathVariable String id) {
        return Result.ok(null);
    }

    @GetMapping("/sku/{asin}")
    public Result<ProfitEngineService.ProfitSummary> getSkuProfit(@PathVariable String asin) {
        return Result.ok(profitEngineService.getSkuProfitSummary(asin));
    }

    @GetMapping("/store")
    public Result<ProfitEngineService.ProfitSummary> getStoreProfit(@RequestParam String storeId) {
        return Result.ok(profitEngineService.getStoreProfitSummary(storeId));
    }

    @GetMapping("/channel/{channel}")
    public Result<ProfitEngineService.ChannelProfitSummary> getChannelProfit(@PathVariable String channel) {
        return Result.ok(profitEngineService.getChannelProfitSummary(channel));
    }

    @GetMapping("/market/{market}")
    public Result<ProfitEngineService.MarketProfitSummary> getMarketProfit(@PathVariable String market) {
        return Result.ok(profitEngineService.getMarketProfitSummary(market));
    }

    @PostMapping("/custom-fee/define")
    public Result<ProfitEngineService.CustomFeeDefinition> defineCustomFee(@RequestBody Map<String, Object> request) {
        return Result.ok(profitEngineService.defineCustomFee(
                (String) request.get("feeCode"),
                (String) request.get("feeName"),
                new BigDecimal(request.getOrDefault("defaultAmount", "0").toString()),
                (String) request.getOrDefault("calculationMethod", "FIXED"),
                (String) request.getOrDefault("description", "")
        ));
    }

    @GetMapping("/custom-fee/list")
    public Result<List<ProfitEngineService.CustomFeeDefinition>> listCustomFees() {
        return Result.ok(profitEngineService.listCustomFeeDefinitions());
    }

    @PostMapping("/custom-fee/apply")
    public Result<Void> applyCustomFee(@RequestBody Map<String, Object> request) {
        profitEngineService.applyCustomFee(
                (String) request.get("orderId"),
                (String) request.get("feeCode"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.getOrDefault("remark", "")
        );
        return Result.ok(null);
    }

    @GetMapping("/custom-fee/order/{orderId}")
    public Result<List<ProfitEngineService.CustomFeeEntry>> getOrderCustomFees(@PathVariable String orderId) {
        return Result.ok(profitEngineService.getCustomFeesForOrder(orderId));
    }

    @GetMapping("/alert")
    public Result<List<ProfitEngineService.ProfitAlert>> getAlerts(
            @RequestParam(defaultValue = "5") BigDecimal threshold) {
        return Result.ok(profitEngineService.checkProfitAlerts(threshold));
    }

    @PostMapping("/ai/analyze")
    public Result<ProfitEngineService.AiProfitAnalysis> aiAnalyze(@RequestBody Map<String, Object> request) {
        return Result.ok(profitEngineService.aiAnalyze(
                (String) request.get("dimension"),
                (String) request.get("dimensionValue")
        ));
    }
}
