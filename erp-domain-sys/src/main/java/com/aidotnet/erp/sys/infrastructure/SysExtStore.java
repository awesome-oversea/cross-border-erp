package com.aidotnet.erp.sys.infrastructure;

import com.aidotnet.erp.sys.domain.AIFeatureToggle;
import com.aidotnet.erp.sys.domain.ComplianceAlert;
import com.aidotnet.erp.sys.domain.ComplianceRule;
import com.aidotnet.erp.sys.domain.ConnectorConfig;
import com.aidotnet.erp.sys.domain.DataDictionary;
import com.aidotnet.erp.sys.domain.DataMaskingRule;
import com.aidotnet.erp.sys.domain.DocumentNumberRule;
import com.aidotnet.erp.sys.domain.DocumentNumberSegment;
import com.aidotnet.erp.sys.domain.InvoiceSetting;
import com.aidotnet.erp.sys.domain.LogisticsRule;
import com.aidotnet.erp.sys.domain.NotificationSetting;
import com.aidotnet.erp.sys.domain.NotificationTemplate;
import com.aidotnet.erp.sys.domain.OperationLog;
import com.aidotnet.erp.sys.domain.PlatformPolicyChange;
import com.aidotnet.erp.sys.domain.BusinessRuleVersion;
import com.aidotnet.erp.sys.domain.SimulationReplay;
import com.aidotnet.erp.sys.domain.RuleExecutionLog;
import com.aidotnet.erp.sys.domain.BusinessAlert;
import com.aidotnet.erp.sys.domain.DomainEventCatalog;
import com.aidotnet.erp.sys.domain.PrintTemplate;
import com.aidotnet.erp.sys.domain.WebhookEndpoint;
import com.aidotnet.erp.sys.domain.WebhookDelivery;
import com.aidotnet.erp.sys.domain.ManualImportTask;
import com.aidotnet.erp.sys.domain.PmsDraftDocument;
import com.aidotnet.erp.sys.domain.PmsFeedback;
import com.aidotnet.erp.sys.domain.PmsDataTrustRule;
import com.aidotnet.erp.sys.domain.ConnectorCallLog;
import com.aidotnet.erp.sys.domain.ConnectorSecret;
import com.aidotnet.erp.sys.domain.ContentAuditRule;
import com.aidotnet.erp.sys.domain.ContentAuditResult;
import com.aidotnet.erp.sys.domain.ContentAuditViolation;
import com.aidotnet.erp.sys.domain.DataTrustLevel;
import com.aidotnet.erp.sys.domain.TrademarkRecord;
import com.aidotnet.erp.sys.infrastructure.data.AIFeatureToggleDO;
import com.aidotnet.erp.sys.infrastructure.data.BusinessAlertDO;
import com.aidotnet.erp.sys.infrastructure.data.BusinessRuleVersionDO;
import com.aidotnet.erp.sys.infrastructure.data.ConnectorCallLogDO;
import com.aidotnet.erp.sys.infrastructure.data.ConnectorConfigDO;
import com.aidotnet.erp.sys.infrastructure.data.ConnectorSecretDO;
import com.aidotnet.erp.sys.infrastructure.data.ContentAuditResultDO;
import com.aidotnet.erp.sys.infrastructure.data.ContentAuditRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.DataDictionaryDO;
import com.aidotnet.erp.sys.infrastructure.data.DataMaskingRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.DocumentNumberRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.DocumentNumberSegmentDO;
import com.aidotnet.erp.sys.infrastructure.data.DomainEventCatalogDO;
import com.aidotnet.erp.sys.infrastructure.data.InvoiceSettingDO;
import com.aidotnet.erp.sys.infrastructure.data.LogisticsRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.ManualImportTaskDO;
import com.aidotnet.erp.sys.infrastructure.data.NotificationSettingDO;
import com.aidotnet.erp.sys.infrastructure.data.NotificationTemplateDO;
import com.aidotnet.erp.sys.infrastructure.data.OperationLogDO;
import com.aidotnet.erp.sys.infrastructure.data.PmsDataTrustRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.PmsDraftDocumentDO;
import com.aidotnet.erp.sys.infrastructure.data.PmsFeedbackDO;
import com.aidotnet.erp.sys.infrastructure.data.PrintTemplateDO;
import com.aidotnet.erp.sys.infrastructure.data.RuleExecutionLogDO;
import com.aidotnet.erp.sys.infrastructure.data.SimulationReplayDO;
import com.aidotnet.erp.sys.infrastructure.data.TrademarkRecordDO;
import com.aidotnet.erp.sys.infrastructure.data.WebhookDeliveryDO;
import com.aidotnet.erp.sys.infrastructure.data.WebhookEndpointDO;
import com.aidotnet.erp.sys.infrastructure.mapper.SysExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class SysExtStore {

    private final SysExtMapper mapper;
    private final ObjectMapper objectMapper;

    public SysExtStore(SysExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public DataDictionary saveDataDictionary(DataDictionary dict) {
        DataDictionaryDO existing = mapper.selectDataDictionary(dict.tenantId(), dict.dictId());
        DataDictionaryDO data = toDictData(dict);
        if (existing == null) {
            mapper.insertDataDictionary(data);
        } else {
            mapper.updateDataDictionary(data);
        }
        return dict;
    }

    public Optional<DataDictionary> findDataDictionary(String tenantId, String dictId) {
        return Optional.ofNullable(mapper.selectDataDictionary(tenantId, dictId)).map(this::toDictDomain);
    }

    public List<DataDictionary> listDataDictionaries(String tenantId, String dictType) {
        return mapper.selectDataDictionaries(tenantId, dictType).stream().map(this::toDictDomain).collect(Collectors.toList());
    }

    public Optional<DataDictionary> findDataDictionaryByCode(String tenantId, String dictCode) {
        return Optional.ofNullable(mapper.selectDataDictionaryByCode(tenantId, dictCode)).map(this::toDictDomain);
    }

    public AIFeatureToggle saveAIFeatureToggle(AIFeatureToggle toggle) {
        AIFeatureToggleDO existing = mapper.selectAIFeatureToggle(toggle.tenantId(), toggle.toggleId());
        AIFeatureToggleDO data = toToggleData(toggle);
        if (existing == null) {
            mapper.insertAIFeatureToggle(data);
        } else {
            mapper.updateAIFeatureToggle(data);
        }
        return toggle;
    }

    public Optional<AIFeatureToggle> findAIFeatureToggle(String tenantId, String toggleId) {
        return Optional.ofNullable(mapper.selectAIFeatureToggle(tenantId, toggleId)).map(this::toToggleDomain);
    }

    public List<AIFeatureToggle> listAIFeatureToggles(String tenantId, String domain) {
        return mapper.selectAIFeatureToggles(tenantId, domain).stream().map(this::toToggleDomain).collect(Collectors.toList());
    }

    public Optional<AIFeatureToggle> findAIFeatureToggleByCode(String tenantId, String featureCode) {
        return Optional.ofNullable(mapper.selectAIFeatureToggleByCode(tenantId, featureCode)).map(this::toToggleDomain);
    }

    public LogisticsRule saveLogisticsRule(LogisticsRule rule) {
        LogisticsRuleDO existing = mapper.selectLogisticsRule(rule.tenantId(), rule.ruleId());
        LogisticsRuleDO data = toLogisticsRuleData(rule);
        if (existing == null) {
            mapper.insertLogisticsRule(data);
        } else {
            mapper.updateLogisticsRule(data);
        }
        return rule;
    }

    public Optional<LogisticsRule> findLogisticsRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectLogisticsRule(tenantId, ruleId)).map(this::toLogisticsRuleDomain);
    }

    public List<LogisticsRule> listLogisticsRules(String tenantId, String countryCode) {
        return mapper.selectLogisticsRules(tenantId, countryCode).stream().map(this::toLogisticsRuleDomain).collect(Collectors.toList());
    }

    public List<LogisticsRule> listActiveLogisticsRules(String tenantId, String countryCode, String channel) {
        return mapper.selectActiveLogisticsRules(tenantId, countryCode, channel).stream().map(this::toLogisticsRuleDomain).collect(Collectors.toList());
    }

    private DataDictionaryDO toDictData(DataDictionary d) {
        DataDictionaryDO data = new DataDictionaryDO();
        data.setDictId(d.dictId());
        data.setTenantId(d.tenantId());
        data.setDictCode(d.dictCode());
        data.setDictName(d.dictName());
        data.setDictType(d.dictType());
        data.setParentCode(d.parentCode());
        data.setSortOrder(d.sortOrder());
        data.setEnabled(d.enabled());
        data.setRemark(d.remark());
        data.setCreatedAt(d.createdAt() != null ? d.createdAt() : Instant.now());
        data.setUpdatedAt(d.updatedAt() != null ? d.updatedAt() : Instant.now());
        return data;
    }

    private DataDictionary toDictDomain(DataDictionaryDO d) {
        return new DataDictionary(d.getDictId(), d.getTenantId(), d.getDictCode(), d.getDictName(), d.getDictType(),
                d.getParentCode(), d.getSortOrder(), d.isEnabled(), d.getRemark(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private AIFeatureToggleDO toToggleData(AIFeatureToggle t) {
        AIFeatureToggleDO data = new AIFeatureToggleDO();
        data.setToggleId(t.toggleId());
        data.setTenantId(t.tenantId());
        data.setFeatureCode(t.featureCode());
        data.setFeatureName(t.featureName());
        data.setDomain(t.domain());
        data.setEnabled(t.enabled());
        data.setDescription(t.description());
        data.setConfigJson(t.configJson());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private AIFeatureToggle toToggleDomain(AIFeatureToggleDO d) {
        return new AIFeatureToggle(d.getToggleId(), d.getTenantId(), d.getFeatureCode(), d.getFeatureName(),
                d.getDomain(), d.isEnabled(), d.getDescription(), d.getConfigJson(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private LogisticsRuleDO toLogisticsRuleData(LogisticsRule r) {
        LogisticsRuleDO data = new LogisticsRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleName(r.ruleName());
        data.setCountryCode(r.countryCode());
        data.setChannel(r.channel());
        data.setWeightMinKg(r.weightMinKg());
        data.setWeightMaxKg(r.weightMaxKg());
        data.setBaseCost(r.baseCost());
        data.setCostPerKg(r.costPerKg());
        data.setEstimatedDaysMin(r.estimatedDaysMin());
        data.setEstimatedDaysMax(r.estimatedDaysMax());
        data.setEnabled(r.enabled());
        data.setPriority(r.priority());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private LogisticsRule toLogisticsRuleDomain(LogisticsRuleDO d) {
        return new LogisticsRule(d.getRuleId(), d.getTenantId(), d.getRuleName(), d.getCountryCode(), d.getChannel(),
                d.getWeightMinKg(), d.getWeightMaxKg(), d.getBaseCost(), d.getCostPerKg(),
                d.getEstimatedDaysMin(), d.getEstimatedDaysMax(), d.isEnabled(), d.getPriority(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public ConnectorConfig saveConnectorConfig(ConnectorConfig config) {
        ConnectorConfigDO existing = mapper.selectConnectorConfig(config.tenantId(), config.configId());
        ConnectorConfigDO data = toConnectorConfigData(config);
        if (existing == null) {
            mapper.insertConnectorConfig(data);
        } else {
            mapper.updateConnectorConfig(data);
        }
        return config;
    }

    public Optional<ConnectorConfig> findConnectorConfig(String tenantId, String configId) {
        return Optional.ofNullable(mapper.selectConnectorConfig(tenantId, configId)).map(this::toConnectorConfigDomain);
    }

    public List<ConnectorConfig> listConnectorConfigs(String tenantId, String connectorType, String platform) {
        return mapper.selectConnectorConfigs(tenantId, connectorType, platform).stream().map(this::toConnectorConfigDomain).collect(Collectors.toList());
    }

    public List<ConnectorConfig> listActiveConnectorConfigs(String tenantId, String connectorType) {
        return mapper.selectActiveConnectorConfigs(tenantId, connectorType).stream().map(this::toConnectorConfigDomain).collect(Collectors.toList());
    }

    public InvoiceSetting saveInvoiceSetting(InvoiceSetting setting) {
        InvoiceSettingDO existing = mapper.selectInvoiceSetting(setting.tenantId(), setting.settingId());
        InvoiceSettingDO data = toInvoiceSettingData(setting);
        if (existing == null) {
            mapper.insertInvoiceSetting(data);
        } else {
            mapper.updateInvoiceSetting(data);
        }
        return setting;
    }

    public Optional<InvoiceSetting> findInvoiceSetting(String tenantId, String settingId) {
        return Optional.ofNullable(mapper.selectInvoiceSetting(tenantId, settingId)).map(this::toInvoiceSettingDomain);
    }

    public List<InvoiceSetting> listInvoiceSettings(String tenantId, String settingType) {
        return mapper.selectInvoiceSettings(tenantId, settingType).stream().map(this::toInvoiceSettingDomain).collect(Collectors.toList());
    }

    public NotificationSetting saveNotificationSetting(NotificationSetting setting) {
        NotificationSettingDO existing = mapper.selectNotificationSetting(setting.tenantId(), setting.settingId());
        NotificationSettingDO data = toNotificationSettingData(setting);
        if (existing == null) {
            mapper.insertNotificationSetting(data);
        } else {
            mapper.updateNotificationSetting(data);
        }
        return setting;
    }

    public Optional<NotificationSetting> findNotificationSetting(String tenantId, String settingId) {
        return Optional.ofNullable(mapper.selectNotificationSetting(tenantId, settingId)).map(this::toNotificationSettingDomain);
    }

    public List<NotificationSetting> listNotificationSettings(String tenantId, String channel) {
        return mapper.selectNotificationSettings(tenantId, channel).stream().map(this::toNotificationSettingDomain).collect(Collectors.toList());
    }

    public NotificationTemplate saveNotificationTemplate(NotificationTemplate template) {
        NotificationTemplateDO existing = mapper.selectNotificationTemplate(template.tenantId(), template.templateId());
        NotificationTemplateDO data = toNotificationTemplateData(template);
        if (existing == null) {
            mapper.insertNotificationTemplate(data);
        } else {
            mapper.updateNotificationTemplate(data);
        }
        return template;
    }

    public Optional<NotificationTemplate> findNotificationTemplate(String tenantId, String templateId) {
        return Optional.ofNullable(mapper.selectNotificationTemplate(tenantId, templateId)).map(this::toNotificationTemplateDomain);
    }

    public List<NotificationTemplate> listNotificationTemplates(String tenantId, String channel) {
        return mapper.selectNotificationTemplates(tenantId, channel).stream().map(this::toNotificationTemplateDomain).collect(Collectors.toList());
    }

    public Optional<NotificationTemplate> findNotificationTemplateByCode(String tenantId, String templateCode) {
        return Optional.ofNullable(mapper.selectNotificationTemplateByCode(tenantId, templateCode)).map(this::toNotificationTemplateDomain);
    }

    public void saveOperationLog(OperationLog log) {
        mapper.insertOperationLog(toOperationLogData(log));
    }

    public List<OperationLog> listOperationLogs(String tenantId, String module, String userId) {
        return mapper.selectOperationLogs(tenantId, module, userId).stream().map(this::toOperationLogDomain).collect(Collectors.toList());
    }

    public List<OperationLog> listOperationLogsByTraceId(String tenantId, String traceId) {
        return mapper.selectOperationLogsByTraceId(tenantId, traceId).stream().map(this::toOperationLogDomain).collect(Collectors.toList());
    }

    public DataMaskingRule saveDataMaskingRule(DataMaskingRule rule) {
        DataMaskingRuleDO existing = mapper.selectDataMaskingRule(rule.tenantId(), rule.ruleId());
        DataMaskingRuleDO data = toDataMaskingRuleData(rule);
        if (existing == null) {
            mapper.insertDataMaskingRule(data);
        } else {
            mapper.updateDataMaskingRule(data);
        }
        return rule;
    }

    public Optional<DataMaskingRule> findDataMaskingRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectDataMaskingRule(tenantId, ruleId)).map(this::toDataMaskingRuleDomain);
    }

    public List<DataMaskingRule> listDataMaskingRules(String tenantId, String fieldType) {
        return mapper.selectDataMaskingRules(tenantId, fieldType).stream().map(this::toDataMaskingRuleDomain).collect(Collectors.toList());
    }

    public Optional<DataMaskingRule> findDataMaskingRuleByCode(String tenantId, String ruleCode) {
        return Optional.ofNullable(mapper.selectDataMaskingRuleByCode(tenantId, ruleCode)).map(this::toDataMaskingRuleDomain);
    }

    private ConnectorConfigDO toConnectorConfigData(ConnectorConfig c) {
        ConnectorConfigDO data = new ConnectorConfigDO();
        data.setConfigId(c.configId());
        data.setTenantId(c.tenantId());
        data.setConnectorType(c.connectorType());
        data.setPlatform(c.platform());
        data.setConnectorName(c.connectorName());
        data.setConfig(toJson(c.config()));
        data.setStatus(c.status());
        data.setVersion(c.version());
        data.setDescription(c.description());
        data.setLastSyncAt(c.lastSyncAt());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private ConnectorConfig toConnectorConfigDomain(ConnectorConfigDO d) {
        return new ConnectorConfig(d.getConfigId(), d.getTenantId(), d.getConnectorType(), d.getPlatform(),
                d.getConnectorName(), fromJson(d.getConfig(), new TypeReference<Map<String, Object>>() {}),
                d.getStatus(), d.getVersion(), d.getDescription(), d.getLastSyncAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private InvoiceSettingDO toInvoiceSettingData(InvoiceSetting s) {
        InvoiceSettingDO data = new InvoiceSettingDO();
        data.setSettingId(s.settingId());
        data.setTenantId(s.tenantId());
        data.setSettingType(s.settingType());
        data.setSettingName(s.settingName());
        data.setConfig(toJson(s.config()));
        data.setEnabled(s.enabled());
        data.setDescription(s.description());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(s.updatedAt() != null ? s.updatedAt() : Instant.now());
        return data;
    }

    private InvoiceSetting toInvoiceSettingDomain(InvoiceSettingDO d) {
        return new InvoiceSetting(d.getSettingId(), d.getTenantId(), d.getSettingType(), d.getSettingName(),
                fromJson(d.getConfig(), new TypeReference<Map<String, Object>>() {}),
                d.isEnabled(), d.getDescription(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private NotificationSettingDO toNotificationSettingData(NotificationSetting s) {
        NotificationSettingDO data = new NotificationSettingDO();
        data.setSettingId(s.settingId());
        data.setTenantId(s.tenantId());
        data.setChannel(s.channel());
        data.setChannelName(s.channelName());
        data.setRules(toJson(s.rules()));
        data.setEnabled(s.enabled());
        data.setDescription(s.description());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(s.updatedAt() != null ? s.updatedAt() : Instant.now());
        return data;
    }

    private NotificationSetting toNotificationSettingDomain(NotificationSettingDO d) {
        return new NotificationSetting(d.getSettingId(), d.getTenantId(), d.getChannel(), d.getChannelName(),
                fromJson(d.getRules(), new TypeReference<Map<String, Object>>() {}),
                d.isEnabled(), d.getDescription(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private NotificationTemplateDO toNotificationTemplateData(NotificationTemplate t) {
        NotificationTemplateDO data = new NotificationTemplateDO();
        data.setTemplateId(t.templateId());
        data.setTenantId(t.tenantId());
        data.setTemplateCode(t.templateCode());
        data.setTemplateName(t.templateName());
        data.setChannel(t.channel());
        data.setSubject(t.subject());
        data.setContent(t.content());
        data.setVariables(toJson(t.variables()));
        data.setEnabled(t.enabled());
        data.setDescription(t.description());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private NotificationTemplate toNotificationTemplateDomain(NotificationTemplateDO d) {
        return new NotificationTemplate(d.getTemplateId(), d.getTenantId(), d.getTemplateCode(), d.getTemplateName(),
                d.getChannel(), d.getSubject(), d.getContent(),
                fromJson(d.getVariables(), new TypeReference<Map<String, String>>() {}),
                d.isEnabled(), d.getDescription(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private OperationLogDO toOperationLogData(OperationLog l) {
        OperationLogDO data = new OperationLogDO();
        data.setLogId(l.logId());
        data.setTenantId(l.tenantId());
        data.setUserId(l.userId());
        data.setUsername(l.username());
        data.setModule(l.module());
        data.setAction(l.action());
        data.setTargetObjectType(l.targetObjectType());
        data.setTargetObjectId(l.targetObjectId());
        data.setDetail(l.detail());
        data.setIpAddress(l.ipAddress());
        data.setUserAgent(l.userAgent());
        data.setTraceId(l.traceId());
        data.setOperatedAt(l.operatedAt());
        return data;
    }

    private OperationLog toOperationLogDomain(OperationLogDO d) {
        return new OperationLog(d.getLogId(), d.getTenantId(), d.getUserId(), d.getUsername(), d.getModule(),
                d.getAction(), d.getTargetObjectType(), d.getTargetObjectId(), d.getDetail(),
                d.getIpAddress(), d.getUserAgent(), d.getTraceId(), d.getOperatedAt());
    }

    private DataMaskingRuleDO toDataMaskingRuleData(DataMaskingRule r) {
        DataMaskingRuleDO data = new DataMaskingRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleCode(r.ruleCode());
        data.setRuleName(r.ruleName());
        data.setFieldType(r.fieldType());
        data.setMaskPattern(r.maskPattern());
        data.setReplaceChar(r.replaceChar());
        data.setKeepPrefix(r.keepPrefix());
        data.setKeepSuffix(r.keepSuffix());
        data.setEnabled(r.enabled());
        data.setDescription(r.description());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private DataMaskingRule toDataMaskingRuleDomain(DataMaskingRuleDO d) {
        return new DataMaskingRule(d.getRuleId(), d.getTenantId(), d.getRuleCode(), d.getRuleName(), d.getFieldType(),
                d.getMaskPattern(), d.getReplaceChar(), d.getKeepPrefix(), d.getKeepSuffix(),
                d.isEnabled(), d.getDescription(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveComplianceRule(ComplianceRule rule) {
        mapper.insertComplianceRule(rule);
    }

    public Optional<ComplianceRule> findComplianceRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectComplianceRule(tenantId, ruleId));
    }

    public List<ComplianceRule> findComplianceRules(String tenantId, String platform, String ruleType) {
        return mapper.selectComplianceRules(tenantId, platform, ruleType);
    }

    public void saveComplianceAlert(ComplianceAlert alert) {
        mapper.insertComplianceAlert(alert);
    }

    public Optional<ComplianceAlert> findComplianceAlert(String tenantId, String alertId) {
        return Optional.ofNullable(mapper.selectComplianceAlert(tenantId, alertId));
    }

    public List<ComplianceAlert> findComplianceAlerts(String tenantId, String platform, String status) {
        return mapper.selectComplianceAlerts(tenantId, platform, status);
    }

    public void savePlatformPolicyChange(PlatformPolicyChange change) {
        mapper.insertPlatformPolicyChange(change);
    }

    public List<PlatformPolicyChange> findPlatformPolicyChanges(String platform, String policyArea) {
        return mapper.selectPlatformPolicyChanges(platform, policyArea);
    }

    public DocumentNumberRule saveDocumentNumberRule(DocumentNumberRule rule) {
        DocumentNumberRuleDO existing = mapper.selectDocumentNumberRule(rule.tenantId(), rule.ruleId());
        DocumentNumberRuleDO data = toDocumentNumberRuleData(rule);
        if (existing == null) {
            mapper.insertDocumentNumberRule(data);
        } else {
            mapper.updateDocumentNumberRule(data);
        }
        return rule;
    }

    public Optional<DocumentNumberRule> findDocumentNumberRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectDocumentNumberRule(tenantId, ruleId)).map(this::toDocumentNumberRuleDomain);
    }

    public Optional<DocumentNumberRule> findDocumentNumberRuleByType(String tenantId, String documentType) {
        return Optional.ofNullable(mapper.selectDocumentNumberRuleByType(tenantId, documentType)).map(this::toDocumentNumberRuleDomain);
    }

    public List<DocumentNumberRule> listDocumentNumberRules(String tenantId) {
        return mapper.selectDocumentNumberRules(tenantId).stream().map(this::toDocumentNumberRuleDomain).collect(java.util.stream.Collectors.toList());
    }

    public DocumentNumberSegment saveDocumentNumberSegment(DocumentNumberSegment segment) {
        mapper.insertDocumentNumberSegment(toDocumentNumberSegmentData(segment));
        return segment;
    }

    private DocumentNumberRuleDO toDocumentNumberRuleData(DocumentNumberRule r) {
        DocumentNumberRuleDO data = new DocumentNumberRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleName(r.ruleName());
        data.setDocumentType(r.documentType());
        data.setPrefix(r.prefix());
        data.setDateFormat(r.dateFormat());
        data.setSequenceLength(r.sequenceLength());
        data.setCurrentSequence(r.currentSequence());
        data.setStep(r.step());
        data.setResetDaily(r.resetDaily());
        data.setResetMonthly(r.resetMonthly());
        data.setResetYearly(r.resetYearly());
        data.setLastResetAt(r.lastResetAt());
        data.setCreatedAt(r.createdAt());
        data.setUpdatedAt(r.updatedAt());
        return data;
    }

    private DocumentNumberRule toDocumentNumberRuleDomain(DocumentNumberRuleDO d) {
        return new DocumentNumberRule(d.getRuleId(), d.getTenantId(), d.getRuleName(), d.getDocumentType(),
                d.getPrefix(), d.getDateFormat(), d.getSequenceLength(), d.getCurrentSequence(), d.getStep(),
                d.isResetDaily(), d.isResetMonthly(), d.isResetYearly(), d.getLastResetAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private DocumentNumberSegmentDO toDocumentNumberSegmentData(DocumentNumberSegment s) {
        DocumentNumberSegmentDO data = new DocumentNumberSegmentDO();
        data.setSegmentId(s.segmentId());
        data.setTenantId(s.tenantId());
        data.setDocumentType(s.documentType());
        data.setDatePart(s.datePart());
        data.setSequencePart(s.sequencePart());
        data.setFullNumber(s.fullNumber());
        data.setGeneratedAt(s.generatedAt());
        return data;
    }

    public BusinessRuleVersion saveRuleVersion(BusinessRuleVersion version) {
        BusinessRuleVersionDO data = toRuleVersionData(version);
        mapper.insertBusinessRuleVersion(data);
        return version;
    }

    public List<BusinessRuleVersion> listRuleVersions(String tenantId, String ruleId, String ruleType) {
        return mapper.selectBusinessRuleVersions(tenantId, ruleId, ruleType).stream()
                .map(this::toRuleVersionDomain).collect(Collectors.toList());
    }

    public SimulationReplay saveSimulationReplay(SimulationReplay replay) {
        SimulationReplayDO data = toSimulationReplayData(replay);
        mapper.insertSimulationReplay(data);
        return replay;
    }

    public List<SimulationReplay> listSimulationReplays(String tenantId, String ruleId) {
        return mapper.selectSimulationReplays(tenantId, ruleId).stream()
                .map(this::toSimulationReplayDomain).collect(Collectors.toList());
    }

    public RuleExecutionLog saveRuleExecutionLog(RuleExecutionLog execLog) {
        RuleExecutionLogDO data = toRuleExecutionLogData(execLog);
        mapper.insertRuleExecutionLog(data);
        return execLog;
    }

    public List<RuleExecutionLog> listRuleExecutionLogs(String tenantId, String ruleId, String businessType) {
        return mapper.selectRuleExecutionLogs(tenantId, ruleId, businessType).stream()
                .map(this::toRuleExecutionLogDomain).collect(Collectors.toList());
    }

    public List<RuleExecutionLog> listRuleExecutionLogsByReference(String tenantId, String referenceId) {
        return mapper.selectRuleExecutionLogsByReference(tenantId, referenceId).stream()
                .map(this::toRuleExecutionLogDomain).collect(Collectors.toList());
    }

    public WebhookEndpoint saveWebhookEndpoint(WebhookEndpoint endpoint) {
        WebhookEndpointDO existing = mapper.selectWebhookEndpoint(endpoint.tenantId(), endpoint.endpointId());
        WebhookEndpointDO data = toWebhookEndpointData(endpoint);
        if (existing == null) {
            mapper.insertWebhookEndpoint(data);
        } else {
            mapper.updateWebhookEndpoint(data);
        }
        return endpoint;
    }

    public Optional<WebhookEndpoint> findWebhookEndpoint(String tenantId, String endpointId) {
        return Optional.ofNullable(mapper.selectWebhookEndpoint(tenantId, endpointId)).map(this::toWebhookEndpointDomain);
    }

    public List<WebhookEndpoint> listWebhookEndpoints(String tenantId) {
        return mapper.selectWebhookEndpoints(tenantId).stream().map(this::toWebhookEndpointDomain).collect(Collectors.toList());
    }

    public void deleteWebhookEndpoint(String tenantId, String endpointId) {
        mapper.deleteWebhookEndpoint(tenantId, endpointId);
    }

    public WebhookDelivery saveWebhookDelivery(WebhookDelivery delivery) {
        WebhookDeliveryDO data = toWebhookDeliveryData(delivery);
        mapper.insertWebhookDelivery(data);
        return delivery;
    }

    public Optional<WebhookDelivery> findWebhookDelivery(String tenantId, String deliveryId) {
        return Optional.ofNullable(mapper.selectWebhookDelivery(tenantId, deliveryId)).map(this::toWebhookDeliveryDomain);
    }

    public List<WebhookDelivery> listWebhookDeliveries(String tenantId, String endpointId) {
        return mapper.selectWebhookDeliveries(tenantId, endpointId).stream().map(this::toWebhookDeliveryDomain).collect(Collectors.toList());
    }

    public ManualImportTask saveManualImportTask(ManualImportTask task) {
        ManualImportTaskDO existing = mapper.selectManualImportTask(task.tenantId(), task.taskId());
        ManualImportTaskDO data = toManualImportTaskData(task);
        if (existing == null) {
            mapper.insertManualImportTask(data);
        } else {
            mapper.updateManualImportTask(data);
        }
        return task;
    }

    public Optional<ManualImportTask> findManualImportTask(String tenantId, String taskId) {
        return Optional.ofNullable(mapper.selectManualImportTask(tenantId, taskId)).map(this::toManualImportTaskDomain);
    }

    public List<ManualImportTask> listManualImportTasks(String tenantId, String importType) {
        return mapper.selectManualImportTasks(tenantId, importType).stream().map(this::toManualImportTaskDomain).collect(Collectors.toList());
    }

    public PrintTemplate savePrintTemplate(PrintTemplate template) {
        PrintTemplateDO existing = mapper.selectPrintTemplate(template.tenantId(), template.templateId());
        PrintTemplateDO data = toPrintTemplateData(template);
        if (existing == null) {
            mapper.insertPrintTemplate(data);
        } else {
            mapper.updatePrintTemplate(data);
        }
        return template;
    }

    public Optional<PrintTemplate> findPrintTemplate(String tenantId, String templateId) {
        return Optional.ofNullable(mapper.selectPrintTemplate(tenantId, templateId)).map(this::toPrintTemplateDomain);
    }

    public Optional<PrintTemplate> findPrintTemplateByCode(String tenantId, String templateCode) {
        return Optional.ofNullable(mapper.selectPrintTemplateByCode(tenantId, templateCode)).map(this::toPrintTemplateDomain);
    }

    public List<PrintTemplate> listPrintTemplates(String tenantId, String templateType) {
        return mapper.selectPrintTemplates(tenantId, templateType).stream().map(this::toPrintTemplateDomain).collect(Collectors.toList());
    }

    public DomainEventCatalog saveDomainEventCatalog(DomainEventCatalog catalog) {
        DomainEventCatalogDO existing = mapper.selectDomainEventCatalog(catalog.tenantId(), catalog.eventId());
        DomainEventCatalogDO data = toDomainEventCatalogData(catalog);
        if (existing == null) {
            mapper.insertDomainEventCatalog(data);
        } else {
            mapper.insertDomainEventCatalog(data);
        }
        return catalog;
    }

    public Optional<DomainEventCatalog> findDomainEventCatalog(String tenantId, String eventId) {
        return Optional.ofNullable(mapper.selectDomainEventCatalog(tenantId, eventId)).map(this::toDomainEventCatalogDomain);
    }

    public Optional<DomainEventCatalog> findDomainEventCatalogByCode(String tenantId, String eventCode) {
        return Optional.ofNullable(mapper.selectDomainEventCatalogByCode(tenantId, eventCode)).map(this::toDomainEventCatalogDomain);
    }

    public List<DomainEventCatalog> listDomainEventCatalogs(String tenantId, String domain) {
        return mapper.selectDomainEventCatalogs(tenantId, domain).stream().map(this::toDomainEventCatalogDomain).collect(Collectors.toList());
    }

    public BusinessAlert saveBusinessAlert(BusinessAlert alert) {
        BusinessAlertDO existing = mapper.selectBusinessAlert(alert.tenantId(), alert.alertId());
        BusinessAlertDO data = toBusinessAlertData(alert);
        if (existing == null) {
            mapper.insertBusinessAlert(data);
        } else {
            mapper.updateBusinessAlert(data);
        }
        return alert;
    }

    public Optional<BusinessAlert> findBusinessAlert(String tenantId, String alertId) {
        return Optional.ofNullable(mapper.selectBusinessAlert(tenantId, alertId)).map(this::toBusinessAlertDomain);
    }

    public List<BusinessAlert> listBusinessAlerts(String tenantId, String alertType, String status) {
        return mapper.selectBusinessAlerts(tenantId, alertType, status).stream().map(this::toBusinessAlertDomain).collect(Collectors.toList());
    }

    public PmsDraftDocument savePmsDraftDocument(PmsDraftDocument draft) {
        PmsDraftDocumentDO existing = mapper.selectPmsDraftDocument(draft.tenantId(), draft.draftId());
        PmsDraftDocumentDO data = toPmsDraftDocumentData(draft);
        if (existing == null) {
            mapper.insertPmsDraftDocument(data);
        } else {
            mapper.updatePmsDraftDocument(data);
        }
        return draft;
    }

    public Optional<PmsDraftDocument> findPmsDraftDocument(String tenantId, String draftId) {
        return Optional.ofNullable(mapper.selectPmsDraftDocument(tenantId, draftId)).map(this::toPmsDraftDocumentDomain);
    }

    public List<PmsDraftDocument> listPmsDraftDocuments(String tenantId, String domain) {
        return mapper.selectPmsDraftDocuments(tenantId, domain).stream().map(this::toPmsDraftDocumentDomain).collect(Collectors.toList());
    }

    public PmsFeedback savePmsFeedback(PmsFeedback feedback) {
        PmsFeedbackDO existing = mapper.selectPmsFeedback(feedback.tenantId(), feedback.feedbackId());
        PmsFeedbackDO data = toPmsFeedbackData(feedback);
        if (existing == null) {
            mapper.insertPmsFeedback(data);
        } else {
            mapper.updatePmsFeedback(data);
        }
        return feedback;
    }

    public Optional<PmsFeedback> findPmsFeedback(String tenantId, String feedbackId) {
        return Optional.ofNullable(mapper.selectPmsFeedback(tenantId, feedbackId)).map(this::toPmsFeedbackDomain);
    }

    public List<PmsFeedback> listPmsFeedbacks(String tenantId) {
        return mapper.selectPmsFeedbacks(tenantId).stream().map(this::toPmsFeedbackDomain).collect(Collectors.toList());
    }

    public PmsDataTrustRule savePmsDataTrustRule(PmsDataTrustRule rule) {
        PmsDataTrustRuleDO existing = mapper.selectPmsDataTrustRule(rule.tenantId(), rule.domain(), rule.objectType());
        PmsDataTrustRuleDO data = toPmsDataTrustRuleData(rule);
        if (existing == null) {
            mapper.insertPmsDataTrustRule(data);
        } else {
            mapper.updatePmsDataTrustRule(data);
        }
        return rule;
    }

    public PmsDataTrustRule findDataTrustRule(String tenantId, String domain, String objectType) {
        PmsDataTrustRuleDO data = mapper.selectPmsDataTrustRule(tenantId, domain, objectType);
        return data != null ? toPmsDataTrustRuleDomain(data) : null;
    }

    public List<PmsDataTrustRule> listPmsDataTrustRules(String tenantId) {
        return mapper.selectPmsDataTrustRules(tenantId).stream().map(this::toPmsDataTrustRuleDomain).collect(Collectors.toList());
    }

    public AIFeatureToggle findAIFeatureToggleByDomain(String tenantId, String domain) {
        return listAIFeatureToggles(tenantId, domain).stream().findFirst().orElse(null);
    }

    public List<AIFeatureToggle> listAllAIFeatureToggles(String tenantId) {
        return listAIFeatureToggles(tenantId, null);
    }

    public void saveConnectorCallLog(ConnectorCallLog callLog) {
        ConnectorCallLogDO data = toConnectorCallLogData(callLog);
        mapper.insertConnectorCallLog(data);
    }

    public List<ConnectorCallLog> listConnectorCallLogs(String tenantId, String configId) {
        return mapper.selectConnectorCallLogs(tenantId, configId).stream().map(this::toConnectorCallLogDomain).collect(Collectors.toList());
    }

    public void saveConnectorSecret(ConnectorSecret secret) {
        ConnectorSecretDO existing = mapper.selectConnectorSecret(secret.tenantId(), secret.secretId());
        ConnectorSecretDO data = toConnectorSecretData(secret);
        if (existing == null) {
            mapper.insertConnectorSecret(data);
        } else {
            mapper.updateConnectorSecret(data);
        }
    }

    public Optional<ConnectorSecret> findConnectorSecret(String tenantId, String secretId) {
        return Optional.ofNullable(mapper.selectConnectorSecret(tenantId, secretId)).map(this::toConnectorSecretDomain);
    }

    public List<ConnectorSecret> listConnectorSecrets(String tenantId, String configId) {
        return mapper.selectConnectorSecrets(tenantId, configId).stream().map(this::toConnectorSecretDomain).collect(Collectors.toList());
    }

    public void saveContentAuditRule(ContentAuditRule rule) {
        ContentAuditRuleDO existing = mapper.selectContentAuditRule(rule.tenantId(), rule.ruleId());
        ContentAuditRuleDO data = toContentAuditRuleData(rule);
        if (existing == null) {
            mapper.insertContentAuditRule(data);
        } else {
            mapper.updateContentAuditRule(data);
        }
    }

    public Optional<ContentAuditRule> findContentAuditRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectContentAuditRule(tenantId, ruleId)).map(this::toContentAuditRuleDomain);
    }

    public List<ContentAuditRule> listContentAuditRules(String tenantId, String ruleType, Boolean enabled) {
        return mapper.selectContentAuditRules(tenantId, ruleType, enabled).stream().map(this::toContentAuditRuleDomain).collect(Collectors.toList());
    }

    public void saveContentAuditResult(ContentAuditResult result) {
        ContentAuditResultDO data = toContentAuditResultData(result);
        mapper.insertContentAuditResult(data);
    }

    public List<ContentAuditResult> listContentAuditResults(String tenantId, String sourceType) {
        return mapper.selectContentAuditResults(tenantId, sourceType).stream().map(this::toContentAuditResultDomain).collect(Collectors.toList());
    }

    public void saveTrademarkRecord(TrademarkRecord record) {
        TrademarkRecordDO data = toTrademarkRecordData(record);
        mapper.insertTrademarkRecord(data);
    }

    public List<TrademarkRecord> listTrademarkRecords(String tenantId, String status) {
        return mapper.selectTrademarkRecords(tenantId, status).stream().map(this::toTrademarkRecordDomain).collect(Collectors.toList());
    }

    private BusinessRuleVersionDO toRuleVersionData(BusinessRuleVersion v) {
        BusinessRuleVersionDO data = new BusinessRuleVersionDO();
        data.setVersionId(v.versionId());
        data.setTenantId(v.tenantId());
        data.setRuleId(v.ruleId());
        data.setRuleType(v.ruleType());
        data.setRuleName(v.ruleName());
        data.setVersion(v.version());
        data.setContentJson(v.contentJson());
        data.setChangeDescription(v.changeDescription());
        data.setChangedBy(v.changedBy());
        data.setCreatedAt(v.createdAt() != null ? v.createdAt() : Instant.now());
        return data;
    }

    private BusinessRuleVersion toRuleVersionDomain(BusinessRuleVersionDO d) {
        return new BusinessRuleVersion(d.getVersionId(), d.getTenantId(), d.getRuleId(), d.getRuleType(),
                d.getRuleName(), d.getVersion(), d.getContentJson(), d.getChangeDescription(), d.getChangedBy(), d.getCreatedAt());
    }

    private RuleExecutionLogDO toRuleExecutionLogData(RuleExecutionLog l) {
        RuleExecutionLogDO data = new RuleExecutionLogDO();
        data.setLogId(l.logId());
        data.setTenantId(l.tenantId());
        data.setRuleId(l.ruleId());
        data.setRuleVersion(l.ruleVersion());
        data.setRuleType(l.ruleType());
        data.setBusinessType(l.businessType());
        data.setReferenceId(l.referenceId());
        data.setInputContext(toJson(l.inputContext()));
        data.setOutputResult(toJson(l.outputResult()));
        data.setSuccess(l.success());
        data.setErrorMessage(l.errorMessage());
        data.setExecutionTimeMs(l.executionTimeMs());
        data.setExecutedAt(l.executedAt());
        return data;
    }

    private RuleExecutionLog toRuleExecutionLogDomain(RuleExecutionLogDO d) {
        return new RuleExecutionLog(d.getLogId(), d.getTenantId(), d.getRuleId(), d.getRuleVersion(),
                d.getRuleType(), d.getBusinessType(), d.getReferenceId(),
                fromJson(d.getInputContext(), new TypeReference<Map<String, Object>>() {}),
                fromJson(d.getOutputResult(), new TypeReference<Map<String, Object>>() {}),
                d.getSuccess(), d.getErrorMessage(), d.getExecutionTimeMs(), d.getExecutedAt());
    }

    private SimulationReplayDO toSimulationReplayData(SimulationReplay r) {
        SimulationReplayDO data = new SimulationReplayDO();
        data.setReplayId(r.replayId());
        data.setTenantId(r.tenantId());
        data.setRuleId(r.ruleId());
        data.setRuleVersion(r.ruleVersion());
        data.setRuleType(r.ruleType());
        data.setInputContext(toJson(r.inputContext()));
        data.setOutputResult(toJson(r.outputResult()));
        data.setPassed(r.passed());
        data.setErrorMessage(r.errorMessage());
        data.setReplayedAt(r.replayedAt());
        return data;
    }

    private SimulationReplay toSimulationReplayDomain(SimulationReplayDO d) {
        return new SimulationReplay(d.getReplayId(), d.getTenantId(), d.getRuleId(), d.getRuleVersion(),
                d.getRuleType(),
                fromJson(d.getInputContext(), new TypeReference<Map<String, Object>>() {}),
                fromJson(d.getOutputResult(), new TypeReference<Map<String, Object>>() {}),
                d.getPassed(), d.getErrorMessage(), d.getReplayedAt());
    }

    private WebhookEndpointDO toWebhookEndpointData(WebhookEndpoint e) {
        WebhookEndpointDO data = new WebhookEndpointDO();
        data.setEndpointId(e.endpointId());
        data.setTenantId(e.tenantId());
        data.setName(e.name());
        data.setUrl(e.url());
        data.setEventType(e.eventType());
        data.setHeaders(toJson(e.headers()));
        data.setSecret(e.secret());
        data.setActive(e.active());
        data.setRetryCount(e.retryCount());
        data.setTimeoutSeconds(e.timeoutSeconds());
        data.setSubscribedEvents(toJson(e.subscribedEvents()));
        data.setCreatedAt(e.createdAt() != null ? e.createdAt() : Instant.now());
        data.setUpdatedAt(e.updatedAt() != null ? e.updatedAt() : Instant.now());
        return data;
    }

    private WebhookEndpoint toWebhookEndpointDomain(WebhookEndpointDO d) {
        return new WebhookEndpoint(d.getEndpointId(), d.getTenantId(), d.getName(), d.getUrl(), d.getEventType(),
                fromJson(d.getHeaders(), new TypeReference<Map<String, String>>() {}),
                d.getSecret(), d.getActive(), d.getRetryCount(), d.getTimeoutSeconds(),
                fromJson(d.getSubscribedEvents(), new TypeReference<List<String>>() {}),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private WebhookDeliveryDO toWebhookDeliveryData(WebhookDelivery d) {
        WebhookDeliveryDO data = new WebhookDeliveryDO();
        data.setDeliveryId(d.deliveryId());
        data.setTenantId(d.tenantId());
        data.setEndpointId(d.endpointId());
        data.setEventType(d.eventType());
        data.setPayload(toJson(d.payload()));
        data.setStatusCode(d.statusCode());
        data.setResponse(d.response());
        data.setSuccess(d.success());
        data.setAttemptCount(d.attemptCount());
        data.setNextRetryAt(d.nextRetryAt());
        data.setDeliveredAt(d.deliveredAt());
        return data;
    }

    private WebhookDelivery toWebhookDeliveryDomain(WebhookDeliveryDO d) {
        return new WebhookDelivery(d.getDeliveryId(), d.getTenantId(), d.getEndpointId(), d.getEventType(),
                fromJson(d.getPayload(), new TypeReference<Map<String, Object>>() {}),
                d.getStatusCode(), d.getResponse(), d.getSuccess(), d.getAttemptCount(), d.getNextRetryAt(), d.getDeliveredAt());
    }

    private ManualImportTaskDO toManualImportTaskData(ManualImportTask t) {
        ManualImportTaskDO data = new ManualImportTaskDO();
        data.setTaskId(t.taskId());
        data.setTenantId(t.tenantId());
        data.setImportType(t.importType());
        data.setFileName(t.fileName());
        data.setFileSize(t.fileSize());
        data.setStatus(t.status());
        data.setTotalRows(t.totalRows());
        data.setSuccessRows(t.successRows());
        data.setFailedRows(t.failedRows());
        data.setColumnMapping(toJson(t.columnMapping()));
        data.setErrorReportUrl(t.errorReportUrl());
        data.setImportedBy(t.importedBy());
        data.setStartedAt(t.startedAt());
        data.setCompletedAt(t.completedAt());
        return data;
    }

    private ManualImportTask toManualImportTaskDomain(ManualImportTaskDO d) {
        return new ManualImportTask(d.getTaskId(), d.getTenantId(), d.getImportType(), d.getFileName(),
                d.getFileSize(), d.getStatus(), d.getTotalRows(), d.getSuccessRows(), d.getFailedRows(),
                fromJson(d.getColumnMapping(), new TypeReference<Map<String, Object>>() {}),
                d.getErrorReportUrl(), d.getImportedBy(), d.getStartedAt(), d.getCompletedAt());
    }

    private PrintTemplateDO toPrintTemplateData(PrintTemplate t) {
        PrintTemplateDO data = new PrintTemplateDO();
        data.setTemplateId(t.templateId());
        data.setTenantId(t.tenantId());
        data.setTemplateCode(t.templateCode());
        data.setTemplateName(t.templateName());
        data.setTemplateType(t.templateType());
        data.setContent(t.content());
        data.setPaperSize(t.paperSize());
        data.setOrientation(t.orientation());
        data.setVariables(toJson(t.variables()));
        data.setEnabled(t.enabled());
        data.setDescription(t.description());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private PrintTemplate toPrintTemplateDomain(PrintTemplateDO d) {
        return new PrintTemplate(d.getTemplateId(), d.getTenantId(), d.getTemplateCode(), d.getTemplateName(),
                d.getTemplateType(), d.getContent(), d.getPaperSize(), d.getOrientation(),
                fromJson(d.getVariables(), new TypeReference<Map<String, Object>>() {}),
                d.getEnabled(), d.getDescription(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private DomainEventCatalogDO toDomainEventCatalogData(DomainEventCatalog c) {
        DomainEventCatalogDO data = new DomainEventCatalogDO();
        data.setEventId(c.eventId());
        data.setTenantId(c.tenantId());
        data.setEventCode(c.eventCode());
        data.setEventName(c.eventName());
        data.setDomain(c.domain());
        data.setAggregateType(c.aggregateType());
        data.setEventType(c.eventType());
        data.setDescription(c.description());
        data.setPayloadSchema(c.payloadSchema());
        data.setSubscribers(toJson(c.subscribers()));
        data.setVersion(c.version());
        data.setEnabled(c.enabled());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private DomainEventCatalog toDomainEventCatalogDomain(DomainEventCatalogDO d) {
        return new DomainEventCatalog(d.getEventId(), d.getTenantId(), d.getEventCode(), d.getEventName(),
                d.getDomain(), d.getAggregateType(), d.getEventType(), d.getDescription(), d.getPayloadSchema(),
                fromJson(d.getSubscribers(), new TypeReference<List<String>>() {}),
                d.getVersion(), d.getEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private BusinessAlertDO toBusinessAlertData(BusinessAlert a) {
        BusinessAlertDO data = new BusinessAlertDO();
        data.setAlertId(a.alertId());
        data.setTenantId(a.tenantId());
        data.setAlertType(a.alertType());
        data.setAlertCode(a.alertCode());
        data.setSeverity(a.severity());
        data.setDomain(a.domain());
        data.setTitle(a.title());
        data.setDescription(a.description());
        data.setSourceType(a.sourceType());
        data.setSourceId(a.sourceId());
        data.setStatus(a.status());
        data.setAssignedTo(a.assignedTo());
        data.setResolution(a.resolution());
        data.setOccurredAt(a.occurredAt());
        data.setResolvedAt(a.resolvedAt());
        data.setCreatedAt(a.createdAt() != null ? a.createdAt() : Instant.now());
        data.setUpdatedAt(a.updatedAt() != null ? a.updatedAt() : Instant.now());
        return data;
    }

    private BusinessAlert toBusinessAlertDomain(BusinessAlertDO d) {
        return new BusinessAlert(d.getAlertId(), d.getTenantId(), d.getAlertType(), d.getAlertCode(),
                d.getSeverity(), d.getDomain(), d.getTitle(), d.getDescription(), d.getSourceType(), d.getSourceId(),
                d.getStatus(), d.getAssignedTo(), d.getResolution(), d.getOccurredAt(), d.getResolvedAt(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private PmsDraftDocumentDO toPmsDraftDocumentData(PmsDraftDocument d) {
        PmsDraftDocumentDO data = new PmsDraftDocumentDO();
        data.setDraftId(d.draftId());
        data.setTenantId(d.tenantId());
        data.setErpReferenceId(d.erpReferenceId());
        data.setDomain(d.domain());
        data.setDraftType(d.draftType());
        data.setTargetBusinessType(d.targetBusinessType());
        data.setTargetBusinessId(d.targetBusinessId());
        data.setContentJson(d.contentJson());
        data.setTrustLevel(d.trustLevel().name());
        data.setSourceSystem(d.sourceSystem());
        data.setActorId(d.actorId());
        data.setActorType(d.actorType());
        data.setAgentId(d.agentId());
        data.setScope(d.scope());
        data.setPurpose(d.purpose());
        data.setTraceId(d.traceId());
        data.setApprovalStatus(d.approvalStatus());
        data.setApprovedBy(d.approvedBy());
        data.setApprovedAt(d.approvedAt());
        data.setExecutionStatus(d.executionStatus());
        data.setExecutionResult(d.executionResult());
        data.setCreatedAt(d.createdAt() != null ? d.createdAt() : Instant.now());
        data.setUpdatedAt(d.updatedAt() != null ? d.updatedAt() : Instant.now());
        return data;
    }

    private PmsDraftDocument toPmsDraftDocumentDomain(PmsDraftDocumentDO d) {
        return new PmsDraftDocument(d.getDraftId(), d.getTenantId(), d.getErpReferenceId(), d.getDomain(),
                d.getDraftType(), d.getTargetBusinessType(), d.getTargetBusinessId(), d.getContentJson(),
                DataTrustLevel.valueOf(d.getTrustLevel()), d.getSourceSystem(), d.getActorId(), d.getActorType(),
                d.getAgentId(), d.getScope(), d.getPurpose(), d.getTraceId(), d.getApprovalStatus(),
                d.getApprovedBy(), d.getApprovedAt(), d.getExecutionStatus(), d.getExecutionResult(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private PmsFeedbackDO toPmsFeedbackData(PmsFeedback f) {
        PmsFeedbackDO data = new PmsFeedbackDO();
        data.setFeedbackId(f.feedbackId());
        data.setTenantId(f.tenantId());
        data.setErpReferenceId(f.erpReferenceId());
        data.setRecommendationId(f.recommendationId());
        data.setDomain(f.domain());
        data.setFeedbackType(f.feedbackType());
        data.setExecutionStatus(f.executionStatus());
        data.setBusinessResult(f.businessResult());
        data.setBusinessMetricsJson(f.businessMetricsJson());
        data.setFailureReason(f.failureReason());
        data.setOperatorId(f.operatorId());
        data.setTraceId(f.traceId());
        data.setDelivered(f.delivered());
        data.setRetryCount(f.retryCount());
        data.setDeliveredAt(f.deliveredAt());
        data.setCreatedAt(f.createdAt() != null ? f.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private PmsFeedback toPmsFeedbackDomain(PmsFeedbackDO d) {
        return new PmsFeedback(d.getFeedbackId(), d.getTenantId(), d.getErpReferenceId(), d.getRecommendationId(),
                d.getDomain(), d.getFeedbackType(), d.getExecutionStatus(), d.getBusinessResult(),
                d.getBusinessMetricsJson(), d.getFailureReason(), d.getOperatorId(), d.getTraceId(),
                d.getDelivered(), d.getRetryCount(), d.getDeliveredAt(), d.getCreatedAt());
    }

    private PmsDataTrustRuleDO toPmsDataTrustRuleData(PmsDataTrustRule r) {
        PmsDataTrustRuleDO data = new PmsDataTrustRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setDomain(r.domain());
        data.setObjectType(r.objectType());
        data.setTrustLevel(r.trustLevel().name());
        data.setDescription(r.description());
        data.setAllowedActions(toJson(r.allowedActions()));
        data.setCanOverwriteErp(r.canOverwriteErp());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private PmsDataTrustRule toPmsDataTrustRuleDomain(PmsDataTrustRuleDO d) {
        return new PmsDataTrustRule(d.getRuleId(), d.getTenantId(), d.getDomain(), d.getObjectType(),
                DataTrustLevel.valueOf(d.getTrustLevel()), d.getDescription(),
                fromJson(d.getAllowedActions(), new TypeReference<List<String>>() {}),
                d.getCanOverwriteErp(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ConnectorCallLogDO toConnectorCallLogData(ConnectorCallLog l) {
        ConnectorCallLogDO data = new ConnectorCallLogDO();
        data.setLogId(l.logId());
        data.setTenantId(l.tenantId());
        data.setConfigId(l.configId());
        data.setConnectorType(l.connectorType());
        data.setPlatform(l.platform());
        data.setEndpoint(l.endpoint());
        data.setMethod(l.method());
        data.setTraceId(l.traceId());
        data.setStatusCode(l.statusCode());
        data.setDurationMs(l.durationMs());
        data.setSuccess(l.success());
        data.setErrorMessage(l.errorMessage());
        data.setCalledAt(l.calledAt());
        return data;
    }

    private ConnectorCallLog toConnectorCallLogDomain(ConnectorCallLogDO d) {
        return new ConnectorCallLog(d.getLogId(), d.getTenantId(), d.getConfigId(), d.getConnectorType(),
                d.getPlatform(), d.getEndpoint(), d.getMethod(), d.getTraceId(), d.getStatusCode(),
                d.getDurationMs(), d.getSuccess(), d.getErrorMessage(), d.getCalledAt());
    }

    private ConnectorSecretDO toConnectorSecretData(ConnectorSecret s) {
        ConnectorSecretDO data = new ConnectorSecretDO();
        data.setSecretId(s.secretId());
        data.setTenantId(s.tenantId());
        data.setConfigId(s.configId());
        data.setKeyType(s.keyType());
        data.setEncryptedValue(s.encryptedValue());
        data.setMaskedPreview(s.maskedPreview());
        data.setLastRotatedBy(s.lastRotatedBy());
        data.setLastRotatedAt(s.lastRotatedAt());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(s.updatedAt() != null ? s.updatedAt() : Instant.now());
        return data;
    }

    private ConnectorSecret toConnectorSecretDomain(ConnectorSecretDO d) {
        return new ConnectorSecret(d.getSecretId(), d.getTenantId(), d.getConfigId(), d.getKeyType(),
                d.getEncryptedValue(), d.getMaskedPreview(), d.getLastRotatedBy(), d.getLastRotatedAt(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ContentAuditRuleDO toContentAuditRuleData(ContentAuditRule r) {
        ContentAuditRuleDO data = new ContentAuditRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleType(r.ruleType());
        data.setCategory(r.category());
        data.setKeyword(r.keyword());
        data.setKeywordPattern(r.keywordPattern());
        data.setSeverity(r.severity());
        data.setAction(r.action());
        data.setReplacement(r.replacement());
        data.setDescription(r.description());
        data.setEnabled(r.enabled());
        data.setApplicablePlatforms(toJson(r.applicablePlatforms()));
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private ContentAuditRule toContentAuditRuleDomain(ContentAuditRuleDO d) {
        return new ContentAuditRule(d.getRuleId(), d.getTenantId(), d.getRuleType(), d.getCategory(),
                d.getKeyword(), d.getKeywordPattern(), d.getSeverity(), d.getAction(), d.getReplacement(),
                d.getDescription(), d.getEnabled(),
                fromJson(d.getApplicablePlatforms(), new TypeReference<List<String>>() {}),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ContentAuditResultDO toContentAuditResultData(ContentAuditResult r) {
        ContentAuditResultDO data = new ContentAuditResultDO();
        data.setResultId(r.resultId());
        data.setTenantId(r.tenantId());
        data.setAuditType(r.auditType());
        data.setSourceType(r.sourceType());
        data.setSourceId(r.sourceId());
        data.setPassed(r.passed());
        data.setViolations(toJson(r.violations()));
        data.setAuditedBy(r.auditedBy());
        data.setAuditedAt(r.auditedAt());
        return data;
    }

    private ContentAuditResult toContentAuditResultDomain(ContentAuditResultDO d) {
        return new ContentAuditResult(d.getResultId(), d.getTenantId(), d.getAuditType(), d.getSourceType(),
                d.getSourceId(), d.getPassed(),
                fromJson(d.getViolations(), new TypeReference<List<ContentAuditViolation>>() {}),
                d.getAuditedBy(), d.getAuditedAt());
    }

    private TrademarkRecordDO toTrademarkRecordData(TrademarkRecord r) {
        TrademarkRecordDO data = new TrademarkRecordDO();
        data.setTrademarkId(r.trademarkId());
        data.setTenantId(r.tenantId());
        data.setTrademarkName(r.trademarkName());
        data.setRegistrationNumber(r.registrationNumber());
        data.setJurisdiction(r.jurisdiction());
        data.setNiceClasses(toJson(r.niceClasses()));
        data.setOwner(r.owner());
        data.setStatus(r.status());
        data.setRegisteredAt(r.registeredAt());
        data.setExpiresAt(r.expiresAt());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private TrademarkRecord toTrademarkRecordDomain(TrademarkRecordDO d) {
        return new TrademarkRecord(d.getTrademarkId(), d.getTenantId(), d.getTrademarkName(),
                d.getRegistrationNumber(), d.getJurisdiction(),
                fromJson(d.getNiceClasses(), new TypeReference<List<String>>() {}),
                d.getOwner(), d.getStatus(), d.getRegisteredAt(), d.getExpiresAt(),
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
