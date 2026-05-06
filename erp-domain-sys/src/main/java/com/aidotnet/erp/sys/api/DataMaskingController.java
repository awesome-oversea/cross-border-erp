package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.application.SysExtService.CreateDataMaskingRuleCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateDataMaskingRuleCommand;
import com.aidotnet.erp.sys.domain.DataMaskingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("sysDataMaskingController")
@RequestMapping("/sys/api/in/v1/masking")
public class DataMaskingController {

    private final SysExtService sysExtService;

    public DataMaskingController(SysExtService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping("/rules")
    public Result<DataMaskingRule> createRule(@Valid @RequestBody CreateDataMaskingRuleRequest request) {
        return Result.ok(sysExtService.createDataMaskingRule(currentTenant(), new CreateDataMaskingRuleCommand(
                request.ruleCode(), request.ruleName(), request.fieldType(), request.maskPattern(),
                request.replaceChar(), request.keepPrefix(), request.keepSuffix(), request.description())));
    }

    @PutMapping("/rules/{ruleId}")
    public Result<DataMaskingRule> updateRule(@PathVariable String ruleId, @Valid @RequestBody UpdateDataMaskingRuleRequest request) {
        return Result.ok(sysExtService.updateDataMaskingRule(currentTenant(), ruleId, new UpdateDataMaskingRuleCommand(
                request.ruleName(), request.maskPattern(), request.replaceChar(),
                request.keepPrefix(), request.keepSuffix(), request.description())));
    }

    @PatchMapping("/rules/{ruleId}/toggle")
    public Result<DataMaskingRule> toggleRule(@PathVariable String ruleId, @RequestParam boolean enabled) {
        return Result.ok(sysExtService.toggleDataMaskingRule(currentTenant(), ruleId, enabled));
    }

    @GetMapping("/rules")
    public Result<List<DataMaskingRule>> listRules(@RequestParam(required = false) String fieldType) {
        return Result.ok(sysExtService.listDataMaskingRules(currentTenant(), fieldType));
    }

    @GetMapping("/rules/{ruleId}")
    public Result<DataMaskingRule> getRule(@PathVariable String ruleId) {
        return Result.ok(sysExtService.getDataMaskingRule(currentTenant(), ruleId));
    }

    @PostMapping("/mask")
    public Result<String> maskValue(@Valid @RequestBody MaskValueRequest request) {
        return Result.ok(sysExtService.maskValue(currentTenant(), request.fieldType(), request.value()));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateDataMaskingRuleRequest(
            @NotBlank String ruleCode, @NotBlank String ruleName, @NotBlank String fieldType,
            @NotBlank String maskPattern, String replaceChar, int keepPrefix, int keepSuffix,
            String description) {}
    public record UpdateDataMaskingRuleRequest(
            String ruleName, String maskPattern, String replaceChar,
            int keepPrefix, int keepSuffix, String description) {}
    public record MaskValueRequest(@NotBlank String fieldType, @NotBlank String value) {}
}
