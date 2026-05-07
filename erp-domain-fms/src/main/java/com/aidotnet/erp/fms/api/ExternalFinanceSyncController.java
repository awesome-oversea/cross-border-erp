package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.ExternalFinanceSyncService;
import com.aidotnet.erp.fms.domain.FinanceSyncConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS 澶栭儴璐㈠姟绯荤粺闆嗘垚鎺у埗鍣?
 * <p>
 * 鎻忚堪: 璐㈠姟鍩熷閲戣澏/鐢ㄥ弸绛夊閮ㄨ储鍔＄郴缁熺殑鍚屾閰嶇疆绠＄悊鍏ュ彛銆?
 *       璇ユ帶鍒跺櫒涓撴敞浜庨泦鎴愰厤缃殑寤虹珛涓庢煡璇紝閬垮厤鍜屽嚟璇佺敓鎴愩€佹垚鏈綊闆嗙瓑鏍稿績璐㈠姟娣峰湪鍚屼竴鎺ュ彛涓€?
 * </p>
 *
 * @author ERP绯荤粺
 */
@RestController
@RequestMapping("/fms/api/in/v1")
public class ExternalFinanceSyncController {

    private final ExternalFinanceSyncService externalFinanceSyncService;

    public ExternalFinanceSyncController(ExternalFinanceSyncService externalFinanceSyncService) {
        this.externalFinanceSyncService = externalFinanceSyncService;
    }

    @PostMapping("/finance-sync-configs")
    public Result<FinanceSyncConfigView> createFinanceSyncConfig(
            @Valid @RequestBody CreateFinanceSyncConfigRequest request) {
        FinanceSyncConfig config = externalFinanceSyncService.createSyncConfig(
                currentTenant(),
                request.financeSystem(),
                request.apiUrl(),
                request.apiKey(),
                request.apiSecret(),
                request.accountSet(),
                request.enabled(),
                request.mappingRules() != null ? request.mappingRules() : Map.of());
        return Result.ok(toView(config));
    }

    @GetMapping("/finance-sync-configs")
    public Result<List<FinanceSyncConfigView>> listFinanceSyncConfigs() {
        return Result.ok(externalFinanceSyncService.listSyncConfigs(currentTenant()).stream()
                .map(this::toView)
                .toList());
    }

    private FinanceSyncConfigView toView(FinanceSyncConfig config) {
        return new FinanceSyncConfigView(
                config.configId(),
                config.financeSystem(),
                config.apiUrl(),
                config.accountSet(),
                config.enabled(),
                config.mappingRules(),
                config.lastSyncAt(),
                config.createdAt(),
                config.updatedAt());
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "绉熸埛涓嶈兘涓虹┖");
        }
        return tenantId;
    }

    public record CreateFinanceSyncConfigRequest(
            @NotBlank String financeSystem,
            @NotBlank String apiUrl,
            @NotBlank String apiKey,
            String apiSecret,
            String accountSet,
            boolean enabled,
            Map<String, String> mappingRules) {}

    public record FinanceSyncConfigView(
            String configId,
            String financeSystem,
            String apiUrl,
            String accountSet,
            boolean enabled,
            Map<String, String> mappingRules,
            Instant lastSyncAt,
            Instant createdAt,
            Instant updatedAt) {}
}
