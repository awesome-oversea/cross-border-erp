package com.aidotnet.erp.fba.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fba.application.FbaShipmentService;
import com.aidotnet.erp.fba.application.FbaShipmentService.AddToCartCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.CalculateRestockCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.ConfirmCartCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.CreateCartonLabelCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.CreateFbaShipmentCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.CreateInboundPlanCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.CreateLocationCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.CreatePlanCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.PackFbaShipmentCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.ReceiveFbaShipmentCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.RecordExceptionCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.ResolveExceptionCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.ShipFbaShipmentCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.ShipmentItemCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.SplitInboundPlanCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.SplitInboundPlanResult;
import com.aidotnet.erp.fba.application.FbaShipmentService.SyncInventoryCommand;
import com.aidotnet.erp.fba.application.FbaShipmentService.UpdateInboundPlanCommand;
import com.aidotnet.erp.fba.domain.CartonLabel;
import com.aidotnet.erp.fba.domain.FbaInboundPlan;
import com.aidotnet.erp.fba.domain.FbaInventory;
import com.aidotnet.erp.fba.domain.FbaLocation;
import com.aidotnet.erp.fba.domain.FbaShipment;
import com.aidotnet.erp.fba.domain.FbaShipmentItem;
import com.aidotnet.erp.fba.domain.ReplenishmentPlan;
import com.aidotnet.erp.fba.domain.RestockCartItem;
import com.aidotnet.erp.fba.domain.RestockSuggestion;
import com.aidotnet.erp.fba.domain.ShipmentException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * FBA发货管理控制器
 * <p>
 * 描述: FBA域核心REST API，提供入库计划、发货单、FBA库存、补货计划、补货建议等接口。
 *       路径前缀: /fba/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping({"/fba/api/in/v1", "/api/fba"})
public class FbaShipmentController {

    private final FbaShipmentService fbaShipmentService;

    /**
     * 构造函数 - 依赖注入FBA发货服务
     *
     * @param fbaShipmentService FBA发货应用服务
     */
    public FbaShipmentController(FbaShipmentService fbaShipmentService) {
        this.fbaShipmentService = fbaShipmentService;
    }

    @PostMapping("/inbound-plans")
    public Result<FbaInboundPlan> createInboundPlan(@Valid @RequestBody CreateInboundPlanRequest request) {
        return Result.ok(fbaShipmentService.createInboundPlan(currentTenant(), new CreateInboundPlanCommand(
                request.warehouseId(),
                request.planName(),
                request.sellerSku(),
                request.plannedQuantity(),
                request.storeId(),
                request.siteCode())));
    }

    @PatchMapping("/inbound-plans/{planId}")
    public Result<FbaInboundPlan> updateInboundPlan(@PathVariable String planId,
                                                    @Valid @RequestBody UpdateInboundPlanRequest request) {
        return Result.ok(fbaShipmentService.updateInboundPlan(currentTenant(), planId, new UpdateInboundPlanCommand(
                request.warehouseId(),
                request.planName(),
                request.sellerSku(),
                request.plannedQuantity(),
                request.storeId(),
                request.siteCode())));
    }

    @GetMapping("/inbound-plans")
    public Result<List<FbaInboundPlan>> listInboundPlans(@RequestParam(required = false) String warehouseId,
                                                         @RequestParam(required = false) String status) {
        return Result.ok(fbaShipmentService.listInboundPlans(currentTenant(), warehouseId, status));
    }

    @PatchMapping("/inbound-plans/{planId}/submit")
    public Result<FbaInboundPlan> submitInboundPlan(@PathVariable String planId) {
        return Result.ok(fbaShipmentService.submitInboundPlan(currentTenant(), planId));
    }

    @PatchMapping("/inbound-plans/{planId}/cancel")
    public Result<FbaInboundPlan> cancelInboundPlan(@PathVariable String planId) {
        return Result.ok(fbaShipmentService.cancelInboundPlan(currentTenant(), planId));
    }

    @PostMapping("/inbound-plans/{planId}/split")
    public Result<SplitInboundPlanResult> splitInboundPlan(@PathVariable String planId,
                                                           @Valid @RequestBody SplitInboundPlanRequest request) {
        return Result.ok(fbaShipmentService.splitInboundPlan(currentTenant(), planId,
                new SplitInboundPlanCommand(request.newPlanName(), request.splitQuantity())));
    }

    @PostMapping("/shipments")
    public Result<FbaShipment> create(@Valid @RequestBody CreateShipmentRequest request) {
        return Result.ok(fbaShipmentService.create(currentTenant(), new CreateFbaShipmentCommand(
                request.amazonShipmentId(),
                request.destinationFc(),
                request.planId(),
                request.carrier(),
                request.trackingNo(),
                request.plannedQuantity(),
                request.items() == null ? List.of() : request.items().stream()
                        .map(item -> new ShipmentItemCommand(item.productId(), item.sellerSku(), item.fnsku(),
                                item.quantity(), item.boxQuantity()))
                        .collect(Collectors.toList()))));
    }

    @GetMapping("/shipments")
    public Result<List<FbaShipment>> list(@RequestParam(required = false) String planId) {
        return Result.ok(fbaShipmentService.list(currentTenant(), planId));
    }

    @GetMapping("/shipments/{fbaShipmentId}/items")
    public Result<List<FbaShipmentItem>> listShipmentItems(@PathVariable String fbaShipmentId) {
        return Result.ok(fbaShipmentService.listShipmentItems(currentTenant(), fbaShipmentId));
    }

    @PatchMapping("/shipments/{fbaShipmentId}/submit")
    public Result<FbaShipment> submit(@PathVariable String fbaShipmentId) {
        return Result.ok(fbaShipmentService.submit(currentTenant(), fbaShipmentId));
    }

    @PutMapping("/shipments/{fbaShipmentId}/pack")
    public Result<FbaShipment> pack(@PathVariable String fbaShipmentId, @Valid @RequestBody PackShipmentRequest request) {
        return Result.ok(fbaShipmentService.pack(currentTenant(), fbaShipmentId,
                new PackFbaShipmentCommand(request.cartonCount(), request.totalWeight(), request.carrier())));
    }

    @PutMapping("/shipments/{fbaShipmentId}/ship")
    public Result<FbaShipment> ship(@PathVariable String fbaShipmentId, @Valid @RequestBody ShipShipmentRequest request) {
        return Result.ok(fbaShipmentService.ship(currentTenant(), fbaShipmentId,
                new ShipFbaShipmentCommand(request.carrier(), request.trackingNo())));
    }

    @PatchMapping("/shipments/{fbaShipmentId}/receive")
    public Result<FbaShipment> receive(@PathVariable String fbaShipmentId, @Valid @RequestBody ReceiveRequest request) {
        return Result.ok(fbaShipmentService.receive(currentTenant(), fbaShipmentId,
                new ReceiveFbaShipmentCommand(request.receivedQuantity())));
    }

    @PostMapping("/shipments/{fbaShipmentId}/carton-labels")
    public Result<CartonLabel> createCartonLabel(@PathVariable String fbaShipmentId,
                                                 @Valid @RequestBody CreateCartonLabelRequest request) {
        return Result.ok(fbaShipmentService.createCartonLabel(currentTenant(), new CreateCartonLabelCommand(
                fbaShipmentId,
                request.cartonId(),
                request.sellerSku(),
                request.quantityPerCarton(),
                request.numberOfCartons(),
                request.labelUrl())));
    }

    @GetMapping("/shipments/{fbaShipmentId}/carton-labels")
    public Result<List<CartonLabel>> listCartonLabels(@PathVariable String fbaShipmentId) {
        return Result.ok(fbaShipmentService.listCartonLabels(currentTenant(), fbaShipmentId));
    }

    @PostMapping("/locations")
    public Result<FbaLocation> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        return Result.ok(fbaShipmentService.createLocation(currentTenant(), new CreateLocationCommand(
                request.name(),
                request.countryCode(),
                request.address(),
                request.locationType(),
                request.status() != null ? request.status() : "ACTIVE")));
    }

    @GetMapping("/locations")
    public Result<List<FbaLocation>> listLocations() {
        return Result.ok(fbaShipmentService.listLocations(currentTenant()));
    }

    @PostMapping("/inventories/sync")
    public Result<FbaInventory> syncInventory(@Valid @RequestBody SyncInventoryRequest request) {
        return Result.ok(fbaShipmentService.syncInventory(currentTenant(), new SyncInventoryCommand(
                request.inventoryId(),
                request.warehouseId(),
                request.productId(),
                request.sellerSku(),
                request.fnsku(),
                request.quantity(),
                request.storeId(),
                request.siteCode(),
                request.inventoryAgeDays())));
    }

    @GetMapping("/inventories")
    public Result<List<FbaInventory>> listInventories(@RequestParam(required = false) String sellerSku,
                                                      @RequestParam(required = false) String storeId,
                                                      @RequestParam(required = false) String siteCode,
                                                      @RequestParam(required = false) String warehouseId) {
        return Result.ok(fbaShipmentService.listInventories(currentTenant(), sellerSku, storeId, siteCode, warehouseId));
    }

    @GetMapping("/inventories/{inventoryId}")
    public Result<FbaInventory> getInventory(@PathVariable String inventoryId) {
        return Result.ok(fbaShipmentService.getInventory(currentTenant(), inventoryId));
    }

    @PostMapping("/replenishment-plans")
    public Result<ReplenishmentPlan> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return Result.ok(fbaShipmentService.createPlan(currentTenant(), new CreatePlanCommand(
                request.sellerSku(),
                request.destinationFc(),
                request.suggestedQuantity(),
                request.sourceWarehouseId())));
    }

    @GetMapping("/replenishment-plans")
    public Result<List<ReplenishmentPlan>> listPlans() {
        return Result.ok(fbaShipmentService.listPlans(currentTenant()));
    }

    @PatchMapping("/replenishment-plans/{planId}/submit")
    public Result<ReplenishmentPlan> submitPlan(@PathVariable String planId) {
        return Result.ok(fbaShipmentService.submitPlan(currentTenant(), planId));
    }

    @GetMapping("/restock-suggestions")
    public Result<List<RestockSuggestion>> listRestockSuggestions() {
        return Result.ok(fbaShipmentService.listRestockSuggestions(currentTenant()));
    }

    @PostMapping("/restock-suggestions/calculate")
    public Result<List<RestockSuggestion>> calculateRestockSuggestions(@Valid @RequestBody CalculateRestockRequest request) {
        return Result.ok(fbaShipmentService.calculateRestockSuggestions(currentTenant(),
                new CalculateRestockCommand(request.avgDailySales(), request.leadTimeDays(),
                        request.safetyStockDays(), request.orderProcessingDays())));
    }

    @GetMapping("/restock-cart")
    public Result<List<RestockCartItem>> listRestockCart(@RequestParam String userId) {
        return Result.ok(fbaShipmentService.listRestockCart(currentTenant(), userId));
    }

    @PostMapping("/restock-cart/add")
    public Result<RestockCartItem> addToRestockCart(@Valid @RequestBody AddToCartRequest request) {
        return Result.ok(fbaShipmentService.addToRestockCart(currentTenant(),
                new AddToCartCommand(request.userId(), request.productId(), request.sellerSku(),
                        request.qty(), request.warehouseId())));
    }

    @PostMapping("/restock-cart/confirm")
    public Result<FbaInboundPlan> confirmRestockCart(@Valid @RequestBody ConfirmCartRequest request) {
        return Result.ok(fbaShipmentService.confirmRestockCart(currentTenant(),
                new ConfirmCartCommand(request.userId(), request.warehouseId())));
    }

    @DeleteMapping("/restock-cart/{cartItemId}")
    public Result<Void> removeFromRestockCart(@PathVariable String cartItemId) {
        fbaShipmentService.removeFromRestockCart(currentTenant(), cartItemId);
        return Result.ok();
    }

    @GetMapping("/shipment-exceptions")
    public Result<List<ShipmentException>> listShipmentExceptions(@RequestParam(required = false) String shipmentId) {
        return Result.ok(fbaShipmentService.listShipmentExceptions(currentTenant(), shipmentId));
    }

    @PostMapping("/shipment-exceptions")
    public Result<ShipmentException> recordShipmentException(@Valid @RequestBody RecordExceptionRequest request) {
        return Result.ok(fbaShipmentService.recordShipmentException(currentTenant(),
                new RecordExceptionCommand(request.shipmentId(), request.type(), request.qty(), request.description())));
    }

    @PatchMapping("/shipment-exceptions/{exceptionId}/resolve")
    public Result<ShipmentException> resolveShipmentException(@PathVariable String exceptionId,
                                                               @Valid @RequestBody ResolveExceptionRequest request) {
        return Result.ok(fbaShipmentService.resolveShipmentException(currentTenant(), exceptionId,
                new ResolveExceptionCommand(request.resolvedBy())));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return tenantId;
    }

    public record CreateInboundPlanRequest(
            @NotBlank String warehouseId,
            @NotBlank String planName,
            @NotBlank String sellerSku,
            @Positive int plannedQuantity,
            String storeId,
            String siteCode) {}

    public record UpdateInboundPlanRequest(
            String warehouseId,
            String planName,
            String sellerSku,
            @Positive Integer plannedQuantity,
            String storeId,
            String siteCode) {}

    public record SplitInboundPlanRequest(@NotBlank String newPlanName, @Positive int splitQuantity) {}

    public record ShipmentItemRequest(
            String productId,
            @NotBlank String sellerSku,
            String fnsku,
            @Positive int quantity,
            @PositiveOrZero int boxQuantity) {}

    public record CreateShipmentRequest(
            String amazonShipmentId,
            String destinationFc,
            String planId,
            String carrier,
            String trackingNo,
            @PositiveOrZero int plannedQuantity,
            List<@Valid ShipmentItemRequest> items) {}

    public record PackShipmentRequest(
            @Positive int cartonCount,
            @NotNull @Positive BigDecimal totalWeight,
            String carrier) {}

    public record ShipShipmentRequest(String carrier, @NotBlank String trackingNo) {}

    public record ReceiveRequest(@Positive int receivedQuantity) {}

    public record CreateCartonLabelRequest(
            @NotBlank String cartonId,
            @NotBlank String sellerSku,
            @Positive int quantityPerCarton,
            @Positive int numberOfCartons,
            String labelUrl) {}

    public record CreateLocationRequest(
            @NotBlank String name,
            @NotBlank String countryCode,
            @NotBlank String address,
            @NotBlank String locationType,
            String status) {}

    public record SyncInventoryRequest(
            String inventoryId,
            @NotBlank String warehouseId,
            String productId,
            @NotBlank String sellerSku,
            String fnsku,
            @PositiveOrZero int quantity,
            String storeId,
            String siteCode,
            @PositiveOrZero int inventoryAgeDays) {}

    public record CreatePlanRequest(
            @NotBlank String sellerSku,
            @NotBlank String destinationFc,
            @Positive int suggestedQuantity,
            String sourceWarehouseId) {}

    public record CalculateRestockRequest(
            @NotNull @Positive BigDecimal avgDailySales,
            @Positive int leadTimeDays,
            @PositiveOrZero int safetyStockDays,
            @PositiveOrZero int orderProcessingDays) {}

    public record AddToCartRequest(
            @NotBlank String userId,
            @NotBlank String productId,
            @NotBlank String sellerSku,
            @Positive int qty,
            String warehouseId) {}

    public record ConfirmCartRequest(
            @NotBlank String userId,
            @NotBlank String warehouseId) {}

    public record RecordExceptionRequest(
            @NotBlank String shipmentId,
            @NotNull ShipmentException.ExceptionType type,
            @Positive int qty,
            String description) {}

    public record ResolveExceptionRequest(@NotBlank String resolvedBy) {}
}
