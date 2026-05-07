package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.AutoGenerateVoucherCommand;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.CreateVoucherTemplateCommand;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.VoucherTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("platformFmsVoucherEngineController")
@RequestMapping("/platform/fms/api/v1/voucher-engine")
public class PlatformVoucherEngineController {

    private final InventoryVoucherEngine voucherEngine;

    public PlatformVoucherEngineController(InventoryVoucherEngine voucherEngine) {
        this.voucherEngine = voucherEngine;
    }

    @PostMapping("/templates")
    public Result<VoucherTemplate> createTemplate(@Valid @RequestBody CreateVoucherTemplateRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(voucherEngine.createTemplate(currentTenant(),
                new CreateVoucherTemplateCommand(request.templateName(), request.businessType(),
                        request.debitAccount(), request.creditAccount(), request.description()))));
    }

    @GetMapping("/templates")
    public Result<List<VoucherTemplate>> listTemplates(@RequestParam(required = false) String tenantId,
                                                       @RequestParam(required = false) String businessType) {
        return inTenant(tenantId, () -> Result.ok(voucherEngine.listTemplates(currentTenant(), businessType)));
    }

    @PostMapping("/auto-generate")
    public Result<Voucher> autoGenerate(@Valid @RequestBody AutoGenerateVoucherRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(voucherEngine.autoGenerate(currentTenant(),
                new AutoGenerateVoucherCommand(request.businessType(), request.voucherType(),
                        request.sourceId(), request.amount(), request.currency(), request.sellerSku()))));
    }

    @PostMapping("/{voucherId}/approve")
    public Result<Voucher> approve(@PathVariable String voucherId,
                                   @Valid @RequestBody ApproveVoucherRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(voucherEngine.approveVoucher(currentTenant(),
                voucherId, request.approvedBy())));
    }

    @GetMapping("/{voucherId}")
    public Result<Voucher> getVoucher(@PathVariable String voucherId,
                                      @RequestParam(required = false) String tenantId) {
        return inTenant(tenantId, () -> Result.ok(voucherEngine.getVoucher(currentTenant(), voucherId)));
    }

    @GetMapping("/summary")
    public Result<InventoryVoucherEngine.VoucherSummaryResult> getSummary(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String periodStart,
            @RequestParam(required = false) String periodEnd) {
        return inTenant(tenantId, () -> Result.ok(voucherEngine.getVoucherSummary(currentTenant(), periodStart, periodEnd)));
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

    public record CreateVoucherTemplateRequest(String tenantId,
                                               @NotBlank String templateName,
                                               @NotBlank String businessType,
                                               @NotBlank String debitAccount,
                                               @NotBlank String creditAccount,
                                               String description) {}

    public record AutoGenerateVoucherRequest(String tenantId,
                                             @NotBlank String businessType,
                                             @NotBlank String voucherType,
                                             @NotBlank String sourceId,
                                             @Positive BigDecimal amount,
                                             String currency,
                                             String sellerSku) {}

    public record ApproveVoucherRequest(String tenantId, @NotBlank String approvedBy) {}
}
