package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.invoice.InvoiceTaxService;
import com.aidotnet.erp.common.invoice.InvoiceTaxService.Invoice;
import com.aidotnet.erp.common.invoice.InvoiceTaxService.InvoiceTemplate;
import com.aidotnet.erp.common.invoice.InvoiceTaxService.TaxCalculationResult;
import com.aidotnet.erp.common.invoice.InvoiceTaxService.TaxFilingData;
import com.aidotnet.erp.common.invoice.InvoiceTaxService.TaxRate;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS税务发票控制器
 * <p>
 * 描述: 财务域税务管理接口，提供VAT计算、税务合规检测、税率查询等操作。
 * </p>
 * <p>
 * 路径规范: /fms/api/in/v1/tax-invoice 与 /fms/api/v1/tax-invoice
 * 描述: 保留原有 in 路径兼容，同时为 ERP 内部域服务调用提供统一 v1 入口。
 * </p>
 *
 * @author ERP系统
 * @see InvoiceVoucherService
 * @see ComplianceRiskService
 */
@RestController
@RequestMapping({"/fms/api/in/v1/tax-invoice", "/fms/api/v1/tax-invoice"})
public class TaxInvoiceController {

    private final InvoiceTaxService invoiceTaxService;

    public TaxInvoiceController(InvoiceTaxService invoiceTaxService) {
        this.invoiceTaxService = invoiceTaxService;
    }

    @PostMapping("/tax-rates")
    public Result<TaxRate> addTaxRate(@Valid @RequestBody AddTaxRateRequest request) {
        invoiceTaxService.addTaxRate(request.country(), request.region(), request.taxType(), request.rate());
        return Result.ok(invoiceTaxService.findTaxRate(request.country(), request.region(), request.taxType()));
    }

    @GetMapping("/tax-rates")
    public Result<List<TaxRate>> listTaxRates(@RequestParam(required = false) String country) {
        return Result.ok(invoiceTaxService.listTaxRates(country));
    }

    @PostMapping("/tax-rates/calculate")
    public Result<TaxCalculationResult> calculateTax(@Valid @RequestBody CalculateTaxRequest request) {
        return Result.ok(invoiceTaxService.calculateTaxWithDetails(
                request.country(), request.region(), request.amount(), request.taxTypes()));
    }

    @PostMapping("/invoice-templates")
    public Result<InvoiceTemplate> createTemplate(@Valid @RequestBody CreateInvoiceTemplateRequest request) {
        return Result.ok(invoiceTaxService.createTemplate(
                request.templateId(), request.templateName(), request.templateType(),
                request.country(), request.layout()));
    }

    @GetMapping("/invoice-templates")
    public Result<List<InvoiceTemplate>> listTemplates(@RequestParam(required = false) String templateType) {
        return Result.ok(invoiceTaxService.listTemplates(templateType));
    }

    @PostMapping("/invoices")
    public Result<Invoice> generateInvoice(@Valid @RequestBody GenerateInvoiceRequest request) {
        return Result.ok(invoiceTaxService.generateInvoice(
                request.orderId(), request.invoiceType(), request.templateId(),
                currentTenant(), request.currency(), request.amount(),
                request.customerName(), request.data()));
    }

    @GetMapping("/invoices")
    public Result<List<Invoice>> listInvoices(@RequestParam(required = false) String invoiceType) {
        return Result.ok(invoiceTaxService.listInvoices(currentTenant(), invoiceType));
    }

    @PostMapping("/invoices/{invoiceNumber}/void")
    public Result<Invoice> voidInvoice(@PathVariable String invoiceNumber,
                                       @Valid @RequestBody VoidInvoiceRequest request) {
        return Result.ok(invoiceTaxService.voidInvoice(invoiceNumber, request.reason()));
    }

    @PostMapping("/invoices/{invoiceNumber}/red-flush")
    public Result<Invoice> redFlushInvoice(@PathVariable String invoiceNumber,
                                           @Valid @RequestBody RedFlushRequest request) {
        return Result.ok(invoiceTaxService.redFlushInvoice(invoiceNumber, request.reason()));
    }

    @GetMapping("/tax-filing")
    public Result<TaxFilingData> getTaxFilingData(@RequestParam String country,
                                                   @RequestParam String region,
                                                   @RequestParam LocalDate periodStart,
                                                   @RequestParam LocalDate periodEnd) {
        return Result.ok(invoiceTaxService.getFilingData(country, region, periodStart, periodEnd));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record AddTaxRateRequest(@NotBlank String country, String region,
                                    @NotBlank String taxType, @Positive BigDecimal rate) {}
    public record CalculateTaxRequest(@NotBlank String country, String region,
                                      @Positive BigDecimal amount, List<String> taxTypes) {}
    public record CreateInvoiceTemplateRequest(@NotBlank String templateId, @NotBlank String templateName,
                                               @NotBlank String templateType, @NotBlank String country,
                                               Map<String, Object> layout) {}
    public record GenerateInvoiceRequest(@NotBlank String orderId, @NotBlank String invoiceType,
                                         String templateId, @NotBlank String currency,
                                         @Positive BigDecimal amount, String customerName,
                                         Map<String, Object> data) {}
    public record VoidInvoiceRequest(@NotBlank String reason) {}
    public record RedFlushRequest(@NotBlank String reason) {}
}
