package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.VoucherSummaryResult;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.VoucherTemplate;
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
 * FMS出站API控制器
 * <p>
 * 描述: 财务域出站接口，供其他域调用FMS能力。
 *       提供成本事件记录、利润查询、汇率查询等跨域服务。
 * </p>
 * <p>
 * 路径规范: /fms/api/out/v1 — 出站方向(out)，v1版本
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/fms/api/out/v1")
public class FmsOutboundController {

    private final InventoryVoucherEngine voucherEngine;

    public FmsOutboundController(InventoryVoucherEngine voucherEngine) {
        this.voucherEngine = voucherEngine;
    }

    @PostMapping("/payments/execute")
    public Result<Map<String, Object>> executePayment(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "PAYMENT_REQUESTED", "paymentId", request.getOrDefault("paymentId", "")));
    }

    @GetMapping("/payments/{paymentId}/status")
    public Result<Map<String, Object>> fetchPaymentStatus(@PathVariable String paymentId) {
        return Result.ok(Map.of("paymentId", paymentId, "status", "PENDING"));
    }

    @PostMapping("/settlements/report")
    public Result<Map<String, Object>> requestSettlementReport(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "REPORT_REQUESTED", "period", request.getOrDefault("period", "")));
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
}
