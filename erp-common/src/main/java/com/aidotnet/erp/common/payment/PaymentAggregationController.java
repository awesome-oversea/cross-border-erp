package com.aidotnet.erp.common.payment;

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

@RestController("commonPaymentAggregationController")
@RequestMapping("/fms/api/v1/payment")
public class PaymentAggregationController {

    private final PaymentAggregationService paymentService;

    public PaymentAggregationController(PaymentAggregationService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public Result<PaymentAggregationService.PaymentTransaction> pay(@RequestBody Map<String, Object> request) {
        PaymentAggregationService.PaymentTransaction tx = paymentService.pay(
                (String) request.get("channelCode"),
                (String) request.get("tenantId"),
                (String) request.get("businessType"),
                (String) request.get("businessId"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.getOrDefault("currency", "USD"),
                null
        );
        return Result.ok(tx);
    }

    @PostMapping("/refund")
    public Result<PaymentAggregationService.PaymentTransaction> refund(@RequestBody Map<String, Object> request) {
        PaymentAggregationService.PaymentTransaction tx = paymentService.refund(
                (String) request.get("transactionId"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.getOrDefault("reason", "")
        );
        return Result.ok(tx);
    }

    @PostMapping("/batch-pay")
    public Result<PaymentAggregationService.BatchPayResult> batchPay(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        List<PaymentAggregationService.BatchPayItem> batchItems = items.stream()
                .map(item -> new PaymentAggregationService.BatchPayItem(
                        (String) item.get("businessType"),
                        (String) item.get("businessId"),
                        new BigDecimal(item.get("amount").toString()),
                        (String) item.getOrDefault("currency", "USD")
                ))
                .toList();
        PaymentAggregationService.BatchPayResult result = paymentService.batchPay(
                (String) request.get("tenantId"),
                batchItems,
                (String) request.get("channelCode")
        );
        return Result.ok(result);
    }

    @GetMapping("/channels")
    public Result<List<PaymentAggregationService.PaymentChannel>> getChannels() {
        return Result.ok(paymentService.listChannels());
    }

    @GetMapping("/reconciliation")
    public Result<PaymentAggregationService.ReconciliationResult> getReconciliation(
            @RequestParam String channelCode,
            @RequestParam String periodStart,
            @RequestParam String periodEnd) {
        return Result.ok(paymentService.reconcile(channelCode, periodStart, periodEnd));
    }

    @PostMapping("/settlement/withdraw")
    public Result<PaymentAggregationService.SettlementRecord> settleWithdraw(@RequestBody Map<String, Object> request) {
        PaymentAggregationService.SettlementRecord record = paymentService.settleWithdraw(
                (String) request.get("accountId"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.get("fromCurrency"),
                (String) request.get("toCurrency"),
                (String) request.getOrDefault("settlementType", "WITHDRAW")
        );
        return Result.ok(record);
    }

    @GetMapping("/balance")
    public Result<PaymentAggregationService.PaymentAccount> getBalance(@RequestParam String accountId) {
        return Result.ok(paymentService.getBalance(accountId));
    }

    @PostMapping("/amazon-claim")
    public Result<PaymentAggregationService.SettlementRecord> amazonClaim(@RequestBody Map<String, Object> request) {
        PaymentAggregationService.SettlementRecord record = paymentService.amazonClaim(
                (String) request.get("orderId"),
                new BigDecimal(request.get("claimAmount").toString()),
                (String) request.getOrDefault("reason", "")
        );
        return Result.ok(record);
    }
}
