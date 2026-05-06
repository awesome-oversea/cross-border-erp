package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SystemConfigService;
import com.aidotnet.erp.sys.application.SystemConfigService.SaveConfigCommand;
import com.aidotnet.erp.sys.domain.SystemConfig;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1/configs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @PostMapping
    public Result<SystemConfig> create(@Valid @RequestBody SaveConfigRequest request) {
        return Result.ok(systemConfigService.create(currentTenant(), new SaveConfigCommand(request.configKey(), request.configValue(), request.description())));
    }

    @PutMapping("/{configId}")
    public Result<SystemConfig> update(@PathVariable String configId, @Valid @RequestBody SaveConfigRequest request) {
        return Result.ok(systemConfigService.update(currentTenant(), configId, new SaveConfigCommand(request.configKey(), request.configValue(), request.description())));
    }

    @PatchMapping("/{configId}/enable")
    public Result<SystemConfig> enable(@PathVariable String configId) {
        return Result.ok(systemConfigService.enable(currentTenant(), configId));
    }

    @PatchMapping("/{configId}/disable")
    public Result<SystemConfig> disable(@PathVariable String configId) {
        return Result.ok(systemConfigService.disable(currentTenant(), configId));
    }

    @GetMapping("/key/{configKey}")
    public Result<SystemConfig> getByKey(@PathVariable String configKey) {
        return Result.ok(systemConfigService.getByKey(currentTenant(), configKey));
    }

    @GetMapping
    public Result<List<SystemConfig>> list() {
        return Result.ok(systemConfigService.list(currentTenant()));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record SaveConfigRequest(@NotBlank String configKey, @NotBlank String configValue, String description) {}
}
