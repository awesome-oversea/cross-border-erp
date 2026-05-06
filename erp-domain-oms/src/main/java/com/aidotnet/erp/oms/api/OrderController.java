package com.aidotnet.erp.oms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.oms.application.OrderService;
import com.aidotnet.erp.oms.application.OrderService.ApplyPromotionCommand;
import com.aidotnet.erp.oms.application.OrderService.CreateOrderStrategyCommand;
import com.aidotnet.erp.oms.application.OrderService.ImportOrderCommand;
import com.aidotnet.erp.oms.application.OrderService.ReceivePmsRiskAlertCommand;
import com.aidotnet.erp.oms.application.OrderService.SyncOrdersCommand;
import com.aidotnet.erp.oms.application.OrderService.UpdateOrderStrategyCommand;
import com.aidotnet.erp.oms.domain.OrderLine;
import com.aidotnet.erp.oms.domain.OrderRefund;
import com.aidotnet.erp.oms.domain.OrderRiskCheck;
import com.aidotnet.erp.oms.domain.OrderStrategy;
import com.aidotnet.erp.oms.domain.OrderSyncLog;
import com.aidotnet.erp.oms.domain.PmsRiskAlert;
import com.aidotnet.erp.oms.domain.Promotion;
import com.aidotnet.erp.oms.domain.SalesOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
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
 * 订单管理控制器
 * <p>
 * 描述: OMS域核心REST API，提供订单导入/审核/支付/发货/签收/退款、
 *       风控检查、促销、策略、同步、黑名单等管理接口。
 *       路径前缀: /oms/api/in/v1/orders (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/oms/api/in/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 构造函数 - 依赖注入订单服务
     *
     * @param orderService 订单管理应用服务
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/import")
    public Result<SalesOrder> importOrder(@Valid @RequestBody ImportOrderRequest request) {
        List<OrderLine> lines = request.lines().stream()
                .map(line -> new OrderLine(line.lineId(), line.productId(), line.sellerSku(), line.title(),
                        line.quantity(), line.unitPrice(), line.totalPrice(), line.taxAmount(),
                        line.estimatedUnitCost()))
                .toList();
        return Result.ok(orderService.importOrder(currentTenant(), new ImportOrderCommand(request.platform(),
                request.platformOrderNo(), request.buyerName(), request.countryCode(), request.shippingAddress(),
                request.currency(), lines, request.importMode(), request.storeId(), request.marketplace(),
                request.rawPayload())));
    }

    @PostMapping("/sync")
    public Result<OrderSyncLog> syncOrders(@Valid @RequestBody SyncOrdersRequest request) {
        return Result.ok(orderService.syncOrders(currentTenant(),
                new SyncOrdersCommand(request.platform(), request.syncType())));
    }

    @GetMapping
    public Result<List<SalesOrder>> listOrders() {
        return Result.ok(orderService.list(currentTenant()));
    }

    @GetMapping("/{orderId}")
    public Result<SalesOrder> getOrder(@PathVariable String orderId) {
        return Result.ok(orderService.getOrder(currentTenant(), orderId));
    }

    @PatchMapping("/{orderId}/paid")
    public Result<SalesOrder> markPaid(@PathVariable String orderId) {
        return Result.ok(orderService.markPaid(currentTenant(), orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public Result<SalesOrder> cancel(@PathVariable String orderId) {
        return Result.ok(orderService.cancel(currentTenant(), orderId));
    }

    @PatchMapping("/{orderId}/ship")
    public Result<SalesOrder> ship(@PathVariable String orderId) {
        return Result.ok(orderService.ship(currentTenant(), orderId));
    }

    @PatchMapping("/{orderId}/deliver")
    public Result<SalesOrder> deliver(@PathVariable String orderId) {
        return Result.ok(orderService.deliver(currentTenant(), orderId));
    }

    @PostMapping("/{orderId}/refunds")
    public Result<OrderRefund> requestRefund(@PathVariable String orderId, @Valid @RequestBody RefundRequest request) {
        return Result.ok(orderService.requestRefund(currentTenant(), orderId, request.reason(),
                request.refundAmount(), OrderRefund.RefundType.valueOf(request.refundType())));
    }

    @PatchMapping("/refunds/{refundId}/approve")
    public Result<OrderRefund> approveRefund(@PathVariable String refundId) {
        return Result.ok(orderService.approveRefund(currentTenant(), refundId));
    }

    @PatchMapping("/refunds/{refundId}/reject")
    public Result<OrderRefund> rejectRefund(@PathVariable String refundId) {
        return Result.ok(orderService.rejectRefund(currentTenant(), refundId));
    }

    @PatchMapping("/refunds/{refundId}/complete")
    public Result<OrderRefund> completeRefund(@PathVariable String refundId) {
        return Result.ok(orderService.completeRefund(currentTenant(), refundId));
    }

    @GetMapping("/{orderId}/refunds")
    public Result<List<OrderRefund>> listRefunds(@PathVariable String orderId) {
        return Result.ok(orderService.listRefunds(currentTenant(), orderId));
    }

    @GetMapping("/{orderId}/risk-checks")
    public Result<List<OrderRiskCheck>> listRiskChecks(@PathVariable String orderId) {
        return Result.ok(orderService.listRiskChecks(currentTenant(), orderId));
    }

    @GetMapping("/{orderId}/promotions")
    public Result<List<Promotion>> listPromotions(@PathVariable String orderId) {
        return Result.ok(orderService.listPromotions(currentTenant(), orderId));
    }

    @PostMapping("/{orderId}/promotions")
    public Result<Promotion> applyPromotion(@PathVariable String orderId,
                                            @Valid @RequestBody ApplyPromotionRequest request) {
        return Result.ok(orderService.applyPromotion(currentTenant(), orderId,
                new ApplyPromotionCommand(request.promoType(), request.promoCode(),
                        request.discount(), request.description())));
    }

    @GetMapping("/risk-alerts")
    public Result<List<PmsRiskAlert>> listRiskAlerts(@RequestParam(required = false) String orderId) {
        return Result.ok(orderService.listPmsRiskAlerts(currentTenant(), orderId));
    }

    @PostMapping("/risk-alerts")
    public Result<PmsRiskAlert> receiveRiskAlert(@Valid @RequestBody ReceiveRiskAlertRequest request) {
        return Result.ok(orderService.receivePmsRiskAlert(currentTenant(),
                new ReceivePmsRiskAlertCommand(request.orderId(), request.riskType(), request.riskScore(),
                        request.riskLevel(), request.description(), request.suggestedAction(),
                        request.traceId(), request.idempotencyKey())));
    }

    @PatchMapping("/risk-alerts/{alertId}/review")
    public Result<PmsRiskAlert> reviewRiskAlert(@PathVariable String alertId,
                                                @Valid @RequestBody ReviewRiskAlertRequest request) {
        return Result.ok(orderService.reviewPmsRiskAlert(currentTenant(), alertId,
                request.action(), request.reviewerNote()));
    }

    @GetMapping("/sync-logs")
    public Result<List<OrderSyncLog>> listSyncLogs(@RequestParam(required = false) String platform) {
        return Result.ok(orderService.listSyncLogs(currentTenant(), platform));
    }

    @GetMapping("/strategies")
    public Result<List<OrderStrategy>> listStrategies(@RequestParam(required = false) String strategyType) {
        return Result.ok(orderService.listOrderStrategies(currentTenant(), strategyType));
    }

    @PostMapping("/strategies")
    public Result<OrderStrategy> createStrategy(@Valid @RequestBody CreateStrategyRequest request) {
        return Result.ok(orderService.createOrderStrategy(currentTenant(),
                new CreateOrderStrategyCommand(request.strategyType(), request.name(), request.description(),
                        request.rules(), request.enabled(), request.priority())));
    }

    @GetMapping("/strategies/{strategyId}")
    public Result<OrderStrategy> getStrategy(@PathVariable String strategyId) {
        return Result.ok(orderService.getOrderStrategy(currentTenant(), strategyId));
    }

    @PutMapping("/strategies/{strategyId}")
    public Result<OrderStrategy> updateStrategy(@PathVariable String strategyId,
                                                @Valid @RequestBody UpdateStrategyRequest request) {
        return Result.ok(orderService.updateOrderStrategy(currentTenant(), strategyId,
                new UpdateOrderStrategyCommand(request.strategyType(), request.name(), request.description(),
                        request.rules(), request.enabled(), request.priority())));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record ImportOrderRequest(@NotBlank String platform, @NotBlank String platformOrderNo,
                                     @NotBlank String buyerName, @NotBlank String countryCode,
                                     @NotBlank String shippingAddress, @NotBlank String currency,
                                     @NotEmpty List<@Valid OrderLineRequest> lines, String importMode,
                                     String storeId, String marketplace, String rawPayload) {}

    public record OrderLineRequest(String lineId, String productId, @NotBlank String sellerSku,
                                   @NotBlank String title, @Positive int quantity,
                                   @NotNull @Positive BigDecimal unitPrice, BigDecimal totalPrice,
                                   BigDecimal taxAmount, BigDecimal estimatedUnitCost) {}

    public record RefundRequest(@NotBlank String reason, BigDecimal refundAmount, @NotBlank String refundType) {}

    public record SyncOrdersRequest(@NotBlank String platform, @NotBlank String syncType) {}

    public record ApplyPromotionRequest(@NotBlank String promoType, String promoCode,
                                        @NotNull @Positive BigDecimal discount, String description) {}

    public record ReceiveRiskAlertRequest(@NotBlank String orderId, @NotBlank String riskType,
                                          @NotNull BigDecimal riskScore, @NotBlank String riskLevel,
                                          String description, String suggestedAction,
                                          String traceId, String idempotencyKey) {}

    public record ReviewRiskAlertRequest(@NotBlank String action, String reviewerNote) {}

    public record CreateStrategyRequest(@NotBlank String strategyType, @NotBlank String name,
                                        String description, String rules, boolean enabled, int priority) {}

    public record UpdateStrategyRequest(String strategyType, String name, String description,
                                        String rules, Boolean enabled, Integer priority) {}
}
