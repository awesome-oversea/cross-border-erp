package com.aidotnet.erp.fba.infrastructure.mapper;

import com.aidotnet.erp.fba.infrastructure.data.RemovalOrderDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FbaExtMapper {

    void insertRemovalOrder(RemovalOrderDO order);
    void updateRemovalOrder(RemovalOrderDO order);
    RemovalOrderDO selectRemovalOrder(@Param("tenantId") String tenantId, @Param("removalId") String removalId);
    List<RemovalOrderDO> selectRemovalOrders(@Param("tenantId") String tenantId, @Param("status") String status);
}
