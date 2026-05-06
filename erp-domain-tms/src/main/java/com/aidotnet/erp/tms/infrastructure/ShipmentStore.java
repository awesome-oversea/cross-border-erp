package com.aidotnet.erp.tms.infrastructure;

import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShipmentStatus;
import com.aidotnet.erp.tms.domain.ShippingCost;
import com.aidotnet.erp.tms.domain.TrackingEvent;
import com.aidotnet.erp.tms.infrastructure.data.CarrierDO;
import com.aidotnet.erp.tms.infrastructure.data.ShipmentDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingCostDO;
import com.aidotnet.erp.tms.infrastructure.data.TrackingEventDO;
import com.aidotnet.erp.tms.infrastructure.mapper.ShipmentMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * TMS域发货数据存储
 * <p>
 * 描述: 物流域核心数据存储层，负责发货单、承运商、运费成本、物流轨迹等实体的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class ShipmentStore {

    /** 发货数据MyBatis映射器 */
    private final ShipmentMapper mapper;

    /**
     * 构造函数 - 依赖注入映射器
     *
     * @param mapper 发货MyBatis映射器
     */
    public ShipmentStore(ShipmentMapper mapper) {
        this.mapper = mapper;
    }

    public Carrier saveCarrier(Carrier carrier) {
        CarrierDO existing = mapper.selectCarrier(carrier.tenantId(), carrier.carrierId());
        CarrierDO data = toCarrierData(carrier);
        if (existing == null) {
            mapper.insertCarrier(data);
        } else {
            mapper.updateCarrier(data);
        }
        return carrier;
    }

    public Optional<Carrier> findCarrier(String tenantId, String carrierId) {
        return Optional.ofNullable(mapper.selectCarrier(tenantId, carrierId))
                .map(this::toCarrierDomain);
    }

    public List<Carrier> listCarriers(String tenantId) {
        return mapper.selectCarriers(tenantId).stream()
                .map(this::toCarrierDomain).collect(Collectors.toList());
    }

    public Shipment saveShipment(Shipment shipment) {
        ShipmentDO existing = mapper.selectShipment(shipment.tenantId(), shipment.shipmentId());
        ShipmentDO data = toShipmentData(shipment);
        if (existing == null) {
            mapper.insertShipment(data);
        } else {
            mapper.updateShipment(data);
        }
        syncTrackingEvents(shipment);
        return shipment;
    }

    public Optional<Shipment> findShipment(String tenantId, String shipmentId) {
        ShipmentDO d = mapper.selectShipment(tenantId, shipmentId);
        if (d == null) return Optional.empty();
        List<TrackingEvent> events = loadTrackingEvents(d.getTenantId(), d.getShipmentId());
        return Optional.of(toShipmentDomain(d, events));
    }

    public Optional<Shipment> findByTrackingNo(String tenantId, String trackingNo) {
        ShipmentDO d = mapper.selectShipmentByTrackingNo(tenantId, trackingNo);
        if (d == null) return Optional.empty();
        List<TrackingEvent> events = loadTrackingEvents(d.getTenantId(), d.getShipmentId());
        return Optional.of(toShipmentDomain(d, events));
    }

    public List<Shipment> listShipments(String tenantId) {
        return mapper.selectShipments(tenantId).stream()
                .map(d -> toShipmentDomain(d, loadTrackingEvents(d.getTenantId(), d.getShipmentId())))
                .collect(Collectors.toList());
    }

    public ShippingCost saveShippingCost(ShippingCost cost) {
        ShippingCostDO data = new ShippingCostDO();
        data.setCostId(cost.costId());
        data.setTenantId(cost.tenantId());
        data.setShipmentId(cost.shipmentId());
        data.setCarrierId(cost.carrierId());
        data.setFreightCost(cost.freightCost());
        data.setFuelSurcharge(cost.fuelSurcharge());
        data.setOtherFees(cost.otherFees());
        data.setTotalCost(cost.totalCost());
        data.setCurrency(cost.currency());
        data.setCreatedAt(cost.createdAt() != null ? cost.createdAt() : Instant.now());
        mapper.insertShippingCost(data);
        return cost;
    }

    public List<ShippingCost> listShippingCosts(String tenantId) {
        return mapper.selectShippingCosts(tenantId).stream()
                .map(this::toCostDomain).collect(Collectors.toList());
    }

    public List<ShippingCost> listShippingCostsByCarrier(String tenantId, String carrierId) {
        return mapper.selectShippingCostsByCarrier(tenantId, carrierId).stream()
                .map(this::toCostDomain).collect(Collectors.toList());
    }

    private List<TrackingEvent> loadTrackingEvents(String tenantId, String shipmentId) {
        List<TrackingEventDO> eventDOs = mapper.selectTrackingEvents(tenantId, shipmentId);
        if (eventDOs == null) return Collections.emptyList();
        return eventDOs.stream()
                .map(e -> new TrackingEvent(e.getEventId(), e.getStatus(), e.getLocation(), e.getDescription(), e.getOccurredAt()))
                .collect(Collectors.toList());
    }

    private void syncTrackingEvents(Shipment shipment) {
        List<TrackingEventDO> existingEvents = mapper.selectTrackingEvents(shipment.tenantId(), shipment.shipmentId());
        int existingCount = existingEvents == null ? 0 : existingEvents.size();
        if (shipment.trackingEvents() == null || shipment.trackingEvents().size() <= existingCount) {
            return;
        }
        shipment.trackingEvents().stream()
                .skip(existingCount)
                .map(event -> toTrackingEventData(shipment.tenantId(), shipment.shipmentId(), event))
                .forEach(mapper::insertTrackingEvent);
    }

    private TrackingEventDO toTrackingEventData(String tenantId, String shipmentId, TrackingEvent event) {
        TrackingEventDO data = new TrackingEventDO();
        data.setTenantId(tenantId);
        data.setShipmentId(shipmentId);
        data.setEventId(event.eventId());
        data.setStatus(event.status());
        data.setLocation(event.location());
        data.setDescription(event.description());
        data.setOccurredAt(event.occurredAt());
        return data;
    }

    private ShipmentDO toShipmentData(Shipment s) {
        ShipmentDO data = new ShipmentDO();
        data.setShipmentId(s.shipmentId());
        data.setTenantId(s.tenantId());
        data.setOrderId(s.orderId());
        data.setWarehouseId(s.warehouseId());
        data.setCarrierId(s.carrierId());
        data.setShippingMethodId(s.shippingMethodId());
        data.setTrackingNo(s.trackingNo());
        data.setDestinationCountry(s.destinationCountry());
        data.setWeight(s.weight());
        data.setLength(s.length());
        data.setWidth(s.width());
        data.setHeight(s.height());
        data.setEstimatedDelivery(s.estimatedDelivery());
        data.setActualDelivery(s.actualDelivery());
        data.setStatus(s.status().name());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private Shipment toShipmentDomain(ShipmentDO d, List<TrackingEvent> events) {
        return new Shipment(d.getShipmentId(), d.getTenantId(), d.getOrderId(), d.getWarehouseId(),
                d.getCarrierId(), d.getShippingMethodId(), d.getTrackingNo(), d.getDestinationCountry(),
                d.getWeight(), d.getLength(), d.getWidth(), d.getHeight(),
                d.getEstimatedDelivery(), d.getActualDelivery(),
                ShipmentStatus.valueOf(d.getStatus()), events, d.getCreatedAt(), d.getUpdatedAt());
    }

    private CarrierDO toCarrierData(Carrier c) {
        CarrierDO data = new CarrierDO();
        data.setCarrierId(c.carrierId());
        data.setTenantId(c.tenantId());
        data.setCode(c.code());
        data.setName(c.name());
        data.setCountryCode(c.countryCode());
        data.setType(c.type());
        data.setStatus(c.status());
        data.setContactPerson(c.contactPerson());
        data.setPhone(c.phone());
        data.setApiEnabled(c.apiEnabled());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private Carrier toCarrierDomain(CarrierDO d) {
        return new Carrier(d.getCarrierId(), d.getTenantId(), d.getCode(), d.getName(), d.getCountryCode(),
                d.getType(), d.getStatus(), d.getContactPerson(), d.getPhone(),
                d.getApiEnabled() != null && d.getApiEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ShippingCost toCostDomain(ShippingCostDO d) {
        return new ShippingCost(d.getCostId(), d.getTenantId(), d.getShipmentId(), d.getCarrierId(),
                d.getFreightCost(), d.getFuelSurcharge(), d.getOtherFees(), d.getTotalCost(), d.getCurrency(), d.getCreatedAt());
    }
}
