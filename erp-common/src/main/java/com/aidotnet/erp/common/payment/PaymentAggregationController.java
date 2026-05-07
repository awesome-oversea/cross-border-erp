package com.aidotnet.erp.common.payment;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.payment.PaymentAggregationService.BatchPayItem;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("commonPaymentAggregationController")
@RequestMapping("/platform/fms/api/v1/payment")
public class PaymentAggregationController {

    private final PaymentAggregationService paymentService;

    public PaymentAggregationController(PaymentAggregationService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/channels/register")
    public Result<PaymentAggregationService.PaymentChannel> registerChannel(
            @Valid @RequestBody RegisterChannelRequest request) {
        return inTenant(request.tenantId(), () -> {
            paymentService.registerChannel(request.channelCode(), request.channelName(),
                    request.channelType(), request.config());
            return Result.ok(paymentService.listChannels().stream()
                    .filter(channel -> channel.channelCode().equals(request.channelCode()))
                    .findFirst()
                    .orElse(null));
        });
    }

    @PostMapping("/accounts/register")
    public Result<PaymentAggregationService.PaymentAccount> registerAccount(
            @Valid @RequestBody RegisterAccountRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(paymentService.registerAccount(
                request.channelCode(), request.accountId(), request.accountName(),
                request.currency(), request.credentials())));
    }

    @PostMapping("/pay")
    public Result<PaymentAggregationService.PaymentTransaction> pay(@Valid @RequestBody PayRequest request) {
        PaymentAggregationService.PaymentTransaction tx = paymentService.pay(
                request.channelCode(),
                resolveTenantId(request.tenantId()),
                request.businessType(),
                request.businessId(),
                request.amount(),
                request.currency(),
                request.payParams());
        return Result.ok(tx);
    }

    @PostMapping("/refund")
    public Result<PaymentAggregationService.PaymentTransaction> refund(@Valid @RequestBody RefundRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(paymentService.refund(
                request.transactionId(), request.amount(), request.reason())));
    }

    @PostMapping("/batch-pay")
    public Result<PaymentAggregationService.BatchPayResult> batchPay(@Valid @RequestBody BatchPayRequest request) {
        List<BatchPayItem> batchItems = request.items().stream()
                .map(item -> new BatchPayItem(item.businessType(), item.businessId(),
                        item.amount(), item.currency()))
                .toList();
        return Result.ok(paymentService.batchPay(resolveTenantId(request.tenantId()),
                batchItems, request.channelCode()));
    }

    @GetMapping("/channels")
    public Result<List<PaymentAggregationService.PaymentChannel>> getChannels(
            @RequestParam(required = false) String tenantId) {
        return inTenant(tenantId, () -> Result.ok(paymentService.listChannels()));
    }

    @GetMapping("/reconciliation")
    public Result<PaymentAggregationService.ReconciliationResult> getReconciliation(
            @RequestParam(required = false) String tenantId,
            @RequestParam String channelCode,
            @RequestParam String periodStart,
            @RequestParam String periodEnd) {
        return inTenant(tenantId, () -> Result.ok(paymentService.reconcile(channelCode, periodStart, periodEnd)));
    }

    @PostMapping("/settlement/withdraw")
    public Result<PaymentAggregationService.SettlementRecord> settleWithdraw(
            @Valid @RequestBody WithdrawRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(paymentService.settleWithdraw(
                request.accountId(), request.amount(), request.fromCurrency(),
                request.toCurrency(), request.settlementType())));
    }

    @GetMapping("/balance")
    public Result<PaymentAggregationService.PaymentAccount> getBalance(
            @RequestParam(required = false) String tenantId,
            @RequestParam String accountId) {
        return inTenant(tenantId, () -> Result.ok(paymentService.getBalance(accountId)));
    }

    @PostMapping("/amazon-claim")
    public Result<PaymentAggregationService.SettlementRecord> amazonClaim(
            @Valid @RequestBody AmazonClaimRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(paymentService.amazonClaim(
                request.orderId(), request.claimAmount(), request.reason())));
    }

    /**
     * Shared platform APIs accept explicit tenantId so internal services can call them
     * directly even when no gateway filter has populated the tenant context.
     */
    private <T> T inTenant(String tenantId, Supplier<T> action) {
        String previousTenantId = TenantContext.getTenantId();
        String resolvedTenantId = resolveTenantId(tenantId);
        TenantContext.setTenantId(resolvedTenantId);
        try {
            return action.get();
        } finally {
            TenantContext.setTenantId(previousTenantId);
        }
    }

    private String resolveTenantId(String tenantId) {
        String resolved = hasText(tenantId) ? tenantId.trim() : TenantContext.getTenantId();
        if (!hasText(resolved)) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return resolved.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record RegisterChannelRequest(String tenantId,
                                         @NotBlank String channelCode,
                                         @NotBlank String channelName,
                                         @NotBlank String channelType,
                                         Map<String, String> config) {}

    public record RegisterAccountRequest(String tenantId,
                                         @NotBlank String channelCode,
                                         @NotBlank String accountId,
                                         @NotBlank String accountName,
                                         @NotBlank String currency,
                                         Map<String, String> credentials) {}

    public record PayRequest(String tenantId,
                             @NotBlank String channelCode,
                             @NotBlank String businessType,
                             @NotBlank String businessId,
                             @Positive BigDecimal amount,
                             @NotBlank String currency,
                             Map<String, String> payParams) {}

    public record RefundRequest(String tenantId,
                                @NotBlank String transactionId,
                                @Positive BigDecimal amount,
                                String reason) {}

    public record BatchPayRequest(String tenantId,
                                  @NotBlank String channelCode,
                                  List<BatchPayItemRequest> items) {}

    public record BatchPayItemRequest(@NotBlank String businessType,
                                      @NotBlank String businessId,
                                      @Positive BigDecimal amount,
                                      @NotBlank String currency) {}

    public record WithdrawRequest(String tenantId,
                                  @NotBlank String accountId,
                                  @Positive BigDecimal amount,
                                  @NotBlank String fromCurrency,
                                  @NotBlank String toCurrency,
                                  @NotBlank String settlementType) {}

    public record AmazonClaimRequest(String tenantId,
                                     @NotBlank String orderId,
                                     @Positive BigDecimal claimAmount,
                                     String reason) {}
}
