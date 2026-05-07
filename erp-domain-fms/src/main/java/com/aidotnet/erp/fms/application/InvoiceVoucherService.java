package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.Invoice;
import com.aidotnet.erp.fms.domain.InvoiceSetting;
import com.aidotnet.erp.fms.domain.InvoiceStatus;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.VoucherLine;
import com.aidotnet.erp.fms.domain.VoucherLineType;
import com.aidotnet.erp.fms.domain.Voucher.VoucherStatus;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发票税务服务
 * <p>
 * 描述: FMS域业务中台(5.8)，管理发票模板、税务参数、VAT计算。
 *       支持发票创建/开具/取消，自动关联会计凭证。
 *       集成外部税务服务商(Avalara/TaxJar)进行税率计算。
 * </p>
 * <p>
 * 发票状态流转: DRAFT → ISSUED → PAID → CANCELLED
 * VAT计算: 基于目的地原则，按国家/州税率计算
 * </p>
 *
 * @author ERP系统
 * @see Invoice
 * @see TaxCalculationResult
 * @see TaxRule
 */
@Service
public class InvoiceVoucherService {

    private final FmsExtStore extStore;

    public InvoiceVoucherService(FmsExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public Invoice createInvoice(String tenantId, CreateInvoiceCommand command) {
        BigDecimal taxAmount = command.taxAmount() != null ? command.taxAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = command.subtotalAmount().add(taxAmount);
        Instant now = Instant.now();
        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Invoice invoice = new Invoice(
                UUID.randomUUID().toString(), tenantId, invoiceNumber, command.invoiceType(),
                command.customerId(), command.customerName(), command.countryCode(),
                command.currency(), command.subtotalAmount(), taxAmount, totalAmount,
                command.taxIdNumber(), InvoiceStatus.DRAFT.name(), null,
                command.invoiceDate(), command.dueDate(), command.remark(), now, now);
        return extStore.saveInvoice(invoice);
    }

    @Transactional
    public Invoice issueInvoice(String tenantId, String invoiceId) {
        Invoice invoice = getInvoice(tenantId, invoiceId);
        if (!InvoiceStatus.DRAFT.name().equals(invoice.status())) {
            throw new BizException("INVOICE_STATUS_INVALID", "只有草稿发票可以开具");
        }
        return updateInvoiceStatus(invoice, InvoiceStatus.ISSUED.name());
    }

    @Transactional
    public Invoice voidInvoice(String tenantId, String invoiceId, String reason) {
        Invoice invoice = getInvoice(tenantId, invoiceId);
        if (InvoiceStatus.VOIDED.name().equals(invoice.status())) {
            throw new BizException("INVOICE_STATUS_INVALID", "发票已作废");
        }
        return updateInvoiceStatus(invoice, InvoiceStatus.VOIDED.name());
    }

    @Transactional
    public Invoice redInvoice(String tenantId, String invoiceId) {
        Invoice original = getInvoice(tenantId, invoiceId);
        if (!InvoiceStatus.ISSUED.name().equals(original.status()) && !InvoiceStatus.PAID.name().equals(original.status())) {
            throw new BizException("INVOICE_STATUS_INVALID", "只有已开具或已支付的发票可以红冲");
        }
        updateInvoiceStatus(original, InvoiceStatus.RED_INVOICED.name());
        Instant now = Instant.now();
        String redNumber = "RED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Invoice redInvoice = new Invoice(
                UUID.randomUUID().toString(), tenantId, redNumber, "RED_INVOICE",
                original.customerId(), original.customerName(), original.countryCode(),
                original.currency(), original.subtotalAmount().negate(),
                original.taxAmount().negate(), original.totalAmount().negate(),
                original.taxIdNumber(), InvoiceStatus.ISSUED.name(), null,
                now, null, "红冲原发票: " + original.invoiceNumber(), now, now);
        return extStore.saveInvoice(redInvoice);
    }

    public Invoice getInvoice(String tenantId, String invoiceId) {
        return extStore.findInvoice(tenantId, invoiceId)
                .orElseThrow(() -> new BizException("INVOICE_NOT_FOUND", "发票不存在"));
    }

    public List<Invoice> listInvoices(String tenantId, String countryCode, String status) {
        return extStore.listInvoices(tenantId, countryCode, status);
    }

    @Transactional
    public Voucher createVoucher(String tenantId, CreateVoucherCommand command) {
        BigDecimal totalDebit = command.lines().stream()
                .filter(l -> l.type() == VoucherLineType.DEBIT)
                .map(VoucherLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = command.lines().stream()
                .filter(l -> l.type() == VoucherLineType.CREDIT)
                .map(VoucherLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BizException("VOUCHER_NOT_BALANCED", "凭证借贷不平衡");
        }
        Instant now = Instant.now();
        String voucherNumber = "VOU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Voucher voucher = new Voucher(
                UUID.randomUUID().toString(), tenantId, voucherNumber, command.voucherType(),
                command.referenceType(), command.referenceId(), command.currency(),
                totalDebit, totalCredit, VoucherStatus.DRAFT, command.lines(),
                command.voucherDate(), null, null, null, now, now);
        return extStore.saveVoucher(voucher);
    }

    @Transactional
    public Voucher postVoucher(String tenantId, String voucherId, String postedBy) {
        Voucher voucher = getVoucher(tenantId, voucherId);
        if (voucher.status() != VoucherStatus.DRAFT) {
            throw new BizException("VOUCHER_STATUS_INVALID", "只有草稿凭证可以过账");
        }
        Voucher posted = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(),
                voucher.totalDebit(), voucher.totalCredit(), VoucherStatus.POSTED,
                voucher.lines(), voucher.voucherDate(), postedBy, Instant.now(),
                voucher.exportBatchId(), voucher.createdAt(), Instant.now());
        return extStore.saveVoucher(posted);
    }

    @Transactional
    public Voucher voidVoucher(String tenantId, String voucherId) {
        Voucher voucher = getVoucher(tenantId, voucherId);
        if (voucher.status() == VoucherStatus.VOIDED) {
            throw new BizException("VOUCHER_STATUS_INVALID", "凭证已作废");
        }
        if (voucher.status() == VoucherStatus.EXPORTED) {
            throw new BizException("VOUCHER_STATUS_INVALID", "已导出凭证不可作废");
        }
        Voucher voided = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(),
                voucher.totalDebit(), voucher.totalCredit(), VoucherStatus.VOIDED,
                voucher.lines(), voucher.voucherDate(), voucher.postedBy(), voucher.postedAt(),
                voucher.exportBatchId(), voucher.createdAt(), Instant.now());
        return extStore.saveVoucher(voided);
    }

    @Transactional
    public Voucher markExported(String tenantId, String voucherId, String exportBatchId) {
        Voucher voucher = getVoucher(tenantId, voucherId);
        if (voucher.status() != VoucherStatus.POSTED) {
            throw new BizException("VOUCHER_STATUS_INVALID", "只有已过账凭证可以标记导出");
        }
        Voucher exported = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(),
                voucher.totalDebit(), voucher.totalCredit(), VoucherStatus.EXPORTED,
                voucher.lines(), voucher.voucherDate(), voucher.postedBy(), voucher.postedAt(),
                exportBatchId, voucher.createdAt(), Instant.now());
        return extStore.saveVoucher(exported);
    }

    public Voucher getVoucher(String tenantId, String voucherId) {
        return extStore.findVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
    }

    public List<Voucher> listVouchers(String tenantId, String voucherType, String status) {
        return extStore.listVouchers(tenantId, voucherType, status);
    }

    public List<Voucher> listVouchersByReference(String tenantId, String referenceType, String referenceId) {
        return extStore.listVouchersByReference(tenantId, referenceType, referenceId);
    }

    private Invoice updateInvoiceStatus(Invoice invoice, String status) {
        Invoice updated = new Invoice(
                invoice.invoiceId(), invoice.tenantId(), invoice.invoiceNumber(), invoice.invoiceType(),
                invoice.customerId(), invoice.customerName(), invoice.countryCode(),
                invoice.currency(), invoice.subtotalAmount(), invoice.taxAmount(), invoice.totalAmount(),
                invoice.taxIdNumber(), status, invoice.voucherId(),
                invoice.invoiceDate(), invoice.dueDate(), invoice.remark(),
                invoice.createdAt(), Instant.now());
        return extStore.saveInvoice(updated);
    }

    public record CreateInvoiceCommand(String invoiceType, String customerId, String customerName,
                                       String countryCode, String currency, BigDecimal subtotalAmount,
                                       BigDecimal taxAmount, String taxIdNumber,
                                       Instant invoiceDate, Instant dueDate, String remark) {}
    public record CreateVoucherCommand(String voucherType, String referenceType, String referenceId,
                                       String currency, Instant voucherDate,
                                       List<VoucherLine> lines) {}

    // ========== 自定义发票设置管理(内存存储) ==========
    /**
     * 按店铺+市场维度配置发票参数，包括发票抬头、税务登记号、显示项等。
     * V4需求: "自定义发票设置：配置打印模板" / "VAT/GST计算、税率管理、多国税率自动匹配"
     */

    @Transactional
    public InvoiceSetting saveInvoiceSetting(String tenantId, SaveInvoiceSettingCommand command) {
        Instant now = Instant.now();
        InvoiceSetting existing = extStore.findInvoiceSettingByScope(tenantId, command.storeId(), command.marketplaceId())
                .orElse(null);
        InvoiceSetting setting = new InvoiceSetting(
                existing != null ? existing.settingId() : UUID.randomUUID().toString(),
                tenantId,
                command.storeId(), command.marketplaceId(), command.templateId(),
                command.invoiceTitle(), command.taxRegistrationNo(),
                command.showUnitPrice(), command.showTaxRate(), command.showDiscount(),
                command.remark(), true,
                existing != null ? existing.createdAt() : now, now);
        return extStore.saveInvoiceSetting(setting);
    }

    /**
     * 获取指定店铺在指定市场的发票配置
     */
    public InvoiceSetting getInvoiceSetting(String tenantId, String storeId, String marketplaceId) {
        return extStore.findInvoiceSettingByScope(tenantId, storeId, marketplaceId).orElse(null);
    }

    public List<InvoiceSetting> listInvoiceSettings(String tenantId) {
        return extStore.listInvoiceSettings(tenantId);
    }

    public record SaveInvoiceSettingCommand(String storeId, String marketplaceId, String templateId,
                                             String invoiceTitle, String taxRegistrationNo,
                                             boolean showUnitPrice, boolean showTaxRate, boolean showDiscount,
                                             String remark) {}
}
