package com.aidotnet.erp.common.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FinanceExtensionService {

    private static final Logger log = LoggerFactory.getLogger(FinanceExtensionService.class);
    private final Map<String, PaymentApplication> paymentApplications = new ConcurrentHashMap<>();
    private final Map<String, OffsetRecord> offsetRecords = new ConcurrentHashMap<>();
    private final Map<String, ReconciliationRecord> reconciliationRecords = new ConcurrentHashMap<>();
    private final Map<String, PlatformBill> platformBills = new ConcurrentHashMap<>();
    private final Map<String, ExpenseRecord> expenseRecords = new ConcurrentHashMap<>();

    public PaymentApplication createPaymentApplication(String tenantId, String applicant, String payee,
                                                        String currency, BigDecimal amount, String businessType,
                                                        String businessId, String description) {
        String appId = "PA-" + System.currentTimeMillis();
        PaymentApplication app = new PaymentApplication(appId, tenantId, applicant, payee,
                currency, amount, businessType, businessId, description, "DRAFT", null, null, Instant.now(), Instant.now());
        paymentApplications.put(appId, app);
        log.info("Created payment application: id={}, tenant={}, amount={}", appId, tenantId, amount);
        return app;
    }

    public PaymentApplication approvePaymentApplication(String appId, String approver, String comment) {
        PaymentApplication app = paymentApplications.get(appId);
        if (app == null) throw new IllegalArgumentException("Payment application not found: " + appId);
        if (!"DRAFT".equals(app.status())) throw new IllegalStateException("Only DRAFT applications can be approved");
        PaymentApplication approved = new PaymentApplication(app.appId(), app.tenantId(), app.applicant(),
                app.payee(), app.currency(), app.amount(), app.businessType(), app.businessId(),
                app.description(), "APPROVED", approver, comment, app.createdAt(), Instant.now());
        paymentApplications.put(appId, approved);
        log.info("Approved payment application: id={}, approver={}", appId, approver);
        return approved;
    }

    public PaymentApplication rejectPaymentApplication(String appId, String approver, String comment) {
        PaymentApplication app = paymentApplications.get(appId);
        if (app == null) throw new IllegalArgumentException("Payment application not found: " + appId);
        if (!"DRAFT".equals(app.status())) throw new IllegalStateException("Only DRAFT applications can be rejected");
        PaymentApplication rejected = new PaymentApplication(app.appId(), app.tenantId(), app.applicant(),
                app.payee(), app.currency(), app.amount(), app.businessType(), app.businessId(),
                app.description(), "REJECTED", approver, comment, app.createdAt(), Instant.now());
        paymentApplications.put(appId, rejected);
        log.info("Rejected payment application: id={}, approver={}", appId, approver);
        return rejected;
    }

    public PaymentApplication payPaymentApplication(String appId) {
        PaymentApplication app = paymentApplications.get(appId);
        if (app == null) throw new IllegalArgumentException("Payment application not found: " + appId);
        if (!"APPROVED".equals(app.status())) throw new IllegalStateException("Only APPROVED applications can be paid");
        PaymentApplication paid = new PaymentApplication(app.appId(), app.tenantId(), app.applicant(),
                app.payee(), app.currency(), app.amount(), app.businessType(), app.businessId(),
                app.description(), "PAID", app.approver(), app.approverComment(), app.createdAt(), Instant.now());
        paymentApplications.put(appId, paid);
        log.info("Paid payment application: id={}", appId);
        return paid;
    }

    public List<PaymentApplication> listPaymentApplications(String tenantId, String status) {
        return paymentApplications.values().stream()
                .filter(a -> tenantId == null || tenantId.equals(a.tenantId()))
                .filter(a -> status == null || status.equals(a.status()))
                .toList();
    }

    public OffsetRecord createOffset(String tenantId, String receivableId, String payableId,
                                      BigDecimal offsetAmount, String offsetType, String remark) {
        String offsetId = "OFF-" + System.currentTimeMillis();
        OffsetRecord record = new OffsetRecord(offsetId, tenantId, receivableId, payableId,
                offsetAmount, offsetType, remark, Instant.now());
        offsetRecords.put(offsetId, record);
        log.info("Created offset record: id={}, receivable={}, payable={}, amount={}",
                offsetId, receivableId, payableId, offsetAmount);
        return record;
    }

    public List<OffsetRecord> listOffsets(String tenantId) {
        return offsetRecords.values().stream()
                .filter(o -> tenantId == null || tenantId.equals(o.tenantId()))
                .toList();
    }

    public ReconciliationRecord reconcile(String tenantId, String platform, String period,
                                            BigDecimal systemAmount, BigDecimal platformAmount,
                                            List<ReconciliationItem> items) {
        String recId = "REC-" + System.currentTimeMillis();
        BigDecimal difference = platformAmount.subtract(systemAmount).setScale(2, RoundingMode.HALF_UP);
        String status = difference.compareTo(BigDecimal.ZERO) == 0 ? "MATCHED" : "MISMATCH";

        ReconciliationRecord record = new ReconciliationRecord(recId, tenantId, platform, period,
                systemAmount, platformAmount, difference, status, items, Instant.now());
        reconciliationRecords.put(recId, record);
        log.info("Reconciliation completed: id={}, platform={}, period={}, status={}, diff={}",
                recId, platform, period, status, difference);
        return record;
    }

    public List<ReconciliationRecord> listReconciliations(String tenantId, String platform) {
        return reconciliationRecords.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> platform == null || platform.equals(r.platform()))
                .toList();
    }

    public PlatformBill importPlatformBill(String tenantId, String platform, String billId,
                                             String period, BigDecimal totalAmount, String currency,
                                             List<PlatformBillItem> items) {
        String internalBillId = "PB-" + System.currentTimeMillis();
        PlatformBill bill = new PlatformBill(internalBillId, tenantId, platform, billId,
                period, totalAmount, currency, items, "IMPORTED", Instant.now());
        platformBills.put(internalBillId, bill);
        log.info("Imported platform bill: id={}, platform={}, billId={}, amount={}",
                internalBillId, platform, billId, totalAmount);
        return bill;
    }

    public PlatformBill confirmPlatformBill(String internalBillId) {
        PlatformBill bill = platformBills.get(internalBillId);
        if (bill == null) throw new IllegalArgumentException("Platform bill not found: " + internalBillId);
        if (!"IMPORTED".equals(bill.status())) throw new IllegalStateException("Only IMPORTED bills can be confirmed");
        PlatformBill confirmed = new PlatformBill(bill.internalBillId(), bill.tenantId(), bill.platform(),
                bill.billId(), bill.period(), bill.totalAmount(), bill.currency(),
                bill.items(), "CONFIRMED", bill.importedAt());
        platformBills.put(internalBillId, confirmed);
        log.info("Confirmed platform bill: id={}", internalBillId);
        return confirmed;
    }

    public List<PlatformBill> listPlatformBills(String tenantId, String platform) {
        return platformBills.values().stream()
                .filter(b -> tenantId == null || tenantId.equals(b.tenantId()))
                .filter(b -> platform == null || platform.equals(b.platform()))
                .toList();
    }

    public ExpenseRecord createExpense(String tenantId, String expenseType, String category,
                                        BigDecimal amount, String currency, String description,
                                        String applicant) {
        String expenseId = "EXP-" + System.currentTimeMillis();
        ExpenseRecord record = new ExpenseRecord(expenseId, tenantId, expenseType, category,
                amount, currency, description, applicant, "PENDING", Instant.now());
        expenseRecords.put(expenseId, record);
        log.info("Created expense record: id={}, type={}, amount={}", expenseId, expenseType, amount);
        return record;
    }

    public ExpenseRecord approveExpense(String expenseId, String approver) {
        ExpenseRecord record = expenseRecords.get(expenseId);
        if (record == null) throw new IllegalArgumentException("Expense not found: " + expenseId);
        if (!"PENDING".equals(record.status())) throw new IllegalStateException("Only PENDING expenses can be approved");
        ExpenseRecord approved = new ExpenseRecord(record.expenseId(), record.tenantId(), record.expenseType(),
                record.category(), record.amount(), record.currency(), record.description(),
                record.applicant(), "APPROVED", record.createdAt());
        expenseRecords.put(expenseId, approved);
        log.info("Approved expense: id={}", expenseId);
        return approved;
    }

    public List<ExpenseRecord> listExpenses(String tenantId, String expenseType) {
        return expenseRecords.values().stream()
                .filter(e -> tenantId == null || tenantId.equals(e.tenantId()))
                .filter(e -> expenseType == null || expenseType.equals(e.expenseType()))
                .toList();
    }

    public record PaymentApplication(String appId, String tenantId, String applicant, String payee,
                                      String currency, BigDecimal amount, String businessType,
                                      String businessId, String description, String status,
                                      String approver, String approverComment,
                                      Instant createdAt, Instant updatedAt) {}
    public record OffsetRecord(String offsetId, String tenantId, String receivableId, String payableId,
                                BigDecimal offsetAmount, String offsetType, String remark, Instant createdAt) {}
    public record ReconciliationItem(String itemCode, String description, BigDecimal systemAmount,
                                      BigDecimal platformAmount, BigDecimal difference, String matchStatus) {}
    public record ReconciliationRecord(String recId, String tenantId, String platform, String period,
                                        BigDecimal systemAmount, BigDecimal platformAmount, BigDecimal difference,
                                        String status, List<ReconciliationItem> items, Instant reconciledAt) {}
    public record PlatformBillItem(String itemType, String description, BigDecimal amount, String currency) {}
    public record PlatformBill(String internalBillId, String tenantId, String platform, String billId,
                                String period, BigDecimal totalAmount, String currency,
                                List<PlatformBillItem> items, String status, Instant importedAt) {}
    public record ExpenseRecord(String expenseId, String tenantId, String expenseType, String category,
                                 BigDecimal amount, String currency, String description,
                                 String applicant, String status, Instant createdAt) {}
}
