package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.payment.PaymentAggregationService;
import com.aidotnet.erp.common.payment.PaymentAggregationService.BatchPayItem;
import com.aidotnet.erp.common.payment.PaymentAggregationService.BatchPayResult;
import com.aidotnet.erp.common.payment.PaymentAggregationService.PaymentAccount;
import com.aidotnet.erp.common.payment.PaymentAggregationService.PaymentChannel;
import com.aidotnet.erp.common.payment.PaymentAggregationService.PaymentTransaction;
import com.aidotnet.erp.common.payment.PaymentAggregationService.ReconciliationResult;
import com.aidotnet.erp.common.payment.PaymentAggregationService.SettlementRecord;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

/**
 * FMS支付聚合控制器
 * <p>
 * 描述: 财务域支付聚合接口，对接国内外支付平台(Stripe/PayPal/PingPong/连连)。
 *       提供支付请求、支付审批、支付记录查询等操作。
 * </p>
 * <p>
 * 路径规范: /fms/api/in/v1/payment 与 /fms/api/v1/payment
 * 描述: 保留原有 in 路径兼容，同时为 ERP 内部 14 域直连提供统一 v1 路径。
 * </p>
 *
 * @author ERP系统
 * @see FinanceService
 * @see ExternalFinanceSyncService
 */
@RestController("fmsPaymentAggregationController")
@RequestMapping({"/fms/api/in/v1/payment", "/fms/api/v1/payment"})
public class PaymentAggregationController {

    private final PaymentAggregationService paymentService;

    public PaymentAggregationController(PaymentAggregationService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/channels/register")
    public Result<PaymentChannel> registerChannel(@Valid @RequestBody RegisterChannelRequest request) {
        paymentService.registerChannel(request.channelCode(), request.channelName(),
                request.channelType(), request.config());
        return Result.ok(paymentService.listChannels().stream()
                .filter(c -> c.channelCode().equals(request.channelCode()))
                .findFirst().orElse(null));
    }

    @GetMapping("/channels")
    public Result<List<PaymentChannel>> listChannels() {
        return Result.ok(paymentService.listChannels());
    }

    @PostMapping("/pay")
    public Result<PaymentTransaction> pay(@Valid @RequestBody PayRequest request) {
        return Result.ok(paymentService.pay(request.channelCode(), currentTenant(),
                request.businessType(), request.businessId(), request.amount(),
                request.currency(), request.payParams()));
    }

    @PostMapping("/refund")
    public Result<PaymentTransaction> refund(@Valid @RequestBody RefundRequest request) {
        return Result.ok(paymentService.refund(request.transactionId(), request.refundAmount(), request.reason()));
    }

    @PostMapping("/batch-pay")
    public Result<BatchPayResult> batchPay(@Valid @RequestBody BatchPayRequest request) {
        return Result.ok(paymentService.batchPay(currentTenant(), request.items(), request.channelCode()));
    }

    @PostMapping("/accounts/register")
    public Result<PaymentAccount> registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
        return Result.ok(paymentService.registerAccount(request.channelCode(), request.accountId(),
                request.accountName(), request.currency(), request.credentials()));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public Result<PaymentAccount> getBalance(@PathVariable String accountId) {
        return Result.ok(paymentService.getBalance(accountId));
    }

    @PostMapping("/settle/withdraw")
    public Result<SettlementRecord> settleWithdraw(@Valid @RequestBody WithdrawRequest request) {
        return Result.ok(paymentService.settleWithdraw(request.accountId(), request.amount(),
                request.fromCurrency(), request.toCurrency(), request.settlementType()));
    }

    @PostMapping("/settle/amazon-claim")
    public Result<SettlementRecord> amazonClaim(@Valid @RequestBody AmazonClaimRequest request) {
        return Result.ok(paymentService.amazonClaim(request.orderId(), request.claimAmount(), request.reason()));
    }

    @GetMapping("/reconcile")
    public Result<ReconciliationResult> reconcile(@RequestParam String channelCode,
                                                   @RequestParam String periodStart,
                                                   @RequestParam String periodEnd) {
        return Result.ok(paymentService.reconcile(channelCode, periodStart, periodEnd));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record RegisterChannelRequest(@NotBlank String channelCode, @NotBlank String channelName,
                                         @NotBlank String channelType, Map<String, String> config) {}
    public record PayRequest(@NotBlank String channelCode, @NotBlank String businessType,
                             @NotBlank String businessId, @Positive BigDecimal amount,
                             @NotBlank String currency, Map<String, String> payParams) {}
    public record RefundRequest(@NotBlank String transactionId, @Positive BigDecimal refundAmount,
                                String reason) {}
    public record BatchPayRequest(@NotBlank String channelCode, List<BatchPayItem> items) {}
    public record RegisterAccountRequest(@NotBlank String channelCode, @NotBlank String accountId,
                                         @NotBlank String accountName, @NotBlank String currency,
                                         Map<String, String> credentials) {}
    public record WithdrawRequest(@NotBlank String accountId, @Positive BigDecimal amount,
                                  @NotBlank String fromCurrency, @NotBlank String toCurrency,
                                  @NotBlank String settlementType) {}
    public record AmazonClaimRequest(@NotBlank String orderId, @Positive BigDecimal claimAmount,
                                     String reason) {}
}
