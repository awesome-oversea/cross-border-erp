package com.aidotnet.erp.tms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.tms.application.ShipmentService;
import com.aidotnet.erp.tms.application.ShipmentService.AddTrackingCommand;
import com.aidotnet.erp.tms.application.ShipmentService.CreateCarrierCommand;
import com.aidotnet.erp.tms.application.ShipmentService.CreateShipmentCommand;
import com.aidotnet.erp.tms.application.ShipmentService.RecordShippingCostCommand;
import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShippingCost;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 发货管理控制器
 * <p>
 * 描述: TMS域核心REST API，提供发货单、承运商、物流渠道、运费费率、运费成本、物流轨迹等接口。
 *       路径前缀: /tms/api/in/v1 (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/tms/api/in/v1")
public class ShipmentController {

    private final ShipmentService shipmentService;

    /**
     * 构造函数 - 依赖注入发货服务
     *
     * @param shipmentService 发货管理应用服务
     */
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/carriers")
    public Result<Carrier> createCarrier(@Valid @RequestBody CreateCarrierRequest request) {
        return Result.ok(shipmentService.createCarrier(currentTenant(), new CreateCarrierCommand(
                request.code(), request.name(), request.countryCode(), request.type(),
                request.contactPerson(), request.phone(), request.apiEnabled())));
    }

    @PostMapping("/shipments")
    public Result<Shipment> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        return Result.ok(shipmentService.createShipment(currentTenant(), new CreateShipmentCommand(
                request.orderId(), request.warehouseId(), request.carrierId(), request.shippingMethodId(),
                request.trackingNo(), request.destinationCountry(), request.weight(), request.length(),
                request.width(), request.height(), request.estimatedDelivery())));
    }

    @GetMapping("/shipments")
    public Result<List<Shipment>> listShipments() {
        return Result.ok(shipmentService.listShipments(currentTenant()));
    }

    @PatchMapping("/shipments/{shipmentId}/tracking")
    public Result<Shipment> addTracking(@PathVariable String shipmentId, @Valid @RequestBody AddTrackingRequest request) {
        return Result.ok(shipmentService.addTracking(currentTenant(), shipmentId, new AddTrackingCommand(request.status(), request.location(), request.description())));
    }

    @PatchMapping("/shipments/{shipmentId}/cancel")
    public Result<Shipment> cancel(@PathVariable String shipmentId) {
        return Result.ok(shipmentService.cancel(currentTenant(), shipmentId));
    }

    @PostMapping("/shipping-costs")
    public Result<ShippingCost> recordShippingCost(@Valid @RequestBody RecordShippingCostRequest request) {
        return Result.ok(shipmentService.recordShippingCost(currentTenant(), new RecordShippingCostCommand(
                request.shipmentId(), request.carrierId(), request.freightCost(), request.fuelSurcharge(), request.otherFees(), request.currency())));
    }

    @GetMapping("/shipping-costs")
    public Result<List<ShippingCost>> listShippingCosts() {
        return Result.ok(shipmentService.listShippingCosts(currentTenant()));
    }

    @GetMapping("/shipping-costs/by-carrier")
    public Result<List<ShippingCost>> listShippingCostsByCarrier(@NotBlank String carrierId) {
        return Result.ok(shipmentService.listShippingCostsByCarrier(currentTenant(), carrierId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCarrierRequest(@NotBlank String code, @NotBlank String name, String countryCode,
                                       String type, String contactPerson, String phone,
                                       boolean apiEnabled) {}

    public record CreateShipmentRequest(@NotBlank String orderId, String warehouseId, @NotBlank String carrierId,
                                        String shippingMethodId, @NotBlank String trackingNo,
                                        @NotBlank String destinationCountry, BigDecimal weight,
                                        BigDecimal length, BigDecimal width, BigDecimal height,
                                        java.time.Instant estimatedDelivery) {}

    public record AddTrackingRequest(@NotBlank String status, @NotBlank String location, @NotBlank String description) {}

    public record RecordShippingCostRequest(@NotBlank String shipmentId, @NotBlank String carrierId,
                                            @Positive BigDecimal freightCost, BigDecimal fuelSurcharge,
                                            BigDecimal otherFees, @NotBlank String currency) {}
}
