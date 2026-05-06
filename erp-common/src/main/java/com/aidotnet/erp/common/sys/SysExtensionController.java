package com.aidotnet.erp.common.sys;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/v1")
public class SysExtensionController {

    private final SysExtensionService sysExtService;

    public SysExtensionController(SysExtensionService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping("/connector-configs")
    public Result<SysExtensionService.ConnectorConfig> createConnectorConfig(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) request.getOrDefault("config", Map.of());
        return Result.ok(sysExtService.createConnectorConfig(
                (String) request.get("tenantId"),
                (String) request.get("connectorType"),
                (String) request.get("connectorName"),
                (String) request.get("platform"),
                config,
                (String) request.getOrDefault("authType", "OAUTH2"),
                request.containsKey("active") && (Boolean) request.get("active")
        ));
    }

    @PutMapping("/connector-configs/{configId}")
    public Result<SysExtensionService.ConnectorConfig> updateConnectorConfig(
            @PathVariable String configId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) request.get("config");
        return Result.ok(sysExtService.updateConnectorConfig(
                configId,
                (String) request.get("connectorName"),
                config,
                request.containsKey("active") && (Boolean) request.get("active")
        ));
    }

    @GetMapping("/connector-configs")
    public Result<List<SysExtensionService.ConnectorConfig>> listConnectorConfigs(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String connectorType) {
        return Result.ok(sysExtService.listConnectorConfigs(tenantId, connectorType));
    }

    @PostMapping("/logistics-rules")
    public Result<SysExtensionService.LogisticsRule> createLogisticsRule(@RequestBody Map<String, Object> request) {
        BigDecimal weightMin = request.containsKey("weightMin") ? new BigDecimal(request.get("weightMin").toString()) : null;
        BigDecimal weightMax = request.containsKey("weightMax") ? new BigDecimal(request.get("weightMax").toString()) : null;
        return Result.ok(sysExtService.createLogisticsRule(
                (String) request.get("tenantId"),
                (String) request.get("ruleName"),
                (String) request.get("ruleType"),
                (String) request.get("originCountry"),
                (String) request.get("destinationCountry"),
                weightMin, weightMax,
                (String) request.get("preferredCarrier"),
                request.containsKey("priority") ? ((Number) request.get("priority")).intValue() : 0,
                !request.containsKey("active") || (Boolean) request.get("active")
        ));
    }

    @PutMapping("/logistics-rules/{ruleId}")
    public Result<SysExtensionService.LogisticsRule> updateLogisticsRule(
            @PathVariable String ruleId, @RequestBody Map<String, Object> request) {
        return Result.ok(sysExtService.updateLogisticsRule(
                ruleId,
                (String) request.get("ruleName"),
                (String) request.get("preferredCarrier"),
                request.containsKey("priority") ? ((Number) request.get("priority")).intValue() : 0,
                !request.containsKey("active") || (Boolean) request.get("active")
        ));
    }

    @GetMapping("/logistics-rules")
    public Result<List<SysExtensionService.LogisticsRule>> listLogisticsRules(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String ruleType) {
        return Result.ok(sysExtService.listLogisticsRules(tenantId, ruleType));
    }

    @PostMapping("/logistics-rules/match")
    public Result<SysExtensionService.LogisticsRule> matchLogisticsRule(@RequestBody Map<String, Object> request) {
        BigDecimal weight = request.containsKey("weight") ? new BigDecimal(request.get("weight").toString()) : null;
        SysExtensionService.LogisticsRule matched = sysExtService.matchLogisticsRule(
                (String) request.get("tenantId"),
                (String) request.get("originCountry"),
                (String) request.get("destinationCountry"),
                weight);
        return Result.ok(matched);
    }

    @PostMapping("/ai-feature-toggles")
    public Result<SysExtensionService.AiFeatureToggle> createAiFeatureToggle(@RequestBody Map<String, Object> request) {
        return Result.ok(sysExtService.createAiFeatureToggle(
                (String) request.get("tenantId"),
                (String) request.get("featureKey"),
                (String) request.get("featureName"),
                (String) request.get("domain"),
                !request.containsKey("enabled") || (Boolean) request.get("enabled"),
                (String) request.getOrDefault("description", "")
        ));
    }

    @PutMapping("/ai-feature-toggles/{toggleId}")
    public Result<SysExtensionService.AiFeatureToggle> updateAiFeatureToggle(
            @PathVariable String toggleId, @RequestBody Map<String, Object> request) {
        return Result.ok(sysExtService.updateAiFeatureToggle(
                toggleId,
                (Boolean) request.getOrDefault("enabled", true),
                (String) request.get("description")
        ));
    }

    @GetMapping("/ai-feature-toggles")
    public Result<List<SysExtensionService.AiFeatureToggle>> listAiFeatureToggles(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String domain) {
        return Result.ok(sysExtService.listAiFeatureToggles(tenantId, domain));
    }

    @GetMapping("/ai-feature-toggles/check")
    public Result<Map<String, Boolean>> checkAiFeature(
            @RequestParam String tenantId, @RequestParam String featureKey) {
        return Result.ok(Map.of("enabled", sysExtService.isAiFeatureEnabled(tenantId, featureKey)));
    }
}
