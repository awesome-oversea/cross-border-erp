package com.aidotnet.erp.scm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.scm.application.PurchasePlanningService;
import com.aidotnet.erp.scm.application.PurchasePlanningService.CreatePurchaseOrderFromPlanCommand;
import com.aidotnet.erp.scm.application.PurchasePlanningService.SelectedPlanLineCommand;
import com.aidotnet.erp.scm.application.PurchaseService;
import com.aidotnet.erp.scm.application.PurchaseService.CreatePaymentRequestFromPoCommand;
import com.aidotnet.erp.scm.application.PurchaseService.CreatePurchaseOrderCommand;
import com.aidotnet.erp.scm.application.PurchaseService.CreateSupplierCommand;
import com.aidotnet.erp.scm.application.PurchaseService.EvaluateSupplierCommand;
import com.aidotnet.erp.scm.application.PurchaseService.GenerateSuggestionCommand;
import com.aidotnet.erp.scm.application.PurchaseService.ReceiptLine;
import com.aidotnet.erp.scm.application.PurchaseService.ReceiveCommand;
import com.aidotnet.erp.scm.application.PurchaseService.SupplierContactCommand;
import com.aidotnet.erp.scm.application.PurchaseService.SupplierQualificationCommand;
import com.aidotnet.erp.scm.application.PurchaseService.SupplierScoreCommand;
import com.aidotnet.erp.scm.application.PurchaseService.UpdateSupplierCommand;
import com.aidotnet.erp.scm.application.PurchaseTrackingService;
import com.aidotnet.erp.scm.application.ScmExtService;
import com.aidotnet.erp.scm.application.ScmExtService.ApproveCommand;
import com.aidotnet.erp.scm.application.ScmExtService.CreateApprovalFlowCommand;
import com.aidotnet.erp.scm.client.FmsClient;
import com.aidotnet.erp.scm.domain.PurchaseApproval;
import com.aidotnet.erp.scm.domain.PurchaseException;
import com.aidotnet.erp.scm.domain.PurchaseExceptionStatus;
import com.aidotnet.erp.scm.domain.PurchaseExceptionType;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchasePlan;
import com.aidotnet.erp.scm.domain.PurchaseTracking;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.domain.SupplierProfile;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购管理控制器
 * <p>
 * 描述: SCM域核心REST API，提供供应商管理、采购单管理、补货建议等接口。
 *       路径前缀: /scm/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/scm/api/in/v1")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseTrackingService purchaseTrackingService;
    private final ScmExtService scmExtService;
    private final PurchasePlanningService purchasePlanningService;

    public PurchaseController(
            PurchaseService purchaseService,
            PurchaseTrackingService purchaseTrackingService,
            ScmExtService scmExtService,
            PurchasePlanningService purchasePlanningService) {
        this.purchaseService = purchaseService;
        this.purchaseTrackingService = purchaseTrackingService;
        this.scmExtService = scmExtService;
        this.purchasePlanningService = purchasePlanningService;
    }

    @PostMapping("/suppliers")
    public Result<SupplierProfile> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return Result.ok(purchaseService.createSupplier(currentTenant(), new CreateSupplierCommand(
                request.name(),
                request.companyName(),
                request.contactName(),
                request.countryCode(),
                request.creditRating(),
                request.leadTimeDays(),
                request.moq(),
                toContactCommands(request.contacts()),
                toQualificationCommands(request.qualifications()),
                toScoreCommand(request.score()))));
    }

    @GetMapping("/suppliers")
    public Result<List<SupplierProfile>> listSuppliers() {
        return Result.ok(purchaseService.listSupplierProfiles(currentTenant()));
    }

    @GetMapping("/suppliers/{supplierId}")
    public Result<SupplierProfile> getSupplier(@PathVariable String supplierId) {
        return Result.ok(purchaseService.getSupplierProfile(currentTenant(), supplierId));
    }

    @PutMapping("/suppliers/{supplierId}")
    public Result<SupplierProfile> updateSupplier(
            @PathVariable String supplierId, @Valid @RequestBody UpdateSupplierRequest request) {
        return Result.ok(purchaseService.updateSupplier(currentTenant(), supplierId, new UpdateSupplierCommand(
                request.name(),
                request.companyName(),
                request.contactName(),
                request.countryCode(),
                request.creditRating(),
                request.leadTimeDays(),
                request.moq(),
                request.status(),
                toContactCommands(request.contacts()),
                toQualificationCommands(request.qualifications()),
                toScoreCommand(request.score()))));
    }

    @PostMapping("/purchase-orders")
    public Result<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        List<PurchaseOrderLine> lines = request.lines().stream()
                .map(line -> new PurchaseOrderLine(
                        line.lineId(),
                        line.productId(),
                        line.sellerSku(),
                        line.quantity(),
                        0,
                        line.unitCost(),
                        line.unitCost().multiply(BigDecimal.valueOf(line.quantity())),
                        line.expectedDate()))
                .toList();
        return Result.ok(purchaseService.createPurchaseOrder(currentTenant(), new CreatePurchaseOrderCommand(
                request.supplierId(),
                request.currency(),
                request.paymentTerms(),
                request.shippingTerms(),
                request.purchaseType(),
                request.expectedDeliveryDate(),
                request.notes(),
                lines)));
    }

    @GetMapping("/purchase-orders")
    public Result<List<PurchaseOrder>> listPurchaseOrders() {
        return Result.ok(purchaseService.listPurchaseOrders(currentTenant()));
    }

    @GetMapping("/purchase-orders/{poId}")
    public Result<PurchaseOrder> getPurchaseOrder(@PathVariable String poId) {
        return Result.ok(purchaseService.getPurchaseOrder(currentTenant(), poId));
    }

    @PostMapping("/purchase-plans/generate")
    public Result<PurchasePlan> generatePurchasePlan() {
        return Result.ok(purchasePlanningService.generatePlan(currentTenant()));
    }

    @GetMapping("/purchase-plans")
    public Result<List<PurchasePlan>> listPurchasePlans() {
        return Result.ok(purchasePlanningService.listPurchasePlans(currentTenant()));
    }

    @GetMapping("/purchase-plans/{planId}")
    public Result<PurchasePlan> getPurchasePlan(@PathVariable String planId) {
        return Result.ok(purchasePlanningService.getPurchasePlan(currentTenant(), planId));
    }

    @PostMapping("/purchase-plans/{planId}/purchase-orders")
    public Result<PurchaseOrder> createPurchaseOrderFromPlan(
            @PathVariable String planId, @Valid @RequestBody CreatePurchaseOrderFromPlanRequest request) {
        PurchasePlanningService.PurchaseOrderCreationResult result = purchasePlanningService.createPurchaseOrderFromPlan(
                currentTenant(),
                planId,
                new CreatePurchaseOrderFromPlanCommand(
                        request.supplierId(),
                        request.currency(),
                        request.paymentTerms(),
                        request.shippingTerms(),
                        request.purchaseType(),
                        request.expectedDeliveryDate(),
                        request.notes(),
                        request.lines().stream()
                                .map(line -> new SelectedPlanLineCommand(line.planLineId(), line.unitCost()))
                                .toList()));
        return Result.ok(result.purchaseOrder());
    }

    @PatchMapping("/purchase-orders/{poId}/submit")
    public Result<PurchaseOrder> submit(@PathVariable String poId) {
        return Result.ok(purchaseService.submit(currentTenant(), poId));
    }

    @PatchMapping("/purchase-orders/{poId}/approve")
    public Result<PurchaseOrder> approve(@PathVariable String poId) {
        return Result.ok(purchaseService.approve(currentTenant(), poId));
    }

    @PostMapping("/purchase-orders/{poId}/approvals")
    public Result<List<PurchaseApproval>> createApprovalFlow(
            @PathVariable String poId, @Valid @RequestBody CreateApprovalFlowRequest request) {
        return Result.ok(scmExtService.createApprovalFlow(currentTenant(),
                new CreateApprovalFlowCommand(
                        poId,
                        request.approvers().stream().map(ApprovalNodeRequest::approverId).toList())));
    }

    @GetMapping("/purchase-orders/{poId}/approvals")
    public Result<List<PurchaseApproval>> listApprovals(@PathVariable String poId) {
        return Result.ok(scmExtService.listApprovalsByPo(currentTenant(), poId));
    }

    @PostMapping("/purchase-orders/{poId}/approvals/{approvalId}/approve")
    public Result<PurchaseApproval> approveFlowNode(
            @PathVariable String poId,
            @PathVariable String approvalId,
            @Valid @RequestBody ApproveFlowRequest request) {
        return Result.ok(scmExtService.approvePurchase(currentTenant(), approvalId, new ApproveCommand(poId, request.comment())));
    }

    @PostMapping("/purchase-orders/{poId}/approvals/{approvalId}/reject")
    public Result<PurchaseApproval> rejectFlowNode(
            @PathVariable String poId,
            @PathVariable String approvalId,
            @Valid @RequestBody ApproveFlowRequest request) {
        return Result.ok(scmExtService.rejectPurchase(currentTenant(), approvalId, new ApproveCommand(poId, request.comment())));
    }

    @PatchMapping("/purchase-orders/{poId}/receive")
    public Result<PurchaseOrder> receive(@PathVariable String poId, @Valid @RequestBody ReceiveRequest request) {
        List<ReceiptLine> receipts = request.receipts().stream()
                .map(receipt -> new ReceiptLine(receipt.lineId(), receipt.quantity()))
                .toList();
        return Result.ok(purchaseService.receive(currentTenant(), poId, new ReceiveCommand(request.warehouseId(), receipts)));
    }

    @PatchMapping("/purchase-orders/{poId}/cancel")
    public Result<PurchaseOrder> cancel(@PathVariable String poId) {
        return Result.ok(purchaseService.cancel(currentTenant(), poId));
    }

    @PostMapping("/purchase-orders/{poId}/payment-requests")
    public Result<FmsClient.PaymentRequestResponse> createPaymentRequest(
            @PathVariable String poId, @Valid @RequestBody CreatePaymentRequestFromPoRequest request) {
        return Result.ok(purchaseService.createPaymentRequest(currentTenant(), poId,
                new CreatePaymentRequestFromPoCommand(
                        request.requestId(),
                        request.amount(),
                        request.requestedBy(),
                        request.approvalFlow())));
    }

    @GetMapping("/purchase-orders/{poId}/payment-requests")
    public Result<List<FmsClient.PaymentRequestResponse>> listPaymentRequests(@PathVariable String poId) {
        return Result.ok(purchaseService.listPaymentRequests(currentTenant(), poId));
    }

    @PostMapping("/purchase-orders/{poId}/trackings/init")
    public Result<List<PurchaseTracking>> initTrackings(@PathVariable String poId) {
        purchaseTrackingService.initTracking(currentTenant(), poId);
        return Result.ok(purchaseTrackingService.listTrackings(currentTenant(), poId));
    }

    @PostMapping("/purchase-orders/{poId}/trackings/{lineId}/receipts")
    public Result<PurchaseTracking> recordTrackingReceipt(
            @PathVariable String poId,
            @PathVariable String lineId,
            @Valid @RequestBody TrackingReceiptRequest request) {
        return Result.ok(purchaseTrackingService.recordReceipt(
                currentTenant(), poId, lineId, request.receivedQuantity(), request.actualUnitCost()));
    }

    @PostMapping("/purchase-orders/{poId}/trackings/{lineId}/damages")
    public Result<PurchaseTracking> recordTrackingDamage(
            @PathVariable String poId,
            @PathVariable String lineId,
            @Valid @RequestBody TrackingDamageRequest request) {
        return Result.ok(purchaseTrackingService.recordDamage(
                currentTenant(), poId, lineId, request.damagedQuantity(), request.reason()));
    }

    @PostMapping("/purchase-orders/{poId}/trackings/{lineId}/returns")
    public Result<PurchaseTracking> recordTrackingReturn(
            @PathVariable String poId,
            @PathVariable String lineId,
            @Valid @RequestBody TrackingReturnRequest request) {
        return Result.ok(purchaseTrackingService.recordReturn(
                currentTenant(), poId, lineId, request.returnedQuantity(), request.reason()));
    }

    @GetMapping("/purchase-orders/{poId}/trackings")
    public Result<List<PurchaseTracking>> listTrackings(@PathVariable String poId) {
        return Result.ok(purchaseTrackingService.listTrackings(currentTenant(), poId));
    }

    @PostMapping("/purchase-orders/{poId}/exceptions")
    public Result<PurchaseException> createPurchaseException(
            @PathVariable String poId, @Valid @RequestBody CreatePurchaseExceptionRequest request) {
        return Result.ok(purchaseTrackingService.createException(
                currentTenant(),
                poId,
                request.lineId(),
                request.sellerSku(),
                request.exceptionType(),
                request.expectedValue(),
                request.actualValue(),
                request.description()));
    }

    @GetMapping("/purchase-orders/{poId}/exceptions")
    public Result<List<PurchaseException>> listPurchaseExceptions(@PathVariable String poId) {
        return Result.ok(purchaseTrackingService.listExceptions(currentTenant(), poId));
    }

    @PatchMapping("/purchase-exceptions/{exceptionId}/handle")
    public Result<PurchaseException> handlePurchaseException(
            @PathVariable String exceptionId, @Valid @RequestBody HandlePurchaseExceptionRequest request) {
        return Result.ok(purchaseTrackingService.handleException(
                currentTenant(), exceptionId, request.handlerId(), request.handlerNote(), request.resolution()));
    }

    @PostMapping("/replenishment-suggestions")
    public Result<ReplenishmentSuggestion> generateSuggestion(@Valid @RequestBody GenerateSuggestionRequest request) {
        return Result.ok(purchaseService.generateSuggestion(currentTenant(), new GenerateSuggestionCommand(
                request.sellerSku(), request.warehouseId(), request.currentStock(), request.avgDailySales(), request.leadTimeDays())));
    }

    @GetMapping("/replenishment-suggestions")
    public Result<List<ReplenishmentSuggestion>> listSuggestions() {
        return Result.ok(purchaseService.listSuggestions(currentTenant()));
    }

    @GetMapping("/replenishment-suggestions/pending")
    public Result<List<ReplenishmentSuggestion>> listPendingSuggestions() {
        return Result.ok(purchaseService.listPendingSuggestions(currentTenant()));
    }

    @PatchMapping("/replenishment-suggestions/{suggestionId}/accept")
    public Result<ReplenishmentSuggestion> acceptSuggestion(@PathVariable String suggestionId) {
        return Result.ok(purchaseService.acceptSuggestion(currentTenant(), suggestionId));
    }

    @PatchMapping("/replenishment-suggestions/{suggestionId}/reject")
    public Result<ReplenishmentSuggestion> rejectSuggestion(@PathVariable String suggestionId) {
        return Result.ok(purchaseService.rejectSuggestion(currentTenant(), suggestionId));
    }

    @PostMapping("/suppliers/{supplierId}/evaluations")
    public Result<SupplierEvaluation> evaluateSupplier(
            @PathVariable String supplierId, @Valid @RequestBody EvaluateSupplierRequest request) {
        return Result.ok(purchaseService.evaluateSupplier(currentTenant(), new EvaluateSupplierCommand(
                supplierId,
                request.qualityScore(),
                request.deliveryScore(),
                request.priceScore(),
                request.serviceScore(),
                request.comment())));
    }

    @GetMapping("/suppliers/{supplierId}/evaluations")
    public Result<List<SupplierEvaluation>> listSupplierEvaluations(@PathVariable String supplierId) {
        return Result.ok(purchaseService.listSupplierEvaluations(currentTenant(), supplierId));
    }

    private List<SupplierContactCommand> toContactCommands(List<SupplierContactRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(request -> new SupplierContactCommand(
                        request.name(), request.role(), request.email(), request.phone(), request.primaryContact()))
                .toList();
    }

    private List<SupplierQualificationCommand> toQualificationCommands(List<SupplierQualificationRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(request -> new SupplierQualificationCommand(
                        request.qualificationType(),
                        request.qualificationNo(),
                        request.issuedBy(),
                        request.validFrom(),
                        request.validUntil(),
                        request.status(),
                        request.remark()))
                .toList();
    }

    private SupplierScoreCommand toScoreCommand(SupplierScoreRequest request) {
        if (request == null) {
            return null;
        }
        return new SupplierScoreCommand(
                request.qualityScore(),
                request.deliveryScore(),
                request.priceScore(),
                request.serviceScore(),
                request.evaluationCount());
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateSupplierRequest(
            @NotBlank String name,
            String companyName,
            String contactName,
            @NotBlank String countryCode,
            String creditRating,
            int leadTimeDays,
            int moq,
            List<@Valid SupplierContactRequest> contacts,
            List<@Valid SupplierQualificationRequest> qualifications,
            @Valid SupplierScoreRequest score) {}

    public record UpdateSupplierRequest(
            String name,
            String companyName,
            String contactName,
            String countryCode,
            String creditRating,
            Integer leadTimeDays,
            Integer moq,
            String status,
            List<@Valid SupplierContactRequest> contacts,
            List<@Valid SupplierQualificationRequest> qualifications,
            @Valid SupplierScoreRequest score) {}

    public record SupplierContactRequest(
            @NotBlank String name, String role, String email, String phone, boolean primaryContact) {}

    public record SupplierQualificationRequest(
            @NotBlank String qualificationType,
            @NotBlank String qualificationNo,
            String issuedBy,
            Instant validFrom,
            Instant validUntil,
            String status,
            String remark) {}

    public record SupplierScoreRequest(
            @NotNull @Positive BigDecimal qualityScore,
            @NotNull @Positive BigDecimal deliveryScore,
            @NotNull @Positive BigDecimal priceScore,
            @NotNull @Positive BigDecimal serviceScore,
            @Positive int evaluationCount) {}

    public record CreatePurchaseOrderRequest(
            @NotBlank String supplierId,
            @NotBlank String currency,
            String paymentTerms,
            String shippingTerms,
            String purchaseType,
            Instant expectedDeliveryDate,
            String notes,
            @NotEmpty List<@Valid PoLineRequest> lines) {}

    public record PoLineRequest(
            String lineId,
            String productId,
            @NotBlank String sellerSku,
            @Positive int quantity,
            @NotNull @Positive BigDecimal unitCost,
            Instant expectedDate) {}

    public record CreatePurchaseOrderFromPlanRequest(
            @NotBlank String supplierId,
            @NotBlank String currency,
            String paymentTerms,
            String shippingTerms,
            String purchaseType,
            Instant expectedDeliveryDate,
            String notes,
            @NotEmpty List<@Valid PlanPoLineRequest> lines) {}

    public record PlanPoLineRequest(@NotBlank String planLineId, @NotNull @Positive BigDecimal unitCost) {}

    public record ApprovalNodeRequest(@NotBlank String approverId) {}

    public record CreateApprovalFlowRequest(@NotEmpty List<@Valid ApprovalNodeRequest> approvers) {}

    public record ApproveFlowRequest(String comment) {}

    public record ReceiveRequest(@NotBlank String warehouseId, @NotEmpty List<@Valid ReceiptLineRequest> receipts) {}

    public record ReceiptLineRequest(@NotBlank String lineId, @Positive int quantity) {}

    public record CreatePaymentRequestFromPoRequest(
            String requestId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String requestedBy,
            @NotNull JsonNode approvalFlow) {}

    public record TrackingReceiptRequest(@Positive int receivedQuantity, BigDecimal actualUnitCost) {}

    public record TrackingDamageRequest(@Positive int damagedQuantity, @NotBlank String reason) {}

    public record TrackingReturnRequest(@Positive int returnedQuantity, @NotBlank String reason) {}

    public record CreatePurchaseExceptionRequest(
            @NotBlank String lineId,
            @NotBlank String sellerSku,
            @NotNull PurchaseExceptionType exceptionType,
            BigDecimal expectedValue,
            BigDecimal actualValue,
            @NotBlank String description) {}

    public record HandlePurchaseExceptionRequest(
            @NotBlank String handlerId, String handlerNote, @NotNull PurchaseExceptionStatus resolution) {}

    public record GenerateSuggestionRequest(
            @NotBlank String sellerSku,
            @NotBlank String warehouseId,
            @Positive int currentStock,
            @Positive int avgDailySales,
            @Positive int leadTimeDays) {}

    public record EvaluateSupplierRequest(
            @NotNull @Positive BigDecimal qualityScore,
            @NotNull @Positive BigDecimal deliveryScore,
            @NotNull @Positive BigDecimal priceScore,
            @NotNull @Positive BigDecimal serviceScore,
            String comment) {}
}
