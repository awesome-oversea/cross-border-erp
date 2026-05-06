package com.aidotnet.erp.tms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.tms.client.FmsClient;
import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.CarrierRecommendation;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShipmentStatus;
import com.aidotnet.erp.tms.domain.ShippingCost;
import com.aidotnet.erp.tms.domain.TrackingEvent;
import com.aidotnet.erp.tms.infrastructure.ShipmentStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 物流发货管理应用服务
 * <p>
 * 描述: 物流域核心服务，负责物流商管理、发货单管理、物流轨迹追踪、
 *       运费成本记录、物流商智能推荐等业务逻辑。
 *       是连接订单域(OMS)、仓储域(WMS)、财务域(FMS)的物流调度中心。
 * </p>
 * <p>
 * 核心能力:
 *   1. 物流商管理 - 创建物流商，维护联系方式和API启用状态
 *   2. 发货单管理 - 创建发货单，关联订单/仓库/物流商/运输方式
 *   3. 物流轨迹 - 添加物流轨迹事件，自动更新发货单状态
 *   4. 运费成本 - 记录运费/燃油附加费/其他费用，自动同步FMS
 *   5. 物流商推荐 - 根据目的地/重量/金额智能推荐最优物流商
 * </p>
 * <p>
 * 业务规则:
 *   1. 物流跟踪号唯一性校验
 *   2. 已签收/已取消发货单不可添加轨迹
 *   3. DELIVERED状态轨迹自动将发货单标记为已签收
 *   4. 运费成本自动记录到FMS成本事件
 *   5. 物流商推荐根据快递/标准/经济三档分类，综合评分排序
 * </p>
 *
 * @author ERP系统
 * @see Shipment
 * @see Carrier
 * @see ShipmentStore
 */
@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentStore shipmentStore;
    private final FmsClient fmsClient;

    public ShipmentService(ShipmentStore shipmentStore, FmsClient fmsClient) {
        this.shipmentStore = shipmentStore;
        this.fmsClient = fmsClient;
    }

    public Carrier createCarrier(String tenantId, CreateCarrierCommand command) {
        Instant now = Instant.now();
        return shipmentStore.saveCarrier(new Carrier(
                UUID.randomUUID().toString(),
                tenantId,
                command.code(),
                command.name(),
                command.countryCode(),
                command.type(),
                Carrier.CarrierStatus.ACTIVE.name(),
                command.contactPerson(),
                command.phone(),
                command.apiEnabled(),
                now,
                now));
    }

    public List<Carrier> listCarriers(String tenantId) {
        return shipmentStore.listCarriers(tenantId);
    }

    public Shipment createShipment(String tenantId, CreateShipmentCommand command) {
        shipmentStore.findCarrier(tenantId, command.carrierId())
                .orElseThrow(() -> new BizException("CARRIER_NOT_FOUND", "Carrier does not exist"));
        shipmentStore.findByTrackingNo(tenantId, command.trackingNo()).ifPresent(existing -> {
            throw new BizException("TRACKING_NO_DUPLICATED", "Tracking number already exists");
        });
        Instant now = Instant.now();
        return shipmentStore.saveShipment(new Shipment(
                UUID.randomUUID().toString(),
                tenantId,
                command.orderId(),
                command.warehouseId(),
                command.carrierId(),
                command.shippingMethodId(),
                command.trackingNo(),
                command.destinationCountry(),
                command.weight(),
                command.length(),
                command.width(),
                command.height(),
                command.estimatedDelivery(),
                null,
                ShipmentStatus.CREATED,
                List.of(),
                now,
                now));
    }

    public Shipment addTracking(String tenantId, String shipmentId, AddTrackingCommand command) {
        Shipment shipment = getShipment(tenantId, shipmentId);
        if (shipment.status() == ShipmentStatus.CANCELLED || shipment.status() == ShipmentStatus.DELIVERED) {
            throw new BizException("SHIPMENT_STATUS_INVALID", "Shipment status does not allow tracking updates");
        }
        List<TrackingEvent> events = new ArrayList<>(shipment.trackingEvents());
        events.add(new TrackingEvent(
                UUID.randomUUID().toString(),
                command.status(),
                command.location(),
                command.description(),
                Instant.now()));
        ShipmentStatus status = "DELIVERED".equalsIgnoreCase(command.status())
                ? ShipmentStatus.DELIVERED
                : ShipmentStatus.IN_TRANSIT;
        return update(shipment, status, events);
    }

    public Shipment cancel(String tenantId, String shipmentId) {
        Shipment shipment = getShipment(tenantId, shipmentId);
        if (shipment.status() == ShipmentStatus.DELIVERED) {
            throw new BizException("SHIPMENT_STATUS_INVALID", "Delivered shipment cannot be cancelled");
        }
        return update(shipment, ShipmentStatus.CANCELLED, shipment.trackingEvents());
    }

    public List<Shipment> listShipments(String tenantId) {
        return shipmentStore.listShipments(tenantId);
    }

    public ShippingCost recordShippingCost(String tenantId, RecordShippingCostCommand command) {
        Shipment shipment = getShipment(tenantId, command.shipmentId());
        BigDecimal freightCost = nonNullAmount(command.freightCost());
        BigDecimal fuelSurcharge = nonNullAmount(command.fuelSurcharge());
        BigDecimal otherFees = nonNullAmount(command.otherFees());
        BigDecimal total = freightCost.add(fuelSurcharge).add(otherFees);
        ShippingCost cost = shipmentStore.saveShippingCost(new ShippingCost(
                UUID.randomUUID().toString(),
                tenantId,
                command.shipmentId(),
                command.carrierId(),
                freightCost,
                fuelSurcharge,
                otherFees,
                total,
                command.currency(),
                Instant.now()));
        recordShippingCostEvent(shipment, cost);
        return cost;
    }

    public List<ShippingCost> listShippingCosts(String tenantId) {
        return shipmentStore.listShippingCosts(tenantId);
    }

    public List<ShippingCost> listShippingCostsByCarrier(String tenantId, String carrierId) {
        return shipmentStore.listShippingCostsByCarrier(tenantId, carrierId);
    }

    public Shipment getShipment(String tenantId, String shipmentId) {
        return shipmentStore.findShipment(tenantId, shipmentId)
                .orElseThrow(() -> new BizException("SHIPMENT_NOT_FOUND", "Shipment does not exist"));
    }

    public List<CarrierRecommendation> recommendCarriers(String tenantId, RecommendCarrierCommand command) {
        if (command.destinationCountry() == null || command.destinationCountry().isBlank()) {
            throw new BizException("DESTINATION_COUNTRY_REQUIRED", "Destination country is required");
        }
        if (command.warehouseCountry() == null || command.warehouseCountry().isBlank()) {
            throw new BizException("WAREHOUSE_COUNTRY_REQUIRED", "Warehouse country is required");
        }
        return shipmentStore.listCarriers(tenantId).stream()
                .map(carrier -> toRecommendation(carrier, command))
                .sorted(recommendationComparator(command))
                .toList();
    }

    private Shipment update(Shipment shipment, ShipmentStatus status, List<TrackingEvent> events) {
        return shipmentStore.saveShipment(new Shipment(
                shipment.shipmentId(),
                shipment.tenantId(),
                shipment.orderId(),
                shipment.warehouseId(),
                shipment.carrierId(),
                shipment.shippingMethodId(),
                shipment.trackingNo(),
                shipment.destinationCountry(),
                shipment.weight(),
                shipment.length(),
                shipment.width(),
                shipment.height(),
                shipment.estimatedDelivery(),
                shipment.actualDelivery(),
                status,
                events,
                shipment.createdAt(),
                Instant.now()));
    }

    private BigDecimal nonNullAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private CarrierRecommendation toRecommendation(Carrier carrier, RecommendCarrierCommand command) {
        CarrierProfile profile = resolveProfile(carrier);
        boolean domestic = command.destinationCountry().equalsIgnoreCase(command.warehouseCountry());
        int estimatedDays = switch (profile) {
            case EXPRESS -> domestic ? 1 : 3;
            case STANDARD -> domestic ? 3 : 6;
            case ECONOMY -> domestic ? 5 : 9;
        };
        BigDecimal amount = nonNullAmount(command.packageAmount());
        BigDecimal baseCost = switch (profile) {
            case EXPRESS -> new BigDecimal("28");
            case STANDARD -> new BigDecimal("18");
            case ECONOMY -> new BigDecimal("10");
        };
        BigDecimal quantityFactor = switch (profile) {
            case EXPRESS -> new BigDecimal("3.5");
            case STANDARD -> new BigDecimal("2.2");
            case ECONOMY -> new BigDecimal("1.5");
        };
        BigDecimal amountFactor = switch (profile) {
            case EXPRESS -> new BigDecimal("0.015");
            case STANDARD -> new BigDecimal("0.010");
            case ECONOMY -> new BigDecimal("0.005");
        };
        BigDecimal estimatedCost = baseCost
                .add(quantityFactor.multiply(BigDecimal.valueOf(command.packageQuantity())))
                .add(amountFactor.multiply(amount));
        if (domestic) {
            estimatedCost = estimatedCost.multiply(new BigDecimal("0.65"));
        }
        if (command.splitShipment()) {
            estimatedCost = estimatedCost.add(new BigDecimal("3.00"));
        }
        estimatedCost = estimatedCost.setScale(2, RoundingMode.HALF_UP);
        int score = switch (profile) {
            case EXPRESS -> 90;
            case STANDARD -> 75;
            case ECONOMY -> 60;
        };
        if (domestic) {
            score += 10;
        }
        if (amount.compareTo(new BigDecimal("500")) >= 0 && profile == CarrierProfile.EXPRESS) {
            score += 10;
        }
        return new CarrierRecommendation(
                carrier.carrierId(),
                carrier.code(),
                carrier.name(),
                profile.name(),
                estimatedCost,
                estimatedDays,
                score);
    }

    private Comparator<CarrierRecommendation> recommendationComparator(RecommendCarrierCommand command) {
        BigDecimal amount = nonNullAmount(command.packageAmount());
        if (amount.compareTo(new BigDecimal("500")) >= 0) {
            return Comparator.comparingInt(CarrierRecommendation::estimatedDeliveryDays)
                    .thenComparing(CarrierRecommendation::estimatedCost);
        }
        return Comparator.comparing(CarrierRecommendation::estimatedCost)
                .thenComparingInt(CarrierRecommendation::estimatedDeliveryDays);
    }

    private CarrierProfile resolveProfile(Carrier carrier) {
        String fingerprint = (carrier.code() + " " + carrier.name()).toUpperCase(Locale.ROOT);
        if (fingerprint.contains("DHL")
                || fingerprint.contains("UPS")
                || fingerprint.contains("FEDEX")
                || fingerprint.contains("EXPRESS")
                || fingerprint.contains("AIR")) {
            return CarrierProfile.EXPRESS;
        }
        if (fingerprint.contains("POSTAL")
                || fingerprint.contains("ECONOMY")
                || fingerprint.contains("SEA")
                || fingerprint.contains("MAIL")) {
            return CarrierProfile.ECONOMY;
        }
        return CarrierProfile.STANDARD;
    }

    private void recordShippingCostEvent(Shipment shipment, ShippingCost cost) {
        try {
            fmsClient.recordCostEvent(new FmsClient.RecordCostEventRequest(
                    "SHIPPING_COST",
                    "TMS_SHIPMENT",
                    shipment.shipmentId(),
                    null,
                    null,
                    cost.currency(),
                    cost.totalCost(),
                    cost.createdAt()));
        } catch (Exception ex) {
            log.warn("Record shipping cost event failed. shipmentId={}", shipment.shipmentId(), ex);
        }
    }

    public record CreateCarrierCommand(String code, String name, String countryCode, String type,
                                       String contactPerson, String phone, boolean apiEnabled) {}

    public record CreateShipmentCommand(String orderId, String warehouseId, String carrierId, String shippingMethodId,
                                        String trackingNo, String destinationCountry,
                                        BigDecimal weight, BigDecimal length, BigDecimal width, BigDecimal height,
                                        Instant estimatedDelivery) {}

    public record AddTrackingCommand(String status, String location, String description) {}

    public record RecordShippingCostCommand(String shipmentId, String carrierId, BigDecimal freightCost,
                                            BigDecimal fuelSurcharge, BigDecimal otherFees, String currency) {}

    public record RecommendCarrierCommand(String destinationCountry, String warehouseCountry,
                                          int packageQuantity, BigDecimal packageAmount, boolean splitShipment) {}

    private enum CarrierProfile {
        EXPRESS,
        STANDARD,
        ECONOMY
    }
}
