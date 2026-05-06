package com.aidotnet.erp.scm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.scm.application.ScmExtService;
import com.aidotnet.erp.scm.application.ScmExtService.ApproveCommand;
import com.aidotnet.erp.scm.application.ScmExtService.CreateQuoteCommand;
import com.aidotnet.erp.scm.application.ScmExtService.SubmitApprovalCommand;
import com.aidotnet.erp.scm.domain.PurchaseApproval;
import com.aidotnet.erp.scm.domain.Quote;
import com.aidotnet.erp.scm.domain.SupplierScore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SCM扩展控制器
 * <p>
 * 描述: SCM域扩展REST API，提供报价管理、审批管理、供应商评分等接口。
 *       路径前缀: /scm/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/scm/api/in/v1")
public class ScmExtController {

    private final ScmExtService scmExtService;

    /**
     * 构造函数 - 依赖注入扩展服务
     *
     * @param scmExtService SCM扩展应用服务
     */
    public ScmExtController(ScmExtService scmExtService) {
        this.scmExtService = scmExtService;
    }

    @PostMapping("/quotes")
    public Result<Quote> createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        return Result.ok(scmExtService.createQuote(currentTenant(), new CreateQuoteCommand(
                request.supplierId(), request.sellerSku(), request.currency(), request.unitPrice(),
                request.quotedQuantity(), request.leadTimeDays(), request.validUntil(), request.remark())));
    }

    @PostMapping("/quotes/{quoteId}/submit")
    public Result<Quote> submitQuote(@PathVariable String quoteId) {
        return Result.ok(scmExtService.submitQuote(currentTenant(), quoteId));
    }

    @PostMapping("/quotes/{quoteId}/accept")
    public Result<Quote> acceptQuote(@PathVariable String quoteId) {
        return Result.ok(scmExtService.acceptQuote(currentTenant(), quoteId));
    }

    @PostMapping("/quotes/{quoteId}/reject")
    public Result<Quote> rejectQuote(@PathVariable String quoteId) {
        return Result.ok(scmExtService.rejectQuote(currentTenant(), quoteId));
    }

    @GetMapping("/quotes/compare")
    public Result<List<Quote>> compareQuotes(@RequestParam String sellerSku) {
        return Result.ok(scmExtService.compareQuotes(currentTenant(), sellerSku));
    }

    @GetMapping("/quotes")
    public Result<List<Quote>> listQuotesBySupplier(@RequestParam String supplierId) {
        return Result.ok(scmExtService.listQuotesBySupplier(currentTenant(), supplierId));
    }

    @GetMapping("/quotes/{quoteId}")
    public Result<Quote> getQuote(@PathVariable String quoteId) {
        return Result.ok(scmExtService.getQuote(currentTenant(), quoteId));
    }

    @PostMapping("/supplier-scores/{supplierId}/recalculate")
    public Result<SupplierScore> recalculateSupplierScore(@PathVariable String supplierId) {
        return Result.ok(scmExtService.recalculateSupplierScore(currentTenant(), supplierId));
    }

    @GetMapping("/supplier-scores/{supplierId}")
    public Result<SupplierScore> getSupplierScore(@PathVariable String supplierId) {
        return Result.ok(scmExtService.getSupplierScore(currentTenant(), supplierId));
    }

    @PostMapping("/purchase-approvals")
    public Result<PurchaseApproval> submitForApproval(@Valid @RequestBody SubmitApprovalRequest request) {
        return Result.ok(scmExtService.submitForApproval(currentTenant(),
                new SubmitApprovalCommand(request.poId(), request.approverId())));
    }

    @PostMapping("/purchase-approvals/{approvalId}/approve")
    public Result<PurchaseApproval> approvePurchase(@PathVariable String approvalId,
                                                    @Valid @RequestBody ApproveRequest request) {
        return Result.ok(scmExtService.approvePurchase(currentTenant(), approvalId,
                new ApproveCommand(request.poId(), request.comment())));
    }

    @PostMapping("/purchase-approvals/{approvalId}/reject")
    public Result<PurchaseApproval> rejectPurchase(@PathVariable String approvalId,
                                                   @Valid @RequestBody ApproveRequest request) {
        return Result.ok(scmExtService.rejectPurchase(currentTenant(), approvalId,
                new ApproveCommand(request.poId(), request.comment())));
    }

    @GetMapping("/purchase-approvals")
    public Result<List<PurchaseApproval>> listApprovalsByPo(@RequestParam String poId) {
        return Result.ok(scmExtService.listApprovalsByPo(currentTenant(), poId));
    }

    @GetMapping("/purchase-approvals/pending")
    public Result<List<PurchaseApproval>> listPendingApprovals(@RequestParam String approverId) {
        return Result.ok(scmExtService.listPendingApprovals(currentTenant(), approverId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateQuoteRequest(@NotBlank String supplierId, @NotBlank String sellerSku, String currency,
                                     @Positive BigDecimal unitPrice, @Positive int quotedQuantity,
                                     int leadTimeDays, Instant validUntil, String remark) {}
    public record SubmitApprovalRequest(@NotBlank String poId, @NotBlank String approverId) {}
    public record ApproveRequest(@NotBlank String poId, String comment) {}
}
