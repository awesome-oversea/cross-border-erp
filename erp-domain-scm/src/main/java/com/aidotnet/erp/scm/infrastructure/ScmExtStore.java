package com.aidotnet.erp.scm.infrastructure;

import com.aidotnet.erp.scm.domain.ApprovalStatus;
import com.aidotnet.erp.scm.domain.ProcessingOrder;
import com.aidotnet.erp.scm.domain.ProcessingOrder.ProcessingOrderStatus;
import com.aidotnet.erp.scm.domain.PurchaseApproval;
import com.aidotnet.erp.scm.domain.PurchaseException;
import com.aidotnet.erp.scm.domain.PurchaseExceptionStatus;
import com.aidotnet.erp.scm.domain.PurchaseTracking;
import com.aidotnet.erp.scm.domain.PurchaseTrackingStatus;
import com.aidotnet.erp.scm.domain.Quote;
import com.aidotnet.erp.scm.domain.QuoteStatus;
import com.aidotnet.erp.scm.domain.SupplierScore;
import com.aidotnet.erp.scm.infrastructure.data.ProcessingOrderDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseApprovalDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseExceptionDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseTrackingDO;
import com.aidotnet.erp.scm.infrastructure.data.QuoteDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierScoreDO;
import com.aidotnet.erp.scm.infrastructure.mapper.ScmExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class ScmExtStore {

    private final ScmExtMapper mapper;
    private final ObjectMapper objectMapper;

    public ScmExtStore(ScmExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public Quote saveQuote(Quote quote) {
        QuoteDO existing = mapper.selectQuote(quote.tenantId(), quote.quoteId());
        QuoteDO data = toQuoteData(quote);
        if (existing == null) {
            mapper.insertQuote(data);
        } else {
            mapper.updateQuote(data);
        }
        return quote;
    }

    public Optional<Quote> findQuote(String tenantId, String quoteId) {
        return Optional.ofNullable(mapper.selectQuote(tenantId, quoteId)).map(this::toQuoteDomain);
    }

    public List<Quote> listQuotesBySupplier(String tenantId, String supplierId) {
        return mapper.selectQuotes(tenantId, supplierId).stream().map(this::toQuoteDomain).collect(Collectors.toList());
    }

    public List<Quote> listQuotesBySku(String tenantId, String sellerSku) {
        return mapper.selectQuotesBySku(tenantId, sellerSku).stream().map(this::toQuoteDomain).collect(Collectors.toList());
    }

    public SupplierScore saveSupplierScore(SupplierScore score) {
        SupplierScoreDO existing = mapper.selectSupplierScore(score.tenantId(), score.supplierId());
        SupplierScoreDO data = toSupplierScoreData(score);
        if (existing == null) {
            mapper.insertSupplierScore(data);
        } else {
            mapper.updateSupplierScore(data);
        }
        return score;
    }

    public Optional<SupplierScore> findSupplierScore(String tenantId, String supplierId) {
        return Optional.ofNullable(mapper.selectSupplierScore(tenantId, supplierId)).map(this::toSupplierScoreDomain);
    }

    public PurchaseApproval saveApproval(PurchaseApproval approval) {
        PurchaseApprovalDO data = toApprovalData(approval);
        PurchaseApprovalDO existing = mapper.selectPurchaseApproval(approval.tenantId(), approval.approvalId());
        if (existing == null) {
            mapper.insertPurchaseApproval(data);
        } else {
            mapper.updatePurchaseApproval(data);
        }
        return approval;
    }

    public Optional<PurchaseApproval> findApproval(String tenantId, String approvalId) {
        return Optional.ofNullable(mapper.selectPurchaseApproval(tenantId, approvalId)).map(this::toApprovalDomain);
    }

    public List<PurchaseApproval> listApprovalsByPo(String tenantId, String poId) {
        return mapper.selectApprovalsByPo(tenantId, poId).stream().map(this::toApprovalDomain).collect(Collectors.toList());
    }

    public List<PurchaseApproval> listPendingApprovals(String tenantId, String approverId) {
        return mapper.selectPendingApprovals(tenantId, approverId).stream().map(this::toApprovalDomain).collect(Collectors.toList());
    }

    private QuoteDO toQuoteData(Quote q) {
        QuoteDO data = new QuoteDO();
        data.setQuoteId(q.quoteId());
        data.setTenantId(q.tenantId());
        data.setSupplierId(q.supplierId());
        data.setSellerSku(q.sellerSku());
        data.setCurrency(q.currency());
        data.setUnitPrice(q.unitPrice());
        data.setTotalPrice(q.totalPrice());
        data.setQuotedQuantity(q.quotedQuantity());
        data.setLeadTimeDays(q.leadTimeDays());
        data.setStatus(q.status().name());
        data.setValidUntil(q.validUntil());
        data.setRemark(q.remark());
        data.setCreatedAt(q.createdAt() != null ? q.createdAt() : Instant.now());
        data.setUpdatedAt(q.updatedAt() != null ? q.updatedAt() : Instant.now());
        return data;
    }

    private Quote toQuoteDomain(QuoteDO d) {
        return new Quote(d.getQuoteId(), d.getTenantId(), d.getSupplierId(), d.getSellerSku(), d.getCurrency(),
                d.getUnitPrice(), d.getTotalPrice(), d.getQuotedQuantity(), d.getLeadTimeDays(),
                QuoteStatus.valueOf(d.getStatus()), d.getValidUntil(), d.getRemark(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private SupplierScoreDO toSupplierScoreData(SupplierScore s) {
        SupplierScoreDO data = new SupplierScoreDO();
        data.setScoreId(s.scoreId());
        data.setTenantId(s.tenantId());
        data.setSupplierId(s.supplierId());
        data.setQualityScore(s.qualityScore());
        data.setDeliveryScore(s.deliveryScore());
        data.setPriceScore(s.priceScore());
        data.setServiceScore(s.serviceScore());
        data.setOverallScore(s.overallScore());
        data.setEvaluationCount(s.evaluationCount());
        data.setScoredAt(s.scoredAt() != null ? s.scoredAt() : Instant.now());
        return data;
    }

    private SupplierScore toSupplierScoreDomain(SupplierScoreDO d) {
        return new SupplierScore(d.getScoreId(), d.getTenantId(), d.getSupplierId(), d.getQualityScore(),
                d.getDeliveryScore(), d.getPriceScore(), d.getServiceScore(), d.getOverallScore(),
                d.getEvaluationCount(), d.getScoredAt());
    }

    private PurchaseApprovalDO toApprovalData(PurchaseApproval a) {
        PurchaseApprovalDO data = new PurchaseApprovalDO();
        data.setApprovalId(a.approvalId());
        data.setTenantId(a.tenantId());
        data.setPoId(a.poId());
        data.setApprovalLevel(a.approvalLevel());
        data.setStatus(a.status().name());
        data.setApproverId(a.approverId());
        data.setComment(a.comment());
        data.setApprovedAt(a.approvedAt());
        data.setCreatedAt(a.createdAt() != null ? a.createdAt() : Instant.now());
        return data;
    }

    private PurchaseApproval toApprovalDomain(PurchaseApprovalDO d) {
        return new PurchaseApproval(
                d.getApprovalId(),
                d.getTenantId(),
                d.getPoId(),
                d.getApprovalLevel() != null ? d.getApprovalLevel() : 1,
                ApprovalStatus.valueOf(d.getStatus()),
                d.getApproverId(),
                d.getComment(),
                d.getApprovedAt(),
                d.getCreatedAt());
    }

    public ProcessingOrder saveProcessingOrder(ProcessingOrder order) {
        ProcessingOrderDO existing = mapper.selectProcessingOrder(order.tenantId(), order.processId());
        ProcessingOrderDO data = toProcessingOrderData(order);
        if (existing == null) {
            mapper.insertProcessingOrder(data);
        } else {
            mapper.updateProcessingOrder(data);
        }
        return order;
    }

    public Optional<ProcessingOrder> findProcessingOrder(String tenantId, String processId) {
        return Optional.ofNullable(mapper.selectProcessingOrder(tenantId, processId)).map(this::toProcessingOrderDomain);
    }

    public List<ProcessingOrder> listProcessingOrders(String tenantId) {
        return mapper.selectProcessingOrders(tenantId).stream().map(this::toProcessingOrderDomain).collect(Collectors.toList());
    }

    public PurchaseTracking saveTracking(PurchaseTracking t) {
        PurchaseTrackingDO existing = mapper.selectTracking(t.tenantId(), t.poId(), t.lineId());
        PurchaseTrackingDO data = toTrackingData(t);
        if (existing == null) {
            mapper.insertTracking(data);
        } else {
            mapper.updateTracking(data);
        }
        return t;
    }

    public Optional<PurchaseTracking> findTracking(String tenantId, String poId, String lineId) {
        return Optional.ofNullable(mapper.selectTracking(tenantId, poId, lineId)).map(this::toTrackingDomain);
    }

    public List<PurchaseTracking> listTrackings(String tenantId, String poId) {
        return mapper.selectTrackings(tenantId, poId).stream().map(this::toTrackingDomain).collect(Collectors.toList());
    }

    public PurchaseException saveException(PurchaseException e) {
        PurchaseExceptionDO existing = mapper.selectException(e.tenantId(), e.exceptionId());
        PurchaseExceptionDO data = toExceptionData(e);
        if (existing == null) {
            mapper.insertException(data);
        } else {
            mapper.updateException(data);
        }
        return e;
    }

    public Optional<PurchaseException> findException(String tenantId, String exceptionId) {
        return Optional.ofNullable(mapper.selectException(tenantId, exceptionId)).map(this::toExceptionDomain);
    }

    public List<PurchaseException> listExceptions(String tenantId, String poId) {
        return mapper.selectExceptions(tenantId, poId).stream().map(this::toExceptionDomain).collect(Collectors.toList());
    }

    public List<PurchaseException> listPendingExceptions(String tenantId) {
        return mapper.selectPendingExceptions(tenantId).stream().map(this::toExceptionDomain).collect(Collectors.toList());
    }

    private PurchaseTrackingDO toTrackingData(PurchaseTracking t) {
        PurchaseTrackingDO data = new PurchaseTrackingDO();
        data.setTrackingId(t.trackingId());
        data.setTenantId(t.tenantId());
        data.setPoId(t.poId());
        data.setLineId(t.lineId());
        data.setSellerSku(t.sellerSku());
        data.setOrderedQuantity(t.orderedQuantity());
        data.setReceivedQuantity(t.receivedQuantity());
        data.setPendingQuantity(t.pendingQuantity());
        data.setDamagedQuantity(t.damagedQuantity());
        data.setReturnedQuantity(t.returnedQuantity());
        data.setOrderedUnitCost(t.orderedUnitCost());
        data.setActualUnitCost(t.actualUnitCost());
        data.setStatus(t.status().name());
        data.setLastReceivedAt(t.lastReceivedAt());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private PurchaseTracking toTrackingDomain(PurchaseTrackingDO d) {
        return new PurchaseTracking(d.getTrackingId(), d.getTenantId(), d.getPoId(), d.getLineId(),
                d.getSellerSku(), d.getOrderedQuantity(), d.getReceivedQuantity(), d.getPendingQuantity(),
                d.getDamagedQuantity(), d.getReturnedQuantity(), d.getOrderedUnitCost(), d.getActualUnitCost(),
                PurchaseTrackingStatus.valueOf(d.getStatus()), d.getLastReceivedAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private PurchaseExceptionDO toExceptionData(PurchaseException e) {
        PurchaseExceptionDO data = new PurchaseExceptionDO();
        data.setExceptionId(e.exceptionId());
        data.setTenantId(e.tenantId());
        data.setPoId(e.poId());
        data.setLineId(e.lineId());
        data.setSellerSku(e.sellerSku());
        data.setExceptionType(e.exceptionType().name());
        data.setExpectedValue(e.expectedValue());
        data.setActualValue(e.actualValue());
        data.setDescription(e.description());
        data.setStatus(e.status().name());
        data.setHandlerId(e.handlerId());
        data.setHandlerNote(e.handlerNote());
        data.setCreatedAt(e.createdAt() != null ? e.createdAt() : Instant.now());
        data.setHandledAt(e.handledAt());
        return data;
    }

    private PurchaseException toExceptionDomain(PurchaseExceptionDO d) {
        return new PurchaseException(d.getExceptionId(), d.getTenantId(), d.getPoId(), d.getLineId(),
                d.getSellerSku(), com.aidotnet.erp.scm.domain.PurchaseExceptionType.valueOf(d.getExceptionType()),
                d.getExpectedValue(), d.getActualValue(), d.getDescription(),
                PurchaseExceptionStatus.valueOf(d.getStatus()), d.getHandlerId(), d.getHandlerNote(),
                d.getCreatedAt(), d.getHandledAt());
    }

    private ProcessingOrderDO toProcessingOrderData(ProcessingOrder o) {
        ProcessingOrderDO data = new ProcessingOrderDO();
        data.setProcessId(o.processId());
        data.setTenantId(o.tenantId());
        data.setName(o.name());
        try {
            data.setRawMaterials(objectMapper.writeValueAsString(o.rawMaterials()));
        } catch (JsonProcessingException e) {
            data.setRawMaterials("[]");
        }
        data.setOutputSku(o.outputSku());
        data.setOutputQuantity(o.outputQuantity());
        data.setTotalCost(o.totalCost());
        data.setCurrency(o.currency());
        data.setStatus(o.status().name());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setUpdatedAt(o.updatedAt() != null ? o.updatedAt() : Instant.now());
        return data;
    }

    private ProcessingOrder toProcessingOrderDomain(ProcessingOrderDO d) {
        List<ProcessingOrder.RawMaterial> materials;
        try {
            materials = objectMapper.readValue(d.getRawMaterials(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            materials = List.of();
        }
        return new ProcessingOrder(d.getProcessId(), d.getTenantId(), d.getName(), materials,
                d.getOutputSku(), d.getOutputQuantity(), d.getTotalCost(), d.getCurrency(),
                ProcessingOrderStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }
}
