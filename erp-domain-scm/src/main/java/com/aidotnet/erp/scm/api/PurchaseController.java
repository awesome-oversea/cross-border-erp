package com.aidotnet.erp.scm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.scm.application.PurchaseService;
import com.aidotnet.erp.scm.application.PurchaseService.CreatePurchaseOrderCommand;
import com.aidotnet.erp.scm.application.PurchaseService.CreateSupplierCommand;
import com.aidotnet.erp.scm.application.PurchaseService.EvaluateSupplierCommand;
import com.aidotnet.erp.scm.application.PurchaseService.GenerateSuggestionCommand;
import com.aidotnet.erp.scm.application.PurchaseService.ReceiptLine;
import com.aidotnet.erp.scm.application.PurchaseService.ReceiveCommand;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.domain.Supplier;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/suppliers")
    public Result<Supplier> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return Result.ok(purchaseService.createSupplier(currentTenant(), new CreateSupplierCommand(
                request.name(), request.companyName(), request.contactName(), request.countryCode(),
                request.creditRating(), request.leadTimeDays(), request.moq())));
    }

    @GetMapping("/suppliers")
    public Result<List<Supplier>> listSuppliers() {
        return Result.ok(purchaseService.listSuppliers(currentTenant()));
    }

    @GetMapping("/suppliers/{supplierId}")
    public Result<Supplier> getSupplier(@PathVariable String supplierId) {
        return Result.ok(purchaseService.listSuppliers(currentTenant()).stream()
                .filter(s -> s.supplierId().equals(supplierId))
                .findFirst()
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在")));
    }

    @PutMapping("/suppliers/{supplierId}")
    public Result<Supplier> updateSupplier(@PathVariable String supplierId,
                                           @Valid @RequestBody UpdateSupplierRequest request) {
        Supplier existing = purchaseService.listSuppliers(currentTenant()).stream()
                .filter(s -> s.supplierId().equals(supplierId))
                .findFirst()
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在"));
        Supplier updated = new Supplier(existing.supplierId(), existing.tenantId(),
                request.name() != null ? request.name() : existing.name(),
                request.companyName() != null ? request.companyName() : existing.companyName(),
                request.contactName() != null ? request.contactName() : existing.contactName(),
                request.countryCode() != null ? request.countryCode() : existing.countryCode(),
                request.creditRating() != null ? request.creditRating() : existing.creditRating(),
                request.leadTimeDays() > 0 ? request.leadTimeDays() : existing.leadTimeDays(),
                request.moq() > 0 ? request.moq() : existing.moq(),
                request.status() != null ? request.status() : existing.status(),
                existing.createdAt(), Instant.now());
        return Result.ok(purchaseService.updateSupplier(updated));
    }

    @PostMapping("/purchase-orders")
    public Result<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        List<PurchaseOrderLine> lines = request.lines().stream()
                .map(line -> new PurchaseOrderLine(line.lineId(), line.productId(), line.sellerSku(),
                        line.quantity(), 0, line.unitCost(),
                        line.unitCost().multiply(BigDecimal.valueOf(line.quantity())), line.expectedDate()))
                .toList();
        return Result.ok(purchaseService.createPurchaseOrder(currentTenant(), new CreatePurchaseOrderCommand(
                request.supplierId(), request.currency(), request.paymentTerms(), request.shippingTerms(),
                request.purchaseType(), request.expectedDeliveryDate(), request.notes(), lines)));
    }

    @GetMapping("/purchase-orders")
    public Result<List<PurchaseOrder>> listPurchaseOrders() {
        return Result.ok(purchaseService.listPurchaseOrders(currentTenant()));
    }

    @GetMapping("/purchase-orders/{poId}")
    public Result<PurchaseOrder> getPurchaseOrder(@PathVariable String poId) {
        return Result.ok(purchaseService.getPurchaseOrder(currentTenant(), poId));
    }

    @PatchMapping("/purchase-orders/{poId}/submit")
    public Result<PurchaseOrder> submit(@PathVariable String poId) {
        return Result.ok(purchaseService.submit(currentTenant(), poId));
    }

    @PatchMapping("/purchase-orders/{poId}/approve")
    public Result<PurchaseOrder> approve(@PathVariable String poId) {
        return Result.ok(purchaseService.approve(currentTenant(), poId));
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
    public Result<SupplierEvaluation> evaluateSupplier(@PathVariable String supplierId, @Valid @RequestBody EvaluateSupplierRequest request) {
        return Result.ok(purchaseService.evaluateSupplier(currentTenant(), new EvaluateSupplierCommand(supplierId,
                request.qualityScore(), request.deliveryScore(), request.priceScore(), request.serviceScore(), request.comment())));
    }

    @GetMapping("/suppliers/{supplierId}/evaluations")
    public Result<List<SupplierEvaluation>> listSupplierEvaluations(@PathVariable String supplierId) {
        return Result.ok(purchaseService.listSupplierEvaluations(currentTenant(), supplierId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateSupplierRequest(@NotBlank String name, String companyName, String contactName,
                                        @NotBlank String countryCode, String creditRating,
                                        int leadTimeDays, int moq) {}

    public record UpdateSupplierRequest(String name, String companyName, String contactName,
                                        String countryCode, String creditRating,
                                        int leadTimeDays, int moq, String status) {}

    public record CreatePurchaseOrderRequest(@NotBlank String supplierId, @NotBlank String currency,
                                             String paymentTerms, String shippingTerms, String purchaseType,
                                             Instant expectedDeliveryDate, String notes,
                                             @NotEmpty List<@Valid PoLineRequest> lines) {}

    public record PoLineRequest(String lineId, String productId, @NotBlank String sellerSku,
                                @Positive int quantity, @NotNull @Positive BigDecimal unitCost,
                                Instant expectedDate) {}

    public record ReceiveRequest(@NotBlank String warehouseId, @NotEmpty List<@Valid ReceiptLineRequest> receipts) {}

    public record ReceiptLineRequest(@NotBlank String lineId, @Positive int quantity) {}

    public record GenerateSuggestionRequest(@NotBlank String sellerSku, @NotBlank String warehouseId,
                                            @Positive int currentStock, @Positive int avgDailySales,
                                            @Positive int leadTimeDays) {}

    public record EvaluateSupplierRequest(@NotNull @Positive BigDecimal qualityScore, @NotNull @Positive BigDecimal deliveryScore,
                                          @NotNull @Positive BigDecimal priceScore, @NotNull @Positive BigDecimal serviceScore,
                                          String comment) {}
}
