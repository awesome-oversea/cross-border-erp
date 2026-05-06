package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.Voucher.VoucherStatus;
import com.aidotnet.erp.fms.domain.VoucherLine;
import com.aidotnet.erp.fms.domain.VoucherLineType;
import com.aidotnet.erp.fms.domain.VoucherTemplate;
import com.aidotnet.erp.fms.infrastructure.FinanceStore;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 进销存凭证引擎
 * <p>
 * 描述: FMS域业务中台(5.14)，自动生成进销存凭证。
 *       根据业务事件(采购入库/销售出库/调拨/盘点)自动生成会计凭证(Voucher)，
 *       支持会计科目映射和凭证模板(VoucherTemplate)。
 * </p>
 * <p>
 * 凭证生成规则:
 *   1. 采购入库 → 借:库存商品 贷:应付账款
 *   2. 销售出库 → 借:主营业务成本 贷:库存商品
 *   3. 销售收款 → 借:银行存款 贷:应收账款
 *   4. 期末调汇 → 借/贷:汇兑损益 贷/借:外币货币资金
 * </p>
 *
 * @author ERP系统
 * @see Voucher
 * @see VoucherLine
 * @see VoucherTemplate
 */
@Service
public class InventoryVoucherEngine {

    private final FmsExtStore extStore;
    private final FinanceStore financeStore;

    public InventoryVoucherEngine(FmsExtStore extStore, FinanceStore financeStore) {
        this.extStore = extStore;
        this.financeStore = financeStore;
    }

    @Transactional
    public VoucherTemplate createTemplate(String tenantId, CreateVoucherTemplateCommand command) {
        Instant now = Instant.now();
        VoucherTemplate template = new VoucherTemplate(
                UUID.randomUUID().toString(), tenantId, command.templateName(), command.businessType(),
                command.debitAccount(), command.creditAccount(), command.description(), true, now, now);
        return extStore.saveVoucherTemplate(template);
    }

    @Transactional
    public VoucherTemplate updateTemplate(String tenantId, String templateId, CreateVoucherTemplateCommand command) {
        VoucherTemplate existing = extStore.listVoucherTemplates(tenantId, command.businessType()).stream()
                .filter(t -> t.templateId().equals(templateId))
                .findFirst()
                .orElseThrow(() -> new BizException("TEMPLATE_NOT_FOUND", "凭证模板不存在"));
        Instant now = Instant.now();
        VoucherTemplate updated = new VoucherTemplate(
                existing.templateId(), existing.tenantId(), command.templateName(), command.businessType(),
                command.debitAccount(), command.creditAccount(), command.description(), existing.enabled(), existing.createdAt(), now);
        return extStore.saveVoucherTemplate(updated);
    }

    @Transactional
    public VoucherTemplate toggleTemplate(String tenantId, String templateId, boolean enabled) {
        List<VoucherTemplate> all = extStore.listVoucherTemplates(tenantId, null);
        VoucherTemplate existing = all.stream()
                .filter(t -> t.templateId().equals(templateId))
                .findFirst()
                .orElseThrow(() -> new BizException("TEMPLATE_NOT_FOUND", "凭证模板不存在"));
        Instant now = Instant.now();
        VoucherTemplate updated = new VoucherTemplate(
                existing.templateId(), existing.tenantId(), existing.templateName(), existing.businessType(),
                existing.debitAccount(), existing.creditAccount(), existing.description(), enabled, existing.createdAt(), now);
        return extStore.saveVoucherTemplate(updated);
    }

    public List<VoucherTemplate> listTemplates(String tenantId, String businessType) {
        return extStore.listVoucherTemplates(tenantId, businessType);
    }

    @Transactional
    public Voucher autoGenerate(String tenantId, AutoGenerateVoucherCommand command) {
        List<VoucherTemplate> templates = extStore.listVoucherTemplates(tenantId, command.businessType());
        templates = templates.stream().filter(VoucherTemplate::enabled).toList();
        if (templates.isEmpty()) {
            throw new BizException("TEMPLATE_NOT_FOUND", "未找到对应的凭证模板: " + command.businessType());
        }
        VoucherTemplate template = templates.get(0);
        List<VoucherLine> lines = buildVoucherLines(template, command);
        Instant now = Instant.now();
        Voucher voucher = new Voucher(
                UUID.randomUUID().toString(), tenantId, null, command.voucherType(),
                command.businessType(), command.sourceId(), command.currency(), command.amount(), command.amount(),
                VoucherStatus.DRAFT, lines, now, null, null, null, now, now);
        return financeStore.saveVoucher(voucher);
    }

    @Transactional
    public List<Voucher> batchAutoGenerate(String tenantId, List<AutoGenerateVoucherCommand> commands) {
        List<Voucher> results = new ArrayList<>();
        for (AutoGenerateVoucherCommand command : commands) {
            results.add(autoGenerate(tenantId, command));
        }
        return results;
    }

    private List<VoucherLine> buildVoucherLines(VoucherTemplate template, AutoGenerateVoucherCommand command) {
        List<VoucherLine> lines = new ArrayList<>();
        lines.add(new VoucherLine(UUID.randomUUID().toString(), template.debitAccount(),
                template.debitAccount(), VoucherLineType.DEBIT, command.amount(), command.sellerSku()));
        lines.add(new VoucherLine(UUID.randomUUID().toString(), template.creditAccount(),
                template.creditAccount(), VoucherLineType.CREDIT, command.amount(), command.sellerSku()));
        return lines;
    }

    @Transactional
    public Voucher generatePurchaseInbound(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "PURCHASE_INBOUND", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateSalesOutbound(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "SALES_OUTBOUND", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateReturnInbound(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "RETURN_INBOUND", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateReturnOutbound(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "RETURN_OUTBOUND", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateInventoryGain(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "INVENTORY_GAIN", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateInventoryLoss(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "INVENTORY_LOSS", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateTransferIn(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "TRANSFER_IN", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateTransferOut(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "TRANSFER_OUT", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateCostSettlement(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "COST_SETTLEMENT", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher generateAdjustment(String tenantId, InventoryVoucherCommand command) {
        return autoGenerate(tenantId, new AutoGenerateVoucherCommand(
                "ADJUSTMENT", "INVENTORY", command.sourceId(),
                command.amount(), command.currency(), command.sellerSku()));
    }

    @Transactional
    public Voucher approveVoucher(String tenantId, String voucherId, String approvedBy) {
        Voucher voucher = financeStore.findVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
        if (voucher.status() != VoucherStatus.DRAFT) {
            throw new BizException("INVALID_STATUS", "只有草稿状态的凭证可以审核");
        }
        Instant now = Instant.now();
        Voucher approved = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(), voucher.totalDebit(), voucher.totalCredit(),
                VoucherStatus.POSTED, voucher.lines(), voucher.voucherDate(), approvedBy, now,
                voucher.exportBatchId(), voucher.createdAt(), now);
        return financeStore.saveVoucher(approved);
    }

    @Transactional
    public List<Voucher> batchApproveVouchers(String tenantId, List<String> voucherIds, String approvedBy) {
        List<Voucher> results = new ArrayList<>();
        for (String voucherId : voucherIds) {
            results.add(approveVoucher(tenantId, voucherId, approvedBy));
        }
        return results;
    }

    @Transactional
    public Voucher voidVoucher(String tenantId, String voucherId) {
        Voucher voucher = financeStore.findVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
        Instant now = Instant.now();
        Voucher voided = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(), voucher.totalDebit(), voucher.totalCredit(),
                VoucherStatus.VOIDED, voucher.lines(), voucher.voucherDate(), voucher.postedBy(), voucher.postedAt(),
                voucher.exportBatchId(), voucher.createdAt(), now);
        return financeStore.saveVoucher(voided);
    }

    public List<Voucher> queryVouchers(String tenantId, String voucherType, String status) {
        return financeStore.listVouchers(tenantId, voucherType, status);
    }

    public Voucher getVoucher(String tenantId, String voucherId) {
        return financeStore.findVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
    }

    public List<Voucher> queryVouchersByReference(String tenantId, String referenceType, String referenceId) {
        return extStore.listVouchersByReference(tenantId, referenceType, referenceId);
    }

    public VoucherSummaryResult getVoucherSummary(String tenantId, String periodStart, String periodEnd) {
        List<Voucher> vouchers = financeStore.listVouchers(tenantId, null, VoucherStatus.POSTED.name());
        Instant start = periodStart != null ? LocalDate.parse(periodStart).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant end = periodEnd != null ? LocalDate.parse(periodEnd).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        List<Voucher> filtered = vouchers.stream()
                .filter(v -> start == null || !v.voucherDate().isBefore(start))
                .filter(v -> end == null || v.voucherDate().isBefore(end))
                .toList();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        Map<String, BigDecimal> byType = new LinkedHashMap<>();
        for (Voucher v : filtered) {
            totalDebit = totalDebit.add(v.totalDebit());
            totalCredit = totalCredit.add(v.totalCredit());
            byType.merge(v.referenceType(), v.totalDebit(), BigDecimal::add);
        }
        return new VoucherSummaryResult(tenantId, periodStart, periodEnd, filtered.size(),
                totalDebit, totalCredit, byType);
    }

    @Transactional
    public boolean pushToKingdee(String tenantId, String voucherId) {
        Voucher voucher = financeStore.findVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
        if (voucher.status() != VoucherStatus.POSTED) {
            throw new BizException("INVALID_STATUS", "只有已过账的凭证可以推送到金蝶");
        }
        Instant now = Instant.now();
        String batchId = "KD-" + UUID.randomUUID().toString().substring(0, 8);
        Voucher exported = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(), voucher.totalDebit(), voucher.totalCredit(),
                VoucherStatus.EXPORTED, voucher.lines(), voucher.voucherDate(), voucher.postedBy(), voucher.postedAt(),
                batchId, voucher.createdAt(), now);
        financeStore.saveVoucher(exported);
        return true;
    }

    @Transactional
    public boolean pushToYonyou(String tenantId, String voucherId) {
        Voucher voucher = financeStore.findVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
        if (voucher.status() != VoucherStatus.POSTED) {
            throw new BizException("INVALID_STATUS", "只有已过账的凭证可以推送到用友");
        }
        Instant now = Instant.now();
        String batchId = "YY-" + UUID.randomUUID().toString().substring(0, 8);
        Voucher exported = new Voucher(
                voucher.voucherId(), voucher.tenantId(), voucher.voucherNumber(), voucher.voucherType(),
                voucher.referenceType(), voucher.referenceId(), voucher.currency(), voucher.totalDebit(), voucher.totalCredit(),
                VoucherStatus.EXPORTED, voucher.lines(), voucher.voucherDate(), voucher.postedBy(), voucher.postedAt(),
                batchId, voucher.createdAt(), now);
        financeStore.saveVoucher(exported);
        return true;
    }

    @Transactional
    public int batchPushToKingdee(String tenantId, List<String> voucherIds) {
        int count = 0;
        for (String voucherId : voucherIds) {
            if (pushToKingdee(tenantId, voucherId)) count++;
        }
        return count;
    }

    @Transactional
    public int batchPushToYonyou(String tenantId, List<String> voucherIds) {
        int count = 0;
        for (String voucherId : voucherIds) {
            if (pushToYonyou(tenantId, voucherId)) count++;
        }
        return count;
    }

    public List<Map<String, Object>> exportVouchers(String tenantId, String voucherType, String status, String format) {
        List<Voucher> vouchers = financeStore.listVouchers(tenantId, voucherType, status);
        List<Map<String, Object>> exportData = new ArrayList<>();
        for (Voucher v : vouchers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("voucherId", v.voucherId());
            row.put("voucherNumber", v.voucherNumber());
            row.put("voucherType", v.voucherType());
            row.put("referenceType", v.referenceType());
            row.put("referenceId", v.referenceId());
            row.put("currency", v.currency());
            row.put("totalDebit", v.totalDebit());
            row.put("totalCredit", v.totalCredit());
            row.put("status", v.status().name());
            row.put("voucherDate", v.voucherDate());
            row.put("postedBy", v.postedBy());
            row.put("postedAt", v.postedAt());
            row.put("exportBatchId", v.exportBatchId());
            List<Map<String, Object>> lineItems = new ArrayList<>();
            for (VoucherLine l : v.lines()) {
                Map<String, Object> line = new LinkedHashMap<>();
                line.put("lineId", l.lineId());
                line.put("accountCode", l.accountCode());
                line.put("accountName", l.accountName());
                line.put("type", l.type().name());
                line.put("amount", l.amount());
                line.put("remark", l.remark());
                lineItems.add(line);
            }
            row.put("lines", lineItems);
            exportData.add(row);
        }
        return exportData;
    }

    public record CreateVoucherTemplateCommand(
            String templateName, String businessType, String debitAccount,
            String creditAccount, String description) {}

    public record AutoGenerateVoucherCommand(
            String businessType, String voucherType, String sourceId,
            BigDecimal amount, String currency, String sellerSku) {}

    public record InventoryVoucherCommand(
            String sourceId, BigDecimal amount, String currency, String sellerSku) {}

    public record VoucherSummaryResult(
            String tenantId, String periodStart, String periodEnd,
            int count, BigDecimal totalDebit, BigDecimal totalCredit,
            Map<String, BigDecimal> byType) {}
}
