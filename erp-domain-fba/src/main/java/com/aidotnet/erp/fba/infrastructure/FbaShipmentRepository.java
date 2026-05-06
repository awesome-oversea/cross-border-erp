package com.aidotnet.erp.fba.infrastructure;

import com.aidotnet.erp.fba.domain.CartonLabel;
import com.aidotnet.erp.fba.domain.FbaInboundPlan;
import com.aidotnet.erp.fba.domain.FbaInboundPlanStatus;
import com.aidotnet.erp.fba.domain.FbaInventory;
import com.aidotnet.erp.fba.domain.FbaLocation;
import com.aidotnet.erp.fba.domain.FbaShipment;
import com.aidotnet.erp.fba.domain.FbaShipmentItem;
import com.aidotnet.erp.fba.domain.FbaShipmentStatus;
import com.aidotnet.erp.fba.domain.ReplenishmentPlan;
import com.aidotnet.erp.fba.domain.RestockCartItem;
import com.aidotnet.erp.fba.domain.RestockSuggestion;
import com.aidotnet.erp.fba.domain.ShipmentException;
import com.aidotnet.erp.fba.infrastructure.data.CartonLabelDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaInboundPlanDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaInventoryDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaLocationDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaShipmentDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaShipmentItemDO;
import com.aidotnet.erp.fba.infrastructure.data.ReplenishmentPlanDO;
import com.aidotnet.erp.fba.infrastructure.mapper.FbaShipmentMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * FBA域发货数据存储
 * <p>
 * 描述: FBA域核心数据存储层，负责FBA发货单、入库计划、FBA库存、补货计划等实体的CRUD操作。
 *       补货建议、补货购物车和发货异常暂用内存ConcurrentHashMap存储。
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class FbaShipmentRepository {

    /** FBA发货MyBatis映射器 */
    private final FbaShipmentMapper mapper;
    /** 补货建议内存存储(临时) */
    private final ConcurrentHashMap<String, RestockSuggestion> restockSuggestionStore = new ConcurrentHashMap<>();
    /** 补货购物车内存存储(临时) */
    private final ConcurrentHashMap<String, RestockCartItem> restockCartStore = new ConcurrentHashMap<>();
    /** 发货异常内存存储(临时) */
    private final ConcurrentHashMap<String, ShipmentException> shipmentExceptionStore = new ConcurrentHashMap<>();

    public FbaShipmentRepository(FbaShipmentMapper mapper) {
        this.mapper = mapper;
    }

    public FbaShipment save(FbaShipment shipment) {
        FbaShipmentDO existing = mapper.selectShipment(shipment.tenantId(), shipment.fbaShipmentId());
        FbaShipmentDO data = toShipmentData(shipment);
        if (existing == null) {
            mapper.insertShipment(data);
        } else {
            mapper.updateShipment(data);
        }
        return shipment;
    }

    public Optional<FbaShipment> find(String tenantId, String fbaShipmentId) {
        return Optional.ofNullable(mapper.selectShipment(tenantId, fbaShipmentId)).map(this::toShipmentDomain);
    }

    public Optional<FbaShipment> findByAmazonShipmentId(String tenantId, String amazonShipmentId) {
        return Optional.ofNullable(mapper.selectShipmentByAmazonId(tenantId, amazonShipmentId))
                .map(this::toShipmentDomain);
    }

    public List<FbaShipment> list(String tenantId, String planId) {
        return mapper.selectShipments(tenantId, planId).stream().map(this::toShipmentDomain).collect(Collectors.toList());
    }

    public FbaShipmentItem saveShipmentItem(FbaShipmentItem item) {
        mapper.insertShipmentItem(toShipmentItemData(item));
        return item;
    }

    public List<FbaShipmentItem> listShipmentItems(String tenantId, String shipmentId) {
        return mapper.selectShipmentItems(tenantId, shipmentId).stream()
                .map(this::toShipmentItemDomain)
                .collect(Collectors.toList());
    }

    public CartonLabel saveCartonLabel(CartonLabel label) {
        mapper.insertCartonLabel(toCartonLabelData(label));
        return label;
    }

    public List<CartonLabel> listCartonLabels(String tenantId, String fbaShipmentId) {
        return mapper.selectCartonLabels(tenantId, fbaShipmentId).stream()
                .map(this::toCartonLabelDomain)
                .collect(Collectors.toList());
    }

    public FbaInboundPlan saveInboundPlan(FbaInboundPlan plan) {
        FbaInboundPlanDO existing = mapper.selectInboundPlan(plan.tenantId(), plan.planId());
        FbaInboundPlanDO data = toInboundPlanData(plan);
        if (existing == null) {
            mapper.insertInboundPlan(data);
        } else {
            mapper.updateInboundPlan(data);
        }
        return plan;
    }

    public Optional<FbaInboundPlan> findInboundPlan(String tenantId, String planId) {
        return Optional.ofNullable(mapper.selectInboundPlan(tenantId, planId)).map(this::toInboundPlanDomain);
    }

    public List<FbaInboundPlan> listInboundPlans(String tenantId, String warehouseId, String status) {
        return mapper.selectInboundPlans(tenantId, warehouseId, status).stream()
                .map(this::toInboundPlanDomain)
                .collect(Collectors.toList());
    }

    public FbaLocation saveLocation(FbaLocation location) {
        mapper.insertLocation(toLocationData(location));
        return location;
    }

    public Optional<FbaLocation> findLocation(String tenantId, String locationId) {
        return Optional.ofNullable(mapper.selectLocation(tenantId, locationId)).map(this::toLocationDomain);
    }

    public List<FbaLocation> listLocations(String tenantId) {
        return mapper.selectLocations(tenantId).stream().map(this::toLocationDomain).collect(Collectors.toList());
    }

    public FbaInventory saveInventory(FbaInventory inventory) {
        FbaInventoryDO existing = mapper.selectInventory(inventory.tenantId(), inventory.inventoryId());
        FbaInventoryDO data = toInventoryData(inventory);
        if (existing == null) {
            mapper.insertInventory(data);
        } else {
            mapper.updateInventory(data);
        }
        return inventory;
    }

    public Optional<FbaInventory> findInventory(String tenantId, String inventoryId) {
        return Optional.ofNullable(mapper.selectInventory(tenantId, inventoryId)).map(this::toInventoryDomain);
    }

    public Optional<FbaInventory> findInventoryByKey(String tenantId, String warehouseId, String sellerSku, String fnsku,
                                                     String storeId, String siteCode) {
        return Optional.ofNullable(mapper.selectInventoryByKey(tenantId, warehouseId, sellerSku, fnsku, storeId, siteCode))
                .map(this::toInventoryDomain);
    }

    public List<FbaInventory> listInventories(String tenantId, String sellerSku, String storeId, String siteCode,
                                              String warehouseId) {
        return mapper.selectInventories(tenantId, sellerSku, storeId, siteCode, warehouseId).stream()
                .map(this::toInventoryDomain)
                .collect(Collectors.toList());
    }

    public ReplenishmentPlan savePlan(ReplenishmentPlan plan) {
        ReplenishmentPlanDO existing = mapper.selectPlan(plan.tenantId(), plan.planId());
        ReplenishmentPlanDO data = toPlanData(plan);
        if (existing == null) {
            mapper.insertPlan(data);
        } else {
            mapper.updatePlan(data);
        }
        return plan;
    }

    public Optional<ReplenishmentPlan> findPlan(String tenantId, String planId) {
        return Optional.ofNullable(mapper.selectPlan(tenantId, planId)).map(this::toPlanDomain);
    }

    public List<ReplenishmentPlan> listPlans(String tenantId) {
        return mapper.selectPlans(tenantId).stream().map(this::toPlanDomain).collect(Collectors.toList());
    }

    private FbaShipmentDO toShipmentData(FbaShipment shipment) {
        FbaShipmentDO data = new FbaShipmentDO();
        data.setFbaShipmentId(shipment.fbaShipmentId());
        data.setTenantId(shipment.tenantId());
        data.setAmazonShipmentId(shipment.amazonShipmentId());
        data.setDestinationFc(shipment.destinationFc());
        data.setPlanId(shipment.planId());
        data.setCarrier(shipment.carrier());
        data.setTrackingNo(shipment.trackingNo());
        data.setPlannedQuantity(shipment.plannedQuantity());
        data.setReceivedQuantity(shipment.receivedQuantity());
        data.setCartonCount(shipment.cartonCount());
        data.setTotalWeight(shipment.totalWeight());
        data.setStatus(shipment.status().name());
        data.setPackedAt(shipment.packedAt());
        data.setShippedAt(shipment.shippedAt());
        data.setCreatedAt(shipment.createdAt() != null ? shipment.createdAt() : Instant.now());
        data.setUpdatedAt(shipment.updatedAt() != null ? shipment.updatedAt() : Instant.now());
        return data;
    }

    private FbaShipment toShipmentDomain(FbaShipmentDO data) {
        return new FbaShipment(
                data.getFbaShipmentId(),
                data.getTenantId(),
                data.getAmazonShipmentId(),
                data.getDestinationFc(),
                data.getPlanId(),
                data.getCarrier(),
                data.getTrackingNo(),
                defaultInt(data.getPlannedQuantity()),
                defaultInt(data.getReceivedQuantity()),
                defaultInt(data.getCartonCount()),
                data.getTotalWeight() != null ? data.getTotalWeight() : BigDecimal.ZERO,
                FbaShipmentStatus.valueOf(data.getStatus()),
                data.getPackedAt(),
                data.getShippedAt(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private FbaShipmentItemDO toShipmentItemData(FbaShipmentItem item) {
        FbaShipmentItemDO data = new FbaShipmentItemDO();
        data.setItemId(item.itemId());
        data.setTenantId(item.tenantId());
        data.setShipmentId(item.shipmentId());
        data.setProductId(item.productId());
        data.setSellerSku(item.sellerSku());
        data.setFnsku(item.fnsku());
        data.setQuantity(item.quantity());
        data.setBoxQuantity(item.boxQuantity());
        data.setCreatedAt(item.createdAt() != null ? item.createdAt() : Instant.now());
        return data;
    }

    private FbaShipmentItem toShipmentItemDomain(FbaShipmentItemDO data) {
        return new FbaShipmentItem(
                data.getItemId(),
                data.getTenantId(),
                data.getShipmentId(),
                data.getProductId(),
                data.getSellerSku(),
                data.getFnsku(),
                defaultInt(data.getQuantity()),
                defaultInt(data.getBoxQuantity()),
                data.getCreatedAt());
    }

    private CartonLabelDO toCartonLabelData(CartonLabel label) {
        CartonLabelDO data = new CartonLabelDO();
        data.setLabelId(label.labelId());
        data.setTenantId(label.tenantId());
        data.setFbaShipmentId(label.fbaShipmentId());
        data.setCartonId(label.cartonId());
        data.setSellerSku(label.sellerSku());
        data.setQuantityPerCarton(label.quantityPerCarton());
        data.setNumberOfCartons(label.numberOfCartons());
        data.setLabelUrl(label.labelUrl());
        data.setCreatedAt(label.createdAt() != null ? label.createdAt() : Instant.now());
        return data;
    }

    private CartonLabel toCartonLabelDomain(CartonLabelDO data) {
        return new CartonLabel(
                data.getLabelId(),
                data.getTenantId(),
                data.getFbaShipmentId(),
                data.getCartonId(),
                data.getSellerSku(),
                defaultInt(data.getQuantityPerCarton()),
                defaultInt(data.getNumberOfCartons()),
                data.getLabelUrl(),
                data.getCreatedAt());
    }

    private FbaInboundPlanDO toInboundPlanData(FbaInboundPlan plan) {
        FbaInboundPlanDO data = new FbaInboundPlanDO();
        data.setPlanId(plan.planId());
        data.setTenantId(plan.tenantId());
        data.setWarehouseId(plan.warehouseId());
        data.setPlanName(plan.planName());
        data.setSellerSku(plan.sellerSku());
        data.setPlannedQuantity(plan.plannedQuantity());
        data.setStoreId(plan.storeId());
        data.setSiteCode(plan.siteCode());
        data.setSourcePlanId(plan.sourcePlanId());
        data.setStatus(plan.status().name());
        data.setCreatedAt(plan.createdAt() != null ? plan.createdAt() : Instant.now());
        data.setUpdatedAt(plan.updatedAt() != null ? plan.updatedAt() : Instant.now());
        return data;
    }

    private FbaInboundPlan toInboundPlanDomain(FbaInboundPlanDO data) {
        return new FbaInboundPlan(
                data.getPlanId(),
                data.getTenantId(),
                data.getWarehouseId(),
                data.getPlanName(),
                data.getSellerSku(),
                defaultInt(data.getPlannedQuantity()),
                data.getStoreId(),
                data.getSiteCode(),
                data.getSourcePlanId(),
                FbaInboundPlanStatus.valueOf(data.getStatus()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private FbaLocationDO toLocationData(FbaLocation location) {
        FbaLocationDO data = new FbaLocationDO();
        data.setLocationId(location.locationId());
        data.setTenantId(location.tenantId());
        data.setName(location.name());
        data.setCountryCode(location.countryCode());
        data.setAddress(location.address());
        data.setLocationType(location.locationType());
        data.setStatus(location.status());
        data.setCreatedAt(location.createdAt() != null ? location.createdAt() : Instant.now());
        data.setUpdatedAt(location.updatedAt() != null ? location.updatedAt() : Instant.now());
        return data;
    }

    private FbaLocation toLocationDomain(FbaLocationDO data) {
        return new FbaLocation(
                data.getLocationId(),
                data.getTenantId(),
                data.getName(),
                data.getCountryCode(),
                data.getAddress(),
                data.getLocationType(),
                data.getStatus(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private FbaInventoryDO toInventoryData(FbaInventory inventory) {
        FbaInventoryDO data = new FbaInventoryDO();
        data.setInventoryId(inventory.inventoryId());
        data.setTenantId(inventory.tenantId());
        data.setWarehouseId(inventory.warehouseId());
        data.setProductId(inventory.productId());
        data.setSellerSku(inventory.sellerSku());
        data.setFnsku(inventory.fnsku());
        data.setQuantity(inventory.quantity());
        data.setStoreId(inventory.storeId());
        data.setSiteCode(inventory.siteCode());
        data.setInventoryAgeDays(inventory.inventoryAgeDays());
        data.setLastUpdated(inventory.lastUpdated() != null ? inventory.lastUpdated() : Instant.now());
        return data;
    }

    private FbaInventory toInventoryDomain(FbaInventoryDO data) {
        return new FbaInventory(
                data.getInventoryId(),
                data.getTenantId(),
                data.getWarehouseId(),
                data.getProductId(),
                data.getSellerSku(),
                data.getFnsku(),
                defaultInt(data.getQuantity()),
                data.getStoreId(),
                data.getSiteCode(),
                defaultInt(data.getInventoryAgeDays()),
                data.getLastUpdated());
    }

    private ReplenishmentPlanDO toPlanData(ReplenishmentPlan plan) {
        ReplenishmentPlanDO data = new ReplenishmentPlanDO();
        data.setPlanId(plan.planId());
        data.setTenantId(plan.tenantId());
        data.setSellerSku(plan.sellerSku());
        data.setDestinationFc(plan.destinationFc());
        data.setSuggestedQuantity(plan.suggestedQuantity());
        data.setSourceWarehouseId(plan.sourceWarehouseId());
        data.setStatus(plan.status().name());
        data.setCreatedAt(plan.createdAt() != null ? plan.createdAt() : Instant.now());
        data.setUpdatedAt(plan.updatedAt() != null ? plan.updatedAt() : Instant.now());
        return data;
    }

    private ReplenishmentPlan toPlanDomain(ReplenishmentPlanDO data) {
        return new ReplenishmentPlan(
                data.getPlanId(),
                data.getTenantId(),
                data.getSellerSku(),
                data.getDestinationFc(),
                defaultInt(data.getSuggestedQuantity()),
                data.getSourceWarehouseId(),
                ReplenishmentPlan.PlanStatus.valueOf(data.getStatus()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    public RestockSuggestion saveRestockSuggestion(RestockSuggestion suggestion) {
        restockSuggestionStore.put(suggestion.suggestionId(), suggestion);
        return suggestion;
    }

    public List<RestockSuggestion> listRestockSuggestions(String tenantId) {
        return restockSuggestionStore.values().stream()
                .filter(s -> s.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public RestockCartItem saveRestockCartItem(RestockCartItem item) {
        restockCartStore.put(item.cartItemId(), item);
        return item;
    }

    public List<RestockCartItem> listRestockCartItems(String tenantId, String userId) {
        return restockCartStore.values().stream()
                .filter(i -> i.tenantId().equals(tenantId) && (userId == null || i.userId().equals(userId)))
                .collect(Collectors.toList());
    }

    public void removeRestockCartItem(String cartItemId) {
        restockCartStore.remove(cartItemId);
    }

    public ShipmentException saveShipmentException(ShipmentException exception) {
        shipmentExceptionStore.put(exception.exceptionId(), exception);
        return exception;
    }

    public Optional<ShipmentException> findShipmentException(String tenantId, String exceptionId) {
        return Optional.ofNullable(shipmentExceptionStore.get(exceptionId))
                .filter(e -> e.tenantId().equals(tenantId));
    }

    public List<ShipmentException> listShipmentExceptions(String tenantId, String shipmentId) {
        return shipmentExceptionStore.values().stream()
                .filter(e -> e.tenantId().equals(tenantId))
                .filter(e -> shipmentId == null || e.shipmentId().equals(shipmentId))
                .collect(Collectors.toList());
    }
}
