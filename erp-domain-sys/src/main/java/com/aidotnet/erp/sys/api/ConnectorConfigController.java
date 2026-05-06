package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.application.SysExtService.CreateConnectorConfigCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateConnectorConfigCommand;
import com.aidotnet.erp.sys.domain.ConnectorConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/sys/api/in/v1/connector-configs")
public class ConnectorConfigController {

    private final SysExtService sysExtService;

    public ConnectorConfigController(SysExtService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping
    public Result<ConnectorConfig> create(@Valid @RequestBody CreateConnectorConfigRequest request) {
        return Result.ok(sysExtService.createConnectorConfig(currentTenant(), new CreateConnectorConfigCommand(
                request.connectorType(), request.platform(), request.connectorName(),
                request.config(), request.version(), request.description())));
    }

    @PutMapping("/{configId}")
    public Result<ConnectorConfig> update(@PathVariable String configId, @Valid @RequestBody UpdateConnectorConfigRequest request) {
        return Result.ok(sysExtService.updateConnectorConfig(currentTenant(), configId, new UpdateConnectorConfigCommand(
                request.connectorName(), request.config(), request.version(), request.description())));
    }

    @PatchMapping("/{configId}/status")
    public Result<ConnectorConfig> updateStatus(@PathVariable String configId, @RequestParam String status) {
        return Result.ok(sysExtService.updateConnectorStatus(currentTenant(), configId, status));
    }

    @GetMapping
    public Result<List<ConnectorConfig>> list(@RequestParam(required = false) String connectorType,
                                               @RequestParam(required = false) String platform) {
        return Result.ok(sysExtService.listConnectorConfigs(currentTenant(), connectorType, platform));
    }

    @GetMapping("/active")
    public Result<List<ConnectorConfig>> listActive(@RequestParam(required = false) String connectorType) {
        return Result.ok(sysExtService.listActiveConnectorConfigs(currentTenant(), connectorType));
    }

    @GetMapping("/{configId}")
    public Result<ConnectorConfig> get(@PathVariable String configId) {
        return Result.ok(sysExtService.getConnectorConfig(currentTenant(), configId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateConnectorConfigRequest(
            @NotBlank String connectorType, @NotBlank String platform, @NotBlank String connectorName,
            Map<String, Object> config, String version, String description) {}
    public record UpdateConnectorConfigRequest(
            String connectorName, Map<String, Object> config, String version, String description) {}
}
