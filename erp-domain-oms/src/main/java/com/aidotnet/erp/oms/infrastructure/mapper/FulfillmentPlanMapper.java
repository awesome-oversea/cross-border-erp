package com.aidotnet.erp.oms.infrastructure.mapper;

import com.aidotnet.erp.oms.infrastructure.data.FulfillmentPackageDO;
import com.aidotnet.erp.oms.infrastructure.data.FulfillmentPackageLineDO;
import com.aidotnet.erp.oms.infrastructure.data.FulfillmentPlanDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * OMS域履约计划MyBatis映射器
 * <p>
 * 描述: 履约计划数据访问层，提供履约计划、包裹、包裹行的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface FulfillmentPlanMapper {

    // ---- 履约计划 ----

    /** 新增履约计划 */
    void insertPlan(FulfillmentPlanDO plan);

    /** 更新履约计划 */
    void updatePlan(FulfillmentPlanDO plan);

    /** 按订单ID查询履约计划 */
    FulfillmentPlanDO selectPlanByOrderId(@Param("tenantId") String tenantId, @Param("orderId") String orderId);

    /** 按计划ID查询履约计划 */
    FulfillmentPlanDO selectPlanById(@Param("tenantId") String tenantId, @Param("planId") String planId);

    // ---- 履约包裹 ----

    /** 新增履约包裹 */
    void insertPackage(FulfillmentPackageDO fulfillmentPackage);

    /** 按计划ID查询包裹列表 */
    List<FulfillmentPackageDO> selectPackagesByPlanId(@Param("planId") String planId);

    /** 按计划ID删除包裹(更新时先删后插) */
    void deletePackagesByPlanId(@Param("planId") String planId);

    // ---- 包裹行 ----

    /** 新增包裹行 */
    void insertPackageLine(FulfillmentPackageLineDO line);

    /** 按包裹ID查询包裹行 */
    List<FulfillmentPackageLineDO> selectPackageLinesByPackageId(@Param("packageId") String packageId);

    /** 按计划ID删除包裹行(更新时先删后插) */
    void deletePackageLinesByPlanId(@Param("planId") String planId);
}
