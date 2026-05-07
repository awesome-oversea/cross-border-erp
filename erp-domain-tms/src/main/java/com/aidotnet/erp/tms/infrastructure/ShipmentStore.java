package com.aidotnet.erp.tms.infrastructure;

import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShipmentStatus;
import com.aidotnet.erp.tms.domain.ShippingCost;
import com.aidotnet.erp.tms.domain.ShippingMethod;
import com.aidotnet.erp.tms.domain.ShippingRate;
import com.aidotnet.erp.tms.domain.TrackingEvent;
import com.aidotnet.erp.tms.infrastructure.data.CarrierDO;
import com.aidotnet.erp.tms.infrastructure.data.ShipmentDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingCostDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingMethodDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingRateDO;
import com.aidotnet.erp.tms.infrastructure.data.TrackingEventDO;
import com.aidotnet.erp.tms.infrastructure.mapper.ShipmentMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * TMS鍩熷彂璐ф暟鎹瓨鍌? * <p>
 * 鎻忚堪: 鐗╂祦鍩熸牳蹇冩暟鎹瓨鍌ㄥ眰锛岃礋璐ｅ彂璐у崟銆佹壙杩愬晢銆佽繍璐规垚鏈€佺墿娴佽建杩圭瓑瀹炰綋鐨凜RUD鎿嶄綔銆? * </p>
 *
 * @author ERP绯荤粺
 */
@Repository
public class ShipmentStore {

    /** 鍙戣揣鏁版嵁MyBatis鏄犲皠鍣?*/
    private final ShipmentMapper mapper;

    /**
     * 鏋勯€犲嚱鏁?- 渚濊禆娉ㄥ叆鏄犲皠鍣?     *
     * @param mapper 鍙戣揣MyBatis鏄犲皠鍣?     */
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

    public Optional<Carrier> findCarrierByCode(String tenantId, String code) {
        return Optional.ofNullable(mapper.selectCarrierByCode(tenantId, code))
                .map(this::toCarrierDomain);
    }

    public List<Carrier> listCarriers(String tenantId) {
        return mapper.selectCarriers(tenantId).stream()
                .map(this::toCarrierDomain)
                .collect(Collectors.toList());
    }

    public List<ShippingMethod> listShippingMethods(String tenantId) {
        return mapper.selectShippingMethods(tenantId).stream()
                .map(this::toShippingMethodDomain)
                .collect(Collectors.toList());
    }

    public ShippingMethod saveShippingMethod(ShippingMethod method) {
        ShippingMethodDO existing = mapper.selectShippingMethod(method.tenantId(), method.methodId());
        ShippingMethodDO data = toShippingMethodData(method);
        if (existing == null) {
            mapper.insertShippingMethod(data);
        } else {
            mapper.updateShippingMethod(data);
        }
        return method;
    }

    public Optional<ShippingMethod> findShippingMethod(String tenantId, String methodId) {
        return Optional.ofNullable(mapper.selectShippingMethod(tenantId, methodId))
                .map(this::toShippingMethodDomain);
    }

    public Optional<ShippingMethod> findShippingMethodByCode(String tenantId, String carrierId, String methodCode) {
        return Optional.ofNullable(mapper.selectShippingMethodByCode(tenantId, carrierId, methodCode))
                .map(this::toShippingMethodDomain);
    }

    public List<ShippingMethod> listShippingMethodsByCarrier(String tenantId, String carrierId) {
        return mapper.selectShippingMethodsByCarrier(tenantId, carrierId).stream()
                .map(this::toShippingMethodDomain)
                .collect(Collectors.toList());
    }

    public ShippingRate saveShippingRate(ShippingRate rate) {
        ShippingRateDO existing = mapper.selectShippingRate(rate.tenantId(), rate.rateId());
        ShippingRateDO data = toShippingRateData(rate);
        if (existing == null) {
            mapper.insertShippingRate(data);
        } else {
            mapper.updateShippingRate(data);
        }
        return rate;
    }

    public Optional<ShippingRate> findShippingRate(String tenantId, String rateId) {
        return Optional.ofNullable(mapper.selectShippingRate(tenantId, rateId))
                .map(this::toShippingRateDomain);
    }

    public List<ShippingRate> listShippingRatesByMethod(String tenantId, String methodId) {
        return mapper.selectShippingRatesByMethod(tenantId, methodId).stream()
                .map(this::toShippingRateDomain)
                .collect(Collectors.toList());
    }

    public List<ShippingRate> listShippingRates(String tenantId) {
        return mapper.selectShippingRates(tenantId).stream()
                .map(this::toShippingRateDomain)
                .collect(Collectors.toList());
    }

    public List<ShippingRate> listShippingRatesByRoute(String tenantId, String originCountry, String destinationCountry) {
        return mapper.selectShippingRatesByRoute(tenantId, originCountry, destinationCountry).stream()
                .map(this::toShippingRateDomain)
                .collect(Collectors.toList());
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
        ShipmentDO data = mapper.selectShipment(tenantId, shipmentId);
        if (data == null) {
            return Optional.empty();
        }
        List<TrackingEvent> events = loadTrackingEvents(data.getTenantId(), data.getShipmentId());
        return Optional.of(toShipmentDomain(data, events));
    }

    public Optional<Shipment> findByTrackingNo(String tenantId, String trackingNo) {
        ShipmentDO data = mapper.selectShipmentByTrackingNo(tenantId, trackingNo);
        if (data == null) {
            return Optional.empty();
        }
        List<TrackingEvent> events = loadTrackingEvents(data.getTenantId(), data.getShipmentId());
        return Optional.of(toShipmentDomain(data, events));
    }

    public List<Shipment> listShipments(String tenantId) {
        return mapper.selectShipments(tenantId).stream()
                .map(data -> toShipmentDomain(data, loadTrackingEvents(data.getTenantId(), data.getShipmentId())))
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
                .map(this::toCostDomain)
                .collect(Collectors.toList());
    }

    public List<ShippingCost> listShippingCostsByCarrier(String tenantId, String carrierId) {
        return mapper.selectShippingCostsByCarrier(tenantId, carrierId).stream()
                .map(this::toCostDomain)
                .collect(Collectors.toList());
    }

    public Optional<ShippingCost> findLatestShippingCostByShipment(String tenantId, String shipmentId) {
        return Optional.ofNullable(mapper.selectLatestShippingCostByShipment(tenantId, shipmentId))
                .map(this::toCostDomain);
    }

    private List<TrackingEvent> loadTrackingEvents(String tenantId, String shipmentId) {
        List<TrackingEventDO> eventDOs = mapper.selectTrackingEvents(tenantId, shipmentId);
        if (eventDOs == null) {
            return Collections.emptyList();
        }
        return eventDOs.stream()
                .map(event -> new TrackingEvent(
                        event.getEventId(),
                        event.getStatus(),
                        event.getLocation(),
                        event.getDescription(),
                        event.getOccurredAt()))
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

    private ShipmentDO toShipmentData(Shipment shipment) {
        ShipmentDO data = new ShipmentDO();
        data.setShipmentId(shipment.shipmentId());
        data.setTenantId(shipment.tenantId());
        data.setOrderId(shipment.orderId());
        data.setWarehouseId(shipment.warehouseId());
        data.setCarrierId(shipment.carrierId());
        data.setShippingMethodId(shipment.shippingMethodId());
        data.setTrackingNo(shipment.trackingNo());
        data.setDestinationCountry(shipment.destinationCountry());
        data.setWeight(shipment.weight());
        data.setLength(shipment.length());
        data.setWidth(shipment.width());
        data.setHeight(shipment.height());
        data.setEstimatedDelivery(shipment.estimatedDelivery());
        data.setActualDelivery(shipment.actualDelivery());
        data.setEstimatedFreight(shipment.estimatedFreight());
        data.setEstimatedFreightCurrency(shipment.estimatedFreightCurrency());
        data.setEstimatedChargeableWeight(shipment.estimatedChargeableWeight());
        data.setEstimatedAt(shipment.estimatedAt());
        data.setStatus(shipment.status().name());
        data.setCreatedAt(shipment.createdAt() != null ? shipment.createdAt() : Instant.now());
        data.setUpdatedAt(shipment.updatedAt() != null ? shipment.updatedAt() : Instant.now());
        return data;
    }

    private Shipment toShipmentDomain(ShipmentDO data, List<TrackingEvent> events) {
        return new Shipment(
                data.getShipmentId(),
                data.getTenantId(),
                data.getOrderId(),
                data.getWarehouseId(),
                data.getCarrierId(),
                data.getShippingMethodId(),
                data.getTrackingNo(),
                data.getDestinationCountry(),
                data.getWeight(),
                data.getLength(),
                data.getWidth(),
                data.getHeight(),
                data.getEstimatedDelivery(),
                data.getActualDelivery(),
                data.getEstimatedFreight(),
                data.getEstimatedFreightCurrency(),
                data.getEstimatedChargeableWeight(),
                data.getEstimatedAt(),
                ShipmentStatus.valueOf(data.getStatus()),
                events,
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private CarrierDO toCarrierData(Carrier carrier) {
        CarrierDO data = new CarrierDO();
        data.setCarrierId(carrier.carrierId());
        data.setTenantId(carrier.tenantId());
        data.setCode(carrier.code());
        data.setName(carrier.name());
        data.setCountryCode(carrier.countryCode());
        data.setType(carrier.type());
        data.setStatus(carrier.status());
        data.setContactPerson(carrier.contactPerson());
        data.setPhone(carrier.phone());
        data.setApiEnabled(carrier.apiEnabled());
        data.setFeatured(carrier.featured());
        data.setAuthorizationStatus(carrier.authorizationStatus());
        data.setAuthorizationValidUntil(carrier.authorizationValidUntil());
        data.setLastAuthorizedAt(carrier.lastAuthorizedAt());
        data.setCreatedAt(carrier.createdAt() != null ? carrier.createdAt() : Instant.now());
        data.setUpdatedAt(carrier.updatedAt() != null ? carrier.updatedAt() : Instant.now());
        return data;
    }

    private Carrier toCarrierDomain(CarrierDO data) {
        return new Carrier(
                data.getCarrierId(),
                data.getTenantId(),
                data.getCode(),
                data.getName(),
                data.getCountryCode(),
                data.getType(),
                data.getStatus(),
                data.getContactPerson(),
                data.getPhone(),
                data.getApiEnabled() != null && data.getApiEnabled(),
                data.getFeatured() != null && data.getFeatured(),
                data.getAuthorizationStatus(),
                data.getAuthorizationValidUntil(),
                data.getLastAuthorizedAt(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private ShippingMethodDO toShippingMethodData(ShippingMethod method) {
        ShippingMethodDO data = new ShippingMethodDO();
        data.setMethodId(method.methodId());
        data.setTenantId(method.tenantId());
        data.setCarrierId(method.carrierId());
        data.setMethodCode(method.methodCode());
        data.setMethodName(method.methodName());
        data.setTransportMode(method.transportMode());
        data.setRateType(method.rateType());
        data.setEnabled(method.enabled());
        data.setEstimatedDaysMin(method.estimatedDaysMin());
        data.setEstimatedDaysMax(method.estimatedDaysMax());
        data.setCreatedAt(method.createdAt() != null ? method.createdAt() : Instant.now());
        data.setUpdatedAt(method.updatedAt() != null ? method.updatedAt() : Instant.now());
        return data;
    }

    private ShippingMethod toShippingMethodDomain(ShippingMethodDO data) {
        return new ShippingMethod(
                data.getMethodId(),
                data.getTenantId(),
                data.getCarrierId(),
                data.getMethodCode(),
                data.getMethodName(),
                data.getTransportMode(),
                data.getRateType(),
                data.getEnabled() != null && data.getEnabled(),
                data.getEstimatedDaysMin(),
                data.getEstimatedDaysMax(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private ShippingRateDO toShippingRateData(ShippingRate rate) {
        ShippingRateDO data = new ShippingRateDO();
        data.setRateId(rate.rateId());
        data.setTenantId(rate.tenantId());
        data.setMethodId(rate.methodId());
        data.setOriginCountry(rate.originCountry());
        data.setDestinationCountry(rate.destinationCountry());
        data.setZoneCode(rate.zoneCode());
        data.setWeightMinKg(rate.weightMinKg());
        data.setWeightMaxKg(rate.weightMaxKg());
        data.setBaseCost(rate.baseCost());
        data.setCostPerKg(rate.costPerKg());
        data.setCurrency(rate.currency());
        data.setEffectiveFrom(rate.effectiveFrom());
        data.setEffectiveTo(rate.effectiveTo());
        data.setCreatedAt(rate.createdAt() != null ? rate.createdAt() : Instant.now());
        data.setUpdatedAt(rate.updatedAt() != null ? rate.updatedAt() : Instant.now());
        return data;
    }

    private ShippingRate toShippingRateDomain(ShippingRateDO data) {
        return new ShippingRate(
                data.getRateId(),
                data.getTenantId(),
                data.getMethodId(),
                data.getOriginCountry(),
                data.getDestinationCountry(),
                data.getZoneCode(),
                data.getWeightMinKg(),
                data.getWeightMaxKg(),
                data.getBaseCost(),
                data.getCostPerKg(),
                data.getCurrency(),
                data.getEffectiveFrom(),
                data.getEffectiveTo(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private ShippingCost toCostDomain(ShippingCostDO data) {
        return new ShippingCost(
                data.getCostId(),
                data.getTenantId(),
                data.getShipmentId(),
                data.getCarrierId(),
                data.getFreightCost(),
                data.getFuelSurcharge(),
                data.getOtherFees(),
                data.getTotalCost(),
                data.getCurrency(),
                data.getCreatedAt());
    }
}
