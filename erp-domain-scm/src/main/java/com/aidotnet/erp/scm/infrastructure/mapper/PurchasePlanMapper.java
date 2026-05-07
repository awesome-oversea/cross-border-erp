package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.PurchasePlanDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchasePlanLineDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchasePlanMapper {

    void insertPlan(PurchasePlanDO plan);

    void updatePlan(PurchasePlanDO plan);

    PurchasePlanDO selectPlan(@Param("tenantId") String tenantId, @Param("planId") String planId);

    List<PurchasePlanDO> selectPlans(@Param("tenantId") String tenantId);

    void insertLine(PurchasePlanLineDO line);

    List<PurchasePlanLineDO> selectLines(@Param("planId") String planId);

    void deleteLines(@Param("planId") String planId);
}
