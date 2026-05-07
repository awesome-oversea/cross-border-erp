package com.aidotnet.erp.wms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.wms.application.WarehouseOperationService;
import com.aidotnet.erp.wms.application.WarehouseOperationService.CreateOutboundPackageCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.OutboundPackageLineCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.ShipOutboundPackageCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.WeighOutboundPackageCommand;
import com.aidotnet.erp.wms.domain.OutboundPackage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
 * 出库包裹控制器
 *
 * 描述: 提供 WMS 出库包裹打包、称重、发货查询接口，保持仓储域内聚。
 */
@RestController
@RequestMapping("/wms/api/in/v1")
public class OutboundPackageController {

    private final WarehouseOperationService warehouseOperationService;

    public OutboundPackageController(WarehouseOperationService warehouseOperationService) {
        this.warehouseOperationService = warehouseOperationService;
    }

    @PostMapping("/outbound-orders/{orderId}/packages")
    public Result<OutboundPackage> createPackage(@PathVariable String orderId,
                                                 @Valid @RequestBody CreateOutboundPackageRequest request) {
        List<OutboundPackageLineCommand> lines = request.lines().stream()
                .map(line -> new OutboundPackageLineCommand(line.lineId(), line.quantity()))
                .toList();
        return Result.ok(warehouseOperationService.createOutboundPackage(
                currentTenant(), orderId, new CreateOutboundPackageCommand(request.remark(), lines)));
    }

    @GetMapping("/outbound-orders/{orderId}/packages")
    public Result<List<OutboundPackage>> listPackages(@PathVariable String orderId) {
        return Result.ok(warehouseOperationService.listOutboundPackages(currentTenant(), orderId));
    }

    @PostMapping("/packages/{packageId}/weigh")
    public Result<OutboundPackage> weighPackage(@PathVariable String packageId,
                                                @Valid @RequestBody WeighPackageRequest request) {
        return Result.ok(warehouseOperationService.weighOutboundPackage(
                currentTenant(), packageId, new WeighOutboundPackageCommand(request.weightKg())));
    }

    @PostMapping("/packages/{packageId}/ship")
    public Result<OutboundPackage> shipPackage(@PathVariable String packageId,
                                               @Valid @RequestBody ShipPackageRequest request) {
        return Result.ok(warehouseOperationService.shipOutboundPackage(
                currentTenant(), packageId, new ShipOutboundPackageCommand(request.carrierCode(), request.trackingNo())));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record OutboundPackageLineRequest(@NotBlank String lineId, @Positive int quantity) {}

    public record CreateOutboundPackageRequest(String remark, List<OutboundPackageLineRequest> lines) {}

    public record WeighPackageRequest(@DecimalMin(value = "0.001") BigDecimal weightKg) {}

    public record ShipPackageRequest(@NotBlank String carrierCode, @NotBlank String trackingNo) {}
}
