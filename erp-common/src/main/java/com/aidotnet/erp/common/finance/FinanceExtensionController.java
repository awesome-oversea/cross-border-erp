package com.aidotnet.erp.common.finance;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/fms/api/v1")
public class FinanceExtensionController {

    private final FinanceExtensionService financeExtService;

    public FinanceExtensionController(FinanceExtensionService financeExtService) {
        this.financeExtService = financeExtService;
    }

    @PostMapping("/payment-applications")
    public Result<FinanceExtensionService.PaymentApplication> createPaymentApplication(@RequestBody Map<String, Object> request) {
        return Result.ok(financeExtService.createPaymentApplication(
                (String) request.get("tenantId"),
                (String) request.get("applicant"),
                (String) request.get("payee"),
                (String) request.getOrDefault("currency", "USD"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.get("businessType"),
                (String) request.get("businessId"),
                (String) request.getOrDefault("description", "")
        ));
    }

    @PatchMapping("/payment-applications/{appId}/approve")
    public Result<FinanceExtensionService.PaymentApplication> approvePaymentApplication(
            @PathVariable String appId, @RequestBody Map<String, String> request) {
        return Result.ok(financeExtService.approvePaymentApplication(appId,
                request.get("approver"), request.getOrDefault("comment", "")));
    }

    @PatchMapping("/payment-applications/{appId}/reject")
    public Result<FinanceExtensionService.PaymentApplication> rejectPaymentApplication(
            @PathVariable String appId, @RequestBody Map<String, String> request) {
        return Result.ok(financeExtService.rejectPaymentApplication(appId,
                request.get("approver"), request.getOrDefault("comment", "")));
    }

    @PatchMapping("/payment-applications/{appId}/pay")
    public Result<FinanceExtensionService.PaymentApplication> payPaymentApplication(@PathVariable String appId) {
        return Result.ok(financeExtService.payPaymentApplication(appId));
    }

    @GetMapping("/payment-applications")
    public Result<List<FinanceExtensionService.PaymentApplication>> listPaymentApplications(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status) {
        return Result.ok(financeExtService.listPaymentApplications(tenantId, status));
    }

    @PostMapping("/offsets")
    public Result<FinanceExtensionService.OffsetRecord> createOffset(@RequestBody Map<String, Object> request) {
        return Result.ok(financeExtService.createOffset(
                (String) request.get("tenantId"),
                (String) request.get("receivableId"),
                (String) request.get("payableId"),
                new BigDecimal(request.get("offsetAmount").toString()),
                (String) request.getOrDefault("offsetType", "MANUAL"),
                (String) request.getOrDefault("remark", "")
        ));
    }

    @GetMapping("/offsets")
    public Result<List<FinanceExtensionService.OffsetRecord>> listOffsets(
            @RequestParam(required = false) String tenantId) {
        return Result.ok(financeExtService.listOffsets(tenantId));
    }

    @PostMapping("/reconciliation")
    public Result<FinanceExtensionService.ReconciliationRecord> reconcile(@RequestBody Map<String, Object> request) {
        return Result.ok(financeExtService.reconcile(
                (String) request.get("tenantId"),
                (String) request.get("platform"),
                (String) request.get("period"),
                new BigDecimal(request.get("systemAmount").toString()),
                new BigDecimal(request.get("platformAmount").toString()),
                List.of()
        ));
    }

    @GetMapping("/reconciliation")
    public Result<List<FinanceExtensionService.ReconciliationRecord>> listReconciliations(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(financeExtService.listReconciliations(tenantId, platform));
    }

    @PostMapping("/platform-bills/import")
    public Result<FinanceExtensionService.PlatformBill> importPlatformBill(@RequestBody Map<String, Object> request) {
        return Result.ok(financeExtService.importPlatformBill(
                (String) request.get("tenantId"),
                (String) request.get("platform"),
                (String) request.get("billId"),
                (String) request.get("period"),
                new BigDecimal(request.get("totalAmount").toString()),
                (String) request.getOrDefault("currency", "USD"),
                List.of()
        ));
    }

    @PatchMapping("/platform-bills/{billId}/confirm")
    public Result<FinanceExtensionService.PlatformBill> confirmPlatformBill(@PathVariable String billId) {
        return Result.ok(financeExtService.confirmPlatformBill(billId));
    }

    @GetMapping("/platform-bills")
    public Result<List<FinanceExtensionService.PlatformBill>> listPlatformBills(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(financeExtService.listPlatformBills(tenantId, platform));
    }

    @PostMapping("/expenses")
    public Result<FinanceExtensionService.ExpenseRecord> createExpense(@RequestBody Map<String, Object> request) {
        return Result.ok(financeExtService.createExpense(
                (String) request.get("tenantId"),
                (String) request.get("expenseType"),
                (String) request.getOrDefault("category", "GENERAL"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.getOrDefault("currency", "USD"),
                (String) request.getOrDefault("description", ""),
                (String) request.get("applicant")
        ));
    }

    @PatchMapping("/expenses/{expenseId}/approve")
    public Result<FinanceExtensionService.ExpenseRecord> approveExpense(@PathVariable String expenseId,
                                                                         @RequestBody Map<String, String> request) {
        return Result.ok(financeExtService.approveExpense(expenseId, request.get("approver")));
    }

    @GetMapping("/expenses")
    public Result<List<FinanceExtensionService.ExpenseRecord>> listExpenses(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String expenseType) {
        return Result.ok(financeExtService.listExpenses(tenantId, expenseType));
    }
}
