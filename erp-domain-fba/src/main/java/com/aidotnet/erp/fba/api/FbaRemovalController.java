package com.aidotnet.erp.fba.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fba.application.FbaRemovalService;
import com.aidotnet.erp.fba.application.FbaRemovalService.CreateRemovalCommand;
import com.aidotnet.erp.fba.application.FbaRemovalService.RemovalItemCommand;
import com.aidotnet.erp.fba.domain.RemovalOrder;
import com.aidotnet.erp.fba.domain.RemovalOrder.RemovalStatus;
import com.aidotnet.erp.fba.domain.RemovalOrder.RemovalType;
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
 * FBA移除管理控制器
 * <p>
 * 描述: FBA域移除REST API，提供移除订单创建、查询等接口。
 *       路径前缀: /fba/api/in/v1/removals (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/fba/api/in/v1/removals")
public class FbaRemovalController {

    private final FbaRemovalService service;

    /**
     * 构造函数 - 依赖注入移除服务
     *
     * @param service FBA移除应用服务
     */
    public FbaRemovalController(FbaRemovalService service) {
        this.service = service;
    }

    @PostMapping
    public Result<RemovalOrder> createRemoval(@Valid @RequestBody CreateRemovalRequest request) {
        CreateRemovalCommand command = new CreateRemovalCommand(request.fbaSku(), request.quantity(),
                request.removalType(), request.returnAddressId(), request.reason(),
                request.items().stream().map(i -> new RemovalItemCommand(i.fbaSku(), i.quantity(),
                        i.disposalFee(), i.liquidationRevenue())).toList());
        return Result.ok(service.createRemovalOrder(currentTenant(), command));
    }

    @PatchMapping("/{removalId}/process")
    public Result<RemovalOrder> processRemoval(@PathVariable String removalId) {
        return Result.ok(service.processRemoval(currentTenant(), removalId));
    }

    @PatchMapping("/{removalId}/complete")
    public Result<RemovalOrder> completeRemoval(@PathVariable String removalId) {
        return Result.ok(service.completeRemoval(currentTenant(), removalId));
    }

    @PatchMapping("/{removalId}/cancel")
    public Result<RemovalOrder> cancelRemoval(@PathVariable String removalId) {
        return Result.ok(service.cancelRemoval(currentTenant(), removalId));
    }

    @GetMapping
    public Result<List<RemovalOrder>> listRemovals(@RequestParam(required = false) RemovalStatus status) {
        return Result.ok(service.listRemovalOrders(currentTenant(), status));
    }

    @GetMapping("/{removalId}")
    public Result<RemovalOrder> getRemoval(@PathVariable String removalId) {
        return Result.ok(service.getRemovalOrder(currentTenant(), removalId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateRemovalRequest(@NotBlank String fbaSku, @Positive int quantity,
                                        @NotBlank RemovalType removalType, String returnAddressId,
                                        String reason, List<RemovalItemRequest> items) {}
    public record RemovalItemRequest(@NotBlank String fbaSku, @Positive int quantity,
                                      BigDecimal disposalFee, BigDecimal liquidationRevenue) {}
}
