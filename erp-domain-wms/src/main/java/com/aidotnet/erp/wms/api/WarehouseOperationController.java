package com.aidotnet.erp.wms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.wms.application.WarehouseOperationService;
import com.aidotnet.erp.wms.application.WarehouseOperationService.CreateInboundOrderCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.CreateOutboundOrderCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.InboundOrderLineCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.MoveInventoryCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.OutboundOrderLineCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.PerformQualityCheckCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.PickLineCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.PickOutboundCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.ReceiveInboundCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.ReceiveLineCommand;
import com.aidotnet.erp.wms.domain.InboundOrder;
import com.aidotnet.erp.wms.domain.InboundOrderLine;
import com.aidotnet.erp.wms.domain.InventoryMovement;
import com.aidotnet.erp.wms.domain.OutboundOrder;
import com.aidotnet.erp.wms.domain.OutboundOrderLine;
import com.aidotnet.erp.wms.domain.QualityCheck;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仓库作业控制器
 * <p>
 * 描述: WMS域仓库作业REST API，提供入库、出库、质检等操作接口。
 *       路径前缀: /wms/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/wms/api/in/v1")
public class WarehouseOperationController {

    private final WarehouseOperationService warehouseOperationService;

    /**
     * 构造函数 - 依赖注入仓库作业服务
     *
     * @param warehouseOperationService 仓库作业应用服务
     */
    public WarehouseOperationController(WarehouseOperationService warehouseOperationService) {
        this.warehouseOperationService = warehouseOperationService;
    }

    @PostMapping("/inbound-orders")
    public Result<InboundOrder> createInboundOrder(@Valid @RequestBody CreateInboundOrderRequest request) {
        List<InboundOrderLineCommand> lines = request.lines().stream()
                .map(l -> new InboundOrderLineCommand(l.sellerSku(), l.locationId(), l.expectedQuantity(), l.unitCost(), l.batchNo()))
                .toList();
        return Result.ok(warehouseOperationService.createInboundOrder(currentTenant(),
                new CreateInboundOrderCommand(request.warehouseId(), request.referenceType(), request.referenceId(), request.remark(), lines)));
    }

    @PostMapping("/inbound-orders/{orderId}/receive")
    public Result<InboundOrder> receiveInboundOrder(@PathVariable String orderId,
                                                    @Valid @RequestBody ReceiveInboundRequest request) {
        List<ReceiveLineCommand> receipts = request.receipts().stream()
                .map(r -> new ReceiveLineCommand(r.lineId(), r.receivedQuantity()))
                .toList();
        return Result.ok(warehouseOperationService.receiveInboundOrder(currentTenant(), orderId, new ReceiveInboundCommand(receipts)));
    }

    @GetMapping("/inbound-orders")
    public Result<List<InboundOrder>> listInboundOrders(String warehouseId) {
        return Result.ok(warehouseOperationService.listInboundOrders(currentTenant(), warehouseId));
    }

    @GetMapping("/inbound-orders/{orderId}")
    public Result<InboundOrder> getInboundOrder(@PathVariable String orderId) {
        return Result.ok(warehouseOperationService.getInboundOrder(currentTenant(), orderId));
    }

    @GetMapping("/inbound-orders/{orderId}/lines")
    public Result<List<InboundOrderLine>> listInboundOrderLines(@PathVariable String orderId) {
        return Result.ok(warehouseOperationService.listInboundOrderLines(orderId));
    }

    @PostMapping("/outbound-orders")
    public Result<OutboundOrder> createOutboundOrder(@Valid @RequestBody CreateOutboundOrderRequest request) {
        List<OutboundOrderLineCommand> lines = request.lines().stream()
                .map(l -> new OutboundOrderLineCommand(l.sellerSku(), l.locationId(), l.requiredQuantity(), l.batchNo()))
                .toList();
        return Result.ok(warehouseOperationService.createOutboundOrder(currentTenant(),
                new CreateOutboundOrderCommand(request.warehouseId(), request.referenceType(), request.referenceId(), request.remark(), lines)));
    }

    @PostMapping("/outbound-orders/{orderId}/pick")
    public Result<OutboundOrder> pickOutboundOrder(@PathVariable String orderId,
                                                   @Valid @RequestBody PickOutboundRequest request) {
        List<PickLineCommand> picks = request.picks().stream()
                .map(p -> new PickLineCommand(p.lineId(), p.pickedQuantity()))
                .toList();
        return Result.ok(warehouseOperationService.pickOutboundOrder(currentTenant(), orderId, new PickOutboundCommand(picks)));
    }

    @GetMapping("/outbound-orders")
    public Result<List<OutboundOrder>> listOutboundOrders(String warehouseId) {
        return Result.ok(warehouseOperationService.listOutboundOrders(currentTenant(), warehouseId));
    }

    @GetMapping("/outbound-orders/{orderId}")
    public Result<OutboundOrder> getOutboundOrder(@PathVariable String orderId) {
        return Result.ok(warehouseOperationService.getOutboundOrder(currentTenant(), orderId));
    }

    @GetMapping("/outbound-orders/{orderId}/lines")
    public Result<List<OutboundOrderLine>> listOutboundOrderLines(@PathVariable String orderId) {
        return Result.ok(warehouseOperationService.listOutboundOrderLines(orderId));
    }

    @PostMapping("/movements")
    public Result<InventoryMovement> moveInventory(@Valid @RequestBody MoveInventoryRequest request) {
        return Result.ok(warehouseOperationService.moveInventory(currentTenant(),
                new MoveInventoryCommand(request.warehouseId(), request.sellerSku(), request.fromLocationId(), request.toLocationId(), request.quantity())));
    }

    @GetMapping("/movements")
    public Result<List<InventoryMovement>> listMovements(String warehouseId) {
        return Result.ok(warehouseOperationService.listMovements(currentTenant(), warehouseId));
    }

    @PostMapping("/quality-checks")
    public Result<QualityCheck> performQualityCheck(@Valid @RequestBody PerformQualityCheckRequest request) {
        return Result.ok(warehouseOperationService.performQualityCheck(currentTenant(),
                new PerformQualityCheckCommand(request.warehouseId(), request.inboundOrderId(), request.sellerSku(),
                        request.sampleQuantity(), request.passQuantity(), request.failQuantity(), request.inspector(), request.remark())));
    }

    @GetMapping("/quality-checks")
    public Result<List<QualityCheck>> listQualityChecks(String warehouseId) {
        return Result.ok(warehouseOperationService.listQualityChecks(currentTenant(), warehouseId));
    }

    @GetMapping("/inbound-orders/{inboundOrderId}/quality-checks")
    public Result<List<QualityCheck>> listQualityChecksByInboundOrder(@PathVariable String inboundOrderId) {
        return Result.ok(warehouseOperationService.listQualityChecksByInboundOrder(currentTenant(), inboundOrderId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record InboundLineRequest(@NotBlank String sellerSku, String locationId, @Positive int expectedQuantity,
                                     BigDecimal unitCost, String batchNo) {}
    public record CreateInboundOrderRequest(@NotBlank String warehouseId, String referenceType, String referenceId,
                                            String remark, List<InboundLineRequest> lines) {}
    public record ReceiveLineRequest(@NotBlank String lineId, @Positive int receivedQuantity) {}
    public record ReceiveInboundRequest(List<ReceiveLineRequest> receipts) {}
    public record OutboundLineRequest(@NotBlank String sellerSku, String locationId, @Positive int requiredQuantity, String batchNo) {}
    public record CreateOutboundOrderRequest(@NotBlank String warehouseId, String referenceType, String referenceId,
                                             String remark, List<OutboundLineRequest> lines) {}
    public record PickLineRequest(@NotBlank String lineId, @Positive int pickedQuantity) {}
    public record PickOutboundRequest(List<PickLineRequest> picks) {}
    public record MoveInventoryRequest(@NotBlank String warehouseId, @NotBlank String sellerSku,
                                       String fromLocationId, String toLocationId, @Positive int quantity) {}
    public record PerformQualityCheckRequest(@NotBlank String warehouseId, String inboundOrderId, @NotBlank String sellerSku,
                                             @Positive int sampleQuantity, @Positive int passQuantity, @Positive int failQuantity,
                                             String inspector, String remark) {}
}
