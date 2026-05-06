package com.aidotnet.erp.oms.infrastructure.mapper;

import com.aidotnet.erp.oms.infrastructure.data.PlatformShipmentSyncLogDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlatformShipmentSyncLogMapper {

    void insert(PlatformShipmentSyncLogDO data);

    List<PlatformShipmentSyncLogDO> selectByOrderId(@Param("tenantId") String tenantId, @Param("orderId") String orderId);
}
