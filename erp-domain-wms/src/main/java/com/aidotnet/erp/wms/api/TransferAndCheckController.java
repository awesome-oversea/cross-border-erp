package com.aidotnet.erp.wms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.wms.application.TransferAndCheckService;
import com.aidotnet.erp.wms.application.TransferAndCheckService.CreateStockCheckCommand;
import com.aidotnet.erp.wms.application.TransferAndCheckService.CreateTransferCommand;
import com.aidotnet.erp.wms.application.TransferAndCheckService.ReceiveTransferLineCommand;
import com.aidotnet.erp.wms.application.TransferAndCheckService.StockCheckLineCommand;
import com.aidotnet.erp.wms.application.TransferAndCheckService.TransferLineCommand;
import com.aidotnet.erp.wms.domain.StockCheckOrder;
import com.aidotnet.erp.wms.domain.StockCheckOrderLine;
import com.aidotnet.erp.wms.domain.TransferOrder;
import com.aidotnet.erp.wms.domain.TransferOrderLine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调拨与盘点控制器
 * <p>
 * 描述: WMS域调拨与盘点REST API，提供调拨单、盘点单管理接口。
 *       路径前缀: /wms/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/wms/api/in/v1")
public class TransferAndCheckController {

    private final TransferAndCheckService service;

    /**
     * 构造函数 - 依赖注入调拨盘点服务
     *
     * @param service 调拨盘点应用服务
     */
    public TransferAndCheckController(TransferAndCheckService service) {
        this.service = service;
    }

    @PostMapping("/transfers")
    public Result<TransferOrder> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        CreateTransferCommand command = new CreateTransferCommand(request.fromWarehouseId(), request.toWarehouseId(),
                request.remark(), request.lines().stream().map(l -> new TransferLineCommand(l.sellerSku(),
                        l.transferQuantity(), l.unitCost(), l.batchNo())).toList());
        return Result.ok(service.createTransferOrder(currentTenant(), command));
    }

    @PatchMapping("/transfers/{transferId}/ship")
    public Result<TransferOrder> shipTransfer(@PathVariable String transferId) {
        return Result.ok(service.shipTransfer(currentTenant(), transferId));
    }

    @PatchMapping("/transfers/{transferId}/receive")
    public Result<TransferOrder> receiveTransfer(@PathVariable String transferId,
                                                  @Valid @RequestBody List<ReceiveTransferLineRequest> receipts) {
        List<ReceiveTransferLineCommand> commands = receipts.stream()
                .map(r -> new ReceiveTransferLineCommand(r.lineId(), r.receivedQuantity())).toList();
        return Result.ok(service.receiveTransfer(currentTenant(), transferId, commands));
    }

    @GetMapping("/transfers")
    public Result<List<TransferOrder>> listTransfers(@RequestParam String warehouseId) {
        return Result.ok(service.listTransferOrders(currentTenant(), warehouseId));
    }

    @GetMapping("/transfers/{transferId}")
    public Result<TransferOrder> getTransfer(@PathVariable String transferId) {
        return Result.ok(service.getTransferOrder(currentTenant(), transferId));
    }

    @GetMapping("/transfers/{transferId}/lines")
    public Result<List<TransferOrderLine>> listTransferLines(@PathVariable String transferId) {
        return Result.ok(service.listTransferOrderLines(transferId));
    }

    @PostMapping("/stock-checks")
    public Result<StockCheckOrder> createStockCheck(@Valid @RequestBody CreateStockCheckRequest request) {
        CreateStockCheckCommand command = new CreateStockCheckCommand(request.warehouseId(), request.checkType(),
                request.checkedBy(), request.remark(), request.lines().stream()
                        .map(l -> new StockCheckLineCommand(l.sellerSku(), l.locationId())).toList());
        return Result.ok(service.createStockCheckOrder(currentTenant(), command));
    }

    @PatchMapping("/stock-checks/{checkOrderId}/lines/{lineId}/count")
    public Result<StockCheckOrderLine> countLine(@PathVariable String checkOrderId, @PathVariable String lineId,
                                                  @Valid @RequestBody CountLineRequest request) {
        return Result.ok(service.countStockCheckLine(currentTenant(), checkOrderId, lineId, request.actualQuantity()));
    }

    @PatchMapping("/stock-checks/{checkOrderId}/complete")
    public Result<StockCheckOrder> completeCheck(@PathVariable String checkOrderId) {
        return Result.ok(service.completeStockCheck(currentTenant(), checkOrderId));
    }

    @PatchMapping("/stock-checks/{checkOrderId}/adjust")
    public Result<StockCheckOrder> adjustCheck(@PathVariable String checkOrderId) {
        return Result.ok(service.adjustStockCheck(currentTenant(), checkOrderId));
    }

    @GetMapping("/stock-checks")
    public Result<List<StockCheckOrder>> listStockChecks(@RequestParam String warehouseId) {
        return Result.ok(service.listStockCheckOrders(currentTenant(), warehouseId));
    }

    @GetMapping("/stock-checks/{checkOrderId}")
    public Result<StockCheckOrder> getStockCheck(@PathVariable String checkOrderId) {
        return Result.ok(service.getStockCheckOrder(currentTenant(), checkOrderId));
    }

    @GetMapping("/stock-checks/{checkOrderId}/lines")
    public Result<List<StockCheckOrderLine>> listStockCheckLines(@PathVariable String checkOrderId) {
        return Result.ok(service.listStockCheckOrderLines(checkOrderId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateTransferRequest(@NotBlank String fromWarehouseId, @NotBlank String toWarehouseId,
                                        String remark, List<TransferLineRequest> lines) {}
    public record TransferLineRequest(@NotBlank String sellerSku, @Positive int transferQuantity,
                                      BigDecimal unitCost, String batchNo) {}
    public record ReceiveTransferLineRequest(@NotBlank String lineId, @Positive int receivedQuantity) {}
    public record CreateStockCheckRequest(@NotBlank String warehouseId,
                                          @NotBlank StockCheckOrder.CheckType checkType,
                                          String checkedBy, String remark,
                                          List<StockCheckLineRequest> lines) {}
    public record StockCheckLineRequest(@NotBlank String sellerSku, String locationId) {}
    public record CountLineRequest(@Positive int actualQuantity) {}
}
