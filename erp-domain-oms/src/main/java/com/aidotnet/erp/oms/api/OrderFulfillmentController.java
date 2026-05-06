package com.aidotnet.erp.oms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.oms.application.OrderService;
import com.aidotnet.erp.oms.application.OrderService.MergePackagesCommand;
import com.aidotnet.erp.oms.application.OrderService.SplitLineAllocationCommand;
import com.aidotnet.erp.oms.application.OrderService.SplitOrderCommand;
import com.aidotnet.erp.oms.application.OrderService.SplitPackageCommand;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPlan;
import com.aidotnet.erp.oms.domain.PlatformShipmentSyncLog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单履约控制器
 * <p>
 * 描述: OMS域履约管理REST API，提供履约计划生成、发货、同步等接口。
 *       路径前缀: /oms/api/in/v1/orders (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/oms/api/in/v1/orders")
public class OrderFulfillmentController {

    private final OrderService orderService;

    /**
     * 构造函数 - 依赖注入订单服务
     *
     * @param orderService 订单管理应用服务
     */
    public OrderFulfillmentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/fulfillment-plans/generate")
    public Result<OrderFulfillmentPlan> generate(@PathVariable String orderId) {
        return Result.ok(orderService.generateFulfillmentPlan(currentTenant(), orderId));
    }

    @GetMapping("/{orderId}/fulfillment-plans/current")
    public Result<OrderFulfillmentPlan> current(@PathVariable String orderId) {
        return Result.ok(orderService.getCurrentFulfillmentPlan(currentTenant(), orderId));
    }

    @PostMapping("/{orderId}/allocate")
    public Result<OrderFulfillmentPlan> allocate(@PathVariable String orderId) {
        return Result.ok(orderService.allocateFulfillmentPlan(currentTenant(), orderId));
    }

    @PostMapping("/{orderId}/split")
    public Result<OrderFulfillmentPlan> splitOrder(@PathVariable String orderId,
                                                   @Valid @RequestBody SplitOrderRequest request) {
        List<SplitPackageCommand> packages = request.packages().stream()
                .map(pkg -> new SplitPackageCommand(
                        pkg.packageId(),
                        pkg.note(),
                        pkg.allocations().stream()
                                .map(allocation -> new SplitLineAllocationCommand(allocation.lineId(), allocation.quantity()))
                                .toList()))
                .toList();
        return Result.ok(orderService.splitOrder(currentTenant(), orderId, new SplitOrderCommand(packages)));
    }

    @PostMapping("/{orderId}/merge-packages")
    public Result<OrderFulfillmentPlan> mergePackages(@PathVariable String orderId,
                                                      @Valid @RequestBody MergePackagesRequest request) {
        return Result.ok(orderService.mergePackages(
                currentTenant(),
                orderId,
                new MergePackagesCommand(request.packageIds(), request.note())));
    }

    @PostMapping("/{orderId}/platform-shipments/retry")
    public Result<OrderFulfillmentPlan> retryPlatformShipmentSync(@PathVariable String orderId) {
        return Result.ok(orderService.retryPlatformShipmentSync(currentTenant(), orderId));
    }

    @GetMapping("/{orderId}/platform-shipments/logs")
    public Result<List<PlatformShipmentSyncLog>> listPlatformShipmentSyncLogs(@PathVariable String orderId) {
        return Result.ok(orderService.listPlatformShipmentSyncLogs(currentTenant(), orderId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "绉熸埛涓嶈兘涓虹┖");
        }
        return tenantId;
    }

    public record SplitOrderRequest(@NotEmpty List<@Valid SplitPackageRequest> packages) {}

    public record SplitPackageRequest(String packageId, String note,
                                      @NotEmpty List<@Valid SplitLineAllocationRequest> allocations) {}

    public record SplitLineAllocationRequest(@NotBlank String lineId, @Positive int quantity) {}

    public record MergePackagesRequest(@NotEmpty List<@NotBlank String> packageIds, String note) {}
}
