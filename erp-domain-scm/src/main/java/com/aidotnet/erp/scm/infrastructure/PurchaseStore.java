package com.aidotnet.erp.scm.infrastructure;

import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.domain.Supplier;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseOrderDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseOrderLineDO;
import com.aidotnet.erp.scm.infrastructure.data.ReplenishmentSuggestionDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierEvaluationDO;
import com.aidotnet.erp.scm.infrastructure.mapper.PurchaseMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.ReplenishmentSuggestionMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierEvaluationMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * SCM域采购数据存储
 * <p>
 * 描述: 供应链域核心数据存储层，负责供应商、采购单、补货建议、供应商评估等实体的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class PurchaseStore {

    /** 供应商映射器 */
    private final SupplierMapper supplierMapper;
    /** 采购单映射器 */
    private final PurchaseMapper purchaseMapper;
    /** 补货建议映射器 */
    private final ReplenishmentSuggestionMapper suggestionMapper;
    /** 供应商评估映射器 */
    private final SupplierEvaluationMapper evaluationMapper;

    /**
     * 构造函数 - 依赖注入所有映射器
     */
    public PurchaseStore(SupplierMapper supplierMapper, PurchaseMapper purchaseMapper,
                         ReplenishmentSuggestionMapper suggestionMapper,
                         SupplierEvaluationMapper evaluationMapper) {
        this.supplierMapper = supplierMapper;
        this.purchaseMapper = purchaseMapper;
        this.suggestionMapper = suggestionMapper;
        this.evaluationMapper = evaluationMapper;
    }

    public Supplier saveSupplier(Supplier supplier) {
        SupplierDO existing = supplierMapper.selectById(supplier.tenantId(), supplier.supplierId());
        SupplierDO data = toSupplierData(supplier);
        if (existing == null) {
            supplierMapper.insert(data);
        } else {
            supplierMapper.update(data);
        }
        return supplier;
    }

    public Optional<Supplier> findSupplier(String tenantId, String supplierId) {
        return Optional.ofNullable(supplierMapper.selectById(tenantId, supplierId))
                .map(this::toSupplierDomain);
    }

    public List<Supplier> listSuppliers(String tenantId) {
        return supplierMapper.selectByTenant(tenantId).stream()
                .map(this::toSupplierDomain).collect(Collectors.toList());
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        PurchaseOrderDO existing = purchaseMapper.selectOrder(purchaseOrder.tenantId(), purchaseOrder.poId());
        PurchaseOrderDO data = toPurchaseOrderData(purchaseOrder);
        if (existing == null) {
            purchaseMapper.insertOrder(data);
        } else {
            purchaseMapper.updateOrder(data);
        }
        purchaseMapper.deleteLines(purchaseOrder.poId());
        if (purchaseOrder.lines() != null) {
            for (PurchaseOrderLine line : purchaseOrder.lines()) {
                PurchaseOrderLineDO lineData = toLineData(purchaseOrder.poId(), line);
                purchaseMapper.insertLine(lineData);
            }
        }
        return purchaseOrder;
    }

    public Optional<PurchaseOrder> findPurchaseOrder(String tenantId, String poId) {
        PurchaseOrderDO orderDO = purchaseMapper.selectOrder(tenantId, poId);
        if (orderDO == null) return Optional.empty();
        List<PurchaseOrderLineDO> lineDOs = purchaseMapper.selectLines(poId);
        return Optional.of(toPurchaseOrderDomain(orderDO, lineDOs));
    }

    public List<PurchaseOrder> listPurchaseOrders(String tenantId) {
        return purchaseMapper.selectOrders(tenantId).stream()
                .map(orderDO -> {
                    List<PurchaseOrderLineDO> lineDOs = purchaseMapper.selectLines(orderDO.getPoId());
                    return toPurchaseOrderDomain(orderDO, lineDOs);
                }).collect(Collectors.toList());
    }

    public ReplenishmentSuggestion saveSuggestion(ReplenishmentSuggestion suggestion) {
        ReplenishmentSuggestionDO existing = suggestionMapper.selectById(suggestion.tenantId(), suggestion.suggestionId());
        ReplenishmentSuggestionDO data = toSuggestionData(suggestion);
        if (existing == null) {
            suggestionMapper.insert(data);
        } else {
            suggestionMapper.update(data);
        }
        return suggestion;
    }

    public Optional<ReplenishmentSuggestion> findSuggestion(String tenantId, String suggestionId) {
        return Optional.ofNullable(suggestionMapper.selectById(tenantId, suggestionId))
                .map(this::toSuggestionDomain);
    }

    public List<ReplenishmentSuggestion> listSuggestions(String tenantId) {
        return suggestionMapper.selectByTenant(tenantId).stream()
                .map(this::toSuggestionDomain).collect(Collectors.toList());
    }

    public List<ReplenishmentSuggestion> findPendingSuggestions(String tenantId) {
        return suggestionMapper.selectPending(tenantId).stream()
                .map(this::toSuggestionDomain).collect(Collectors.toList());
    }

    public SupplierEvaluation saveEvaluation(SupplierEvaluation evaluation) {
        evaluationMapper.insert(toEvaluationData(evaluation));
        return evaluation;
    }

    public List<SupplierEvaluation> listEvaluations(String tenantId, String supplierId) {
        return evaluationMapper.selectBySupplier(tenantId, supplierId).stream()
                .map(this::toEvaluationDomain).collect(Collectors.toList());
    }

    public Optional<SupplierEvaluation> findLatestEvaluation(String tenantId, String supplierId) {
        return Optional.ofNullable(evaluationMapper.selectLatest(tenantId, supplierId))
                .map(this::toEvaluationDomain);
    }

    private SupplierDO toSupplierData(Supplier s) {
        SupplierDO data = new SupplierDO();
        data.setSupplierId(s.supplierId());
        data.setTenantId(s.tenantId());
        data.setName(s.name());
        data.setCompanyName(s.companyName());
        data.setContactName(s.contactName());
        data.setCountryCode(s.countryCode());
        data.setCreditRating(s.creditRating());
        data.setLeadTimeDays(s.leadTimeDays());
        data.setMoq(s.moq());
        data.setStatus(s.status());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(s.updatedAt() != null ? s.updatedAt() : Instant.now());
        return data;
    }

    private Supplier toSupplierDomain(SupplierDO d) {
        return new Supplier(d.getSupplierId(), d.getTenantId(), d.getName(), d.getCompanyName(),
                d.getContactName(), d.getCountryCode(), d.getCreditRating(),
                d.getLeadTimeDays() != null ? d.getLeadTimeDays() : 0,
                d.getMoq() != null ? d.getMoq() : 0,
                d.getStatus(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private PurchaseOrderDO toPurchaseOrderData(PurchaseOrder po) {
        PurchaseOrderDO data = new PurchaseOrderDO();
        data.setPoId(po.poId());
        data.setTenantId(po.tenantId());
        data.setSupplierId(po.supplierId());
        data.setPoNumber(po.poNumber());
        data.setCurrency(po.currency());
        data.setTotalAmount(po.totalAmount());
        data.setStatus(po.status().name());
        data.setPaymentTerms(po.paymentTerms());
        data.setShippingTerms(po.shippingTerms());
        data.setPurchaseType(po.purchaseType());
        data.setExpectedDeliveryDate(po.expectedDeliveryDate());
        data.setActualDeliveryDate(po.actualDeliveryDate());
        data.setNotes(po.notes());
        data.setCreatedBy(po.createdBy());
        data.setApprovedBy(po.approvedBy());
        data.setCreatedAt(po.createdAt() != null ? po.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private PurchaseOrderLineDO toLineData(String poId, PurchaseOrderLine line) {
        PurchaseOrderLineDO data = new PurchaseOrderLineDO();
        data.setPoId(poId);
        data.setLineId(line.lineId());
        data.setProductId(line.productId());
        data.setSellerSku(line.sellerSku());
        data.setQuantity(line.quantity());
        data.setReceivedQuantity(line.receivedQuantity());
        data.setUnitCost(line.unitCost());
        data.setTotalPrice(line.totalPrice());
        data.setExpectedDate(line.expectedDate());
        return data;
    }

    private PurchaseOrder toPurchaseOrderDomain(PurchaseOrderDO d, List<PurchaseOrderLineDO> lineDOs) {
        List<PurchaseOrderLine> lines = lineDOs.stream()
                .map(l -> new PurchaseOrderLine(l.getLineId(), l.getProductId(), l.getSellerSku(),
                        l.getQuantity() != null ? l.getQuantity() : 0,
                        l.getReceivedQuantity() != null ? l.getReceivedQuantity() : 0,
                        l.getUnitCost(), l.getTotalPrice(), l.getExpectedDate()))
                .collect(Collectors.toList());
        return new PurchaseOrder(d.getPoId(), d.getTenantId(), d.getSupplierId(), d.getPoNumber(),
                d.getCurrency(), d.getTotalAmount(),
                PurchaseOrderStatus.valueOf(d.getStatus()), d.getPaymentTerms(), d.getShippingTerms(),
                d.getPurchaseType(), d.getExpectedDeliveryDate(), d.getActualDeliveryDate(),
                d.getNotes(), d.getCreatedBy(), d.getApprovedBy(), lines,
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ReplenishmentSuggestionDO toSuggestionData(ReplenishmentSuggestion s) {
        ReplenishmentSuggestionDO data = new ReplenishmentSuggestionDO();
        data.setSuggestionId(s.suggestionId());
        data.setTenantId(s.tenantId());
        data.setSellerSku(s.sellerSku());
        data.setWarehouseId(s.warehouseId());
        data.setCurrentStock(s.currentStock());
        data.setAvgDailySales(s.avgDailySales());
        data.setLeadTimeDays(s.leadTimeDays());
        data.setSafetyStock(s.safetyStock());
        data.setSuggestedQuantity(s.suggestedQuantity());
        data.setPriority(s.priority().name());
        data.setStatus(s.status().name());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private ReplenishmentSuggestion toSuggestionDomain(ReplenishmentSuggestionDO d) {
        return new ReplenishmentSuggestion(d.getSuggestionId(), d.getTenantId(),
                d.getSellerSku(), d.getWarehouseId(),
                d.getCurrentStock() != null ? d.getCurrentStock() : 0,
                d.getAvgDailySales() != null ? d.getAvgDailySales() : 0,
                d.getLeadTimeDays() != null ? d.getLeadTimeDays() : 0,
                d.getSafetyStock() != null ? d.getSafetyStock() : 0,
                d.getSuggestedQuantity() != null ? d.getSuggestedQuantity() : 0,
                ReplenishmentSuggestion.SuggestionPriority.valueOf(d.getPriority()),
                ReplenishmentSuggestion.SuggestionStatus.valueOf(d.getStatus()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private SupplierEvaluationDO toEvaluationData(SupplierEvaluation e) {
        SupplierEvaluationDO data = new SupplierEvaluationDO();
        data.setEvaluationId(e.evaluationId());
        data.setTenantId(e.tenantId());
        data.setSupplierId(e.supplierId());
        data.setQualityScore(e.qualityScore());
        data.setDeliveryScore(e.deliveryScore());
        data.setPriceScore(e.priceScore());
        data.setServiceScore(e.serviceScore());
        data.setOverallScore(e.overallScore());
        data.setComment(e.comment());
        data.setEvaluatedAt(e.evaluatedAt() != null ? e.evaluatedAt() : Instant.now());
        return data;
    }

    private SupplierEvaluation toEvaluationDomain(SupplierEvaluationDO d) {
        return new SupplierEvaluation(d.getEvaluationId(), d.getTenantId(), d.getSupplierId(),
                d.getQualityScore(), d.getDeliveryScore(), d.getPriceScore(),
                d.getServiceScore(), d.getOverallScore(), d.getComment(), d.getEvaluatedAt());
    }
}
