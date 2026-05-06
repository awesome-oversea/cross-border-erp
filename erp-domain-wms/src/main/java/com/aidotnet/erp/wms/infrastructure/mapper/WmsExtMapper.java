package com.aidotnet.erp.wms.infrastructure.mapper;

import com.aidotnet.erp.wms.infrastructure.data.InventoryMovementDO;
import com.aidotnet.erp.wms.infrastructure.data.StockCheckOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.StockCheckOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.TransferOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.TransferOrderLineDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsExtMapper {

    void insertTransferOrder(TransferOrderDO order);
    void updateTransferOrder(TransferOrderDO order);
    TransferOrderDO selectTransferOrder(@Param("tenantId") String tenantId, @Param("transferId") String transferId);
    List<TransferOrderDO> selectTransferOrders(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    void insertTransferOrderLine(TransferOrderLineDO line);
    void updateTransferOrderLine(TransferOrderLineDO line);
    TransferOrderLineDO selectTransferOrderLine(@Param("lineId") String lineId);
    List<TransferOrderLineDO> selectTransferOrderLines(@Param("transferId") String transferId);

    void insertStockCheckOrder(StockCheckOrderDO order);
    void updateStockCheckOrder(StockCheckOrderDO order);
    StockCheckOrderDO selectStockCheckOrder(@Param("tenantId") String tenantId, @Param("checkOrderId") String checkOrderId);
    List<StockCheckOrderDO> selectStockCheckOrders(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    void insertStockCheckOrderLine(StockCheckOrderLineDO line);
    void updateStockCheckOrderLine(StockCheckOrderLineDO line);
    StockCheckOrderLineDO selectStockCheckOrderLine(@Param("lineId") String lineId);
    List<StockCheckOrderLineDO> selectStockCheckOrderLines(@Param("checkOrderId") String checkOrderId);

    void insertInventoryMovement(InventoryMovementDO movement);
}
