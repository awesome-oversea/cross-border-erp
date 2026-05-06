package com.aidotnet.erp.tms.infrastructure.mapper;

import com.aidotnet.erp.tms.infrastructure.data.CarrierDO;
import com.aidotnet.erp.tms.infrastructure.data.ShipmentDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingBatchDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingCostDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingMethodDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingRateDO;
import com.aidotnet.erp.tms.infrastructure.data.TrackingEventDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * TMS域发货数据MyBatis映射器
 * <p>
 * 描述: 物流域核心数据访问层，提供承运商、发货单、物流轨迹、运费成本、物流渠道、运费费率、批量发货等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface ShipmentMapper {

    // ---- 承运商 ----

    /** 新增承运商 */
    void insertCarrier(CarrierDO carrier);
    /** 更新承运商 */
    void updateCarrier(CarrierDO carrier);
    /** 按租户+承运商ID查询 */
    CarrierDO selectCarrier(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);
    /** 按租户查询承运商列表 */
    List<CarrierDO> selectCarriers(@Param("tenantId") String tenantId);

    // ---- 发货单 ----

    /** 新增发货单 */
    void insertShipment(ShipmentDO shipment);
    /** 更新发货单 */
    void updateShipment(ShipmentDO shipment);
    /** 按租户+发货单ID查询 */
    ShipmentDO selectShipment(@Param("tenantId") String tenantId, @Param("shipmentId") String shipmentId);
    /** 按租户+追踪号查询发货单 */
    ShipmentDO selectShipmentByTrackingNo(@Param("tenantId") String tenantId, @Param("trackingNo") String trackingNo);
    /** 按租户查询发货单列表 */
    List<ShipmentDO> selectShipments(@Param("tenantId") String tenantId);

    // ---- 物流轨迹 ----

    /** 新增物流轨迹事件 */
    void insertTrackingEvent(TrackingEventDO event);
    /** 按租户+发货单ID查询轨迹列表 */
    List<TrackingEventDO> selectTrackingEvents(@Param("tenantId") String tenantId, @Param("shipmentId") String shipmentId);

    // ---- 运费成本 ----

    /** 新增运费成本 */
    void insertShippingCost(ShippingCostDO cost);
    /** 按租户查询运费成本列表 */
    List<ShippingCostDO> selectShippingCosts(@Param("tenantId") String tenantId);
    /** 按租户+承运商查询运费成本列表 */
    List<ShippingCostDO> selectShippingCostsByCarrier(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);

    // ---- 物流渠道 ----

    /** 新增物流渠道 */
    void insertShippingMethod(ShippingMethodDO method);
    /** 更新物流渠道 */
    void updateShippingMethod(ShippingMethodDO method);
    /** 按租户+渠道ID查询 */
    ShippingMethodDO selectShippingMethod(@Param("tenantId") String tenantId, @Param("methodId") String methodId);
    /** 按租户查询渠道列表 */
    List<ShippingMethodDO> selectShippingMethods(@Param("tenantId") String tenantId);
    /** 按租户+承运商查询渠道列表 */
    List<ShippingMethodDO> selectShippingMethodsByCarrier(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);

    // ---- 运费费率 ----

    /** 新增运费费率 */
    void insertShippingRate(ShippingRateDO rate);
    /** 更新运费费率 */
    void updateShippingRate(ShippingRateDO rate);
    /** 按租户+费率ID查询 */
    ShippingRateDO selectShippingRate(@Param("tenantId") String tenantId, @Param("rateId") String rateId);
    /** 按租户+渠道ID查询费率列表 */
    List<ShippingRateDO> selectShippingRatesByMethod(@Param("tenantId") String tenantId, @Param("methodId") String methodId);

    // ---- 批量发货 ----

    /** 新增批量发货 */
    void insertShippingBatch(ShippingBatchDO batch);
    /** 更新批量发货 */
    void updateShippingBatch(ShippingBatchDO batch);
    /** 按租户+批次ID查询 */
    ShippingBatchDO selectShippingBatch(@Param("tenantId") String tenantId, @Param("batchId") String batchId);
    /** 按租户查询批量发货列表 */
    List<ShippingBatchDO> selectShippingBatches(@Param("tenantId") String tenantId);
}
