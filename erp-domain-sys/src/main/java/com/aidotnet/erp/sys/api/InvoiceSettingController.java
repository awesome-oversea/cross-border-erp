package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.application.SysExtService.CreateInvoiceSettingCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateInvoiceSettingCommand;
import com.aidotnet.erp.sys.domain.InvoiceSetting;
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
@RequestMapping("/sys/api/in/v1/invoice-settings")
public class InvoiceSettingController {

    private final SysExtService sysExtService;

    public InvoiceSettingController(SysExtService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping
    public Result<InvoiceSetting> create(@Valid @RequestBody CreateInvoiceSettingRequest request) {
        return Result.ok(sysExtService.createInvoiceSetting(currentTenant(), new CreateInvoiceSettingCommand(
                request.settingType(), request.settingName(), request.config(), request.description())));
    }

    @PutMapping("/{settingId}")
    public Result<InvoiceSetting> update(@PathVariable String settingId, @Valid @RequestBody UpdateInvoiceSettingRequest request) {
        return Result.ok(sysExtService.updateInvoiceSetting(currentTenant(), settingId, new UpdateInvoiceSettingCommand(
                request.settingName(), request.config(), request.description())));
    }

    @PatchMapping("/{settingId}/toggle")
    public Result<InvoiceSetting> toggle(@PathVariable String settingId, @RequestParam boolean enabled) {
        return Result.ok(sysExtService.toggleInvoiceSetting(currentTenant(), settingId, enabled));
    }

    @GetMapping
    public Result<List<InvoiceSetting>> list(@RequestParam(required = false) String settingType) {
        return Result.ok(sysExtService.listInvoiceSettings(currentTenant(), settingType));
    }

    @GetMapping("/{settingId}")
    public Result<InvoiceSetting> get(@PathVariable String settingId) {
        return Result.ok(sysExtService.getInvoiceSetting(currentTenant(), settingId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateInvoiceSettingRequest(
            @NotBlank String settingType, @NotBlank String settingName,
            Map<String, Object> config, String description) {}
    public record UpdateInvoiceSettingRequest(
            String settingName, Map<String, Object> config, String description) {}
}
