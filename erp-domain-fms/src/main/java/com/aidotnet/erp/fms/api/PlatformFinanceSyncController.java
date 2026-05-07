package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.ExternalFinanceSyncService;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine;
import com.aidotnet.erp.fms.domain.ExternalFinanceVoucher;
import com.aidotnet.erp.fms.domain.FinanceSyncConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("platformFmsFinanceSyncController")
@RequestMapping("/platform/fms/api/v1/finance-sync")
public class PlatformFinanceSyncController {

    private final ExternalFinanceSyncService externalFinanceSyncService;
    private final InventoryVoucherEngine voucherEngine;

    public PlatformFinanceSyncController(ExternalFinanceSyncService externalFinanceSyncService,
                                         InventoryVoucherEngine voucherEngine) {
        this.externalFinanceSyncService = externalFinanceSyncService;
        this.voucherEngine = voucherEngine;
    }

    @PostMapping("/configs")
    public Result<FinanceSyncConfig> createConfig(@Valid @RequestBody CreateFinanceSyncConfigRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(externalFinanceSyncService.createSyncConfig(
                currentTenant(), request.financeSystem(), request.apiUrl(), request.apiKey(),
                request.apiSecret(), request.accountSet(), request.enabled(),
                request.mappingRules() != null ? request.mappingRules() : Map.of())));
    }

    @GetMapping("/configs")
    public Result<List<FinanceSyncConfig>> listConfigs(@RequestParam(required = false) String tenantId,
                                                       @RequestParam(required = false) String financeSystem) {
        return inTenant(tenantId, () -> Result.ok(externalFinanceSyncService.listSyncConfigs(currentTenant()).stream()
                .filter(config -> financeSystem == null || financeSystem.isBlank()
                        || financeSystem.equalsIgnoreCase(config.financeSystem()))
                .toList()));
    }

    @PostMapping("/configs/{configId}/test")
    public Result<Boolean> testConfig(@PathVariable String configId,
                                      @RequestBody(required = false) TenantRequest request) {
        return inTenant(request != null ? request.tenantId() : null,
                () -> Result.ok(externalFinanceSyncService.testConnection(currentTenant(), configId)));
    }

    @PostMapping("/vouchers/{voucherId}/push")
    public Result<Boolean> pushVoucher(@PathVariable String voucherId,
                                       @Valid @RequestBody PushVoucherRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(pushVoucherInternal(currentTenant(), voucherId, request.financeSystem())));
    }

    @GetMapping("/vouchers/{voucherId}")
    public Result<ExternalFinanceVoucher> getExternalVoucher(@PathVariable String voucherId,
                                                             @RequestParam(required = false) String tenantId) {
        return inTenant(tenantId, () -> Result.ok(externalFinanceSyncService.findVoucher(currentTenant(), voucherId)));
    }

    @GetMapping("/vouchers")
    public Result<List<ExternalFinanceVoucher>> listExternalVouchers(@RequestParam(required = false) String tenantId,
                                                                     @RequestParam(required = false) String financeSystem,
                                                                     @RequestParam(required = false) String syncStatus) {
        return inTenant(tenantId, () -> Result.ok(externalFinanceSyncService.listVouchers(currentTenant(), syncStatus).stream()
                .filter(voucher -> financeSystem == null || financeSystem.isBlank()
                        || financeSystem.equalsIgnoreCase(voucher.financeSystem()))
                .toList()));
    }

    @PostMapping("/vouchers/{voucherId}/retry")
    public Result<ExternalFinanceVoucher> retryVoucher(@PathVariable String voucherId,
                                                       @RequestBody(required = false) TenantRequest request) {
        return inTenant(request != null ? request.tenantId() : null,
                () -> Result.ok(voucherEngine.retryExternalVoucher(currentTenant(), voucherId)));
    }

    private boolean pushVoucherInternal(String tenantId, String voucherId, String financeSystem) {
        return switch (financeSystem.toUpperCase()) {
            case "KINGDEE" -> voucherEngine.pushToKingdee(tenantId, voucherId);
            case "YONYOU" -> voucherEngine.pushToYonyou(tenantId, voucherId);
            default -> throw new BizException("UNSUPPORTED_FINANCE_SYSTEM", "Unsupported finance system: " + financeSystem);
        };
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return tenantId;
    }

    private <T> T inTenant(String tenantId, Supplier<T> action) {
        String previousTenantId = TenantContext.getTenantId();
        String resolvedTenantId = resolveTenantId(tenantId);
        TenantContext.setTenantId(resolvedTenantId);
        try {
            return action.get();
        } finally {
            TenantContext.setTenantId(previousTenantId);
        }
    }

    private String resolveTenantId(String tenantId) {
        String resolved = tenantId != null && !tenantId.isBlank() ? tenantId.trim() : TenantContext.getTenantId();
        if (resolved == null || resolved.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return resolved.trim();
    }

    public record CreateFinanceSyncConfigRequest(String tenantId,
                                                 @NotBlank String financeSystem,
                                                 @NotBlank String apiUrl,
                                                 @NotBlank String apiKey,
                                                 String apiSecret,
                                                 String accountSet,
                                                 boolean enabled,
                                                 Map<String, String> mappingRules) {}

    public record TenantRequest(String tenantId) {}

    public record PushVoucherRequest(String tenantId, @NotBlank String financeSystem) {}
}
