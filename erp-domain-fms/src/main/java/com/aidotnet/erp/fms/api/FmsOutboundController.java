package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.ExternalFinanceSyncService;
import com.aidotnet.erp.fms.application.FmsOutboundService;
import com.aidotnet.erp.fms.application.FmsOutboundService.ExecutePaymentCommand;
import com.aidotnet.erp.fms.application.FmsOutboundService.PaymentExecutionResult;
import com.aidotnet.erp.fms.application.FmsOutboundService.ProfitReportQuery;
import com.aidotnet.erp.fms.application.FmsOutboundService.ProfitReportResult;
import com.aidotnet.erp.fms.application.FmsOutboundService.SettlementReportQuery;
import com.aidotnet.erp.fms.application.FmsOutboundService.SettlementReportResult;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.VoucherSummaryResult;
import com.aidotnet.erp.fms.domain.ExternalFinanceVoucher;
import com.aidotnet.erp.fms.domain.Voucher;
import java.time.Instant;
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
 * FMS出站API控制器
 * <p>
 * 描述: 财务域对其他领域/中台开放的出站能力入口。
 * 1. 支付执行与支付状态查询复用付款申请领域模型，保证状态口径一致。
 * 2. 结算报表接口直接返回真实平台结算汇总，供BI、运营、结算协同场景复用。
 * 3. 凭证相关接口继续承担财务凭证查询与外部财务系统推送能力。
 * </p>
 * <p>
 * 路径规范: /fms/api/out/v1
 * </p>
 */
@RestController
@RequestMapping("/fms/api/out/v1")
public class FmsOutboundController {

    private final InventoryVoucherEngine voucherEngine;
    private final ExternalFinanceSyncService externalFinanceSyncService;
    private final FmsOutboundService fmsOutboundService;

    public FmsOutboundController(InventoryVoucherEngine voucherEngine,
                                 ExternalFinanceSyncService externalFinanceSyncService,
                                 FmsOutboundService fmsOutboundService) {
        this.voucherEngine = voucherEngine;
        this.externalFinanceSyncService = externalFinanceSyncService;
        this.fmsOutboundService = fmsOutboundService;
    }

    @PostMapping("/payments/execute")
    public Result<PaymentExecutionResult> executePayment(@RequestBody ExecutePaymentRequest request) {
        return Result.ok(fmsOutboundService.executePayment(currentTenant(),
                new ExecutePaymentCommand(request.paymentId(), request.requestId(), request.paidBy(), request.paidAt())));
    }

    @GetMapping("/payments/{paymentId}/status")
    public Result<PaymentExecutionResult> fetchPaymentStatus(@PathVariable String paymentId) {
        return Result.ok(fmsOutboundService.fetchPaymentStatus(currentTenant(), paymentId));
    }

    @PostMapping("/settlements/report")
    public Result<SettlementReportResult> requestSettlementReport(@RequestBody SettlementReportRequest request) {
        return Result.ok(fmsOutboundService.buildSettlementReport(currentTenant(),
                new SettlementReportQuery(request.period(), request.periodStart(), request.periodEnd(),
                        request.platform(), request.store(), request.status())));
    }

    @PostMapping("/profits/report")
    public Result<ProfitReportResult> requestProfitReport(@RequestBody ProfitReportRequest request) {
        return Result.ok(fmsOutboundService.buildProfitReport(currentTenant(),
                new ProfitReportQuery(request.dimensionType(), request.dimensionId(), request.sellerSku(),
                        request.storeId(), request.marketplaceId(), request.currency(), request.alertStatus())));
    }

    @GetMapping("/vouchers")
    public Result<List<Voucher>> listVouchers(
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String status) {
        return Result.ok(voucherEngine.queryVouchers(currentTenant(), voucherType, status));
    }

    @GetMapping("/vouchers/{voucherId}")
    public Result<Voucher> getVoucher(@PathVariable String voucherId) {
        return Result.ok(voucherEngine.getVoucher(currentTenant(), voucherId));
    }

    @GetMapping("/external-vouchers")
    public Result<List<ExternalFinanceVoucher>> listExternalVouchers(
            @RequestParam(required = false) String financeSystem,
            @RequestParam(required = false) String syncStatus) {
        return Result.ok(externalFinanceSyncService.listVouchers(currentTenant(), syncStatus).stream()
                .filter(v -> financeSystem == null || financeSystem.isBlank() || financeSystem.equals(v.financeSystem()))
                .toList());
    }

    @GetMapping("/external-vouchers/{voucherId}")
    public Result<ExternalFinanceVoucher> getExternalVoucher(@PathVariable String voucherId) {
        return Result.ok(externalFinanceSyncService.findVoucher(currentTenant(), voucherId));
    }

    @PostMapping("/external-vouchers/{voucherId}/retry")
    public Result<ExternalFinanceVoucher> retryExternalVoucher(@PathVariable String voucherId) {
        return Result.ok(voucherEngine.retryExternalVoucher(currentTenant(), voucherId));
    }

    @PostMapping("/vouchers/{voucherId}/push-kingdee")
    public Result<Boolean> pushToKingdee(@PathVariable String voucherId) {
        return Result.ok(voucherEngine.pushToKingdee(currentTenant(), voucherId));
    }

    @PostMapping("/vouchers/{voucherId}/push-yonyou")
    public Result<Boolean> pushToYonyou(@PathVariable String voucherId) {
        return Result.ok(voucherEngine.pushToYonyou(currentTenant(), voucherId));
    }

    @PostMapping("/vouchers/batch-push-kingdee")
    public Result<Integer> batchPushToKingdee(@RequestBody List<String> voucherIds) {
        return Result.ok(voucherEngine.batchPushToKingdee(currentTenant(), voucherIds));
    }

    @PostMapping("/vouchers/batch-push-yonyou")
    public Result<Integer> batchPushToYonyou(@RequestBody List<String> voucherIds) {
        return Result.ok(voucherEngine.batchPushToYonyou(currentTenant(), voucherIds));
    }

    @GetMapping("/vouchers/export")
    public Result<List<Map<String, Object>>> exportVouchers(
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "JSON") String format) {
        return Result.ok(voucherEngine.exportVouchers(currentTenant(), voucherType, status, format));
    }

    @GetMapping("/vouchers/summary")
    public Result<VoucherSummaryResult> getVoucherSummary(
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd) {
        return Result.ok(voucherEngine.getVoucherSummary(currentTenant(), periodStart, periodEnd));
    }

    @GetMapping("/vouchers/by-reference")
    public Result<List<Voucher>> queryByReference(
            @RequestParam String referenceType,
            @RequestParam String referenceId) {
        return Result.ok(voucherEngine.queryVouchersByReference(currentTenant(), referenceType, referenceId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record ExecutePaymentRequest(String paymentId, String requestId, String paidBy, Instant paidAt) {}

    public record SettlementReportRequest(String period,
                                          String periodStart,
                                          String periodEnd,
                                          String platform,
                                          String store,
                                          String status) {}

    public record ProfitReportRequest(String dimensionType,
                                      String dimensionId,
                                      String sellerSku,
                                      String storeId,
                                      String marketplaceId,
                                      String currency,
                                      String alertStatus) {}
}
