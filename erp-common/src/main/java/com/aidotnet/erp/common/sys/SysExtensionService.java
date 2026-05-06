package com.aidotnet.erp.common.sys;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SysExtensionService {

    private static final Logger log = LoggerFactory.getLogger(SysExtensionService.class);
    private final Map<String, ConnectorConfig> connectorConfigs = new ConcurrentHashMap<>();
    private final Map<String, LogisticsRule> logisticsRules = new ConcurrentHashMap<>();
    private final Map<String, AiFeatureToggle> aiFeatureToggles = new ConcurrentHashMap<>();

    public ConnectorConfig createConnectorConfig(String tenantId, String connectorType, String connectorName,
                                                  String platform, Map<String, Object> config,
                                                  String authType, boolean active) {
        String configId = "CONN-CFG-" + System.currentTimeMillis();
        ConnectorConfig cfg = new ConnectorConfig(configId, tenantId, connectorType, connectorName,
                platform, config, authType, active, Instant.now(), Instant.now());
        connectorConfigs.put(configId, cfg);
        log.info("Created connector config: id={}, type={}, platform={}", configId, connectorType, platform);
        return cfg;
    }

    public ConnectorConfig updateConnectorConfig(String configId, String connectorName,
                                                   Map<String, Object> config, boolean active) {
        ConnectorConfig existing = connectorConfigs.get(configId);
        if (existing == null) throw new IllegalArgumentException("Connector config not found: " + configId);
        ConnectorConfig updated = new ConnectorConfig(configId, existing.tenantId(),
                existing.connectorType(), connectorName != null ? connectorName : existing.connectorName(),
                existing.platform(), config != null ? config : existing.config(),
                existing.authType(), active, existing.createdAt(), Instant.now());
        connectorConfigs.put(configId, updated);
        log.info("Updated connector config: id={}", configId);
        return updated;
    }

    public List<ConnectorConfig> listConnectorConfigs(String tenantId, String connectorType) {
        return connectorConfigs.values().stream()
                .filter(c -> tenantId == null || tenantId.equals(c.tenantId()))
                .filter(c -> connectorType == null || connectorType.equals(c.connectorType()))
                .toList();
    }

    public LogisticsRule createLogisticsRule(String tenantId, String ruleName, String ruleType,
                                              String originCountry, String destinationCountry,
                                              BigDecimal weightMin, BigDecimal weightMax,
                                              String preferredCarrier, int priority, boolean active) {
        String ruleId = "LOG-RULE-" + System.currentTimeMillis();
        LogisticsRule rule = new LogisticsRule(ruleId, tenantId, ruleName, ruleType,
                originCountry, destinationCountry, weightMin, weightMax,
                preferredCarrier, priority, active, Instant.now(), Instant.now());
        logisticsRules.put(ruleId, rule);
        log.info("Created logistics rule: id={}, name={}, type={}", ruleId, ruleName, ruleType);
        return rule;
    }

    public LogisticsRule updateLogisticsRule(String ruleId, String ruleName, String preferredCarrier,
                                               int priority, boolean active) {
        LogisticsRule existing = logisticsRules.get(ruleId);
        if (existing == null) throw new IllegalArgumentException("Logistics rule not found: " + ruleId);
        LogisticsRule updated = new LogisticsRule(ruleId, existing.tenantId(),
                ruleName != null ? ruleName : existing.ruleName(),
                existing.ruleType(), existing.originCountry(), existing.destinationCountry(),
                existing.weightMin(), existing.weightMax(),
                preferredCarrier != null ? preferredCarrier : existing.preferredCarrier(),
                priority, active, existing.createdAt(), Instant.now());
        logisticsRules.put(ruleId, updated);
        log.info("Updated logistics rule: id={}", ruleId);
        return updated;
    }

    public List<LogisticsRule> listLogisticsRules(String tenantId, String ruleType) {
        return logisticsRules.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> ruleType == null || ruleType.equals(r.ruleType()))
                .sorted((a, b) -> Integer.compare(a.priority(), b.priority()))
                .toList();
    }

    public LogisticsRule matchLogisticsRule(String tenantId, String originCountry, String destinationCountry,
                                             BigDecimal weight) {
        return logisticsRules.values().stream()
                .filter(r -> tenantId.equals(r.tenantId()))
                .filter(r -> r.active())
                .filter(r -> originCountry == null || originCountry.equals(r.originCountry()) || r.originCountry() == null)
                .filter(r -> destinationCountry == null || destinationCountry.equals(r.destinationCountry()) || r.destinationCountry() == null)
                .filter(r -> weight == null || (r.weightMin() == null || weight.compareTo(r.weightMin()) >= 0)
                        && (r.weightMax() == null || weight.compareTo(r.weightMax()) <= 0))
                .min((a, b) -> Integer.compare(a.priority(), b.priority()))
                .orElse(null);
    }

    public AiFeatureToggle createAiFeatureToggle(String tenantId, String featureKey, String featureName,
                                                   String domain, boolean enabled, String description) {
        String toggleId = "AI-TOGGLE-" + System.currentTimeMillis();
        AiFeatureToggle toggle = new AiFeatureToggle(toggleId, tenantId, featureKey, featureName,
                domain, enabled, description, Instant.now(), Instant.now());
        aiFeatureToggles.put(toggleId, toggle);
        log.info("Created AI feature toggle: id={}, key={}, domain={}, enabled={}", toggleId, featureKey, domain, enabled);
        return toggle;
    }

    public AiFeatureToggle updateAiFeatureToggle(String toggleId, boolean enabled, String description) {
        AiFeatureToggle existing = aiFeatureToggles.get(toggleId);
        if (existing == null) throw new IllegalArgumentException("AI feature toggle not found: " + toggleId);
        AiFeatureToggle updated = new AiFeatureToggle(toggleId, existing.tenantId(),
                existing.featureKey(), existing.featureName(), existing.domain(),
                enabled, description != null ? description : existing.description(),
                existing.createdAt(), Instant.now());
        aiFeatureToggles.put(toggleId, updated);
        log.info("Updated AI feature toggle: id={}, enabled={}", toggleId, enabled);
        return updated;
    }

    public List<AiFeatureToggle> listAiFeatureToggles(String tenantId, String domain) {
        return aiFeatureToggles.values().stream()
                .filter(t -> tenantId == null || tenantId.equals(t.tenantId()))
                .filter(t -> domain == null || domain.equals(t.domain()))
                .toList();
    }

    public boolean isAiFeatureEnabled(String tenantId, String featureKey) {
        return aiFeatureToggles.values().stream()
                .anyMatch(t -> tenantId.equals(t.tenantId())
                        && featureKey.equals(t.featureKey())
                        && t.enabled());
    }

    public record ConnectorConfig(String configId, String tenantId, String connectorType, String connectorName,
                                   String platform, Map<String, Object> config, String authType,
                                   boolean active, Instant createdAt, Instant updatedAt) {}
    public record LogisticsRule(String ruleId, String tenantId, String ruleName, String ruleType,
                                 String originCountry, String destinationCountry,
                                 BigDecimal weightMin, BigDecimal weightMax,
                                 String preferredCarrier, int priority, boolean active,
                                 Instant createdAt, Instant updatedAt) {}
    public record AiFeatureToggle(String toggleId, String tenantId, String featureKey, String featureName,
                                   String domain, boolean enabled, String description,
                                   Instant createdAt, Instant updatedAt) {}
}
