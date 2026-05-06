package com.aidotnet.erp.tms.infrastructure.mapper;

import com.aidotnet.erp.tms.infrastructure.data.CarrierDO;
import com.aidotnet.erp.tms.infrastructure.data.LogisticsStrategyDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingBatchDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingRateDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TmsExtMapper {

    void insertShippingBatch(ShippingBatchDO batch);
    void updateShippingBatch(ShippingBatchDO batch);
    ShippingBatchDO selectShippingBatch(@Param("tenantId") String tenantId, @Param("batchId") String batchId);
    List<ShippingBatchDO> selectShippingBatches(@Param("tenantId") String tenantId, @Param("carrierId") String carrierId);

    void insertLogisticsStrategy(LogisticsStrategyDO strategy);
    void updateLogisticsStrategy(LogisticsStrategyDO strategy);
    LogisticsStrategyDO selectLogisticsStrategy(@Param("tenantId") String tenantId, @Param("strategyId") String strategyId);
    List<LogisticsStrategyDO> selectLogisticsStrategies(@Param("tenantId") String tenantId, @Param("strategyType") String strategyType);

    List<CarrierDO> selectCarriers(@Param("tenantId") String tenantId);
    List<ShippingRateDO> selectShippingRatesByRoute(@Param("tenantId") String tenantId, @Param("originCountry") String originCountry, @Param("destinationCountry") String destinationCountry);
}
