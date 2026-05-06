package com.aidotnet.erp.sys.infrastructure.mapper;

import com.aidotnet.erp.sys.infrastructure.data.AuditFlowConfigDO;
import com.aidotnet.erp.sys.infrastructure.data.AuditFlowStepDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditFlowMapper {

    void insertFlowConfig(AuditFlowConfigDO data);
    void updateFlowConfig(AuditFlowConfigDO data);
    AuditFlowConfigDO selectFlowConfig(@Param("tenantId") String tenantId, @Param("flowId") String flowId);
    AuditFlowConfigDO selectFlowConfigByBusinessType(@Param("tenantId") String tenantId, @Param("businessType") String businessType);
    List<AuditFlowConfigDO> selectFlowConfigs(@Param("tenantId") String tenantId);

    void insertFlowStep(AuditFlowStepDO data);
    List<AuditFlowStepDO> selectFlowSteps(@Param("tenantId") String tenantId, @Param("flowId") String flowId);
    void deleteFlowStep(@Param("tenantId") String tenantId, @Param("stepId") String stepId);
}
