package com.aidotnet.erp.oms.infrastructure;

import com.aidotnet.erp.oms.domain.FulfillmentPackageStatus;
import com.aidotnet.erp.oms.domain.FulfillmentPlanStatus;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPackage;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPackageLine;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPlan;
import com.aidotnet.erp.oms.domain.PlatformShipmentSyncStatus;
import com.aidotnet.erp.oms.infrastructure.data.FulfillmentPackageDO;
import com.aidotnet.erp.oms.infrastructure.data.FulfillmentPackageLineDO;
import com.aidotnet.erp.oms.infrastructure.data.FulfillmentPlanDO;
import com.aidotnet.erp.oms.infrastructure.mapper.FulfillmentPlanMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * OMS域履约计划数据存储
 * <p>
 * 描述: 履约计划数据存储层，负责履约计划、履约包裹、包裹行等实体的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class FulfillmentPlanStore {

    /** 履约计划MyBatis映射器 */
    private final FulfillmentPlanMapper mapper;

    public FulfillmentPlanStore(FulfillmentPlanMapper mapper) {
        this.mapper = mapper;
    }

    public OrderFulfillmentPlan save(OrderFulfillmentPlan plan) {
        FulfillmentPlanDO existing = mapper.selectPlanByOrderId(plan.tenantId(), plan.orderId());
        FulfillmentPlanDO data = toPlanData(plan);
        if (existing == null) {
            mapper.insertPlan(data);
        } else {
            mapper.updatePlan(data);
            mapper.deletePackageLinesByPlanId(plan.planId());
            mapper.deletePackagesByPlanId(plan.planId());
        }
        for (OrderFulfillmentPackage fulfillmentPackage : plan.packages()) {
            mapper.insertPackage(toPackageData(fulfillmentPackage));
            for (OrderFulfillmentPackageLine line : fulfillmentPackage.lines()) {
                mapper.insertPackageLine(toPackageLineData(line, fulfillmentPackage.packageId()));
            }
        }
        return findByOrderId(plan.tenantId(), plan.orderId()).orElse(plan);
    }

    public Optional<OrderFulfillmentPlan> findByOrderId(String tenantId, String orderId) {
        return Optional.ofNullable(mapper.selectPlanByOrderId(tenantId, orderId)).map(this::toPlanDomain);
    }

    private OrderFulfillmentPlan toPlanDomain(FulfillmentPlanDO data) {
        List<OrderFulfillmentPackage> packages = mapper.selectPackagesByPlanId(data.getPlanId()).stream()
                .map(this::toPackageDomain)
                .toList();
        return new OrderFulfillmentPlan(
                data.getPlanId(),
                data.getTenantId(),
                data.getOrderId(),
                FulfillmentPlanStatus.valueOf(data.getStatus()),
                Boolean.TRUE.equals(data.getSplitShipment()),
                Boolean.TRUE.equals(data.getPartialShipment()),
                data.getEstimatedShippingCost(),
                data.getCreatedAt(),
                data.getUpdatedAt(),
                packages);
    }

    private OrderFulfillmentPackage toPackageDomain(FulfillmentPackageDO data) {
        List<OrderFulfillmentPackageLine> lines = mapper.selectPackageLinesByPackageId(data.getPackageId()).stream()
                .map(this::toPackageLineDomain)
                .toList();
        return new OrderFulfillmentPackage(
                data.getPackageId(),
                data.getPlanId(),
                data.getWarehouseId(),
                data.getWarehouseCode(),
                data.getCarrierId(),
                data.getCarrierCode(),
                data.getCarrierName(),
                data.getDestinationCountry(),
                data.getServiceLevel(),
                FulfillmentPackageStatus.valueOf(data.getStatus()),
                data.getTotalQuantity() != null ? data.getTotalQuantity() : 0,
                data.getTotalAmount(),
                data.getEstimatedShippingCost(),
                data.getEstimatedDeliveryDays(),
                data.getNote(),
                data.getShipmentId(),
                data.getTrackingNo(),
                data.getShippedAt(),
                data.getPlatformSyncStatus() == null || data.getPlatformSyncStatus().isBlank()
                        ? PlatformShipmentSyncStatus.NOT_SYNCED
                        : PlatformShipmentSyncStatus.valueOf(data.getPlatformSyncStatus()),
                data.getPlatformSyncAttempts() != null ? data.getPlatformSyncAttempts() : 0,
                data.getPlatformSyncError(),
                data.getPlatformSyncedAt(),
                lines);
    }

    private OrderFulfillmentPackageLine toPackageLineDomain(FulfillmentPackageLineDO data) {
        return new OrderFulfillmentPackageLine(
                data.getPackageLineId(),
                data.getOrderLineId(),
                data.getSellerSku(),
                data.getTitle(),
                data.getQuantity() != null ? data.getQuantity() : 0,
                data.getUnitPrice(),
                data.getLineAmount());
    }

    private FulfillmentPlanDO toPlanData(OrderFulfillmentPlan plan) {
        FulfillmentPlanDO data = new FulfillmentPlanDO();
        data.setPlanId(plan.planId());
        data.setTenantId(plan.tenantId());
        data.setOrderId(plan.orderId());
        data.setStatus(plan.status().name());
        data.setSplitShipment(plan.splitShipment());
        data.setPartialShipment(plan.partialShipment());
        data.setEstimatedShippingCost(plan.estimatedShippingCost());
        data.setCreatedAt(plan.createdAt());
        data.setUpdatedAt(plan.updatedAt());
        return data;
    }

    private FulfillmentPackageDO toPackageData(OrderFulfillmentPackage fulfillmentPackage) {
        FulfillmentPackageDO data = new FulfillmentPackageDO();
        data.setPackageId(fulfillmentPackage.packageId());
        data.setPlanId(fulfillmentPackage.planId());
        data.setWarehouseId(fulfillmentPackage.warehouseId());
        data.setWarehouseCode(fulfillmentPackage.warehouseCode());
        data.setCarrierId(fulfillmentPackage.carrierId());
        data.setCarrierCode(fulfillmentPackage.carrierCode());
        data.setCarrierName(fulfillmentPackage.carrierName());
        data.setDestinationCountry(fulfillmentPackage.destinationCountry());
        data.setServiceLevel(fulfillmentPackage.serviceLevel());
        data.setStatus(fulfillmentPackage.status().name());
        data.setTotalQuantity(fulfillmentPackage.totalQuantity());
        data.setTotalAmount(fulfillmentPackage.totalAmount());
        data.setEstimatedShippingCost(fulfillmentPackage.estimatedShippingCost());
        data.setEstimatedDeliveryDays(fulfillmentPackage.estimatedDeliveryDays());
        data.setNote(fulfillmentPackage.note());
        data.setShipmentId(fulfillmentPackage.shipmentId());
        data.setTrackingNo(fulfillmentPackage.trackingNo());
        data.setShippedAt(fulfillmentPackage.shippedAt());
        data.setPlatformSyncStatus(fulfillmentPackage.platformSyncStatus().name());
        data.setPlatformSyncAttempts(fulfillmentPackage.platformSyncAttempts());
        data.setPlatformSyncError(fulfillmentPackage.platformSyncError());
        data.setPlatformSyncedAt(fulfillmentPackage.platformSyncedAt());
        return data;
    }

    private FulfillmentPackageLineDO toPackageLineData(OrderFulfillmentPackageLine line, String packageId) {
        FulfillmentPackageLineDO data = new FulfillmentPackageLineDO();
        data.setPackageLineId(line.packageLineId());
        data.setPackageId(packageId);
        data.setOrderLineId(line.orderLineId());
        data.setSellerSku(line.sellerSku());
        data.setTitle(line.title());
        data.setQuantity(line.quantity());
        data.setUnitPrice(line.unitPrice());
        data.setLineAmount(line.lineAmount());
        return data;
    }
}
