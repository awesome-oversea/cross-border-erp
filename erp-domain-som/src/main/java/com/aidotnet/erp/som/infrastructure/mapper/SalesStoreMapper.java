package com.aidotnet.erp.som.infrastructure.mapper;

import com.aidotnet.erp.som.infrastructure.data.SalesStoreDO;
import com.aidotnet.erp.som.infrastructure.data.SalesTrackingDO;
import com.aidotnet.erp.som.infrastructure.data.StoreMetricsDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesStoreMapper {

    void insertStore(SalesStoreDO store);
    void updateStore(SalesStoreDO store);
    SalesStoreDO selectStore(@Param("tenantId") String tenantId, @Param("storeId") String storeId);
    SalesStoreDO selectStoreByCode(@Param("tenantId") String tenantId, @Param("platform") String platform, @Param("storeCode") String storeCode);
    List<SalesStoreDO> selectStores(@Param("tenantId") String tenantId);

    void insertTracking(SalesTrackingDO tracking);
    List<SalesTrackingDO> selectTrackingsByStore(@Param("tenantId") String tenantId, @Param("storeId") String storeId);
    List<SalesTrackingDO> selectTrackingsBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);

    void insertMetrics(StoreMetricsDO metrics);
    List<StoreMetricsDO> selectMetricsByStore(@Param("tenantId") String tenantId, @Param("storeId") String storeId);
}
