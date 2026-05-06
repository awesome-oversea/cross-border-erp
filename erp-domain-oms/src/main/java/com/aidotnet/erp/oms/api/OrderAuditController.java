package com.aidotnet.erp.oms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.oms.application.OrderService;
import com.aidotnet.erp.oms.application.OrderService.AddBuyerBlacklistCommand;
import com.aidotnet.erp.oms.domain.BuyerBlacklistEntry;
import com.aidotnet.erp.oms.domain.SalesOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单审核控制器
 * <p>
 * 描述: OMS域审核管理REST API，提供订单审核、买家黑名单管理接口。
 *       路径前缀: /oms/api/in/v1/order-audit (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/oms/api/in/v1/order-audit")
public class OrderAuditController {

    private final OrderService orderService;

    /**
     * 构造函数 - 依赖注入订单服务
     *
     * @param orderService 订单管理应用服务
     */
    public OrderAuditController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PatchMapping("/orders/{orderId}/approve")
    public Result<SalesOrder> approveReview(@PathVariable String orderId) {
        return Result.ok(orderService.approveReview(currentTenant(), orderId));
    }

    @PatchMapping("/orders/{orderId}/reject")
    public Result<SalesOrder> rejectReview(@PathVariable String orderId) {
        return Result.ok(orderService.rejectReview(currentTenant(), orderId));
    }

    @PostMapping("/buyer-blacklist")
    public Result<BuyerBlacklistEntry> addBuyerBlacklist(@Valid @RequestBody BuyerBlacklistRequest request) {
        return Result.ok(orderService.addBuyerBlacklist(currentTenant(),
                new AddBuyerBlacklistCommand(request.buyerName(), request.reason())));
    }

    @GetMapping("/buyer-blacklist")
    public Result<List<BuyerBlacklistEntry>> listBuyerBlacklist() {
        return Result.ok(orderService.listBuyerBlacklist(currentTenant()));
    }

    @DeleteMapping("/buyer-blacklist/{entryId}")
    public Result<Boolean> removeBuyerBlacklist(@PathVariable String entryId) {
        orderService.removeBuyerBlacklist(currentTenant(), entryId);
        return Result.ok(Boolean.TRUE);
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record BuyerBlacklistRequest(@NotBlank String buyerName, @NotBlank String reason) {}
}
