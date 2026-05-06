package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.application.SysExtService.CreateAIFeatureToggleCommand;
import com.aidotnet.erp.sys.application.SysExtService.CreateDictCommand;
import com.aidotnet.erp.sys.application.SysExtService.CreateLogisticsRuleCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateDictCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateLogisticsRuleCommand;
import com.aidotnet.erp.sys.domain.AIFeatureToggle;
import com.aidotnet.erp.sys.domain.DataDictionary;
import com.aidotnet.erp.sys.domain.LogisticsRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1")
public class SysExtController {

    private final SysExtService sysExtService;

    public SysExtController(SysExtService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping("/data-dictionaries")
    public Result<DataDictionary> createDataDictionary(@Valid @RequestBody CreateDictRequest request) {
        return Result.ok(sysExtService.createDataDictionary(currentTenant(), new CreateDictCommand(
                request.dictCode(), request.dictName(), request.dictType(), request.parentCode(),
                request.sortOrder(), request.remark())));
    }

    @PutMapping("/data-dictionaries/{dictId}")
    public Result<DataDictionary> updateDataDictionary(@PathVariable String dictId,
                                                       @Valid @RequestBody UpdateDictRequest request) {
        return Result.ok(sysExtService.updateDataDictionary(currentTenant(), dictId,
                new UpdateDictCommand(request.dictName(), request.sortOrder(), request.remark())));
    }

    @PostMapping("/data-dictionaries/{dictId}/toggle")
    public Result<DataDictionary> toggleDataDictionary(@PathVariable String dictId, @RequestParam boolean enabled) {
        return Result.ok(sysExtService.toggleDataDictionary(currentTenant(), dictId, enabled));
    }

    @GetMapping("/data-dictionaries")
    public Result<List<DataDictionary>> listDataDictionaries(@RequestParam(required = false) String dictType) {
        return Result.ok(sysExtService.listDataDictionaries(currentTenant(), dictType));
    }

    @GetMapping("/data-dictionaries/{dictId}")
    public Result<DataDictionary> getDataDictionary(@PathVariable String dictId) {
        return Result.ok(sysExtService.getDataDictionary(currentTenant(), dictId));
    }

    @PostMapping("/ai-feature-toggles")
    public Result<AIFeatureToggle> createAIFeatureToggle(@Valid @RequestBody CreateAIFeatureToggleRequest request) {
        return Result.ok(sysExtService.createAIFeatureToggle(currentTenant(), new CreateAIFeatureToggleCommand(
                request.featureCode(), request.featureName(), request.domain(), request.description(), request.configJson())));
    }

    @PostMapping("/ai-feature-toggles/{toggleId}/toggle")
    public Result<AIFeatureToggle> toggleAIFeature(@PathVariable String toggleId, @RequestParam boolean enabled) {
        return Result.ok(sysExtService.toggleAIFeature(currentTenant(), toggleId, enabled));
    }

    @GetMapping("/ai-feature-toggles/check")
    public Result<Boolean> isAIFeatureEnabled(@RequestParam String featureCode) {
        return Result.ok(sysExtService.isAIFeatureEnabled(currentTenant(), featureCode));
    }

    @GetMapping("/ai-feature-toggles")
    public Result<List<AIFeatureToggle>> listAIFeatureToggles(@RequestParam(required = false) String domain) {
        return Result.ok(sysExtService.listAIFeatureToggles(currentTenant(), domain));
    }

    @PostMapping("/logistics-rules")
    public Result<LogisticsRule> createLogisticsRule(@Valid @RequestBody CreateLogisticsRuleRequest request) {
        return Result.ok(sysExtService.createLogisticsRule(currentTenant(), new CreateLogisticsRuleCommand(
                request.ruleName(), request.countryCode(), request.channel(), request.weightMinKg(), request.weightMaxKg(),
                request.baseCost(), request.costPerKg(), request.estimatedDaysMin(), request.estimatedDaysMax(), request.priority())));
    }

    @PutMapping("/logistics-rules/{ruleId}")
    public Result<LogisticsRule> updateLogisticsRule(@PathVariable String ruleId,
                                                     @Valid @RequestBody UpdateLogisticsRuleRequest request) {
        return Result.ok(sysExtService.updateLogisticsRule(currentTenant(), ruleId,
                new UpdateLogisticsRuleCommand(request.ruleName(), request.baseCost(), request.costPerKg(),
                        request.estimatedDaysMin(), request.estimatedDaysMax(), request.priority())));
    }

    @PostMapping("/logistics-rules/{ruleId}/toggle")
    public Result<LogisticsRule> toggleLogisticsRule(@PathVariable String ruleId, @RequestParam boolean enabled) {
        return Result.ok(sysExtService.toggleLogisticsRule(currentTenant(), ruleId, enabled));
    }

    @GetMapping("/logistics-rules/calculate")
    public Result<BigDecimal> calculateLogisticsCost(@RequestParam String countryCode, @RequestParam String channel,
                                                     @RequestParam BigDecimal weightKg) {
        return Result.ok(sysExtService.calculateLogisticsCost(currentTenant(), countryCode, channel, weightKg));
    }

    @GetMapping("/logistics-rules")
    public Result<List<LogisticsRule>> listLogisticsRules(@RequestParam(required = false) String countryCode) {
        return Result.ok(sysExtService.listLogisticsRules(currentTenant(), countryCode));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateDictRequest(@NotBlank String dictCode, @NotBlank String dictName, String dictType,
                                    String parentCode, int sortOrder, String remark) {}
    public record UpdateDictRequest(String dictName, int sortOrder, String remark) {}
    public record CreateAIFeatureToggleRequest(@NotBlank String featureCode, @NotBlank String featureName,
                                               String domain, String description, String configJson) {}
    public record CreateLogisticsRuleRequest(@NotBlank String ruleName, @NotBlank String countryCode, String channel,
                                             BigDecimal weightMinKg, BigDecimal weightMaxKg, @Positive BigDecimal baseCost,
                                             BigDecimal costPerKg, int estimatedDaysMin, int estimatedDaysMax, int priority) {}
    public record UpdateLogisticsRuleRequest(String ruleName, BigDecimal baseCost, BigDecimal costPerKg,
                                             int estimatedDaysMin, int estimatedDaysMax, int priority) {}
}
