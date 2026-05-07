package com.aidotnet.erp.wms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.wms.application.WarehouseOperationService;
import com.aidotnet.erp.wms.application.WarehouseOperationService.CompleteProductRepairCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.CreateDefectiveReturnCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.CreateProductRepairCommand;
import com.aidotnet.erp.wms.application.WarehouseOperationService.ProcessDefectiveReturnCommand;
import com.aidotnet.erp.wms.domain.DefectiveReturn;
import com.aidotnet.erp.wms.domain.DefectiveSupplierReply;
import com.aidotnet.erp.wms.domain.ProductRepair;
import com.aidotnet.erp.wms.domain.QualityCheckResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wms/api/in/v1")
public class DefectiveOperationController {

    private final WarehouseOperationService warehouseOperationService;

    public DefectiveOperationController(WarehouseOperationService warehouseOperationService) {
        this.warehouseOperationService = warehouseOperationService;
    }

    @PostMapping("/defective-returns")
    public Result<DefectiveReturn> createDefectiveReturn(@Valid @RequestBody CreateDefectiveReturnRequest request) {
        return Result.ok(warehouseOperationService.createDefectiveReturn(
                currentTenant(),
                new CreateDefectiveReturnCommand(
                        request.warehouseId(),
                        request.poId(),
                        request.supplierId(),
                        request.sellerSku(),
                        request.quantity(),
                        request.reason(),
                        request.remark())));
    }

    @PatchMapping("/defective-returns/{returnId}/process")
    public Result<DefectiveReturn> processDefectiveReturn(@PathVariable String returnId,
                                                          @Valid @RequestBody ProcessDefectiveReturnRequest request) {
        return Result.ok(warehouseOperationService.processDefectiveReturn(
                currentTenant(),
                returnId,
                new ProcessDefectiveReturnCommand(request.supplierReply(), request.processedBy(), request.remark())));
    }

    @GetMapping("/defective-returns/{returnId}")
    public Result<DefectiveReturn> getDefectiveReturn(@PathVariable String returnId) {
        return Result.ok(warehouseOperationService.getDefectiveReturn(currentTenant(), returnId));
    }

    @GetMapping("/defective-returns")
    public Result<List<DefectiveReturn>> listDefectiveReturns(@RequestParam String warehouseId) {
        return Result.ok(warehouseOperationService.listDefectiveReturns(currentTenant(), warehouseId));
    }

    @PostMapping("/product-repairs")
    public Result<ProductRepair> createProductRepair(@Valid @RequestBody CreateProductRepairRequest request) {
        return Result.ok(warehouseOperationService.createProductRepair(
                currentTenant(),
                new CreateProductRepairCommand(
                        request.warehouseId(),
                        request.supplierId(),
                        request.sellerSku(),
                        request.quantity(),
                        request.reason(),
                        request.remark())));
    }

    @PatchMapping("/product-repairs/{repairId}/complete")
    public Result<ProductRepair> completeProductRepair(@PathVariable String repairId,
                                                       @Valid @RequestBody CompleteProductRepairRequest request) {
        return Result.ok(warehouseOperationService.completeProductRepair(
                currentTenant(),
                repairId,
                new CompleteProductRepairCommand(
                        request.qcResult(),
                        request.inboundQuantity(),
                        request.processedBy(),
                        request.remark())));
    }

    @GetMapping("/product-repairs/{repairId}")
    public Result<ProductRepair> getProductRepair(@PathVariable String repairId) {
        return Result.ok(warehouseOperationService.getProductRepair(currentTenant(), repairId));
    }

    @GetMapping("/product-repairs")
    public Result<List<ProductRepair>> listProductRepairs(@RequestParam String warehouseId) {
        return Result.ok(warehouseOperationService.listProductRepairs(currentTenant(), warehouseId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "绉熸埛涓嶈兘涓虹┖");
        }
        return tenantId;
    }

    public record CreateDefectiveReturnRequest(
            @NotBlank String warehouseId,
            @NotBlank String poId,
            @NotBlank String supplierId,
            @NotBlank String sellerSku,
            @Positive int quantity,
            String reason,
            String remark) {}

    public record ProcessDefectiveReturnRequest(
            @NotNull DefectiveSupplierReply supplierReply,
            String processedBy,
            String remark) {}

    public record CreateProductRepairRequest(
            @NotBlank String warehouseId,
            @NotBlank String supplierId,
            @NotBlank String sellerSku,
            @Positive int quantity,
            String reason,
            String remark) {}

    public record CompleteProductRepairRequest(
            @NotNull QualityCheckResult qcResult,
            @PositiveOrZero int inboundQuantity,
            String processedBy,
            String remark) {}
}
