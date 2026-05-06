package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.AIFeatureToggle;
import com.aidotnet.erp.sys.domain.ConnectorConfig;
import com.aidotnet.erp.sys.domain.ConnectorStatus;
import com.aidotnet.erp.sys.domain.DataDictionary;
import com.aidotnet.erp.sys.domain.DataMaskingRule;
import com.aidotnet.erp.sys.domain.InvoiceSetting;
import com.aidotnet.erp.sys.domain.LogisticsRule;
import com.aidotnet.erp.sys.domain.NotificationSetting;
import com.aidotnet.erp.sys.domain.NotificationTemplate;
import com.aidotnet.erp.sys.domain.OperationLog;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统扩展管理应用服务
 * <p>
 * 描述: 系统设置域核心服务，负责数据字典、AI功能开关、物流规则、
 *       连接器配置、发票设置、通知设置/模板、操作日志、脱敏规则等
 *       系统级配置管理业务逻辑。是整个ERP系统的配置中枢。
 * </p>
 * <p>
 * 核心能力:
 *   1. 数据字典 - 创建/更新/启停数据字典，统一枚举值管理
 *   2. AI功能开关 - 创建/切换AI功能开关，控制AI能力启用
 *   3. 物流规则 - 创建/更新/启停物流计费规则，自动计算物流成本
 *   4. 连接器配置 - 创建/更新/状态管理第三方连接器配置
 *   5. 发票设置 - 创建/更新/启停发票相关配置
 *   6. 通知设置 - 创建/更新/启停通知渠道配置
 *   7. 通知模板 - 创建/更新通知模板，支持变量渲染
 *   8. 操作日志 - 记录/查询操作审计日志
 *   9. 脱敏规则 - 创建/管理数据脱敏规则
 * </p>
 * <p>
 * 业务规则:
 *   1. 数据字典编码唯一
 *   2. AI功能开关编码唯一
 *   3. 物流规则按国家+渠道+重量区间匹配
 *   4. 通知模板编码唯一，支持{{变量}}占位符渲染
 *   5. 脱敏规则编码唯一
 * </p>
 *
 * @author ERP系统
 * @see DataDictionary
 * @see AIFeatureToggle
 * @see SysExtStore
 */
@Service
public class SysExtService {

    private final SysExtStore extStore;

    public SysExtService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public DataDictionary createDataDictionary(String tenantId, CreateDictCommand command) {
        extStore.findDataDictionaryByCode(tenantId, command.dictCode())
                .ifPresent(existing -> { throw new BizException("DICT_CODE_DUPLICATED", "字典编码已存在"); });
        Instant now = Instant.now();
        DataDictionary dict = new DataDictionary(UUID.randomUUID().toString(), tenantId, command.dictCode(),
                command.dictName(), command.dictType(), command.parentCode(), command.sortOrder(), true, command.remark(), now, now);
        return extStore.saveDataDictionary(dict);
    }

    @Transactional
    public DataDictionary updateDataDictionary(String tenantId, String dictId, UpdateDictCommand command) {
        DataDictionary existing = getDataDictionary(tenantId, dictId);
        return extStore.saveDataDictionary(new DataDictionary(existing.dictId(), existing.tenantId(), existing.dictCode(),
                command.dictName() != null ? command.dictName() : existing.dictName(), existing.dictType(),
                existing.parentCode(), command.sortOrder() >= 0 ? command.sortOrder() : existing.sortOrder(),
                existing.enabled(), command.remark() != null ? command.remark() : existing.remark(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public DataDictionary toggleDataDictionary(String tenantId, String dictId, boolean enabled) {
        DataDictionary existing = getDataDictionary(tenantId, dictId);
        return extStore.saveDataDictionary(new DataDictionary(existing.dictId(), existing.tenantId(), existing.dictCode(),
                existing.dictName(), existing.dictType(), existing.parentCode(), existing.sortOrder(), enabled,
                existing.remark(), existing.createdAt(), Instant.now()));
    }

    public List<DataDictionary> listDataDictionaries(String tenantId, String dictType) {
        return extStore.listDataDictionaries(tenantId, dictType);
    }

    public DataDictionary getDataDictionary(String tenantId, String dictId) {
        return extStore.findDataDictionary(tenantId, dictId)
                .orElseThrow(() -> new BizException("DICT_NOT_FOUND", "数据字典不存在"));
    }

    @Transactional
    public AIFeatureToggle createAIFeatureToggle(String tenantId, CreateAIFeatureToggleCommand command) {
        extStore.findAIFeatureToggleByCode(tenantId, command.featureCode())
                .ifPresent(existing -> { throw new BizException("FEATURE_CODE_DUPLICATED", "功能编码已存在"); });
        Instant now = Instant.now();
        AIFeatureToggle toggle = new AIFeatureToggle(UUID.randomUUID().toString(), tenantId, command.featureCode(),
                command.featureName(), command.domain(), true, command.description(), command.configJson(), now, now);
        return extStore.saveAIFeatureToggle(toggle);
    }

    @Transactional
    public AIFeatureToggle toggleAIFeature(String tenantId, String toggleId, boolean enabled) {
        AIFeatureToggle existing = getAIFeatureToggle(tenantId, toggleId);
        return extStore.saveAIFeatureToggle(new AIFeatureToggle(existing.toggleId(), existing.tenantId(),
                existing.featureCode(), existing.featureName(), existing.domain(), enabled,
                existing.description(), existing.configJson(), existing.createdAt(), Instant.now()));
    }

    public boolean isAIFeatureEnabled(String tenantId, String featureCode) {
        return extStore.findAIFeatureToggleByCode(tenantId, featureCode)
                .map(AIFeatureToggle::enabled).orElse(false);
    }

    public List<AIFeatureToggle> listAIFeatureToggles(String tenantId, String domain) {
        return extStore.listAIFeatureToggles(tenantId, domain);
    }

    public AIFeatureToggle getAIFeatureToggle(String tenantId, String toggleId) {
        return extStore.findAIFeatureToggle(tenantId, toggleId)
                .orElseThrow(() -> new BizException("AI_FEATURE_NOT_FOUND", "AI功能开关不存在"));
    }

    @Transactional
    public LogisticsRule createLogisticsRule(String tenantId, CreateLogisticsRuleCommand command) {
        Instant now = Instant.now();
        LogisticsRule rule = new LogisticsRule(UUID.randomUUID().toString(), tenantId, command.ruleName(),
                command.countryCode(), command.channel(), command.weightMinKg(), command.weightMaxKg(),
                command.baseCost(), command.costPerKg(), command.estimatedDaysMin(), command.estimatedDaysMax(),
                true, command.priority(), now, now);
        return extStore.saveLogisticsRule(rule);
    }

    @Transactional
    public LogisticsRule updateLogisticsRule(String tenantId, String ruleId, UpdateLogisticsRuleCommand command) {
        LogisticsRule existing = getLogisticsRule(tenantId, ruleId);
        return extStore.saveLogisticsRule(new LogisticsRule(existing.ruleId(), existing.tenantId(),
                command.ruleName() != null ? command.ruleName() : existing.ruleName(), existing.countryCode(),
                existing.channel(), existing.weightMinKg(), existing.weightMaxKg(),
                command.baseCost() != null ? command.baseCost() : existing.baseCost(),
                command.costPerKg() != null ? command.costPerKg() : existing.costPerKg(),
                command.estimatedDaysMin() >= 0 ? command.estimatedDaysMin() : existing.estimatedDaysMin(),
                command.estimatedDaysMax() >= 0 ? command.estimatedDaysMax() : existing.estimatedDaysMax(),
                existing.enabled(), command.priority() >= 0 ? command.priority() : existing.priority(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public LogisticsRule toggleLogisticsRule(String tenantId, String ruleId, boolean enabled) {
        LogisticsRule existing = getLogisticsRule(tenantId, ruleId);
        return extStore.saveLogisticsRule(new LogisticsRule(existing.ruleId(), existing.tenantId(), existing.ruleName(),
                existing.countryCode(), existing.channel(), existing.weightMinKg(), existing.weightMaxKg(),
                existing.baseCost(), existing.costPerKg(), existing.estimatedDaysMin(), existing.estimatedDaysMax(),
                enabled, existing.priority(), existing.createdAt(), Instant.now()));
    }

    public BigDecimal calculateLogisticsCost(String tenantId, String countryCode, String channel, BigDecimal weightKg) {
        List<LogisticsRule> rules = extStore.listActiveLogisticsRules(tenantId, countryCode, channel);
        LogisticsRule matched = rules.stream()
                .filter(r -> weightKg.compareTo(r.weightMinKg()) >= 0 && weightKg.compareTo(r.weightMaxKg()) <= 0)
                .findFirst()
                .orElseThrow(() -> new BizException("LOGISTICS_RULE_NOT_FOUND", "未找到匹配的物流规则"));
        return matched.baseCost().add(matched.costPerKg().multiply(weightKg));
    }

    public List<LogisticsRule> listLogisticsRules(String tenantId, String countryCode) {
        return extStore.listLogisticsRules(tenantId, countryCode);
    }

    public LogisticsRule getLogisticsRule(String tenantId, String ruleId) {
        return extStore.findLogisticsRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("LOGISTICS_RULE_NOT_FOUND", "物流规则不存在"));
    }

    public record CreateDictCommand(String dictCode, String dictName, String dictType, String parentCode,
                                    int sortOrder, String remark) {}
    public record UpdateDictCommand(String dictName, int sortOrder, String remark) {}
    public record CreateAIFeatureToggleCommand(String featureCode, String featureName, String domain,
                                               String description, String configJson) {}
    public record CreateLogisticsRuleCommand(String ruleName, String countryCode, String channel,
                                             BigDecimal weightMinKg, BigDecimal weightMaxKg, BigDecimal baseCost,
                                             BigDecimal costPerKg, int estimatedDaysMin, int estimatedDaysMax, int priority) {}
    public record UpdateLogisticsRuleCommand(String ruleName, BigDecimal baseCost, BigDecimal costPerKg,
                                             int estimatedDaysMin, int estimatedDaysMax, int priority) {}

    @Transactional
    public ConnectorConfig createConnectorConfig(String tenantId, CreateConnectorConfigCommand command) {
        Instant now = Instant.now();
        ConnectorConfig config = new ConnectorConfig(UUID.randomUUID().toString(), tenantId, command.connectorType(),
                command.platform(), command.connectorName(), command.config(), ConnectorStatus.ACTIVE.name(),
                command.version(), command.description(), null, now, now);
        return extStore.saveConnectorConfig(config);
    }

    @Transactional
    public ConnectorConfig updateConnectorConfig(String tenantId, String configId, UpdateConnectorConfigCommand command) {
        ConnectorConfig existing = getConnectorConfig(tenantId, configId);
        return extStore.saveConnectorConfig(new ConnectorConfig(existing.configId(), existing.tenantId(),
                existing.connectorType(), existing.platform(),
                command.connectorName() != null ? command.connectorName() : existing.connectorName(),
                command.config() != null ? command.config() : existing.config(),
                existing.status(),
                command.version() != null ? command.version() : existing.version(),
                command.description() != null ? command.description() : existing.description(),
                existing.lastSyncAt(), existing.createdAt(), Instant.now()));
    }

    @Transactional
    public ConnectorConfig updateConnectorStatus(String tenantId, String configId, String status) {
        ConnectorConfig existing = getConnectorConfig(tenantId, configId);
        return extStore.saveConnectorConfig(new ConnectorConfig(existing.configId(), existing.tenantId(),
                existing.connectorType(), existing.platform(), existing.connectorName(), existing.config(),
                status, existing.version(), existing.description(),
                "SYNCING".equals(status) ? Instant.now() : existing.lastSyncAt(),
                existing.createdAt(), Instant.now()));
    }

    public List<ConnectorConfig> listConnectorConfigs(String tenantId, String connectorType, String platform) {
        return extStore.listConnectorConfigs(tenantId, connectorType, platform);
    }

    public List<ConnectorConfig> listActiveConnectorConfigs(String tenantId, String connectorType) {
        return extStore.listActiveConnectorConfigs(tenantId, connectorType);
    }

    public ConnectorConfig getConnectorConfig(String tenantId, String configId) {
        return extStore.findConnectorConfig(tenantId, configId)
                .orElseThrow(() -> new BizException("CONNECTOR_CONFIG_NOT_FOUND", "连接器配置不存在"));
    }

    @Transactional
    public InvoiceSetting createInvoiceSetting(String tenantId, CreateInvoiceSettingCommand command) {
        Instant now = Instant.now();
        InvoiceSetting setting = new InvoiceSetting(UUID.randomUUID().toString(), tenantId, command.settingType(),
                command.settingName(), command.config(), true, command.description(), now, now);
        return extStore.saveInvoiceSetting(setting);
    }

    @Transactional
    public InvoiceSetting updateInvoiceSetting(String tenantId, String settingId, UpdateInvoiceSettingCommand command) {
        InvoiceSetting existing = getInvoiceSetting(tenantId, settingId);
        return extStore.saveInvoiceSetting(new InvoiceSetting(existing.settingId(), existing.tenantId(),
                existing.settingType(),
                command.settingName() != null ? command.settingName() : existing.settingName(),
                command.config() != null ? command.config() : existing.config(),
                existing.enabled(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public InvoiceSetting toggleInvoiceSetting(String tenantId, String settingId, boolean enabled) {
        InvoiceSetting existing = getInvoiceSetting(tenantId, settingId);
        return extStore.saveInvoiceSetting(new InvoiceSetting(existing.settingId(), existing.tenantId(),
                existing.settingType(), existing.settingName(), existing.config(), enabled,
                existing.description(), existing.createdAt(), Instant.now()));
    }

    public List<InvoiceSetting> listInvoiceSettings(String tenantId, String settingType) {
        return extStore.listInvoiceSettings(tenantId, settingType);
    }

    public InvoiceSetting getInvoiceSetting(String tenantId, String settingId) {
        return extStore.findInvoiceSetting(tenantId, settingId)
                .orElseThrow(() -> new BizException("INVOICE_SETTING_NOT_FOUND", "发票设置不存在"));
    }

    @Transactional
    public NotificationSetting createNotificationSetting(String tenantId, CreateNotificationSettingCommand command) {
        Instant now = Instant.now();
        NotificationSetting setting = new NotificationSetting(UUID.randomUUID().toString(), tenantId, command.channel(),
                command.channelName(), command.rules(), true, command.description(), now, now);
        return extStore.saveNotificationSetting(setting);
    }

    @Transactional
    public NotificationSetting updateNotificationSetting(String tenantId, String settingId, UpdateNotificationSettingCommand command) {
        NotificationSetting existing = getNotificationSetting(tenantId, settingId);
        return extStore.saveNotificationSetting(new NotificationSetting(existing.settingId(), existing.tenantId(),
                existing.channel(),
                command.channelName() != null ? command.channelName() : existing.channelName(),
                command.rules() != null ? command.rules() : existing.rules(),
                existing.enabled(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public NotificationSetting toggleNotificationSetting(String tenantId, String settingId, boolean enabled) {
        NotificationSetting existing = getNotificationSetting(tenantId, settingId);
        return extStore.saveNotificationSetting(new NotificationSetting(existing.settingId(), existing.tenantId(),
                existing.channel(), existing.channelName(), existing.rules(), enabled,
                existing.description(), existing.createdAt(), Instant.now()));
    }

    public List<NotificationSetting> listNotificationSettings(String tenantId, String channel) {
        return extStore.listNotificationSettings(tenantId, channel);
    }

    public NotificationSetting getNotificationSetting(String tenantId, String settingId) {
        return extStore.findNotificationSetting(tenantId, settingId)
                .orElseThrow(() -> new BizException("NOTIFICATION_SETTING_NOT_FOUND", "通知设置不存在"));
    }

    @Transactional
    public NotificationTemplate createNotificationTemplate(String tenantId, CreateNotificationTemplateCommand command) {
        extStore.findNotificationTemplateByCode(tenantId, command.templateCode())
                .ifPresent(existing -> { throw new BizException("TEMPLATE_CODE_DUPLICATED", "模板编码已存在"); });
        Instant now = Instant.now();
        NotificationTemplate template = new NotificationTemplate(UUID.randomUUID().toString(), tenantId,
                command.templateCode(), command.templateName(), command.channel(), command.subject(),
                command.content(), command.variables(), true, command.description(), now, now);
        return extStore.saveNotificationTemplate(template);
    }

    @Transactional
    public NotificationTemplate updateNotificationTemplate(String tenantId, String templateId, UpdateNotificationTemplateCommand command) {
        NotificationTemplate existing = getNotificationTemplate(tenantId, templateId);
        return extStore.saveNotificationTemplate(new NotificationTemplate(existing.templateId(), existing.tenantId(),
                existing.templateCode(),
                command.templateName() != null ? command.templateName() : existing.templateName(),
                existing.channel(),
                command.subject() != null ? command.subject() : existing.subject(),
                command.content() != null ? command.content() : existing.content(),
                command.variables() != null ? command.variables() : existing.variables(),
                existing.enabled(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(), Instant.now()));
    }

    public String renderTemplate(String tenantId, String templateCode, Map<String, String> params) {
        NotificationTemplate template = extStore.findNotificationTemplateByCode(tenantId, templateCode)
                .orElseThrow(() -> new BizException("TEMPLATE_NOT_FOUND", "通知模板不存在"));
        if (!template.enabled()) {
            throw new BizException("TEMPLATE_DISABLED", "通知模板已停用");
        }
        String rendered = template.content();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return rendered;
    }

    public List<NotificationTemplate> listNotificationTemplates(String tenantId, String channel) {
        return extStore.listNotificationTemplates(tenantId, channel);
    }

    public NotificationTemplate getNotificationTemplate(String tenantId, String templateId) {
        return extStore.findNotificationTemplate(tenantId, templateId)
                .orElseThrow(() -> new BizException("TEMPLATE_NOT_FOUND", "通知模板不存在"));
    }

    @Transactional
    public OperationLog recordOperationLog(String tenantId, RecordOperationLogCommand command) {
        OperationLog log = new OperationLog(UUID.randomUUID().toString(), tenantId, command.userId(),
                command.username(), command.module(), command.action(), command.targetObjectType(),
                command.targetObjectId(), command.detail(), command.ipAddress(), command.userAgent(),
                command.traceId(), Instant.now());
        extStore.saveOperationLog(log);
        return log;
    }

    public List<OperationLog> listOperationLogs(String tenantId, String module, String userId) {
        return extStore.listOperationLogs(tenantId, module, userId);
    }

    public List<OperationLog> listOperationLogsByTraceId(String tenantId, String traceId) {
        return extStore.listOperationLogsByTraceId(tenantId, traceId);
    }

    @Transactional
    public DataMaskingRule createDataMaskingRule(String tenantId, CreateDataMaskingRuleCommand command) {
        extStore.findDataMaskingRuleByCode(tenantId, command.ruleCode())
                .ifPresent(existing -> { throw new BizException("MASKING_RULE_CODE_DUPLICATED", "脱敏规则编码已存在"); });
        Instant now = Instant.now();
        DataMaskingRule rule = new DataMaskingRule(UUID.randomUUID().toString(), tenantId, command.ruleCode(),
                command.ruleName(), command.fieldType(), command.maskPattern(), command.replaceChar(),
                command.keepPrefix(), command.keepSuffix(), true, command.description(), now, now);
        return extStore.saveDataMaskingRule(rule);
    }

    @Transactional
    public DataMaskingRule updateDataMaskingRule(String tenantId, String ruleId, UpdateDataMaskingRuleCommand command) {
        DataMaskingRule existing = getDataMaskingRule(tenantId, ruleId);
        return extStore.saveDataMaskingRule(new DataMaskingRule(existing.ruleId(), existing.tenantId(),
                existing.ruleCode(),
                command.ruleName() != null ? command.ruleName() : existing.ruleName(),
                existing.fieldType(),
                command.maskPattern() != null ? command.maskPattern() : existing.maskPattern(),
                command.replaceChar() != null ? command.replaceChar() : existing.replaceChar(),
                command.keepPrefix() >= 0 ? command.keepPrefix() : existing.keepPrefix(),
                command.keepSuffix() >= 0 ? command.keepSuffix() : existing.keepSuffix(),
                existing.enabled(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public DataMaskingRule toggleDataMaskingRule(String tenantId, String ruleId, boolean enabled) {
        DataMaskingRule existing = getDataMaskingRule(tenantId, ruleId);
        return extStore.saveDataMaskingRule(new DataMaskingRule(existing.ruleId(), existing.tenantId(),
                existing.ruleCode(), existing.ruleName(), existing.fieldType(), existing.maskPattern(),
                existing.replaceChar(), existing.keepPrefix(), existing.keepSuffix(), enabled,
                existing.description(), existing.createdAt(), Instant.now()));
    }

    public String maskValue(String tenantId, String fieldType, String value) {
        List<DataMaskingRule> rules = extStore.listDataMaskingRules(tenantId, fieldType);
        DataMaskingRule matched = rules.stream()
                .filter(DataMaskingRule::enabled)
                .findFirst()
                .orElse(null);
        if (matched == null || value == null) return value;
        int prefix = matched.keepPrefix();
        int suffix = matched.keepSuffix();
        if (value.length() <= prefix + suffix) return value;
        String prefixStr = value.substring(0, prefix);
        String suffixStr = value.substring(value.length() - suffix);
        int maskLen = value.length() - prefix - suffix;
        return prefixStr + matched.replaceChar().repeat(maskLen) + suffixStr;
    }

    public List<DataMaskingRule> listDataMaskingRules(String tenantId, String fieldType) {
        return extStore.listDataMaskingRules(tenantId, fieldType);
    }

    public DataMaskingRule getDataMaskingRule(String tenantId, String ruleId) {
        return extStore.findDataMaskingRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("MASKING_RULE_NOT_FOUND", "脱敏规则不存在"));
    }

    public record CreateConnectorConfigCommand(String connectorType, String platform, String connectorName,
                                                Map<String, Object> config, String version, String description) {}
    public record UpdateConnectorConfigCommand(String connectorName, Map<String, Object> config,
                                                String version, String description) {}
    public record CreateInvoiceSettingCommand(String settingType, String settingName,
                                               Map<String, Object> config, String description) {}
    public record UpdateInvoiceSettingCommand(String settingName, Map<String, Object> config, String description) {}
    public record CreateNotificationSettingCommand(String channel, String channelName,
                                                    Map<String, Object> rules, String description) {}
    public record UpdateNotificationSettingCommand(String channelName, Map<String, Object> rules, String description) {}
    public record CreateNotificationTemplateCommand(String templateCode, String templateName, String channel,
                                                     String subject, String content, Map<String, String> variables,
                                                     String description) {}
    public record UpdateNotificationTemplateCommand(String templateName, String subject, String content,
                                                     Map<String, String> variables, String description) {}
    public record RecordOperationLogCommand(String userId, String username, String module, String action,
                                             String targetObjectType, String targetObjectId, String detail,
                                             String ipAddress, String userAgent, String traceId) {}
    public record CreateDataMaskingRuleCommand(String ruleCode, String ruleName, String fieldType,
                                                String maskPattern, String replaceChar, int keepPrefix,
                                                int keepSuffix, String description) {}
    public record UpdateDataMaskingRuleCommand(String ruleName, String maskPattern, String replaceChar,
                                                int keepPrefix, int keepSuffix, String description) {}
}
