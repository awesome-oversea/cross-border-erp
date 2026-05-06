package com.aidotnet.erp.fba.infrastructure.mapper;

import com.aidotnet.erp.fba.infrastructure.data.CartonLabelDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaInboundPlanDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaInventoryDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaLocationDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaShipmentDO;
import com.aidotnet.erp.fba.infrastructure.data.FbaShipmentItemDO;
import com.aidotnet.erp.fba.infrastructure.data.ReplenishmentPlanDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * FBA域发货数据MyBatis映射器
 * <p>
 * 描述: FBA域核心数据访问层，提供FBA发货单、发货单行、箱标、入库计划、
 *       FBA仓库位置、FBA库存、补货计划等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface FbaShipmentMapper {

    // ---- FBA发货单 ----

    /** 新增FBA发货单 */
    void insertShipment(FbaShipmentDO shipment);
    /** 更新FBA发货单 */
    void updateShipment(FbaShipmentDO shipment);
    /** 按租户+FBA发货单ID查询 */
    FbaShipmentDO selectShipment(@Param("tenantId") String tenantId, @Param("fbaShipmentId") String fbaShipmentId);
    /** 按租户+亚马逊发货ID查询 */
    FbaShipmentDO selectShipmentByAmazonId(@Param("tenantId") String tenantId, @Param("amazonShipmentId") String amazonShipmentId);
    /** 按租户+入库计划ID查询发货单列表 */
    List<FbaShipmentDO> selectShipments(@Param("tenantId") String tenantId, @Param("planId") String planId);

    // ---- FBA发货单行 ----

    /** 新增发货单行 */
    void insertShipmentItem(FbaShipmentItemDO item);
    /** 按租户+发货单ID查询发货单行列表 */
    List<FbaShipmentItemDO> selectShipmentItems(@Param("tenantId") String tenantId, @Param("shipmentId") String shipmentId);

    // ---- 箱标 ----

    /** 新增箱标 */
    void insertCartonLabel(CartonLabelDO label);
    /** 按租户+FBA发货单ID查询箱标列表 */
    List<CartonLabelDO> selectCartonLabels(@Param("tenantId") String tenantId, @Param("fbaShipmentId") String fbaShipmentId);

    // ---- FBA入库计划 ----

    /** 新增入库计划 */
    void insertInboundPlan(FbaInboundPlanDO plan);
    /** 更新入库计划 */
    void updateInboundPlan(FbaInboundPlanDO plan);
    /** 按租户+计划ID查询 */
    FbaInboundPlanDO selectInboundPlan(@Param("tenantId") String tenantId, @Param("planId") String planId);
    /** 按租户+仓库+状态查询入库计划列表 */
    List<FbaInboundPlanDO> selectInboundPlans(@Param("tenantId") String tenantId,
                                              @Param("warehouseId") String warehouseId,
                                              @Param("status") String status);

    // ---- FBA仓库位置 ----

    /** 新增FBA仓库位置 */
    void insertLocation(FbaLocationDO location);
    /** 按租户+位置ID查询 */
    FbaLocationDO selectLocation(@Param("tenantId") String tenantId, @Param("locationId") String locationId);
    /** 按租户查询位置列表 */
    List<FbaLocationDO> selectLocations(@Param("tenantId") String tenantId);

    // ---- FBA库存 ----

    /** 新增FBA库存 */
    void insertInventory(FbaInventoryDO inventory);
    /** 更新FBA库存 */
    void updateInventory(FbaInventoryDO inventory);
    /** 按租户+库存ID查询 */
    FbaInventoryDO selectInventory(@Param("tenantId") String tenantId, @Param("inventoryId") String inventoryId);
    /** 按租户+仓库+SKU+FNSKU+店铺+站点查询库存(唯一键) */
    FbaInventoryDO selectInventoryByKey(@Param("tenantId") String tenantId,
                                        @Param("warehouseId") String warehouseId,
                                        @Param("sellerSku") String sellerSku,
                                        @Param("fnsku") String fnsku,
                                        @Param("storeId") String storeId,
                                        @Param("siteCode") String siteCode);
    /** 按多条件查询FBA库存列表 */
    List<FbaInventoryDO> selectInventories(@Param("tenantId") String tenantId,
                                           @Param("sellerSku") String sellerSku,
                                           @Param("storeId") String storeId,
                                           @Param("siteCode") String siteCode,
                                           @Param("warehouseId") String warehouseId);

    // ---- 补货计划 ----

    /** 新增补货计划 */
    void insertPlan(ReplenishmentPlanDO plan);
    /** 更新补货计划 */
    void updatePlan(ReplenishmentPlanDO plan);
    /** 按租户+计划ID查询 */
    ReplenishmentPlanDO selectPlan(@Param("tenantId") String tenantId, @Param("planId") String planId);
    /** 按租户查询补货计划列表 */
    List<ReplenishmentPlanDO> selectPlans(@Param("tenantId") String tenantId);
}
