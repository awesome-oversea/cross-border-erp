package com.aidotnet.erp.tms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.tms.application.ShippingBatchService;
import com.aidotnet.erp.tms.application.ShippingBatchService.CreateBatchCommand;
import com.aidotnet.erp.tms.domain.ShippingBatch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
 * 批量发货控制器
 * <p>
 * 描述: TMS域批量发货REST API，提供批量发货单创建、提交、取消等接口。
 *       路径前缀: /tms/api/in/v1/shipping-batches (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/tms/api/in/v1/shipping-batches")
public class ShippingBatchController {

    private final ShippingBatchService service;

    /**
     * 构造函数 - 依赖注入批量发货服务
     *
     * @param service 批量发货应用服务
     */
    public ShippingBatchController(ShippingBatchService service) {
        this.service = service;
    }

    @PostMapping
    public Result<ShippingBatch> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        CreateBatchCommand command = new CreateBatchCommand(request.carrierId(), request.shipmentIds());
        return Result.ok(service.createBatch(currentTenant(), command));
    }

    @PatchMapping("/{batchId}/submit")
    public Result<ShippingBatch> submitBatch(@PathVariable String batchId) {
        return Result.ok(service.submitBatch(currentTenant(), batchId));
    }

    @PatchMapping("/{batchId}/in-transit")
    public Result<ShippingBatch> markInTransit(@PathVariable String batchId) {
        return Result.ok(service.markInTransit(currentTenant(), batchId));
    }

    @PatchMapping("/{batchId}/complete")
    public Result<ShippingBatch> completeBatch(@PathVariable String batchId) {
        return Result.ok(service.completeBatch(currentTenant(), batchId));
    }

    @GetMapping
    public Result<List<ShippingBatch>> listBatches(@RequestParam(required = false) String carrierId) {
        return Result.ok(service.listBatches(currentTenant(), carrierId));
    }

    @GetMapping("/{batchId}")
    public Result<ShippingBatch> getBatch(@PathVariable String batchId) {
        return Result.ok(service.getBatch(currentTenant(), batchId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateBatchRequest(@NotBlank String carrierId, @NotEmpty List<String> shipmentIds) {}
}
