package com.aidotnet.erp.sys.infrastructure.mapper;

import com.aidotnet.erp.sys.domain.ComplianceAlert;
import com.aidotnet.erp.sys.domain.ComplianceRule;
import com.aidotnet.erp.sys.domain.PlatformPolicyChange;
import com.aidotnet.erp.sys.infrastructure.data.AIFeatureToggleDO;
import com.aidotnet.erp.sys.infrastructure.data.ConnectorConfigDO;
import com.aidotnet.erp.sys.infrastructure.data.DataDictionaryDO;
import com.aidotnet.erp.sys.infrastructure.data.DataMaskingRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.DocumentNumberRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.DocumentNumberSegmentDO;
import com.aidotnet.erp.sys.infrastructure.data.InvoiceSettingDO;
import com.aidotnet.erp.sys.infrastructure.data.LogisticsRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.NotificationSettingDO;
import com.aidotnet.erp.sys.infrastructure.data.NotificationTemplateDO;
import com.aidotnet.erp.sys.infrastructure.data.OperationLogDO;
import com.aidotnet.erp.sys.infrastructure.data.BusinessAlertDO;
import com.aidotnet.erp.sys.infrastructure.data.BusinessRuleVersionDO;
import com.aidotnet.erp.sys.infrastructure.data.ConnectorCallLogDO;
import com.aidotnet.erp.sys.infrastructure.data.ConnectorSecretDO;
import com.aidotnet.erp.sys.infrastructure.data.ContentAuditResultDO;
import com.aidotnet.erp.sys.infrastructure.data.ContentAuditRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.DomainEventCatalogDO;
import com.aidotnet.erp.sys.infrastructure.data.ManualImportTaskDO;
import com.aidotnet.erp.sys.infrastructure.data.PmsDataTrustRuleDO;
import com.aidotnet.erp.sys.infrastructure.data.PmsDraftDocumentDO;
import com.aidotnet.erp.sys.infrastructure.data.PmsFeedbackDO;
import com.aidotnet.erp.sys.infrastructure.data.PrintTemplateDO;
import com.aidotnet.erp.sys.infrastructure.data.RuleExecutionLogDO;
import com.aidotnet.erp.sys.infrastructure.data.SimulationReplayDO;
import com.aidotnet.erp.sys.infrastructure.data.TrademarkRecordDO;
import com.aidotnet.erp.sys.infrastructure.data.WebhookDeliveryDO;
import com.aidotnet.erp.sys.infrastructure.data.WebhookEndpointDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysExtMapper {

    void insertDataDictionary(DataDictionaryDO dict);
    void updateDataDictionary(DataDictionaryDO dict);
    DataDictionaryDO selectDataDictionary(@Param("tenantId") String tenantId, @Param("dictId") String dictId);
    List<DataDictionaryDO> selectDataDictionaries(@Param("tenantId") String tenantId, @Param("dictType") String dictType);
    DataDictionaryDO selectDataDictionaryByCode(@Param("tenantId") String tenantId, @Param("dictCode") String dictCode);

    void insertAIFeatureToggle(AIFeatureToggleDO toggle);
    void updateAIFeatureToggle(AIFeatureToggleDO toggle);
    AIFeatureToggleDO selectAIFeatureToggle(@Param("tenantId") String tenantId, @Param("toggleId") String toggleId);
    List<AIFeatureToggleDO> selectAIFeatureToggles(@Param("tenantId") String tenantId, @Param("domain") String domain);
    AIFeatureToggleDO selectAIFeatureToggleByCode(@Param("tenantId") String tenantId, @Param("featureCode") String featureCode);

    void insertLogisticsRule(LogisticsRuleDO rule);
    void updateLogisticsRule(LogisticsRuleDO rule);
    LogisticsRuleDO selectLogisticsRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<LogisticsRuleDO> selectLogisticsRules(@Param("tenantId") String tenantId, @Param("countryCode") String countryCode);
    List<LogisticsRuleDO> selectActiveLogisticsRules(@Param("tenantId") String tenantId, @Param("countryCode") String countryCode, @Param("channel") String channel);

    void insertConnectorConfig(ConnectorConfigDO config);
    void updateConnectorConfig(ConnectorConfigDO config);
    ConnectorConfigDO selectConnectorConfig(@Param("tenantId") String tenantId, @Param("configId") String configId);
    List<ConnectorConfigDO> selectConnectorConfigs(@Param("tenantId") String tenantId, @Param("connectorType") String connectorType, @Param("platform") String platform);
    List<ConnectorConfigDO> selectActiveConnectorConfigs(@Param("tenantId") String tenantId, @Param("connectorType") String connectorType);

    void insertInvoiceSetting(InvoiceSettingDO setting);
    void updateInvoiceSetting(InvoiceSettingDO setting);
    InvoiceSettingDO selectInvoiceSetting(@Param("tenantId") String tenantId, @Param("settingId") String settingId);
    List<InvoiceSettingDO> selectInvoiceSettings(@Param("tenantId") String tenantId, @Param("settingType") String settingType);

    void insertNotificationSetting(NotificationSettingDO setting);
    void updateNotificationSetting(NotificationSettingDO setting);
    NotificationSettingDO selectNotificationSetting(@Param("tenantId") String tenantId, @Param("settingId") String settingId);
    List<NotificationSettingDO> selectNotificationSettings(@Param("tenantId") String tenantId, @Param("channel") String channel);

    void insertNotificationTemplate(NotificationTemplateDO template);
    void updateNotificationTemplate(NotificationTemplateDO template);
    NotificationTemplateDO selectNotificationTemplate(@Param("tenantId") String tenantId, @Param("templateId") String templateId);
    List<NotificationTemplateDO> selectNotificationTemplates(@Param("tenantId") String tenantId, @Param("channel") String channel);
    NotificationTemplateDO selectNotificationTemplateByCode(@Param("tenantId") String tenantId, @Param("templateCode") String templateCode);

    void insertOperationLog(OperationLogDO log);
    List<OperationLogDO> selectOperationLogs(@Param("tenantId") String tenantId, @Param("module") String module, @Param("userId") String userId);
    List<OperationLogDO> selectOperationLogsByTraceId(@Param("tenantId") String tenantId, @Param("traceId") String traceId);

    void insertDataMaskingRule(DataMaskingRuleDO rule);
    void updateDataMaskingRule(DataMaskingRuleDO rule);
    DataMaskingRuleDO selectDataMaskingRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<DataMaskingRuleDO> selectDataMaskingRules(@Param("tenantId") String tenantId, @Param("fieldType") String fieldType);
    DataMaskingRuleDO selectDataMaskingRuleByCode(@Param("tenantId") String tenantId, @Param("ruleCode") String ruleCode);

    void insertComplianceRule(ComplianceRule rule);
    ComplianceRule selectComplianceRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<ComplianceRule> selectComplianceRules(@Param("tenantId") String tenantId, @Param("platform") String platform, @Param("ruleType") String ruleType);

    void insertComplianceAlert(ComplianceAlert alert);
    ComplianceAlert selectComplianceAlert(@Param("tenantId") String tenantId, @Param("alertId") String alertId);
    List<ComplianceAlert> selectComplianceAlerts(@Param("tenantId") String tenantId, @Param("platform") String platform, @Param("status") String status);

    void insertPlatformPolicyChange(PlatformPolicyChange change);
    List<PlatformPolicyChange> selectPlatformPolicyChanges(@Param("platform") String platform, @Param("policyArea") String policyArea);

    void insertDocumentNumberRule(DocumentNumberRuleDO rule);
    void updateDocumentNumberRule(DocumentNumberRuleDO rule);
    DocumentNumberRuleDO selectDocumentNumberRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    DocumentNumberRuleDO selectDocumentNumberRuleByType(@Param("tenantId") String tenantId, @Param("documentType") String documentType);
    List<DocumentNumberRuleDO> selectDocumentNumberRules(@Param("tenantId") String tenantId);

    void insertDocumentNumberSegment(DocumentNumberSegmentDO segment);

    void insertBusinessRuleVersion(BusinessRuleVersionDO version);
    List<BusinessRuleVersionDO> selectBusinessRuleVersions(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId, @Param("ruleType") String ruleType);

    void insertRuleExecutionLog(RuleExecutionLogDO log);
    List<RuleExecutionLogDO> selectRuleExecutionLogs(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId, @Param("businessType") String businessType);
    List<RuleExecutionLogDO> selectRuleExecutionLogsByReference(@Param("tenantId") String tenantId, @Param("referenceId") String referenceId);

    void insertSimulationReplay(SimulationReplayDO replay);
    List<SimulationReplayDO> selectSimulationReplays(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);

    void insertWebhookEndpoint(WebhookEndpointDO endpoint);
    void updateWebhookEndpoint(WebhookEndpointDO endpoint);
    WebhookEndpointDO selectWebhookEndpoint(@Param("tenantId") String tenantId, @Param("endpointId") String endpointId);
    List<WebhookEndpointDO> selectWebhookEndpoints(@Param("tenantId") String tenantId);
    void deleteWebhookEndpoint(@Param("tenantId") String tenantId, @Param("endpointId") String endpointId);

    void insertWebhookDelivery(WebhookDeliveryDO delivery);
    WebhookDeliveryDO selectWebhookDelivery(@Param("tenantId") String tenantId, @Param("deliveryId") String deliveryId);
    List<WebhookDeliveryDO> selectWebhookDeliveries(@Param("tenantId") String tenantId, @Param("endpointId") String endpointId);

    void insertManualImportTask(ManualImportTaskDO task);
    void updateManualImportTask(ManualImportTaskDO task);
    ManualImportTaskDO selectManualImportTask(@Param("tenantId") String tenantId, @Param("taskId") String taskId);
    List<ManualImportTaskDO> selectManualImportTasks(@Param("tenantId") String tenantId, @Param("importType") String importType);

    void insertPrintTemplate(PrintTemplateDO template);
    void updatePrintTemplate(PrintTemplateDO template);
    PrintTemplateDO selectPrintTemplate(@Param("tenantId") String tenantId, @Param("templateId") String templateId);
    PrintTemplateDO selectPrintTemplateByCode(@Param("tenantId") String tenantId, @Param("templateCode") String templateCode);
    List<PrintTemplateDO> selectPrintTemplates(@Param("tenantId") String tenantId, @Param("templateType") String templateType);

    void insertDomainEventCatalog(DomainEventCatalogDO catalog);
    DomainEventCatalogDO selectDomainEventCatalog(@Param("tenantId") String tenantId, @Param("eventId") String eventId);
    DomainEventCatalogDO selectDomainEventCatalogByCode(@Param("tenantId") String tenantId, @Param("eventCode") String eventCode);
    List<DomainEventCatalogDO> selectDomainEventCatalogs(@Param("tenantId") String tenantId, @Param("domain") String domain);

    void insertBusinessAlert(BusinessAlertDO alert);
    void updateBusinessAlert(BusinessAlertDO alert);
    BusinessAlertDO selectBusinessAlert(@Param("tenantId") String tenantId, @Param("alertId") String alertId);
    List<BusinessAlertDO> selectBusinessAlerts(@Param("tenantId") String tenantId, @Param("alertType") String alertType, @Param("status") String status);

    void insertPmsDraftDocument(PmsDraftDocumentDO draft);
    void updatePmsDraftDocument(PmsDraftDocumentDO draft);
    PmsDraftDocumentDO selectPmsDraftDocument(@Param("tenantId") String tenantId, @Param("draftId") String draftId);
    List<PmsDraftDocumentDO> selectPmsDraftDocuments(@Param("tenantId") String tenantId, @Param("domain") String domain);

    void insertPmsFeedback(PmsFeedbackDO feedback);
    void updatePmsFeedback(PmsFeedbackDO feedback);
    PmsFeedbackDO selectPmsFeedback(@Param("tenantId") String tenantId, @Param("feedbackId") String feedbackId);
    List<PmsFeedbackDO> selectPmsFeedbacks(@Param("tenantId") String tenantId);

    void insertPmsDataTrustRule(PmsDataTrustRuleDO rule);
    void updatePmsDataTrustRule(PmsDataTrustRuleDO rule);
    PmsDataTrustRuleDO selectPmsDataTrustRule(@Param("tenantId") String tenantId, @Param("domain") String domain, @Param("objectType") String objectType);
    List<PmsDataTrustRuleDO> selectPmsDataTrustRules(@Param("tenantId") String tenantId);

    void insertConnectorCallLog(ConnectorCallLogDO log);
    List<ConnectorCallLogDO> selectConnectorCallLogs(@Param("tenantId") String tenantId, @Param("configId") String configId);

    void insertConnectorSecret(ConnectorSecretDO secret);
    void updateConnectorSecret(ConnectorSecretDO secret);
    ConnectorSecretDO selectConnectorSecret(@Param("tenantId") String tenantId, @Param("secretId") String secretId);
    List<ConnectorSecretDO> selectConnectorSecrets(@Param("tenantId") String tenantId, @Param("configId") String configId);

    void insertContentAuditRule(ContentAuditRuleDO rule);
    void updateContentAuditRule(ContentAuditRuleDO rule);
    ContentAuditRuleDO selectContentAuditRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<ContentAuditRuleDO> selectContentAuditRules(@Param("tenantId") String tenantId, @Param("ruleType") String ruleType, @Param("enabled") Boolean enabled);

    void insertContentAuditResult(ContentAuditResultDO result);
    List<ContentAuditResultDO> selectContentAuditResults(@Param("tenantId") String tenantId, @Param("sourceType") String sourceType);

    void insertTrademarkRecord(TrademarkRecordDO record);
    void updateTrademarkRecord(TrademarkRecordDO record);
    List<TrademarkRecordDO> selectTrademarkRecords(@Param("tenantId") String tenantId, @Param("status") String status);
}
