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
import java.util.List;
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
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在"));
        Instant now = Instant.now();
        BigDecimal totalPrice = command.unitPrice().multiply(BigDecimal.valueOf(command.quotedQuantity()));
        Quote quote = new Quote(UUID.randomUUID().toString(), tenantId, command.supplierId(), command.sellerSku(),
                command.currency(), command.unitPrice(), totalPrice, command.quotedQuantity(), command.leadTimeDays(),
                QuoteStatus.DRAFT, command.validUntil(), command.remark(), now, now);
        return extStore.saveQuote(quote);
    }

    @Transactional
    public Quote submitQuote(String tenantId, String quoteId) {
        Quote quote = getQuote(tenantId, quoteId);
        if (quote.status() != QuoteStatus.DRAFT) {
            throw new BizException("QUOTE_STATUS_INVALID", "只有草稿询价可以提交");
        }
        return extStore.saveQuote(new Quote(quote.quoteId(), quote.tenantId(), quote.supplierId(), quote.sellerSku(),
                quote.currency(), quote.unitPrice(), quote.totalPrice(), quote.quotedQuantity(), quote.leadTimeDays(),
                QuoteStatus.SUBMITTED, quote.validUntil(), quote.remark(), quote.createdAt(), Instant.now()));
    }

    @Transactional
    public Quote acceptQuote(String tenantId, String quoteId) {
        Quote quote = getQuote(tenantId, quoteId);
        if (quote.status() != QuoteStatus.SUBMITTED) {
            throw new BizException("QUOTE_STATUS_INVALID", "只有已提交询价可以接受");
        }
        return extStore.saveQuote(new Quote(quote.quoteId(), quote.tenantId(), quote.supplierId(), quote.sellerSku(),
                quote.currency(), quote.unitPrice(), quote.totalPrice(), quote.quotedQuantity(), quote.leadTimeDays(),
                QuoteStatus.ACCEPTED, quote.validUntil(), quote.remark(), quote.createdAt(), Instant.now()));
    }

    @Transactional
    public Quote rejectQuote(String tenantId, String quoteId) {
        Quote quote = getQuote(tenantId, quoteId);
        if (quote.status() != QuoteStatus.SUBMITTED) {
            throw new BizException("QUOTE_STATUS_INVALID", "只有已提交询价可以拒绝");
        }
        return extStore.saveQuote(new Quote(quote.quoteId(), quote.tenantId(), quote.supplierId(), quote.sellerSku(),
                quote.currency(), quote.unitPrice(), quote.totalPrice(), quote.quotedQuantity(), quote.leadTimeDays(),
                QuoteStatus.REJECTED, quote.validUntil(), quote.remark(), quote.createdAt(), Instant.now()));
    }

    public List<Quote> compareQuotes(String tenantId, String sellerSku) {
        return extStore.listQuotesBySku(tenantId, sellerSku);
    }

    public List<Quote> listQuotesBySupplier(String tenantId, String supplierId) {
        return extStore.listQuotesBySupplier(tenantId, supplierId);
    }

    public Quote getQuote(String tenantId, String quoteId) {
        return extStore.findQuote(tenantId, quoteId)
                .orElseThrow(() -> new BizException("QUOTE_NOT_FOUND", "询价不存在"));
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
                .add(avgDelivery.multiply(new BigDecimal("0.3"))
                .add(avgPrice.multiply(new BigDecimal("0.2"))
                .add(avgService.multiply(new BigDecimal("0.2")))));
        overall = overall.setScale(2, RoundingMode.HALF_UP);
        SupplierScore existing = extStore.findSupplierScore(tenantId, supplierId).orElse(null);
        SupplierScore score = new SupplierScore(
                existing != null ? existing.scoreId() : UUID.randomUUID().toString(),
                tenantId, supplierId, avgQuality, avgDelivery, avgPrice, avgService, overall,
                evaluations.size(), Instant.now());
        return extStore.saveSupplierScore(score);
    }

    public SupplierScore getSupplierScore(String tenantId, String supplierId) {
        return extStore.findSupplierScore(tenantId, supplierId)
                .orElseThrow(() -> new BizException("SUPPLIER_SCORE_NOT_FOUND", "供应商评分不存在"));
    }

    @Transactional
    public PurchaseApproval submitForApproval(String tenantId, SubmitApprovalCommand command) {
        PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, command.poId())
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "采购单不存在"));
        if (po.status() != PurchaseOrderStatus.SUBMITTED) {
            throw new BizException("PO_STATUS_INVALID", "只有已提交采购单可以发起审批");
        }
        PurchaseApproval approval = new PurchaseApproval(UUID.randomUUID().toString(), tenantId, command.poId(),
                ApprovalStatus.PENDING, command.approverId(), null, null, Instant.now());
        return extStore.saveApproval(approval);
    }

    @Transactional
    public PurchaseApproval approvePurchase(String tenantId, String approvalId, ApproveCommand command) {
        List<PurchaseApproval> approvals = extStore.listApprovalsByPo(tenantId, command.poId());
        PurchaseApproval approval = approvals.stream()
                .filter(a -> a.approvalId().equals(approvalId))
                .findFirst()
                .orElseThrow(() -> new BizException("APPROVAL_NOT_FOUND", "审批记录不存在"));
        if (approval.status() != ApprovalStatus.PENDING) {
            throw new BizException("APPROVAL_STATUS_INVALID", "审批记录已处理");
        }
        PurchaseApproval updated = new PurchaseApproval(approval.approvalId(), approval.tenantId(), approval.poId(),
                ApprovalStatus.APPROVED, approval.approverId(), command.comment(), Instant.now(), approval.createdAt());
        extStore.saveApproval(updated);
        boolean allApproved = extStore.listApprovalsByPo(tenantId, command.poId()).stream()
                .allMatch(a -> a.status() == ApprovalStatus.APPROVED);
        if (allApproved) {
            PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, command.poId())
                    .orElseThrow(() -> new BizException("PO_NOT_FOUND", "采购单不存在"));
            purchaseStore.savePurchaseOrder(new PurchaseOrder(po.poId(), po.tenantId(), po.supplierId(), po.poNumber(),
                    po.currency(), po.totalAmount(), PurchaseOrderStatus.APPROVED, po.paymentTerms(), po.shippingTerms(),
                    po.purchaseType(), po.expectedDeliveryDate(), po.actualDeliveryDate(), po.notes(),
                    po.createdBy(), approval.approverId(), po.lines(), po.createdAt(), Instant.now()));
        }
        return updated;
    }

    @Transactional
    public PurchaseApproval rejectPurchase(String tenantId, String approvalId, ApproveCommand command) {
        List<PurchaseApproval> approvals = extStore.listApprovalsByPo(tenantId, command.poId());
        PurchaseApproval approval = approvals.stream()
                .filter(a -> a.approvalId().equals(approvalId))
                .findFirst()
                .orElseThrow(() -> new BizException("APPROVAL_NOT_FOUND", "审批记录不存在"));
        if (approval.status() != ApprovalStatus.PENDING) {
            throw new BizException("APPROVAL_STATUS_INVALID", "审批记录已处理");
        }
        PurchaseApproval updated = new PurchaseApproval(approval.approvalId(), approval.tenantId(), approval.poId(),
                ApprovalStatus.REJECTED, approval.approverId(), command.comment(), Instant.now(), approval.createdAt());
        extStore.saveApproval(updated);
        PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, command.poId())
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "采购单不存在"));
        purchaseStore.savePurchaseOrder(new PurchaseOrder(po.poId(), po.tenantId(), po.supplierId(), po.poNumber(),
                po.currency(), po.totalAmount(), PurchaseOrderStatus.DRAFT, po.paymentTerms(), po.shippingTerms(),
                po.purchaseType(), po.expectedDeliveryDate(), po.actualDeliveryDate(), po.notes(),
                po.createdBy(), po.approvedBy(), po.lines(), po.createdAt(), Instant.now()));
        return updated;
    }

    public List<PurchaseApproval> listApprovalsByPo(String tenantId, String poId) {
        return extStore.listApprovalsByPo(tenantId, poId);
    }

    public List<PurchaseApproval> listPendingApprovals(String tenantId, String approverId) {
        return extStore.listPendingApprovals(tenantId, approverId);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    public record CreateQuoteCommand(String supplierId, String sellerSku, String currency, BigDecimal unitPrice,
                                     int quotedQuantity, int leadTimeDays, Instant validUntil, String remark) {}
    public record SubmitApprovalCommand(String poId, String approverId) {}
    public record ApproveCommand(String poId, String comment) {}
}
