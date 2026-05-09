package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.InvoiceVoucherService;
import com.aidotnet.erp.fms.application.InvoiceVoucherService.CreateInvoiceCommand;
import com.aidotnet.erp.fms.application.InvoiceVoucherService.CreateVoucherCommand;
import com.aidotnet.erp.fms.domain.Invoice;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.VoucherLine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS发票凭证控制器
 * <p>
 * 描述: 财务域发票与凭证管理接口，提供发票开具、凭证生成、
 *       凭证过账、凭证导出等操作。
 * </p>
 * <p>
 * 路径规范: /fms/api/in/v1 与 /fms/api/v1
 * 描述: 保留原有 in 路径兼容，同时为 ERP 内部域间调用提供统一 v1 入口。
 * </p>
 *
 * @author ERP系统
 * @see InvoiceVoucherService
 * @see InventoryVoucherEngine
 */
@RestController
@RequestMapping({"/fms/api/in/v1", "/fms/api/v1"})
public class InvoiceVoucherController {

    private final InvoiceVoucherService service;

    public InvoiceVoucherController(InvoiceVoucherService service) {
        this.service = service;
    }

    @PostMapping("/invoices")
    public Result<Invoice> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        CreateInvoiceCommand command = new CreateInvoiceCommand(request.invoiceType(), request.customerId(),
                request.customerName(), request.countryCode(), request.currency(), request.subtotalAmount(),
                request.taxAmount(), request.taxIdNumber(), request.invoiceDate(), request.dueDate(), request.remark());
        return Result.ok(service.createInvoice(currentTenant(), command));
    }

    @PatchMapping("/invoices/{invoiceId}/issue")
    public Result<Invoice> issueInvoice(@PathVariable String invoiceId) {
        return Result.ok(service.issueInvoice(currentTenant(), invoiceId));
    }

    @PatchMapping("/invoices/{invoiceId}/void")
    public Result<Invoice> voidInvoice(@PathVariable String invoiceId, @RequestBody VoidInvoiceRequest request) {
        return Result.ok(service.voidInvoice(currentTenant(), invoiceId, request.reason()));
    }

    @PatchMapping("/invoices/{invoiceId}/red-invoice")
    public Result<Invoice> redInvoice(@PathVariable String invoiceId) {
        return Result.ok(service.redInvoice(currentTenant(), invoiceId));
    }

    @GetMapping("/invoices")
    public Result<List<Invoice>> listInvoices(@RequestParam(required = false) String countryCode,
                                              @RequestParam(required = false) String status) {
        return Result.ok(service.listInvoices(currentTenant(), countryCode, status));
    }

    @GetMapping("/invoices/{invoiceId}")
    public Result<Invoice> getInvoice(@PathVariable String invoiceId) {
        return Result.ok(service.getInvoice(currentTenant(), invoiceId));
    }

    @PostMapping("/vouchers")
    public Result<Voucher> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        CreateVoucherCommand command = new CreateVoucherCommand(request.voucherType(), request.referenceType(),
                request.referenceId(), request.currency(), request.voucherDate(), request.lines());
        return Result.ok(service.createVoucher(currentTenant(), command));
    }

    @PatchMapping("/vouchers/{voucherId}/post")
    public Result<Voucher> postVoucher(@PathVariable String voucherId, @RequestBody PostVoucherRequest request) {
        return Result.ok(service.postVoucher(currentTenant(), voucherId, request.postedBy()));
    }

    @PatchMapping("/vouchers/{voucherId}/void")
    public Result<Voucher> voidVoucher(@PathVariable String voucherId) {
        return Result.ok(service.voidVoucher(currentTenant(), voucherId));
    }

    @PatchMapping("/vouchers/{voucherId}/export")
    public Result<Voucher> markExported(@PathVariable String voucherId, @RequestBody ExportVoucherRequest request) {
        return Result.ok(service.markExported(currentTenant(), voucherId, request.exportBatchId()));
    }

    @GetMapping("/vouchers")
    public Result<List<Voucher>> listVouchers(@RequestParam(required = false) String voucherType,
                                              @RequestParam(required = false) String status) {
        return Result.ok(service.listVouchers(currentTenant(), voucherType, status));
    }

    @GetMapping("/vouchers/{voucherId}")
    public Result<Voucher> getVoucher(@PathVariable String voucherId) {
        return Result.ok(service.getVoucher(currentTenant(), voucherId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateInvoiceRequest(@NotBlank String invoiceType, String customerId, String customerName,
                                       @NotBlank String countryCode, @NotBlank String currency,
                                       @Positive BigDecimal subtotalAmount, BigDecimal taxAmount,
                                       String taxIdNumber, Instant invoiceDate, Instant dueDate, String remark) {}
    public record VoidInvoiceRequest(@NotBlank String reason) {}
    public record CreateVoucherRequest(@NotBlank String voucherType, String referenceType, String referenceId,
                                       @NotBlank String currency, Instant voucherDate,
                                       List<VoucherLine> lines) {}
    public record PostVoucherRequest(@NotBlank String postedBy) {}
    public record ExportVoucherRequest(@NotBlank String exportBatchId) {}
}
