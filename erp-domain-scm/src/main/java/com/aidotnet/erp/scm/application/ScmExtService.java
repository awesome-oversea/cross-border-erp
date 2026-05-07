package com.aidotnet.erp.scm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.domain.ApprovalStatus;
import com.aidotnet.erp.scm.domain.PurchaseApproval;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.Quote;
import com.aidotnet.erp.scm.domain.QuoteStatus;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.domain.SupplierScore;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import com.aidotnet.erp.scm.infrastructure.ScmExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScmExtService {

    private final ScmExtStore extStore;
    private final PurchaseStore purchaseStore;

    public ScmExtService(ScmExtStore extStore, PurchaseStore purchaseStore) {
        this.extStore = extStore;
        this.purchaseStore = purchaseStore;
    }

    @Transactional
    public Quote createQuote(String tenantId, CreateQuoteCommand command) {
        purchaseStore.findSupplier(tenantId, command.supplierId())
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "Supplier does not exist"));
        Instant now = Instant.now();
        BigDecimal totalPrice = command.unitPrice().multiply(BigDecimal.valueOf(command.quotedQuantity()));
        Quote quote = new Quote(
                UUID.randomUUID().toString(),
                tenantId,
                command.supplierId(),
                command.sellerSku(),
                command.currency(),
                command.unitPrice(),
                totalPrice,
                command.quotedQuantity(),
                command.leadTimeDays(),
                QuoteStatus.DRAFT,
                command.validUntil(),
                command.remark(),
                now,
                now);
        return extStore.saveQuote(quote);
    }

    @Transactional
    public Quote submitQuote(String tenantId, String quoteId) {
        Quote quote = getQuote(tenantId, quoteId);
        if (quote.status() != QuoteStatus.DRAFT) {
            throw new BizException("QUOTE_STATUS_INVALID", "Only draft quotes can be submitted");
        }
        return extStore.saveQuote(new Quote(
                quote.quoteId(),
                quote.tenantId(),
                quote.supplierId(),
                quote.sellerSku(),
                quote.currency(),
                quote.unitPrice(),
                quote.totalPrice(),
                quote.quotedQuantity(),
                quote.leadTimeDays(),
                QuoteStatus.SUBMITTED,
                quote.validUntil(),
                quote.remark(),
                quote.createdAt(),
                Instant.now()));
    }

    @Transactional
    public Quote acceptQuote(String tenantId, String quoteId) {
        Quote quote = getQuote(tenantId, quoteId);
        if (quote.status() != QuoteStatus.SUBMITTED) {
            throw new BizException("QUOTE_STATUS_INVALID", "Only submitted quotes can be accepted");
        }
        return extStore.saveQuote(new Quote(
                quote.quoteId(),
                quote.tenantId(),
                quote.supplierId(),
                quote.sellerSku(),
                quote.currency(),
                quote.unitPrice(),
                quote.totalPrice(),
                quote.quotedQuantity(),
                quote.leadTimeDays(),
                QuoteStatus.ACCEPTED,
                quote.validUntil(),
                quote.remark(),
                quote.createdAt(),
                Instant.now()));
    }

    @Transactional
    public Quote rejectQuote(String tenantId, String quoteId) {
        Quote quote = getQuote(tenantId, quoteId);
        if (quote.status() != QuoteStatus.SUBMITTED) {
            throw new BizException("QUOTE_STATUS_INVALID", "Only submitted quotes can be rejected");
        }
        return extStore.saveQuote(new Quote(
                quote.quoteId(),
                quote.tenantId(),
                quote.supplierId(),
                quote.sellerSku(),
                quote.currency(),
                quote.unitPrice(),
                quote.totalPrice(),
                quote.quotedQuantity(),
                quote.leadTimeDays(),
                QuoteStatus.REJECTED,
                quote.validUntil(),
                quote.remark(),
                quote.createdAt(),
                Instant.now()));
    }

    public List<Quote> compareQuotes(String tenantId, String sellerSku) {
        return extStore.listQuotesBySku(tenantId, sellerSku);
    }

    public List<Quote> listQuotesBySupplier(String tenantId, String supplierId) {
        return extStore.listQuotesBySupplier(tenantId, supplierId);
    }

    public Quote getQuote(String tenantId, String quoteId) {
        return extStore.findQuote(tenantId, quoteId)
                .orElseThrow(() -> new BizException("QUOTE_NOT_FOUND", "Quote does not exist"));
    }

    @Transactional
    public SupplierScore recalculateSupplierScore(String tenantId, String supplierId) {
        List<SupplierEvaluation> evaluations = purchaseStore.listEvaluations(tenantId, supplierId);
        if (evaluations.isEmpty()) {
            return extStore.findSupplierScore(tenantId, supplierId).orElse(null);
        }
        BigDecimal avgQuality = average(evaluations.stream().map(SupplierEvaluation::qualityScore).toList());
        BigDecimal avgDelivery = average(evaluations.stream().map(SupplierEvaluation::deliveryScore).toList());
        BigDecimal avgPrice = average(evaluations.stream().map(SupplierEvaluation::priceScore).toList());
        BigDecimal avgService = average(evaluations.stream().map(SupplierEvaluation::serviceScore).toList());
        BigDecimal overall = avgQuality.multiply(new BigDecimal("0.3"))
                .add(avgDelivery.multiply(new BigDecimal("0.3")))
                .add(avgPrice.multiply(new BigDecimal("0.2")))
                .add(avgService.multiply(new BigDecimal("0.2")))
                .setScale(2, RoundingMode.HALF_UP);
        SupplierScore existing = extStore.findSupplierScore(tenantId, supplierId).orElse(null);
        SupplierScore score = new SupplierScore(
                existing != null ? existing.scoreId() : UUID.randomUUID().toString(),
                tenantId,
                supplierId,
                avgQuality,
                avgDelivery,
                avgPrice,
                avgService,
                overall,
                evaluations.size(),
                Instant.now());
        return extStore.saveSupplierScore(score);
    }

    public SupplierScore getSupplierScore(String tenantId, String supplierId) {
        return extStore.findSupplierScore(tenantId, supplierId)
                .orElseThrow(() -> new BizException("SUPPLIER_SCORE_NOT_FOUND", "Supplier score does not exist"));
    }

    @Transactional
    public PurchaseApproval submitForApproval(String tenantId, SubmitApprovalCommand command) {
        return createApprovalFlow(tenantId, new CreateApprovalFlowCommand(command.poId(), List.of(command.approverId()))).get(0);
    }

    @Transactional
    public List<PurchaseApproval> createApprovalFlow(String tenantId, CreateApprovalFlowCommand command) {
        PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, command.poId())
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "Purchase order does not exist"));
        if (po.status() != PurchaseOrderStatus.SUBMITTED) {
            throw new BizException("PO_STATUS_INVALID", "Only submitted purchase orders can start approval");
        }
        if (command.approverIds() == null || command.approverIds().isEmpty()) {
            throw new BizException("PURCHASE_APPROVER_REQUIRED", "Purchase approver is required");
        }
        if (!extStore.listApprovalsByPo(tenantId, command.poId()).isEmpty()) {
            throw new BizException("PURCHASE_APPROVAL_FLOW_EXISTS", "Purchase approval flow already exists");
        }
        Instant now = Instant.now();
        Set<String> dedupApproverIds = new HashSet<>();
        List<PurchaseApproval> approvals = new ArrayList<>();
        for (int i = 0; i < command.approverIds().size(); i++) {
            String approverId = command.approverIds().get(i);
            if (approverId == null || approverId.isBlank()) {
                throw new BizException("PURCHASE_APPROVER_REQUIRED", "Purchase approver is required");
            }
            if (!dedupApproverIds.add(approverId)) {
                throw new BizException("PURCHASE_APPROVER_DUPLICATED", "Purchase approver cannot be duplicated");
            }
            approvals.add(extStore.saveApproval(new PurchaseApproval(
                    UUID.randomUUID().toString(),
                    tenantId,
                    command.poId(),
                    i + 1,
                    ApprovalStatus.PENDING,
                    approverId,
                    null,
                    null,
                    now)));
        }
        return approvals;
    }

    @Transactional
    public PurchaseApproval approvePurchase(String tenantId, String approvalId, ApproveCommand command) {
        PurchaseApproval approval = extStore.findApproval(tenantId, approvalId)
                .orElseThrow(() -> new BizException("APPROVAL_NOT_FOUND", "Approval record does not exist"));
        if (!approval.poId().equals(command.poId())) {
            throw new BizException("APPROVAL_PO_MISMATCH", "Approval record does not match purchase order");
        }
        if (approval.status() != ApprovalStatus.PENDING) {
            throw new BizException("APPROVAL_STATUS_INVALID", "Approval record has already been handled");
        }
        validateSequentialApproval(tenantId, approval.poId(), approvalId);
        PurchaseApproval updated = new PurchaseApproval(
                approval.approvalId(),
                approval.tenantId(),
                approval.poId(),
                approval.approvalLevel(),
                ApprovalStatus.APPROVED,
                approval.approverId(),
                command.comment(),
                Instant.now(),
                approval.createdAt());
        extStore.saveApproval(updated);
        boolean allApproved = extStore.listApprovalsByPo(tenantId, approval.poId()).stream()
                .allMatch(existing -> existing.status() == ApprovalStatus.APPROVED);
        if (allApproved) {
            PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, approval.poId())
                    .orElseThrow(() -> new BizException("PO_NOT_FOUND", "Purchase order does not exist"));
            purchaseStore.savePurchaseOrder(new PurchaseOrder(
                    po.poId(),
                    po.tenantId(),
                    po.supplierId(),
                    po.poNumber(),
                    po.currency(),
                    po.totalAmount(),
                    PurchaseOrderStatus.APPROVED,
                    po.paymentTerms(),
                    po.shippingTerms(),
                    po.purchaseType(),
                    po.expectedDeliveryDate(),
                    po.actualDeliveryDate(),
                    po.notes(),
                    po.createdBy(),
                    approval.approverId(),
                    po.lines(),
                    po.createdAt(),
                    Instant.now()));
        }
        return updated;
    }

    @Transactional
    public PurchaseApproval rejectPurchase(String tenantId, String approvalId, ApproveCommand command) {
        PurchaseApproval approval = extStore.findApproval(tenantId, approvalId)
                .orElseThrow(() -> new BizException("APPROVAL_NOT_FOUND", "Approval record does not exist"));
        if (!approval.poId().equals(command.poId())) {
            throw new BizException("APPROVAL_PO_MISMATCH", "Approval record does not match purchase order");
        }
        if (approval.status() != ApprovalStatus.PENDING) {
            throw new BizException("APPROVAL_STATUS_INVALID", "Approval record has already been handled");
        }
        validateSequentialApproval(tenantId, approval.poId(), approvalId);
        PurchaseApproval updated = new PurchaseApproval(
                approval.approvalId(),
                approval.tenantId(),
                approval.poId(),
                approval.approvalLevel(),
                ApprovalStatus.REJECTED,
                approval.approverId(),
                command.comment(),
                Instant.now(),
                approval.createdAt());
        extStore.saveApproval(updated);
        PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, approval.poId())
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "Purchase order does not exist"));
        purchaseStore.savePurchaseOrder(new PurchaseOrder(
                po.poId(),
                po.tenantId(),
                po.supplierId(),
                po.poNumber(),
                po.currency(),
                po.totalAmount(),
                PurchaseOrderStatus.DRAFT,
                po.paymentTerms(),
                po.shippingTerms(),
                po.purchaseType(),
                po.expectedDeliveryDate(),
                po.actualDeliveryDate(),
                po.notes(),
                po.createdBy(),
                po.approvedBy(),
                po.lines(),
                po.createdAt(),
                Instant.now()));
        return updated;
    }

    public List<PurchaseApproval> listApprovalsByPo(String tenantId, String poId) {
        return extStore.listApprovalsByPo(tenantId, poId);
    }

    public List<PurchaseApproval> listPendingApprovals(String tenantId, String approverId) {
        return extStore.listPendingApprovals(tenantId, approverId);
    }

    private void validateSequentialApproval(String tenantId, String poId, String approvalId) {
        PurchaseApproval nextPendingApproval = extStore.listApprovalsByPo(tenantId, poId).stream()
                .filter(existing -> existing.status() == ApprovalStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new BizException("APPROVAL_STATUS_INVALID", "Approval record has already been handled"));
        if (!nextPendingApproval.approvalId().equals(approvalId)) {
            throw new BizException("PURCHASE_APPROVAL_LEVEL_INVALID", "Previous approval level is still pending");
        }
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    public record CreateQuoteCommand(
            String supplierId,
            String sellerSku,
            String currency,
            BigDecimal unitPrice,
            int quotedQuantity,
            int leadTimeDays,
            Instant validUntil,
            String remark) {}

    public record CreateApprovalFlowCommand(String poId, List<String> approverIds) {}

    public record SubmitApprovalCommand(String poId, String approverId) {}

    public record ApproveCommand(String poId, String comment) {}
}
