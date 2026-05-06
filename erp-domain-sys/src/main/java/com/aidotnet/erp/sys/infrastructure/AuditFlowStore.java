package com.aidotnet.erp.sys.infrastructure;

import com.aidotnet.erp.sys.domain.AuditFlowConfig;
import com.aidotnet.erp.sys.domain.AuditFlowStep;
import com.aidotnet.erp.sys.infrastructure.data.AuditFlowConfigDO;
import com.aidotnet.erp.sys.infrastructure.data.AuditFlowStepDO;
import com.aidotnet.erp.sys.infrastructure.mapper.AuditFlowMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class AuditFlowStore {

    private final AuditFlowMapper mapper;

    public AuditFlowStore(AuditFlowMapper mapper) {
        this.mapper = mapper;
    }

    public AuditFlowConfig saveFlowConfig(AuditFlowConfig config) {
        AuditFlowConfigDO existing = mapper.selectFlowConfig(config.tenantId(), config.flowId());
        AuditFlowConfigDO data = toConfigData(config);
        if (existing == null) {
            mapper.insertFlowConfig(data);
        } else {
            mapper.updateFlowConfig(data);
        }
        return config;
    }

    public Optional<AuditFlowConfig> findFlowConfig(String tenantId, String flowId) {
        return Optional.ofNullable(mapper.selectFlowConfig(tenantId, flowId)).map(this::toConfigDomain);
    }

    public Optional<AuditFlowConfig> findFlowConfigByBusinessType(String tenantId, String businessType) {
        return Optional.ofNullable(mapper.selectFlowConfigByBusinessType(tenantId, businessType)).map(this::toConfigDomain);
    }

    public List<AuditFlowConfig> listFlowConfigs(String tenantId) {
        return mapper.selectFlowConfigs(tenantId).stream().map(this::toConfigDomain).collect(Collectors.toList());
    }

    public AuditFlowStep saveFlowStep(AuditFlowStep step) {
        AuditFlowStepDO data = toStepData(step);
        mapper.insertFlowStep(data);
        return step;
    }

    public List<AuditFlowStep> listFlowSteps(String tenantId, String flowId) {
        return mapper.selectFlowSteps(tenantId, flowId).stream().map(this::toStepDomain).collect(Collectors.toList());
    }

    public void deleteFlowStep(String tenantId, String stepId) {
        mapper.deleteFlowStep(tenantId, stepId);
    }

    private AuditFlowConfigDO toConfigData(AuditFlowConfig c) {
        AuditFlowConfigDO data = new AuditFlowConfigDO();
        data.setFlowId(c.flowId());
        data.setTenantId(c.tenantId());
        data.setFlowCode(c.flowCode());
        data.setFlowName(c.flowName());
        data.setBusinessType(c.businessType());
        data.setRequiredApprovals(c.requiredApprovals());
        data.setEnabled(c.enabled());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private AuditFlowConfig toConfigDomain(AuditFlowConfigDO d) {
        return new AuditFlowConfig(d.getFlowId(), d.getTenantId(), d.getFlowCode(), d.getFlowName(),
                d.getBusinessType(), d.getRequiredApprovals(), d.getEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private AuditFlowStepDO toStepData(AuditFlowStep s) {
        AuditFlowStepDO data = new AuditFlowStepDO();
        data.setStepId(s.stepId());
        data.setFlowId(s.flowId());
        data.setTenantId(s.tenantId());
        data.setStepOrder(s.stepOrder());
        data.setStepName(s.stepName());
        data.setApproverRole(s.approverRole());
        data.setAutoApprove(s.autoApprove());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        return data;
    }

    private AuditFlowStep toStepDomain(AuditFlowStepDO d) {
        return new AuditFlowStep(d.getStepId(), d.getFlowId(), d.getTenantId(), d.getStepOrder(),
                d.getStepName(), d.getApproverRole(), d.getAutoApprove(), d.getCreatedAt());
    }
}
