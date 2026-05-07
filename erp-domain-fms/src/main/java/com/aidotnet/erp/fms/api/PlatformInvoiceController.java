package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.InvoiceVoucherService;
import com.aidotnet.erp.fms.application.InvoiceVoucherService.CreateInvoiceCommand;
import com.aidotnet.erp.fms.application.InvoiceVoucherService.SaveInvoiceSettingCommand;
import com.aidotnet.erp.fms.domain.Invoice;
import com.aidotnet.erp.fms.domain.InvoiceSetting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("platformFmsInvoiceController")
@RequestMapping("/platform/fms/api/v1/invoice")
public class PlatformInvoiceController {

    private final InvoiceVoucherService invoiceVoucherService;

    public PlatformInvoiceController(InvoiceVoucherService invoiceVoucherService) {
        this.invoiceVoucherService = invoiceVoucherService;
    }

    @PostMapping("/generate")
    public Result<Invoice> generate(@Valid @RequestBody GenerateInvoiceRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(invoiceVoucherService.createInvoice(currentTenant(),
                new CreateInvoiceCommand(request.invoiceType(), request.customerId(), request.customerName(),
                        request.countryCode(), request.currency(), request.subtotalAmount(), request.taxAmount(),
                        request.taxIdNumber(), request.invoiceDate(), request.dueDate(), request.remark()))));
    }

    @PostMapping("/{invoiceId}/issue")
    public Result<Invoice> issue(@PathVariable String invoiceId,
                                 @RequestBody(required = false) TenantRequest request) {
        return inTenant(request != null ? request.tenantId() : null,
                () -> Result.ok(invoiceVoucherService.issueInvoice(currentTenant(), invoiceId)));
    }

    @PostMapping("/{invoiceId}/red-flush")
    public Result<Invoice> redFlush(@PathVariable String invoiceId,
                                    @RequestBody(required = false) TenantReasonRequest request) {
        return inTenant(request != null ? request.tenantId() : null,
                () -> Result.ok(invoiceVoucherService.redInvoice(currentTenant(), invoiceId)));
    }

    @PostMapping("/{invoiceId}/void")
    public Result<Invoice> voidInvoice(@PathVariable String invoiceId,
                                       @Valid @RequestBody TenantReasonRequest request) {
        return inTenant(request.tenantId(),
                () -> Result.ok(invoiceVoucherService.voidInvoice(currentTenant(), invoiceId, request.reason())));
    }

    @GetMapping("/list")
    public Result<List<Invoice>> listInvoices(@RequestParam(required = false) String tenantId,
                                              @RequestParam(required = false) String countryCode,
                                              @RequestParam(required = false) String status) {
        return inTenant(tenantId, () -> Result.ok(invoiceVoucherService.listInvoices(currentTenant(), countryCode, status)));
    }

    @GetMapping("/{invoiceId}")
    public Result<Invoice> getInvoice(@PathVariable String invoiceId,
                                      @RequestParam(required = false) String tenantId) {
        return inTenant(tenantId, () -> Result.ok(invoiceVoucherService.getInvoice(currentTenant(), invoiceId)));
    }

    @PostMapping("/settings")
    public Result<InvoiceSetting> saveInvoiceSetting(@Valid @RequestBody SaveInvoiceSettingRequest request) {
        return inTenant(request.tenantId(), () -> Result.ok(invoiceVoucherService.saveInvoiceSetting(currentTenant(),
                new SaveInvoiceSettingCommand(request.storeId(), request.marketplaceId(), request.templateId(),
                        request.invoiceTitle(), request.taxRegistrationNo(), request.showUnitPrice(),
                        request.showTaxRate(), request.showDiscount(), request.remark()))));
    }

    @GetMapping("/settings")
    public Result<List<InvoiceSetting>> listInvoiceSettings(@RequestParam(required = false) String tenantId) {
        return inTenant(tenantId, () -> Result.ok(invoiceVoucherService.listInvoiceSettings(currentTenant())));
    }

    @GetMapping("/settings/current")
    public Result<InvoiceSetting> getCurrentInvoiceSetting(@RequestParam(required = false) String tenantId,
                                                           @RequestParam String storeId,
                                                           @RequestParam(required = false) String marketplaceId) {
        return inTenant(tenantId, () -> Result.ok(invoiceVoucherService.getInvoiceSetting(currentTenant(), storeId, marketplaceId)));
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

    public record GenerateInvoiceRequest(String tenantId,
                                         @NotBlank String invoiceType,
                                         String customerId,
                                         String customerName,
                                         @NotBlank String countryCode,
                                         @NotBlank String currency,
                                         @Positive BigDecimal subtotalAmount,
                                         BigDecimal taxAmount,
                                         String taxIdNumber,
                                         Instant invoiceDate,
                                         Instant dueDate,
                                         String remark) {}

    public record SaveInvoiceSettingRequest(String tenantId,
                                            @NotBlank String storeId,
                                            String marketplaceId,
                                            String templateId,
                                            @NotBlank String invoiceTitle,
                                            String taxRegistrationNo,
                                            boolean showUnitPrice,
                                            boolean showTaxRate,
                                            boolean showDiscount,
                                            String remark) {}

    public record TenantRequest(String tenantId) {}

    public record TenantReasonRequest(String tenantId, String reason) {}
}
