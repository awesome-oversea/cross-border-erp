package com.aidotnet.erp.common.voucher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VoucherEngineService {

    private static final Logger log = LoggerFactory.getLogger(VoucherEngineService.class);
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Voucher> vouchers = new ConcurrentHashMap<>();
    private final Map<String, JournalEntry> entries = new ConcurrentHashMap<>();

    public Voucher autoGenerate(String businessType, String businessId, String tenantId,
                                 List<EntryLine> lines, String description) {
        String voucherNo = "VCH-" + LocalDate.now().toString().replace("-", "") + "-" + String.format("%06d", idGenerator.getAndIncrement());
        BigDecimal totalDebit = lines.stream().map(EntryLine::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(EntryLine::credit).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            log.warn("Voucher not balanced: debit={}, credit={}, business={}", totalDebit, totalCredit, businessId);
        }

        Voucher voucher = new Voucher(voucherNo, businessType, businessId, tenantId,
                totalDebit, totalCredit, lines, description, "DRAFT", LocalDateTime.now(), null);
        vouchers.put(voucherNo, voucher);

        for (EntryLine line : lines) {
            String entryId = "JE-" + idGenerator.getAndIncrement();
            entries.put(entryId, new JournalEntry(entryId, voucherNo, line.accountCode(),
                    line.accountName(), line.debit(), line.credit(), line.summary(), LocalDateTime.now()));
        }

        log.info("Auto-generated voucher: no={}, type={}, business={}, lines={}", voucherNo, businessType, businessId, lines.size());
        return voucher;
    }

    public Voucher generatePurchaseInbound(String purchaseOrderId, String tenantId,
                                            BigDecimal amount, String supplierName) {
        List<EntryLine> lines = List.of(
                new EntryLine("1401", "库存商品", amount, BigDecimal.ZERO, "采购入库-" + purchaseOrderId),
                new EntryLine("2202", "应付账款", BigDecimal.ZERO, amount, "采购入库-" + purchaseOrderId + "-" + supplierName)
        );
        return autoGenerate("PURCHASE_INBOUND", purchaseOrderId, tenantId, lines, "采购入库自动凭证");
    }

    public Voucher generateSalesOutbound(String orderId, String tenantId,
                                           BigDecimal revenue, BigDecimal cost) {
        List<EntryLine> lines = List.of(
                new EntryLine("1122", "应收账款", revenue, BigDecimal.ZERO, "销售出库-" + orderId),
                new EntryLine("6001", "主营业务收入", BigDecimal.ZERO, revenue, "销售出库-" + orderId),
                new EntryLine("6401", "主营业务成本", cost, BigDecimal.ZERO, "销售出库成本-" + orderId),
                new EntryLine("1401", "库存商品", BigDecimal.ZERO, cost, "销售出库成本-" + orderId)
        );
        return autoGenerate("SALES_OUTBOUND", orderId, tenantId, lines, "销售出库自动凭证");
    }

    public Voucher generateReturnInbound(String returnId, String tenantId,
                                           BigDecimal amount, String reason) {
        List<EntryLine> lines = List.of(
                new EntryLine("1401", "库存商品", amount, BigDecimal.ZERO, "退货入库-" + returnId),
                new EntryLine("6401", "主营业务成本", BigDecimal.ZERO, amount, "退货入库成本冲回-" + returnId)
        );
        return autoGenerate("RETURN_INBOUND", returnId, tenantId, lines, "退货入库自动凭证-" + reason);
    }

    public Voucher generateInventoryAdjust(String adjustId, String tenantId,
                                             BigDecimal diffAmount, String adjustType) {
        List<EntryLine> lines;
        if (diffAmount.compareTo(BigDecimal.ZERO) > 0) {
            lines = List.of(
                    new EntryLine("1401", "库存商品", diffAmount, BigDecimal.ZERO, "盘盈调整-" + adjustId),
                    new EntryLine("6701", "营业外收入", BigDecimal.ZERO, diffAmount, "盘盈调整-" + adjustId)
            );
        } else {
            BigDecimal absDiff = diffAmount.abs();
            lines = List.of(
                    new EntryLine("6702", "营业外支出", absDiff, BigDecimal.ZERO, "盘亏调整-" + adjustId),
                    new EntryLine("1401", "库存商品", BigDecimal.ZERO, absDiff, "盘亏调整-" + adjustId)
            );
        }
        return autoGenerate("INVENTORY_ADJUST", adjustId, tenantId, lines, "库存调整自动凭证-" + adjustType);
    }

    public boolean approve(String voucherNo, String approvedBy) {
        Voucher voucher = vouchers.get(voucherNo);
        if (voucher == null) return false;
        Voucher approved = new Voucher(voucher.voucherNo(), voucher.businessType(), voucher.businessId(),
                voucher.tenantId(), voucher.totalDebit(), voucher.totalCredit(), voucher.lines(),
                voucher.description(), "APPROVED", voucher.createdAt(), LocalDateTime.now());
        vouchers.put(voucherNo, approved);
        log.info("Voucher approved: no={}, by={}", voucherNo, approvedBy);
        return true;
    }

    public VoucherSummary summary(String tenantId, LocalDate startDate, LocalDate endDate) {
        List<Voucher> filtered = vouchers.values().stream()
                .filter(v -> tenantId == null || tenantId.equals(v.tenantId()))
                .toList();
        BigDecimal totalDebit = filtered.stream().map(Voucher::totalDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = filtered.stream().map(Voucher::totalCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new VoucherSummary(tenantId, startDate, endDate, filtered.size(), totalDebit, totalCredit);
    }

    public List<JournalEntry> getEntries(String voucherNo) {
        return entries.values().stream().filter(e -> voucherNo.equals(e.voucherNo())).toList();
    }

    public record EntryLine(String accountCode, String accountName, BigDecimal debit, BigDecimal credit, String summary) {}
    public record Voucher(String voucherNo, String businessType, String businessId, String tenantId,
                           BigDecimal totalDebit, BigDecimal totalCredit, List<EntryLine> lines,
                           String description, String status, LocalDateTime createdAt, LocalDateTime approvedAt) {}
    public record JournalEntry(String id, String voucherNo, String accountCode, String accountName,
                                BigDecimal debit, BigDecimal credit, String summary, LocalDateTime createdAt) {}
    public record VoucherSummary(String tenantId, LocalDate startDate, LocalDate endDate,
                                  int voucherCount, BigDecimal totalDebit, BigDecimal totalCredit) {}
}
