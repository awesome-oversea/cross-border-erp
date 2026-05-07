package com.aidotnet.erp.scm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.client.FmsClient;
import com.aidotnet.erp.scm.client.WmsClient;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.PurchaseType;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.domain.Supplier;
import com.aidotnet.erp.scm.domain.SupplierContact;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.domain.SupplierProfile;
import com.aidotnet.erp.scm.domain.SupplierQualification;
import com.aidotnet.erp.scm.domain.SupplierScore;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import com.aidotnet.erp.scm.infrastructure.ScmExtStore;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * 采购管理应用服务
 * <p>
 * 描述: 供应链域核心服务，负责供应商管理、采购单全流程管理、
 *       AI补货建议、供应商评估等业务逻辑。是连接产品域(PDM)、仓储域(WMS)、
 *       财务域(FMS)的关键环节，确保供应链高效运转。
 * </p>
 * <p>
 * 核心能力:
 *   1. 供应商管理 - 创建/更新供应商主数据，维护信用评级/交期/MOQ
 *   2. 采购单管理 - 创建/提交/审批/收货/取消采购单，支持部分收货
 *   3. AI补货建议 - 基于安全库存和再订货点自动生成补货建议
 *   4. 供应商评估 - 多维度评估供应商(质量/交期/价格/服务)
 * </p>
 * <p>
 * 业务规则:
 *   1. 采购单需关联有效供应商
 *   2. 采购单状态流转: DRAFT -> SUBMITTED -> APPROVED -> PARTIALLY_RECEIVED/RECEIVED
 *   3. 收货数量不可超过采购数量
 *   4. 收货后自动记录产品成本事件到FMS
 *   5. 收货后自动入库到WMS
 *   6. 安全库存 = 日均销量 × 交期天数 × 0.5
 *   7. 再订货点 = 日均销量 × 交期天数 + 安全库存
 * </p>
 *
 * @author ERP系统
 * @see PurchaseOrder
 * @see ReplenishmentSuggestion
 * @see PurchaseStore
 */
@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseStore purchaseStore;
    private final ScmExtStore scmExtStore;
    private final FmsClient fmsClient;
    private final WmsClient wmsClient;
    private final PurchaseTrackingService purchaseTrackingService;

    public PurchaseService(
            PurchaseStore purchaseStore,
            ScmExtStore scmExtStore,
            FmsClient fmsClient,
            WmsClient wmsClient,
            PurchaseTrackingService purchaseTrackingService) {
        this.purchaseStore = purchaseStore;
        this.scmExtStore = scmExtStore;
        this.fmsClient = fmsClient;
        this.wmsClient = wmsClient;
        this.purchaseTrackingService = purchaseTrackingService;
    }

    public SupplierProfile createSupplier(String tenantId, CreateSupplierCommand command) {
        Instant now = Instant.now();
        String supplierId = UUID.randomUUID().toString();
        Supplier supplier = new Supplier(
                supplierId,
                tenantId,
                command.name(),
                command.companyName(),
                resolveContactName(command.contactName(), command.contacts()),
                command.countryCode(),
                command.creditRating(),
                command.leadTimeDays(),
                command.moq(),
                Supplier.SupplierStatus.ACTIVE.name(),
                now,
                now);
        SupplierProfile profile = new SupplierProfile(
                supplier,
                buildContacts(tenantId, supplierId, command.contacts(), now),
                buildQualifications(tenantId, supplierId, command.qualifications(), now),
                buildScore(tenantId, supplierId, command.score(), now));
        return purchaseStore.saveSupplierProfile(profile);
    }

    public SupplierProfile getSupplierProfile(String tenantId, String supplierId) {
        return purchaseStore.findSupplierProfile(tenantId, supplierId)
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在"));
    }

    public List<SupplierProfile> listSupplierProfiles(String tenantId) {
        return purchaseStore.listSupplierProfiles(tenantId);
    }

    public SupplierProfile updateSupplier(String tenantId, String supplierId, UpdateSupplierCommand command) {
        SupplierProfile existing = getSupplierProfile(tenantId, supplierId);
        Instant now = Instant.now();
        Supplier supplier = new Supplier(
                existing.supplier().supplierId(),
                existing.supplier().tenantId(),
                command.name() != null ? command.name() : existing.supplier().name(),
                command.companyName() != null ? command.companyName() : existing.supplier().companyName(),
                command.contactName() != null
                        ? command.contactName()
                        : resolveContactName(existing.supplier().contactName(), command.contacts()),
                command.countryCode() != null ? command.countryCode() : existing.supplier().countryCode(),
                command.creditRating() != null ? command.creditRating() : existing.supplier().creditRating(),
                command.leadTimeDays() != null ? command.leadTimeDays() : existing.supplier().leadTimeDays(),
                command.moq() != null ? command.moq() : existing.supplier().moq(),
                command.status() != null ? command.status() : existing.supplier().status(),
                existing.supplier().createdAt(),
                now);
        List<SupplierContact> contacts = command.contacts() != null
                ? buildContacts(tenantId, supplierId, command.contacts(), now)
                : existing.contacts();
        List<SupplierQualification> qualifications = command.qualifications() != null
                ? buildQualifications(tenantId, supplierId, command.qualifications(), now)
                : existing.qualifications();
        SupplierScore score = command.score() != null
                ? buildScore(tenantId, supplierId, command.score(), now)
                : existing.score();
        return purchaseStore.saveSupplierProfile(new SupplierProfile(supplier, contacts, qualifications, score));
    }

    /**
     * 创建采购订单
     * <p>
     * 支持5种采购模式(MARKET/FACTORY/TAOBAO_TMALL_1688/ALIBABA_1688/PROCESSING)，
     * 根据采购模式决定审批流程和行为。
     * </p>
     * <p>
     * 业务规则:
     *   1. 供应商必须处于可用状态(ACTIVE)
     *   2. 采购明细不能为空
     *   3. 采购模式必须为5种合法模式之一
     *   4. 工厂/加工模式需要审批流
     *   5. ALIBABA_1688模式会触发自动同步到1688平台
     * </p>
     */
    public PurchaseOrder createPurchaseOrder(String tenantId, CreatePurchaseOrderCommand command) {
        Supplier supplier = purchaseStore.findSupplier(tenantId, command.supplierId())
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在"));
        if (!supplier.canPlaceOrder()) {
            throw new BizException("SUPPLIER_DISABLED", "停用或未生效供应商不可新建采购单");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BizException("PO_LINE_REQUIRED", "采购明细不能为空");
        }
        // 校验采购模式合法性
        if (command.purchaseType() == null || command.purchaseType().isBlank()) {
            throw new BizException("PURCHASE_TYPE_REQUIRED", "采购模式不能为空");
        }
        try {
            PurchaseType purchaseType = PurchaseType.valueOf(command.purchaseType());
            if (purchaseType.requiresApprovalFlow()) {
                log.info("Purchase type {} requires approval flow, PO will be created as DRAFT", purchaseType);
            }
            if (purchaseType.isExternalPlatform()) {
                log.info("Purchase type {} will sync to external platform", purchaseType);
            }
        } catch (IllegalArgumentException e) {
            throw new BizException("PURCHASE_TYPE_INVALID",
                    "不支持的采购模式，请使用: MARKET/FACTORY/TAOBAO_TMALL_1688/ALIBABA_1688/PROCESSING");
        }
        BigDecimal total = command.lines().stream()
                .map(PurchaseOrderLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant now = Instant.now();
        String poNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return purchaseStore.savePurchaseOrder(new PurchaseOrder(
                UUID.randomUUID().toString(),
                tenantId,
                command.supplierId(),
                poNumber,
                command.currency(),
                total,
                PurchaseOrderStatus.DRAFT,
                command.paymentTerms(),
                command.shippingTerms(),
                command.purchaseType(),
                command.expectedDeliveryDate(),
                null,
                command.notes(),
                null,
                null,
                command.lines(),
                now,
                now));
    }

    public PurchaseOrder submit(String tenantId, String poId) {
        PurchaseOrder po = getPurchaseOrder(tenantId, poId);
        if (po.status() != PurchaseOrderStatus.DRAFT) {
            throw new BizException("PO_STATUS_INVALID", "只有草稿采购单可以提交");
        }
        return update(po, PurchaseOrderStatus.SUBMITTED, po.lines());
    }

    public PurchaseOrder approve(String tenantId, String poId) {
        PurchaseOrder po = getPurchaseOrder(tenantId, poId);
        if (po.status() != PurchaseOrderStatus.SUBMITTED) {
            throw new BizException("PO_STATUS_INVALID", "只有已提交采购单可以审批");
        }
        if (!scmExtStore.listApprovalsByPo(tenantId, poId).isEmpty()) {
            throw new BizException("PURCHASE_APPROVAL_FLOW_REQUIRED", "Purchase order must be approved through approval flow");
        }
        return update(po, PurchaseOrderStatus.APPROVED, po.lines());
    }

    public PurchaseOrder receive(String tenantId, String poId, ReceiveCommand command) {
        PurchaseOrder po = getPurchaseOrder(tenantId, poId);
        if (po.status() != PurchaseOrderStatus.APPROVED && po.status() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new BizException("PO_STATUS_INVALID", "只有已审批采购单可以收货");
        }
        List<PurchaseOrderLine> lines = po.lines().stream().map(line -> {
            int received = command.receipts().stream()
                    .filter(receipt -> receipt.lineId().equals(line.lineId()))
                    .mapToInt(ReceiptLine::quantity)
                    .sum();
            int totalReceived = line.receivedQuantity() + received;
            if (totalReceived > line.quantity()) {
                throw new BizException("PO_RECEIVE_EXCEEDS_ORDERED", "收货数量超过采购数量");
            }
            return new PurchaseOrderLine(
                    line.lineId(),
                    line.productId(),
                    line.sellerSku(),
                    line.quantity(),
                    totalReceived,
                    line.unitCost(),
                    line.totalPrice(),
                    line.expectedDate());
        }).toList();
        boolean allReceived = lines.stream().allMatch(line -> line.receivedQuantity() == line.quantity());
        PurchaseOrder updated = update(
                po,
                allReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED,
                lines);
        syncPurchaseTrackingAfterReceive(tenantId, po, command.receipts());
        recordProductCostsAfterReceive(po, command.receipts());
        receiveInventoryAfterPurchase(po, command);
        return updated;
    }

    public ReplenishmentSuggestion generateSuggestion(String tenantId, GenerateSuggestionCommand command) {
        int safetyStock = calculateSafetyStock(command.avgDailySales(), command.leadTimeDays());
        int reorderPoint = calculateReorderPoint(command.avgDailySales(), command.leadTimeDays(), safetyStock);
        int suggestedQty = 0;
        ReplenishmentSuggestion.SuggestionPriority priority = ReplenishmentSuggestion.SuggestionPriority.LOW;
        if (command.currentStock() <= safetyStock) {
            suggestedQty = reorderPoint * 2 - command.currentStock();
            priority = ReplenishmentSuggestion.SuggestionPriority.URGENT;
        } else if (command.currentStock() <= reorderPoint) {
            suggestedQty = reorderPoint - command.currentStock();
            priority = ReplenishmentSuggestion.SuggestionPriority.HIGH;
        } else if (command.currentStock() <= reorderPoint * 1.2) {
            suggestedQty = (int) (reorderPoint * 0.5);
            priority = ReplenishmentSuggestion.SuggestionPriority.MEDIUM;
        }
        Instant now = Instant.now();
        return purchaseStore.saveSuggestion(new ReplenishmentSuggestion(
                UUID.randomUUID().toString(),
                tenantId,
                command.sellerSku(),
                command.warehouseId(),
                command.currentStock(),
                command.avgDailySales(),
                command.leadTimeDays(),
                safetyStock,
                suggestedQty,
                priority,
                ReplenishmentSuggestion.SuggestionStatus.PENDING,
                now,
                now));
    }

    public ReplenishmentSuggestion acceptSuggestion(String tenantId, String suggestionId) {
        ReplenishmentSuggestion suggestion = getSuggestion(tenantId, suggestionId);
        if (suggestion.status() != ReplenishmentSuggestion.SuggestionStatus.PENDING) {
            throw new BizException("SUGGESTION_STATUS_INVALID", "只有待处理建议可以接受");
        }
        return updateSuggestion(suggestion, ReplenishmentSuggestion.SuggestionStatus.ACCEPTED);
    }

    public ReplenishmentSuggestion rejectSuggestion(String tenantId, String suggestionId) {
        ReplenishmentSuggestion suggestion = getSuggestion(tenantId, suggestionId);
        if (suggestion.status() != ReplenishmentSuggestion.SuggestionStatus.PENDING) {
            throw new BizException("SUGGESTION_STATUS_INVALID", "只有待处理建议可以拒绝");
        }
        return updateSuggestion(suggestion, ReplenishmentSuggestion.SuggestionStatus.REJECTED);
    }

    public List<ReplenishmentSuggestion> listSuggestions(String tenantId) {
        return purchaseStore.listSuggestions(tenantId);
    }

    public List<ReplenishmentSuggestion> listPendingSuggestions(String tenantId) {
        return purchaseStore.findPendingSuggestions(tenantId);
    }

    public SupplierEvaluation evaluateSupplier(String tenantId, EvaluateSupplierCommand command) {
        purchaseStore.findSupplier(tenantId, command.supplierId())
                .orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在"));
        SupplierEvaluation evaluation = purchaseStore.saveEvaluation(new SupplierEvaluation(
                UUID.randomUUID().toString(),
                tenantId,
                command.supplierId(),
                command.qualityScore(),
                command.deliveryScore(),
                command.priceScore(),
                command.serviceScore(),
                calculateOverallScore(
                        command.qualityScore(),
                        command.deliveryScore(),
                        command.priceScore(),
                        command.serviceScore()),
                command.comment(),
                Instant.now()));
        refreshSupplierScoreFromEvaluations(tenantId, command.supplierId());
        return evaluation;
    }

    public List<SupplierEvaluation> listSupplierEvaluations(String tenantId, String supplierId) {
        return purchaseStore.listEvaluations(tenantId, supplierId);
    }

    public PurchaseOrder cancel(String tenantId, String poId) {
        PurchaseOrder po = getPurchaseOrder(tenantId, poId);
        if (po.status() != PurchaseOrderStatus.DRAFT && po.status() != PurchaseOrderStatus.SUBMITTED) {
            throw new BizException("PO_STATUS_INVALID", "只有草稿或已提交采购单可以取消");
        }
        return update(po, PurchaseOrderStatus.CANCELLED, po.lines());
    }

    public List<PurchaseOrder> listPurchaseOrders(String tenantId) {
        return purchaseStore.listPurchaseOrders(tenantId);
    }

    public PurchaseOrder getPurchaseOrder(String tenantId, String poId) {
        return purchaseStore.findPurchaseOrder(tenantId, poId)
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "采购单不存在"));
    }

    public FmsClient.PaymentRequestResponse createPaymentRequest(
            String tenantId, String poId, CreatePaymentRequestFromPoCommand command) {
        PurchaseOrder po = getPurchaseOrder(tenantId, poId);
        validatePaymentRequestable(po);
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("PAYMENT_REQUEST_AMOUNT_INVALID", "付款申请金额必须大于0");
        }
        if (command.amount().compareTo(po.totalAmount()) > 0) {
            throw new BizException("PAYMENT_REQUEST_AMOUNT_INVALID", "付款申请金额不能超过采购单金额");
        }
        return fmsClient.createPaymentRequest(new FmsClient.CreatePaymentRequestRequest(
                        command.requestId(),
                        po.poId(),
                        po.supplierId(),
                        command.amount(),
                        po.currency(),
                        "PROCUREMENT",
                        command.requestedBy(),
                        command.approvalFlow()))
                .data();
    }

    public List<FmsClient.PaymentRequestResponse> listPaymentRequests(String tenantId, String poId) {
        getPurchaseOrder(tenantId, poId);
        return fmsClient.listPaymentRequests(poId).data();
    }

    private ReplenishmentSuggestion getSuggestion(String tenantId, String suggestionId) {
        return purchaseStore.findSuggestion(tenantId, suggestionId)
                .orElseThrow(() -> new BizException("SUGGESTION_NOT_FOUND", "补货建议不存在"));
    }

    private void validatePaymentRequestable(PurchaseOrder po) {
        if (po.status() != PurchaseOrderStatus.APPROVED
                && po.status() != PurchaseOrderStatus.PARTIALLY_RECEIVED
                && po.status() != PurchaseOrderStatus.RECEIVED) {
            throw new BizException("PO_STATUS_INVALID", "采购单当前状态不可发起付款申请");
        }
    }

    private void receiveInventoryAfterPurchase(PurchaseOrder po, ReceiveCommand command) {
        if (command.warehouseId() == null || command.warehouseId().isBlank()) {
            return;
        }
        // 采购收货后同步 WMS 库存口径，后续再由 WMS 入库/质检流程细化待检与异常库存流向。
        for (ReceiptLine receipt : command.receipts()) {
            po.lines().stream()
                    .filter(line -> line.lineId().equals(receipt.lineId()))
                    .findFirst()
                    .ifPresent(line -> receiveInventory(po, command.warehouseId(), line, receipt.quantity()));
        }
    }

    private void receiveInventory(PurchaseOrder po, String warehouseId, PurchaseOrderLine line, int receivedQuantity) {
        try {
            wmsClient.receive(new WmsClient.StockRequest(warehouseId, line.sellerSku(), receivedQuantity));
        } catch (Exception ex) {
            log.warn(
                    "Receive inventory after purchase failed. poId={}, warehouseId={}, sellerSku={}",
                    po.poId(),
                    warehouseId,
                    line.sellerSku(),
                    ex);
        }
    }

    private void recordProductCostsAfterReceive(PurchaseOrder po, List<ReceiptLine> receipts) {
        for (ReceiptLine receipt : receipts) {
            po.lines().stream()
                    .filter(line -> line.lineId().equals(receipt.lineId()))
                    .findFirst()
                    .ifPresent(line -> recordProductCost(po, line, receipt.quantity()));
        }
    }

    private void syncPurchaseTrackingAfterReceive(String tenantId, PurchaseOrder po, List<ReceiptLine> receipts) {
        if (receipts == null || receipts.isEmpty()) {
            return;
        }
        // 首次收货自动建立跟单行，保证采购执行进度、异常处理和数量闭环可以统一查询。
        if (scmExtStore.listTrackings(tenantId, po.poId()).isEmpty()) {
            purchaseTrackingService.initTracking(tenantId, po.poId());
        }
        for (ReceiptLine receipt : receipts) {
            purchaseTrackingService.recordReceipt(tenantId, po.poId(), receipt.lineId(), receipt.quantity(), null);
        }
    }

    private void recordProductCost(PurchaseOrder po, PurchaseOrderLine line, int receivedQuantity) {
        try {
            fmsClient.recordCostEvent(new FmsClient.RecordCostEventRequest(
                    "PRODUCT_COST",
                    "SCM_PURCHASE_ORDER",
                    po.poId(),
                    line.sellerSku(),
                    null,
                    po.currency(),
                    line.unitCost().multiply(BigDecimal.valueOf(receivedQuantity)),
                    Instant.now()));
        } catch (Exception ex) {
            log.warn("Record product cost event failed. poId={}, sellerSku={}", po.poId(), line.sellerSku(), ex);
        }
    }

    private PurchaseOrder update(PurchaseOrder po, PurchaseOrderStatus status, List<PurchaseOrderLine> lines) {
        return purchaseStore.savePurchaseOrder(new PurchaseOrder(
                po.poId(),
                po.tenantId(),
                po.supplierId(),
                po.poNumber(),
                po.currency(),
                po.totalAmount(),
                status,
                po.paymentTerms(),
                po.shippingTerms(),
                po.purchaseType(),
                po.expectedDeliveryDate(),
                po.actualDeliveryDate(),
                po.notes(),
                po.createdBy(),
                po.approvedBy(),
                lines,
                po.createdAt(),
                Instant.now()));
    }

    private ReplenishmentSuggestion updateSuggestion(
            ReplenishmentSuggestion suggestion, ReplenishmentSuggestion.SuggestionStatus status) {
        return purchaseStore.saveSuggestion(new ReplenishmentSuggestion(
                suggestion.suggestionId(),
                suggestion.tenantId(),
                suggestion.sellerSku(),
                suggestion.warehouseId(),
                suggestion.currentStock(),
                suggestion.avgDailySales(),
                suggestion.leadTimeDays(),
                suggestion.safetyStock(),
                suggestion.suggestedQuantity(),
                suggestion.priority(),
                status,
                suggestion.createdAt(),
                Instant.now()));
    }

    private SupplierScore refreshSupplierScoreFromEvaluations(String tenantId, String supplierId) {
        List<SupplierEvaluation> evaluations = purchaseStore.listEvaluations(tenantId, supplierId);
        if (evaluations.isEmpty()) {
            return null;
        }
        BigDecimal qualityScore = average(evaluations.stream().map(SupplierEvaluation::qualityScore).toList());
        BigDecimal deliveryScore = average(evaluations.stream().map(SupplierEvaluation::deliveryScore).toList());
        BigDecimal priceScore = average(evaluations.stream().map(SupplierEvaluation::priceScore).toList());
        BigDecimal serviceScore = average(evaluations.stream().map(SupplierEvaluation::serviceScore).toList());
        String scoreId = purchaseStore.findSupplierScore(tenantId, supplierId)
                .map(SupplierScore::scoreId)
                .orElse(UUID.randomUUID().toString());
        return purchaseStore.saveSupplierScore(new SupplierScore(
                scoreId,
                tenantId,
                supplierId,
                qualityScore,
                deliveryScore,
                priceScore,
                serviceScore,
                calculateOverallScore(qualityScore, deliveryScore, priceScore, serviceScore),
                evaluations.size(),
                Instant.now()));
    }

    private List<SupplierContact> buildContacts(
            String tenantId, String supplierId, List<SupplierContactCommand> contacts, Instant now) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
                .map(contact -> new SupplierContact(
                        UUID.randomUUID().toString(),
                        tenantId,
                        supplierId,
                        contact.name(),
                        contact.role(),
                        contact.email(),
                        contact.phone(),
                        contact.primaryContact(),
                        now,
                        now))
                .toList();
    }

    private List<SupplierQualification> buildQualifications(
            String tenantId,
            String supplierId,
            List<SupplierQualificationCommand> qualifications,
            Instant now) {
        if (qualifications == null) {
            return List.of();
        }
        return qualifications.stream()
                .map(qualification -> new SupplierQualification(
                        UUID.randomUUID().toString(),
                        tenantId,
                        supplierId,
                        qualification.qualificationType(),
                        qualification.qualificationNo(),
                        qualification.issuedBy(),
                        qualification.validFrom(),
                        qualification.validUntil(),
                        qualification.status(),
                        qualification.remark(),
                        now,
                        now))
                .toList();
    }

    private SupplierScore buildScore(String tenantId, String supplierId, SupplierScoreCommand command, Instant now) {
        if (command == null) {
            return null;
        }
        String scoreId = purchaseStore.findSupplierScore(tenantId, supplierId)
                .map(SupplierScore::scoreId)
                .orElse(UUID.randomUUID().toString());
        return new SupplierScore(
                scoreId,
                tenantId,
                supplierId,
                command.qualityScore(),
                command.deliveryScore(),
                command.priceScore(),
                command.serviceScore(),
                calculateOverallScore(
                        command.qualityScore(),
                        command.deliveryScore(),
                        command.priceScore(),
                        command.serviceScore()),
                command.evaluationCount(),
                now);
    }

    private String resolveContactName(String fallback, List<SupplierContactCommand> contacts) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (contacts == null || contacts.isEmpty()) {
            return fallback;
        }
        return contacts.stream()
                .filter(SupplierContactCommand::primaryContact)
                .map(SupplierContactCommand::name)
                .findFirst()
                .orElse(contacts.get(0).name());
    }

    private BigDecimal calculateOverallScore(
            BigDecimal qualityScore,
            BigDecimal deliveryScore,
            BigDecimal priceScore,
            BigDecimal serviceScore) {
        return qualityScore.multiply(new BigDecimal("0.3"))
                .add(deliveryScore.multiply(new BigDecimal("0.3")))
                .add(priceScore.multiply(new BigDecimal("0.2")))
                .add(serviceScore.multiply(new BigDecimal("0.2")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private int calculateSafetyStock(int avgDailySales, int leadTimeDays) {
        return (int) Math.ceil(avgDailySales * leadTimeDays * 0.5);
    }

    private int calculateReorderPoint(int avgDailySales, int leadTimeDays, int safetyStock) {
        return avgDailySales * leadTimeDays + safetyStock;
    }

    public record CreateSupplierCommand(
            String name,
            String companyName,
            String contactName,
            String countryCode,
            String creditRating,
            int leadTimeDays,
            int moq,
            List<SupplierContactCommand> contacts,
            List<SupplierQualificationCommand> qualifications,
            SupplierScoreCommand score) {}

    public record UpdateSupplierCommand(
            String name,
            String companyName,
            String contactName,
            String countryCode,
            String creditRating,
            Integer leadTimeDays,
            Integer moq,
            String status,
            List<SupplierContactCommand> contacts,
            List<SupplierQualificationCommand> qualifications,
            SupplierScoreCommand score) {}

    public record SupplierContactCommand(
            String name, String role, String email, String phone, boolean primaryContact) {}

    public record SupplierQualificationCommand(
            String qualificationType,
            String qualificationNo,
            String issuedBy,
            Instant validFrom,
            Instant validUntil,
            String status,
            String remark) {}

    public record SupplierScoreCommand(
            BigDecimal qualityScore,
            BigDecimal deliveryScore,
            BigDecimal priceScore,
            BigDecimal serviceScore,
            int evaluationCount) {}

    public record CreatePurchaseOrderCommand(
            String supplierId,
            String currency,
            String paymentTerms,
            String shippingTerms,
            String purchaseType,
            Instant expectedDeliveryDate,
            String notes,
            List<PurchaseOrderLine> lines) {}

    public record CreatePaymentRequestFromPoCommand(
            String requestId, BigDecimal amount, String requestedBy, JsonNode approvalFlow) {}

    public record ReceiveCommand(String warehouseId, List<ReceiptLine> receipts) {}

    public record ReceiptLine(String lineId, int quantity) {}

    public record GenerateSuggestionCommand(
            String sellerSku, String warehouseId, int currentStock, int avgDailySales, int leadTimeDays) {}

    public record EvaluateSupplierCommand(
            String supplierId,
            BigDecimal qualityScore,
            BigDecimal deliveryScore,
            BigDecimal priceScore,
            BigDecimal serviceScore,
            String comment) {}
}
