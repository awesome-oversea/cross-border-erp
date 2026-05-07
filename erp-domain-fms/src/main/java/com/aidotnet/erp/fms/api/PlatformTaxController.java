package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.FmsExtService;
import com.aidotnet.erp.fms.application.FmsExtService.CreateTaxRuleCommand;
import com.aidotnet.erp.fms.domain.TaxRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("platformFmsTaxController")
@RequestMapping("/platform/fms/api/v1/tax")
public class PlatformTaxController {

    private final FmsExtService fmsExtService;

    public PlatformTaxController(FmsExtService fmsExtService) {
        this.fmsExtService = fmsExtService;
    }

    @PostMapping("/rules")
    public Result<TaxRule> createRule(@Valid @RequestBody CreateTaxRuleRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(fmsExtService.createTaxRule(currentTenant(),
                new CreateTaxRuleCommand(request.countryCode(), request.taxType(), request.taxRate(),
                        request.taxCategory(), request.effectiveFrom(), request.effectiveTo()))));
    }

    @GetMapping("/rules")
    public Result<List<TaxRule>> listRules(@RequestParam(required = false) String tenantId,
                                           @RequestParam String countryCode) {
        return inTenant(tenantId, () -> Result.ok(fmsExtService.listTaxRules(currentTenant(), countryCode)));
    }

    @PostMapping("/calculate")
    public Result<TaxCalculationView> calculate(@Valid @RequestBody CalculateTaxRequest request) {
        return inTenant(request.tenantId(), () -> {
            BigDecimal taxAmount = fmsExtService.calculateTax(currentTenant(), request.countryCode(),
                    request.taxType(), request.amount());
            return Result.ok(new TaxCalculationView(request.countryCode(), request.taxType(),
                    request.amount(), taxAmount, request.amount().add(taxAmount)));
        });
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

    public record CreateTaxRuleRequest(String tenantId,
                                       @NotBlank String countryCode,
                                       @NotBlank String taxType,
                                       @Positive BigDecimal taxRate,
                                       String taxCategory,
                                       Instant effectiveFrom,
                                       Instant effectiveTo) {}

    public record CalculateTaxRequest(String tenantId,
                                      @NotBlank String countryCode,
                                      @NotBlank String taxType,
                                      @Positive BigDecimal amount) {}

    public record TaxCalculationView(String countryCode, String taxType, BigDecimal amount,
                                     BigDecimal taxAmount, BigDecimal totalAmount) {}
}
