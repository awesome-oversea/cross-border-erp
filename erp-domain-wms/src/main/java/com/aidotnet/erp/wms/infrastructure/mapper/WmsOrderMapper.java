package com.aidotnet.erp.wms.infrastructure.mapper;

import com.aidotnet.erp.wms.infrastructure.data.InboundOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.InboundOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.InventoryMovementDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.QualityCheckDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsOrderMapper {

    void insertInboundOrder(InboundOrderDO order);
    void updateInboundOrder(InboundOrderDO order);
    InboundOrderDO selectInboundOrder(@Param("tenantId") String tenantId, @Param("orderId") String orderId);
    List<InboundOrderDO> selectInboundOrders(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    void insertInboundOrderLine(InboundOrderLineDO line);
    void updateInboundOrderLine(InboundOrderLineDO line);
    List<InboundOrderLineDO> selectInboundOrderLines(@Param("orderId") String orderId);

    void insertOutboundOrder(OutboundOrderDO order);
    void updateOutboundOrder(OutboundOrderDO order);
    OutboundOrderDO selectOutboundOrder(@Param("tenantId") String tenantId, @Param("orderId") String orderId);
    List<OutboundOrderDO> selectOutboundOrders(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    void insertOutboundOrderLine(OutboundOrderLineDO line);
    void updateOutboundOrderLine(OutboundOrderLineDO line);
    List<OutboundOrderLineDO> selectOutboundOrderLines(@Param("orderId") String orderId);

    void insertMovement(InventoryMovementDO movement);
    List<InventoryMovementDO> selectMovements(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);

    void insertQualityCheck(QualityCheckDO qualityCheck);
    QualityCheckDO selectQualityCheck(@Param("tenantId") String tenantId, @Param("checkId") String checkId);
    List<QualityCheckDO> selectQualityChecks(@Param("tenantId") String tenantId, @Param("warehouseId") String warehouseId);
    List<QualityCheckDO> selectQualityChecksByInboundOrder(@Param("tenantId") String tenantId, @Param("inboundOrderId") String inboundOrderId);
}
