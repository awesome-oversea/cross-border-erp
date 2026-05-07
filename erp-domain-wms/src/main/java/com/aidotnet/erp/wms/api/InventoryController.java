package com.aidotnet.erp.wms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.wms.application.InventoryService;
import com.aidotnet.erp.wms.application.InventoryService.CreateLocationCommand;
import com.aidotnet.erp.wms.application.InventoryService.CreateStockCheckCommand;
import com.aidotnet.erp.wms.application.InventoryService.CreateWarehouseCommand;
import com.aidotnet.erp.wms.application.InventoryService.InventoryAvailability;
import com.aidotnet.erp.wms.application.InventoryService.PredictCommand;
import com.aidotnet.erp.wms.application.InventoryService.StockCommand;
import com.aidotnet.erp.wms.domain.InventoryBalance;
import com.aidotnet.erp.wms.domain.InventoryPrediction;
import com.aidotnet.erp.wms.domain.InventoryTransaction;
import com.aidotnet.erp.wms.domain.StockCheck;
import com.aidotnet.erp.wms.domain.Warehouse;
import com.aidotnet.erp.wms.domain.WarehouseLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
 * 库存管理控制器
 * <p>
 * 描述: WMS域核心REST API，提供仓库管理、库存余额、库存事务、盘点、预测等接口。
 *       路径前缀: /wms/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/wms/api/in/v1")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 构造函数 - 依赖注入库存服务
     *
     * @param inventoryService 库存管理应用服务
     */
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/warehouses")
    public Result<Warehouse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        return Result.ok(inventoryService.createWarehouse(currentTenant(), new CreateWarehouseCommand(
                request.code(), request.name(), request.type(), request.countryCode(),
                request.address(), request.contactPerson(), request.phone())));
    }

    @GetMapping("/warehouses")
    public Result<List<Warehouse>> listWarehouses() {
        return Result.ok(inventoryService.listWarehouses(currentTenant()));
    }

    @GetMapping("/warehouses/{warehouseId}")
    public Result<Warehouse> getWarehouse(@PathVariable String warehouseId) {
        return Result.ok(inventoryService.getWarehouse(currentTenant(), warehouseId));
    }

    @PostMapping("/warehouses/{warehouseId}/locations")
    public Result<WarehouseLocation> createLocation(@PathVariable String warehouseId, @Valid @RequestBody CreateLocationRequest request) {
        return Result.ok(inventoryService.createLocation(currentTenant(), new CreateLocationCommand(warehouseId,
                request.locationCode(), request.zone(), request.aisle(), request.shelf(), request.bin())));
    }

    @GetMapping("/warehouses/{warehouseId}/locations")
    public Result<List<WarehouseLocation>> listLocations(@PathVariable String warehouseId) {
        return Result.ok(inventoryService.listLocations(currentTenant(), warehouseId));
    }

    @PostMapping("/inventory/receive")
    public Result<InventoryBalance> receive(@Valid @RequestBody StockRequest request) {
        return Result.ok(inventoryService.receive(currentTenant(), new StockCommand(
                request.warehouseId(), request.sellerSku(), request.quantity(),
                request.referenceType(), request.referenceId(), request.remark())));
    }

    /**
     * 退货入库 - 售后退回商品重新入库
     * <p>
     * 由CRM售后域调用，将客户退回的商品重新入库到指定仓库。
     * 入库操作为可用库存增加，同时在库存事务中记录"RETURN_INBOUND"类型流水。
     * 不指定仓库时使用默认退货仓。
     * </p>
     */
    @PostMapping("/inventory/receive-return")
    public Result<InventoryBalance> receiveReturn(@Valid @RequestBody ReturnReceiveRequest request) {
        String warehouseId = request.warehouseId() != null ? request.warehouseId() : "DEFAULT_RETURN_WAREHOUSE";
        return Result.ok(inventoryService.receive(currentTenant(), new StockCommand(
                warehouseId, request.sellerSku(), request.quantity(),
                "RETURN_INBOUND", request.returnId(), "退货入库")));
    }

    @PostMapping("/inventory/reserve")
    public Result<InventoryBalance> reserve(@Valid @RequestBody StockRequest request) {
        return Result.ok(inventoryService.reserve(currentTenant(), new StockCommand(
                request.warehouseId(), request.sellerSku(), request.quantity(),
                request.referenceType(), request.referenceId(), request.remark())));
    }

    @PostMapping("/inventory/release")
    public Result<InventoryBalance> release(@Valid @RequestBody StockRequest request) {
        return Result.ok(inventoryService.release(currentTenant(), new StockCommand(
                request.warehouseId(), request.sellerSku(), request.quantity(),
                request.referenceType(), request.referenceId(), request.remark())));
    }

    @PostMapping("/inventory/deduct")
    public Result<InventoryBalance> deduct(@Valid @RequestBody StockRequest request) {
        return Result.ok(inventoryService.deduct(currentTenant(), new StockCommand(
                request.warehouseId(), request.sellerSku(), request.quantity(),
                request.referenceType(), request.referenceId(), request.remark())));
    }

    @GetMapping("/warehouses/{warehouseId}/inventory")
    public Result<List<InventoryBalance>> listBalances(@PathVariable String warehouseId) {
        return Result.ok(inventoryService.listBalances(currentTenant(), warehouseId));
    }

    @GetMapping("/warehouses/{warehouseId}/inventory-transactions")
    public Result<List<InventoryTransaction>> listTransactions(@PathVariable String warehouseId,
                                                               @RequestParam(required = false) String sellerSku) {
        return Result.ok(inventoryService.listTransactions(currentTenant(), warehouseId, sellerSku));
    }

    @GetMapping("/inventory/{sellerSku}/availability")
    public Result<InventoryAvailability> getAvailability(@PathVariable String sellerSku) {
        return Result.ok(inventoryService.getAvailability(currentTenant(), sellerSku));
    }

    @PostMapping("/warehouses/{warehouseId}/stock-checks")
    public Result<StockCheck> createStockCheck(@PathVariable String warehouseId, @Valid @RequestBody StockCheckRequest request) {
        return Result.ok(inventoryService.createStockCheck(currentTenant(), new CreateStockCheckCommand(warehouseId,
                request.sellerSku(), request.actualQuantity(), request.checkedBy())));
    }

    @GetMapping("/warehouses/{warehouseId}/stock-checks")
    public Result<List<StockCheck>> listStockChecks(@PathVariable String warehouseId) {
        return Result.ok(inventoryService.listStockChecks(currentTenant(), warehouseId));
    }

    @PatchMapping("/warehouses/{warehouseId}/stock-checks/{checkId}/adjust")
    public Result<StockCheck> adjustStock(@PathVariable String warehouseId, @PathVariable String checkId) {
        return Result.ok(inventoryService.adjustStock(currentTenant(), checkId));
    }

    @PostMapping("/inventory/predict")
    public Result<InventoryPrediction> predict(@Valid @RequestBody PredictRequest request) {
        return Result.ok(inventoryService.predict(currentTenant(), new PredictCommand(request.warehouseId(), request.sellerSku(), request.avgDailySales())));
    }

    @GetMapping("/warehouses/{warehouseId}/predictions")
    public Result<List<InventoryPrediction>> listPredictions(@PathVariable String warehouseId) {
        return Result.ok(inventoryService.listPredictions(currentTenant(), warehouseId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateWarehouseRequest(@NotBlank String code, @NotBlank String name, String type,
                                         @NotBlank String countryCode, String address,
                                         String contactPerson, String phone) {}

    public record CreateLocationRequest(@NotBlank String locationCode, String zone, String aisle, String shelf, String bin) {}

    public record StockRequest(@NotBlank String warehouseId, @NotBlank String sellerSku, @Positive int quantity,
                               String referenceType, String referenceId, String remark) {}

    public record StockCheckRequest(@NotBlank String sellerSku, @Positive int actualQuantity, @NotBlank String checkedBy) {}

    public record PredictRequest(@NotBlank String warehouseId, @NotBlank String sellerSku, @Positive double avgDailySales) {}

    public record ReturnReceiveRequest(String warehouseId, @NotBlank String sellerSku,
                                       @Positive int quantity, @NotBlank String returnId) {}
}
