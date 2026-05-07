package com.aidotnet.erp.tms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.tms.application.ShipmentService;
import com.aidotnet.erp.tms.application.ShipmentService.AddTrackingCommand;
import com.aidotnet.erp.tms.application.ShipmentService.CreateCarrierCommand;
import com.aidotnet.erp.tms.application.ShipmentService.CreateChannelRuleCommand;
import com.aidotnet.erp.tms.application.ShipmentService.CreateShipmentCommand;
import com.aidotnet.erp.tms.application.ShipmentService.CreateShippingMethodCommand;
import com.aidotnet.erp.tms.application.ShipmentService.EstimateShippingRateCommand;
import com.aidotnet.erp.tms.application.ShipmentService.FreightDifference;
import com.aidotnet.erp.tms.application.ShipmentService.FreightEstimate;
import com.aidotnet.erp.tms.application.ShipmentService.RecordShippingCostCommand;
import com.aidotnet.erp.tms.application.ShipmentService.UpdateCarrierCommand;
import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShippingCost;
import com.aidotnet.erp.tms.domain.ShippingMethod;
import com.aidotnet.erp.tms.domain.ShippingRate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
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
 * TMS 领域主控接口。
 * <p>
 * ERP 内部 14 个系统直接调用统一使用 `/tms/api/v1/...`。
 * 平台共享能力统一收敛到 `/platform/tms/api/v1/...`，避免与领域资源冲突。
 * </p>
 */
@RestController
@RequestMapping("/tms/api/v1")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/carriers")
    public Result<Carrier> createCarrier(@Valid @RequestBody CreateCarrierRequest request) {
        return Result.ok(shipmentService.createCarrier(currentTenant(), new CreateCarrierCommand(
                request.code(), request.name(), request.countryCode(), request.type(),
                request.contactPerson(), request.phone(), request.status(),
                request.apiEnabled(), request.featured(), request.authorizationStatus(),
                request.authorizationValidUntil(), request.lastAuthorizedAt())));
    }

    @PatchMapping("/carriers/{carrierId}")
    public Result<Carrier> updateCarrier(@PathVariable String carrierId,
                                         @Valid @RequestBody UpdateCarrierRequest request) {
        return Result.ok(shipmentService.updateCarrier(currentTenant(), carrierId, new UpdateCarrierCommand(
                request.code(), request.name(), request.countryCode(), request.type(),
                request.status(), request.contactPerson(), request.phone(),
                request.apiEnabled(), request.featured(), request.authorizationStatus(),
                request.authorizationValidUntil(), request.lastAuthorizedAt())));
    }

    @GetMapping("/carriers")
    public Result<List<Carrier>> listCarriers() {
        return Result.ok(shipmentService.listCarriers(currentTenant()));
    }

    @GetMapping("/shipping-methods")
    public Result<List<ShippingMethod>> listShippingMethods(@RequestParam(required = false) String carrierId) {
        if (carrierId == null || carrierId.isBlank()) {
            return Result.ok(shipmentService.listShippingMethods(currentTenant()));
        }
        return Result.ok(shipmentService.listShippingMethodsByCarrier(currentTenant(), carrierId));
    }

    @PostMapping("/carriers/{carrierId}/methods")
    public Result<ShippingMethod> createShippingMethod(@PathVariable String carrierId,
                                                       @Valid @RequestBody CreateShippingMethodRequest request) {
        return Result.ok(shipmentService.createShippingMethod(currentTenant(), carrierId, new CreateShippingMethodCommand(
                request.methodCode(), request.methodName(), request.transportMode(), request.rateType(),
                request.enabled(), request.estimatedDaysMin(), request.estimatedDaysMax())));
    }

    @GetMapping("/carriers/{carrierId}/methods")
    public Result<List<ShippingMethod>> listShippingMethodsByCarrier(@PathVariable String carrierId) {
        return Result.ok(shipmentService.listShippingMethodsByCarrier(currentTenant(), carrierId));
    }

    @PostMapping("/carriers/{carrierId}/methods/{methodId}/channel-rules")
    public Result<ShippingRate> createChannelRule(@PathVariable String carrierId,
                                                  @PathVariable String methodId,
                                                  @Valid @RequestBody CreateChannelRuleRequest request) {
        return Result.ok(shipmentService.createChannelRule(currentTenant(), carrierId, methodId, new CreateChannelRuleCommand(
                request.originCountry(), request.destinationCountry(), request.zoneCode(),
                request.weightMinKg(), request.weightMaxKg(), request.baseCost(), request.costPerKg(),
                request.currency(), request.effectiveFrom(), request.effectiveTo())));
    }

    @GetMapping("/carriers/{carrierId}/methods/{methodId}/channel-rules")
    public Result<List<ShippingRate>> listChannelRules(@PathVariable String carrierId, @PathVariable String methodId) {
        return Result.ok(shipmentService.listChannelRules(currentTenant(), carrierId, methodId));
    }

    @GetMapping("/shipping-rates")
    public Result<List<ShippingRate>> listShippingRates(@RequestParam(required = false) String carrierId,
                                                        @RequestParam(required = false) String shippingMethodId,
                                                        @RequestParam(required = false) String originCountry,
                                                        @RequestParam(required = false) String destinationCountry) {
        return Result.ok(shipmentService.listShippingRates(
                currentTenant(), carrierId, shippingMethodId, originCountry, destinationCountry));
    }

    @PostMapping("/shipping-rates/estimate")
    public Result<FreightEstimate> estimateShippingRate(@Valid @RequestBody EstimateShippingRateRequest request) {
        return Result.ok(shipmentService.estimateShippingRate(currentTenant(), new EstimateShippingRateCommand(
                request.carrierId(), request.shippingMethodId(), request.originCountry(),
                request.destinationCountry(), request.weight(), request.volume())));
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

    @GetMapping("/shipments/{shipmentId}")
    public Result<Shipment> getShipment(@PathVariable String shipmentId) {
        return Result.ok(shipmentService.getShipment(currentTenant(), shipmentId));
    }

    @GetMapping("/shipments/{shipmentId}/freight-difference")
    public Result<FreightDifference> getFreightDifference(@PathVariable String shipmentId) {
        return Result.ok(shipmentService.getFreightDifference(currentTenant(), shipmentId));
    }

    @PatchMapping("/shipments/{shipmentId}/tracking")
    public Result<Shipment> addTracking(@PathVariable String shipmentId,
                                        @Valid @RequestBody AddTrackingRequest request) {
        return Result.ok(shipmentService.addTracking(currentTenant(), shipmentId, new AddTrackingCommand(
                request.status(), request.location(), request.description())));
    }

    @PatchMapping("/shipments/{shipmentId}/cancel")
    public Result<Shipment> cancel(@PathVariable String shipmentId) {
        return Result.ok(shipmentService.cancel(currentTenant(), shipmentId));
    }

    @PostMapping("/shipping-costs")
    public Result<ShippingCost> recordShippingCost(@Valid @RequestBody RecordShippingCostRequest request) {
        return Result.ok(shipmentService.recordShippingCost(currentTenant(), new RecordShippingCostCommand(
                request.shipmentId(), request.carrierId(), request.freightCost(), request.fuelSurcharge(),
                request.otherFees(), request.currency())));
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
                                       String type, String contactPerson, String phone, String status,
                                       boolean apiEnabled, boolean featured, String authorizationStatus,
                                       Instant authorizationValidUntil, Instant lastAuthorizedAt) {}

    public record UpdateCarrierRequest(String code, String name, String countryCode, String type, String status,
                                       String contactPerson, String phone, Boolean apiEnabled, Boolean featured,
                                       String authorizationStatus, Instant authorizationValidUntil,
                                       Instant lastAuthorizedAt) {}

    public record CreateShippingMethodRequest(@NotBlank String methodCode, @NotBlank String methodName,
                                              String transportMode, String rateType, boolean enabled,
                                              @PositiveOrZero Integer estimatedDaysMin,
                                              @PositiveOrZero Integer estimatedDaysMax) {}

    public record CreateChannelRuleRequest(String originCountry, String destinationCountry, @NotBlank String zoneCode,
                                           @PositiveOrZero BigDecimal weightMinKg, @PositiveOrZero BigDecimal weightMaxKg,
                                           @PositiveOrZero BigDecimal baseCost, @PositiveOrZero BigDecimal costPerKg,
                                           @NotBlank String currency, Instant effectiveFrom, Instant effectiveTo) {}

    public record EstimateShippingRateRequest(String carrierId, String shippingMethodId, String originCountry,
                                              @NotBlank String destinationCountry, @PositiveOrZero BigDecimal weight,
                                              @PositiveOrZero BigDecimal volume) {}

    public record CreateShipmentRequest(@NotBlank String orderId, String warehouseId, @NotBlank String carrierId,
                                        String shippingMethodId, @NotBlank String trackingNo,
                                        @NotBlank String destinationCountry, BigDecimal weight,
                                        BigDecimal length, BigDecimal width, BigDecimal height,
                                        Instant estimatedDelivery) {}

    public record AddTrackingRequest(@NotBlank String status, @NotBlank String location, @NotBlank String description) {}

    public record RecordShippingCostRequest(@NotBlank String shipmentId, @NotBlank String carrierId,
                                            @Positive BigDecimal freightCost, BigDecimal fuelSurcharge,
                                            BigDecimal otherFees, @NotBlank String currency) {}
}
