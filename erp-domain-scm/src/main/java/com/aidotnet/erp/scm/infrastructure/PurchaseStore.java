package com.aidotnet.erp.scm.infrastructure;

import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.PurchasePlan;
import com.aidotnet.erp.scm.domain.PurchasePlanLine;
import com.aidotnet.erp.scm.domain.PurchasePlanLineStatus;
import com.aidotnet.erp.scm.domain.PurchasePlanStatus;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.domain.Supplier;
import com.aidotnet.erp.scm.domain.SupplierContact;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.domain.SupplierProfile;
import com.aidotnet.erp.scm.domain.SupplierQualification;
import com.aidotnet.erp.scm.domain.SupplierScore;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseOrderDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseOrderLineDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchasePlanDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchasePlanLineDO;
import com.aidotnet.erp.scm.infrastructure.data.ReplenishmentSuggestionDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierContactDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierEvaluationDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierQualificationDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierScoreDO;
import com.aidotnet.erp.scm.infrastructure.mapper.PurchaseMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.PurchasePlanMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.ReplenishmentSuggestionMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierContactMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierEvaluationMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierQualificationMapper;
import com.aidotnet.erp.scm.infrastructure.mapper.SupplierScoreMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseStore {

    private final SupplierMapper supplierMapper;
    private final SupplierContactMapper supplierContactMapper;
    private final SupplierQualificationMapper supplierQualificationMapper;
    private final SupplierScoreMapper supplierScoreMapper;
    private final PurchaseMapper purchaseMapper;
    private final PurchasePlanMapper purchasePlanMapper;
    private final ReplenishmentSuggestionMapper suggestionMapper;
    private final SupplierEvaluationMapper evaluationMapper;

    public PurchaseStore(
            SupplierMapper supplierMapper,
            SupplierContactMapper supplierContactMapper,
            SupplierQualificationMapper supplierQualificationMapper,
            SupplierScoreMapper supplierScoreMapper,
            PurchaseMapper purchaseMapper,
            PurchasePlanMapper purchasePlanMapper,
            ReplenishmentSuggestionMapper suggestionMapper,
            SupplierEvaluationMapper evaluationMapper) {
        this.supplierMapper = supplierMapper;
        this.supplierContactMapper = supplierContactMapper;
        this.supplierQualificationMapper = supplierQualificationMapper;
        this.supplierScoreMapper = supplierScoreMapper;
        this.purchaseMapper = purchaseMapper;
        this.purchasePlanMapper = purchasePlanMapper;
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

    public SupplierProfile saveSupplierProfile(SupplierProfile profile) {
        saveSupplier(profile.supplier());
        if (profile.contacts() != null) {
            replaceSupplierContacts(profile.supplier().tenantId(), profile.supplier().supplierId(), profile.contacts());
        }
        if (profile.qualifications() != null) {
            replaceSupplierQualifications(
                    profile.supplier().tenantId(), profile.supplier().supplierId(), profile.qualifications());
        }
        if (profile.score() != null) {
            saveSupplierScore(profile.score());
        }
        return findSupplierProfile(profile.supplier().tenantId(), profile.supplier().supplierId()).orElse(profile);
    }

    public Optional<Supplier> findSupplier(String tenantId, String supplierId) {
        return Optional.ofNullable(supplierMapper.selectById(tenantId, supplierId)).map(this::toSupplierDomain);
    }

    public Optional<SupplierProfile> findSupplierProfile(String tenantId, String supplierId) {
        return findSupplier(tenantId, supplierId).map(supplier -> new SupplierProfile(
                supplier,
                listSupplierContacts(tenantId, supplierId),
                listSupplierQualifications(tenantId, supplierId),
                findSupplierScore(tenantId, supplierId).orElse(null)));
    }

    public List<Supplier> listSuppliers(String tenantId) {
        return supplierMapper.selectByTenant(tenantId).stream()
                .map(this::toSupplierDomain)
                .collect(Collectors.toList());
    }

    public List<SupplierProfile> listSupplierProfiles(String tenantId) {
        return listSuppliers(tenantId).stream()
                .map(supplier -> new SupplierProfile(
                        supplier,
                        listSupplierContacts(tenantId, supplier.supplierId()),
                        listSupplierQualifications(tenantId, supplier.supplierId()),
                        findSupplierScore(tenantId, supplier.supplierId()).orElse(null)))
                .collect(Collectors.toList());
    }

    public void replaceSupplierContacts(String tenantId, String supplierId, List<SupplierContact> contacts) {
        supplierContactMapper.deleteBySupplier(tenantId, supplierId);
        if (contacts == null) {
            return;
        }
        contacts.stream().map(this::toContactData).forEach(supplierContactMapper::insert);
    }

    public void replaceSupplierQualifications(String tenantId, String supplierId, List<SupplierQualification> qualifications) {
        supplierQualificationMapper.deleteBySupplier(tenantId, supplierId);
        if (qualifications == null) {
            return;
        }
        qualifications.stream().map(this::toQualificationData).forEach(supplierQualificationMapper::insert);
    }

    public List<SupplierContact> listSupplierContacts(String tenantId, String supplierId) {
        return supplierContactMapper.selectBySupplier(tenantId, supplierId).stream()
                .map(this::toSupplierContactDomain)
                .collect(Collectors.toList());
    }

    public List<SupplierQualification> listSupplierQualifications(String tenantId, String supplierId) {
        return supplierQualificationMapper.selectBySupplier(tenantId, supplierId).stream()
                .map(this::toSupplierQualificationDomain)
                .collect(Collectors.toList());
    }

    public SupplierScore saveSupplierScore(SupplierScore score) {
        SupplierScoreDO existing = supplierScoreMapper.selectBySupplier(score.tenantId(), score.supplierId());
        SupplierScoreDO data = toSupplierScoreData(score);
        if (existing == null) {
            supplierScoreMapper.insert(data);
        } else {
            supplierScoreMapper.update(data);
        }
        return score;
    }

    public Optional<SupplierScore> findSupplierScore(String tenantId, String supplierId) {
        return Optional.ofNullable(supplierScoreMapper.selectBySupplier(tenantId, supplierId))
                .map(this::toSupplierScoreDomain);
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
        if (orderDO == null) {
            return Optional.empty();
        }
        List<PurchaseOrderLineDO> lineDOs = purchaseMapper.selectLines(poId);
        return Optional.of(toPurchaseOrderDomain(orderDO, lineDOs));
    }

    public List<PurchaseOrder> listPurchaseOrders(String tenantId) {
        return purchaseMapper.selectOrders(tenantId).stream()
                .map(orderDO -> {
                    List<PurchaseOrderLineDO> lineDOs = purchaseMapper.selectLines(orderDO.getPoId());
                    return toPurchaseOrderDomain(orderDO, lineDOs);
                })
                .collect(Collectors.toList());
    }

    public PurchasePlan savePurchasePlan(PurchasePlan purchasePlan) {
        PurchasePlanDO existing = purchasePlanMapper.selectPlan(purchasePlan.tenantId(), purchasePlan.planId());
        PurchasePlanDO data = toPurchasePlanData(purchasePlan);
        if (existing == null) {
            purchasePlanMapper.insertPlan(data);
        } else {
            purchasePlanMapper.updatePlan(data);
        }
        purchasePlanMapper.deleteLines(purchasePlan.planId());
        if (purchasePlan.lines() != null) {
            for (PurchasePlanLine line : purchasePlan.lines()) {
                purchasePlanMapper.insertLine(toPurchasePlanLineData(
                        purchasePlan.tenantId(), purchasePlan.planId(), line));
            }
        }
        return purchasePlan;
    }

    public Optional<PurchasePlan> findPurchasePlan(String tenantId, String planId) {
        PurchasePlanDO planDO = purchasePlanMapper.selectPlan(tenantId, planId);
        if (planDO == null) {
            return Optional.empty();
        }
        return Optional.of(toPurchasePlanDomain(planDO, purchasePlanMapper.selectLines(planDO.getPlanId())));
    }

    public List<PurchasePlan> listPurchasePlans(String tenantId) {
        return purchasePlanMapper.selectPlans(tenantId).stream()
                .map(planDO -> toPurchasePlanDomain(planDO, purchasePlanMapper.selectLines(planDO.getPlanId())))
                .collect(Collectors.toList());
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
        return Optional.ofNullable(suggestionMapper.selectById(tenantId, suggestionId)).map(this::toSuggestionDomain);
    }

    public List<ReplenishmentSuggestion> listSuggestions(String tenantId) {
        return suggestionMapper.selectByTenant(tenantId).stream()
                .map(this::toSuggestionDomain)
                .collect(Collectors.toList());
    }

    public List<ReplenishmentSuggestion> findPendingSuggestions(String tenantId) {
        return suggestionMapper.selectPending(tenantId).stream()
                .map(this::toSuggestionDomain)
                .collect(Collectors.toList());
    }

    public List<ReplenishmentSuggestion> findAcceptedSuggestions(String tenantId) {
        return suggestionMapper.selectByStatus(tenantId, ReplenishmentSuggestion.SuggestionStatus.ACCEPTED.name())
                .stream()
                .map(this::toSuggestionDomain)
                .collect(Collectors.toList());
    }

    public SupplierEvaluation saveEvaluation(SupplierEvaluation evaluation) {
        evaluationMapper.insert(toEvaluationData(evaluation));
        return evaluation;
    }

    public List<SupplierEvaluation> listEvaluations(String tenantId, String supplierId) {
        return evaluationMapper.selectBySupplier(tenantId, supplierId).stream()
                .map(this::toEvaluationDomain)
                .collect(Collectors.toList());
    }

    public Optional<SupplierEvaluation> findLatestEvaluation(String tenantId, String supplierId) {
        return Optional.ofNullable(evaluationMapper.selectLatest(tenantId, supplierId)).map(this::toEvaluationDomain);
    }

    private SupplierDO toSupplierData(Supplier supplier) {
        SupplierDO data = new SupplierDO();
        data.setSupplierId(supplier.supplierId());
        data.setTenantId(supplier.tenantId());
        data.setName(supplier.name());
        data.setCompanyName(supplier.companyName());
        data.setContactName(supplier.contactName());
        data.setCountryCode(supplier.countryCode());
        data.setCreditRating(supplier.creditRating());
        data.setLeadTimeDays(supplier.leadTimeDays());
        data.setMoq(supplier.moq());
        data.setStatus(supplier.status());
        data.setCreatedAt(supplier.createdAt() != null ? supplier.createdAt() : Instant.now());
        data.setUpdatedAt(supplier.updatedAt() != null ? supplier.updatedAt() : Instant.now());
        return data;
    }

    private Supplier toSupplierDomain(SupplierDO data) {
        return new Supplier(
                data.getSupplierId(),
                data.getTenantId(),
                data.getName(),
                data.getCompanyName(),
                data.getContactName(),
                data.getCountryCode(),
                data.getCreditRating(),
                data.getLeadTimeDays() != null ? data.getLeadTimeDays() : 0,
                data.getMoq() != null ? data.getMoq() : 0,
                data.getStatus(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private SupplierContactDO toContactData(SupplierContact contact) {
        SupplierContactDO data = new SupplierContactDO();
        data.setContactId(contact.contactId());
        data.setTenantId(contact.tenantId());
        data.setSupplierId(contact.supplierId());
        data.setName(contact.name());
        data.setRole(contact.role());
        data.setEmail(contact.email());
        data.setPhone(contact.phone());
        data.setPrimaryContact(contact.primaryContact());
        data.setCreatedAt(contact.createdAt() != null ? contact.createdAt() : Instant.now());
        data.setUpdatedAt(contact.updatedAt() != null ? contact.updatedAt() : Instant.now());
        return data;
    }

    private SupplierContact toSupplierContactDomain(SupplierContactDO data) {
        return new SupplierContact(
                data.getContactId(),
                data.getTenantId(),
                data.getSupplierId(),
                data.getName(),
                data.getRole(),
                data.getEmail(),
                data.getPhone(),
                Boolean.TRUE.equals(data.getPrimaryContact()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private SupplierQualificationDO toQualificationData(SupplierQualification qualification) {
        SupplierQualificationDO data = new SupplierQualificationDO();
        data.setQualificationId(qualification.qualificationId());
        data.setTenantId(qualification.tenantId());
        data.setSupplierId(qualification.supplierId());
        data.setQualificationType(qualification.qualificationType());
        data.setQualificationNo(qualification.qualificationNo());
        data.setIssuedBy(qualification.issuedBy());
        data.setValidFrom(qualification.validFrom());
        data.setValidUntil(qualification.validUntil());
        data.setStatus(qualification.status());
        data.setRemark(qualification.remark());
        data.setCreatedAt(qualification.createdAt() != null ? qualification.createdAt() : Instant.now());
        data.setUpdatedAt(qualification.updatedAt() != null ? qualification.updatedAt() : Instant.now());
        return data;
    }

    private SupplierQualification toSupplierQualificationDomain(SupplierQualificationDO data) {
        return new SupplierQualification(
                data.getQualificationId(),
                data.getTenantId(),
                data.getSupplierId(),
                data.getQualificationType(),
                data.getQualificationNo(),
                data.getIssuedBy(),
                data.getValidFrom(),
                data.getValidUntil(),
                data.getStatus(),
                data.getRemark(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private SupplierScoreDO toSupplierScoreData(SupplierScore score) {
        SupplierScoreDO data = new SupplierScoreDO();
        data.setScoreId(score.scoreId());
        data.setTenantId(score.tenantId());
        data.setSupplierId(score.supplierId());
        data.setQualityScore(score.qualityScore());
        data.setDeliveryScore(score.deliveryScore());
        data.setPriceScore(score.priceScore());
        data.setServiceScore(score.serviceScore());
        data.setOverallScore(score.overallScore());
        data.setEvaluationCount(score.evaluationCount());
        data.setScoredAt(score.scoredAt() != null ? score.scoredAt() : Instant.now());
        return data;
    }

    private SupplierScore toSupplierScoreDomain(SupplierScoreDO data) {
        return new SupplierScore(
                data.getScoreId(),
                data.getTenantId(),
                data.getSupplierId(),
                data.getQualityScore(),
                data.getDeliveryScore(),
                data.getPriceScore(),
                data.getServiceScore(),
                data.getOverallScore(),
                data.getEvaluationCount(),
                data.getScoredAt());
    }

    private PurchaseOrderDO toPurchaseOrderData(PurchaseOrder purchaseOrder) {
        PurchaseOrderDO data = new PurchaseOrderDO();
        data.setPoId(purchaseOrder.poId());
        data.setTenantId(purchaseOrder.tenantId());
        data.setSupplierId(purchaseOrder.supplierId());
        data.setPoNumber(purchaseOrder.poNumber());
        data.setCurrency(purchaseOrder.currency());
        data.setTotalAmount(purchaseOrder.totalAmount());
        data.setStatus(purchaseOrder.status().name());
        data.setPaymentTerms(purchaseOrder.paymentTerms());
        data.setShippingTerms(purchaseOrder.shippingTerms());
        data.setPurchaseType(purchaseOrder.purchaseType());
        data.setExpectedDeliveryDate(purchaseOrder.expectedDeliveryDate());
        data.setActualDeliveryDate(purchaseOrder.actualDeliveryDate());
        data.setNotes(purchaseOrder.notes());
        data.setCreatedBy(purchaseOrder.createdBy());
        data.setApprovedBy(purchaseOrder.approvedBy());
        data.setCreatedAt(purchaseOrder.createdAt() != null ? purchaseOrder.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private PurchasePlanDO toPurchasePlanData(PurchasePlan purchasePlan) {
        PurchasePlanDO data = new PurchasePlanDO();
        data.setPlanId(purchasePlan.planId());
        data.setTenantId(purchasePlan.tenantId());
        data.setPlanNumber(purchasePlan.planNumber());
        data.setStatus(purchasePlan.status().name());
        data.setCreatedAt(purchasePlan.createdAt() != null ? purchasePlan.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private PurchasePlanLineDO toPurchasePlanLineData(String tenantId, String planId, PurchasePlanLine line) {
        PurchasePlanLineDO data = new PurchasePlanLineDO();
        data.setPlanId(planId);
        data.setTenantId(tenantId);
        data.setLineId(line.lineId());
        data.setSellerSku(line.sellerSku());
        data.setOrderDemandQuantity(line.orderDemandQuantity());
        data.setReplenishmentDemandQuantity(line.replenishmentDemandQuantity());
        data.setAvailableInventoryQuantity(line.availableInventoryQuantity());
        data.setInPurchasingQuantity(line.inPurchasingQuantity());
        data.setSuggestedPurchaseQuantity(line.suggestedPurchaseQuantity());
        data.setOrderSourceRefs(serializeRefs(line.orderSourceRefs()));
        data.setSuggestionSourceRefs(serializeRefs(line.suggestionSourceRefs()));
        data.setLineStatus(line.lineStatus().name());
        data.setLinkedPoId(line.linkedPoId());
        data.setCreatedAt(line.createdAt() != null ? line.createdAt() : Instant.now());
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

    private PurchaseOrder toPurchaseOrderDomain(PurchaseOrderDO data, List<PurchaseOrderLineDO> lineDOs) {
        List<PurchaseOrderLine> lines = lineDOs.stream()
                .map(line -> new PurchaseOrderLine(
                        line.getLineId(),
                        line.getProductId(),
                        line.getSellerSku(),
                        line.getQuantity() != null ? line.getQuantity() : 0,
                        line.getReceivedQuantity() != null ? line.getReceivedQuantity() : 0,
                        line.getUnitCost(),
                        line.getTotalPrice(),
                        line.getExpectedDate()))
                .collect(Collectors.toList());
        return new PurchaseOrder(
                data.getPoId(),
                data.getTenantId(),
                data.getSupplierId(),
                data.getPoNumber(),
                data.getCurrency(),
                data.getTotalAmount(),
                PurchaseOrderStatus.valueOf(data.getStatus()),
                data.getPaymentTerms(),
                data.getShippingTerms(),
                data.getPurchaseType(),
                data.getExpectedDeliveryDate(),
                data.getActualDeliveryDate(),
                data.getNotes(),
                data.getCreatedBy(),
                data.getApprovedBy(),
                lines,
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private PurchasePlan toPurchasePlanDomain(PurchasePlanDO data, List<PurchasePlanLineDO> lineDOs) {
        List<PurchasePlanLine> lines = lineDOs.stream()
                .map(line -> new PurchasePlanLine(
                        line.getLineId(),
                        line.getSellerSku(),
                        line.getOrderDemandQuantity() != null ? line.getOrderDemandQuantity() : 0,
                        line.getReplenishmentDemandQuantity() != null ? line.getReplenishmentDemandQuantity() : 0,
                        line.getAvailableInventoryQuantity() != null ? line.getAvailableInventoryQuantity() : 0,
                        line.getInPurchasingQuantity() != null ? line.getInPurchasingQuantity() : 0,
                        line.getSuggestedPurchaseQuantity() != null ? line.getSuggestedPurchaseQuantity() : 0,
                        deserializeRefs(line.getOrderSourceRefs()),
                        deserializeRefs(line.getSuggestionSourceRefs()),
                        PurchasePlanLineStatus.valueOf(line.getLineStatus()),
                        line.getLinkedPoId(),
                        line.getCreatedAt(),
                        line.getUpdatedAt()))
                .collect(Collectors.toList());
        return new PurchasePlan(
                data.getPlanId(),
                data.getTenantId(),
                data.getPlanNumber(),
                PurchasePlanStatus.valueOf(data.getStatus()),
                lines,
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private ReplenishmentSuggestionDO toSuggestionData(ReplenishmentSuggestion suggestion) {
        ReplenishmentSuggestionDO data = new ReplenishmentSuggestionDO();
        data.setSuggestionId(suggestion.suggestionId());
        data.setTenantId(suggestion.tenantId());
        data.setSellerSku(suggestion.sellerSku());
        data.setWarehouseId(suggestion.warehouseId());
        data.setCurrentStock(suggestion.currentStock());
        data.setAvgDailySales(suggestion.avgDailySales());
        data.setLeadTimeDays(suggestion.leadTimeDays());
        data.setSafetyStock(suggestion.safetyStock());
        data.setSuggestedQuantity(suggestion.suggestedQuantity());
        data.setPriority(suggestion.priority().name());
        data.setStatus(suggestion.status().name());
        data.setCreatedAt(suggestion.createdAt() != null ? suggestion.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private ReplenishmentSuggestion toSuggestionDomain(ReplenishmentSuggestion data) {
        return data;
    }

    private String serializeRefs(List<String> refs) {
        if (refs == null || refs.isEmpty()) {
            return null;
        }
        return String.join(",", refs);
    }

    private List<String> deserializeRefs(String refs) {
        if (refs == null || refs.isBlank()) {
            return List.of();
        }
        return Arrays.stream(refs.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private ReplenishmentSuggestion toSuggestionDomain(ReplenishmentSuggestionDO data) {
        return new ReplenishmentSuggestion(
                data.getSuggestionId(),
                data.getTenantId(),
                data.getSellerSku(),
                data.getWarehouseId(),
                data.getCurrentStock() != null ? data.getCurrentStock() : 0,
                data.getAvgDailySales() != null ? data.getAvgDailySales() : 0,
                data.getLeadTimeDays() != null ? data.getLeadTimeDays() : 0,
                data.getSafetyStock() != null ? data.getSafetyStock() : 0,
                data.getSuggestedQuantity() != null ? data.getSuggestedQuantity() : 0,
                ReplenishmentSuggestion.SuggestionPriority.valueOf(data.getPriority()),
                ReplenishmentSuggestion.SuggestionStatus.valueOf(data.getStatus()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private SupplierEvaluationDO toEvaluationData(SupplierEvaluation evaluation) {
        SupplierEvaluationDO data = new SupplierEvaluationDO();
        data.setEvaluationId(evaluation.evaluationId());
        data.setTenantId(evaluation.tenantId());
        data.setSupplierId(evaluation.supplierId());
        data.setQualityScore(evaluation.qualityScore());
        data.setDeliveryScore(evaluation.deliveryScore());
        data.setPriceScore(evaluation.priceScore());
        data.setServiceScore(evaluation.serviceScore());
        data.setOverallScore(evaluation.overallScore());
        data.setComment(evaluation.comment());
        data.setEvaluatedAt(evaluation.evaluatedAt() != null ? evaluation.evaluatedAt() : Instant.now());
        return data;
    }

    private SupplierEvaluation toEvaluationDomain(SupplierEvaluationDO data) {
        return new SupplierEvaluation(
                data.getEvaluationId(),
                data.getTenantId(),
                data.getSupplierId(),
                data.getQualityScore(),
                data.getDeliveryScore(),
                data.getPriceScore(),
                data.getServiceScore(),
                data.getOverallScore(),
                data.getComment(),
                data.getEvaluatedAt());
    }
}
