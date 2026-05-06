package com.aidotnet.erp.wms.infrastructure.mapper;

import com.aidotnet.erp.wms.infrastructure.data.InventoryBalanceDO;
import com.aidotnet.erp.wms.infrastructure.data.InventoryPredictionDO;
import com.aidotnet.erp.wms.infrastructure.data.InventoryTransactionDO;
import com.aidotnet.erp.wms.infrastructure.data.StockCheckDO;
import com.aidotnet.erp.wms.infrastructure.data.StockCheckLineDO;
import com.aidotnet.erp.wms.infrastructure.data.WarehouseDO;
import com.aidotnet.erp.wms.infrastructure.data.WarehouseLocationDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * WMS域库存数据MyBatis映射器
 * <p>
 * 描述: 仓储域核心数据访问层，提供仓库、库位、库存余额、盘点、预测、事务等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface InventoryMapper {

    // ---- 仓库 ----

    /** 新增仓库 */
    void insertWarehouse(WarehouseDO warehouse);

    /** 按租户ID+仓库ID查询 */
    WarehouseDO selectWarehouse(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    /** 按租户ID查询仓库列表 */
    List<WarehouseDO> selectWarehouses(@Param("tenantId") String tenantId);

    // ---- 库存余额 ----

    /** 新增库存余额 */
    void insertBalance(InventoryBalanceDO balance);

    /** 更新库存余额 */
    void updateBalance(InventoryBalanceDO balance);

    /** 按租户+仓库+SKU查询库存余额 */
    InventoryBalanceDO selectBalance(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId,
                                     @Param("sellerSku") String sellerSku);

    /** 按租户+仓库查询库存余额列表 */
    List<InventoryBalanceDO> selectBalances(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    /** 按租户+SKU查询所有仓库库存余额 */
    List<InventoryBalanceDO> selectBalancesBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);

    // ---- 库位 ----

    /** 新增库位 */
    void insertLocation(WarehouseLocationDO location);

    /** 按租户+库位ID查询 */
    WarehouseLocationDO selectLocation(@Param("tenantId") String tenantId, @Param("locationId") String locationId);

    /** 按租户+仓库查询库位列表 */
    List<WarehouseLocationDO> selectLocations(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    // ---- 盘点 ----

    /** 新增盘点记录 */
    void insertStockCheck(StockCheckDO stockCheck);

    /** 更新盘点记录 */
    void updateStockCheck(StockCheckDO stockCheck);

    /** 按盘点ID查询 */
    StockCheckDO selectStockCheck(@Param("tenantId") String tenantId, @Param("checkId") String checkId);

    /** 按仓库查询盘点列表 */
    List<StockCheckDO> selectStockChecks(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    /** 新增盘点行 */
    void insertStockCheckLine(StockCheckLineDO stockCheckLine);

    /** 更新盘点行 */
    void updateStockCheckLine(StockCheckLineDO stockCheckLine);

    /** 按盘点ID查询盘点行 */
    StockCheckLineDO selectStockCheckLine(@Param("tenantId") String tenantId, @Param("checkId") String checkId);

    // ---- 预测 ----

    /** 新增库存预测 */
    void insertPrediction(InventoryPredictionDO prediction);

    /** 按仓库查询预测列表 */
    List<InventoryPredictionDO> selectPredictions(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    // ---- 事务 ----

    /** 新增库存事务 */
    void insertTransaction(InventoryTransactionDO transaction);

    /** 按仓库+SKU查询事务列表 */
    List<InventoryTransactionDO> selectTransactions(@Param("tenantId") String tenantId,
                                                    @Param("warehouseId") String warehouseId,
                                                    @Param("sellerSku") String sellerSku);
}
