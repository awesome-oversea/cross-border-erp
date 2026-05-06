package com.aidotnet.erp.scm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.client.FmsClient;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.domain.Supplier;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
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
    private final FmsClient fmsClient;
    private final com.aidotnet.erp.scm.client.WmsClient wmsClient;

    public PurchaseService(PurchaseStore purchaseStore, FmsClient fmsClient, com.aidotnet.erp.scm.client.WmsClient wmsClient) {
        this.purchaseStore = purchaseStore;
        this.fmsClient = fmsClient;
        this.wmsClient = wmsClient;
    }

    public Supplier createSupplier(String tenantId, CreateSupplierCommand command) {
        Instant now = Instant.now();
        return purchaseStore.saveSupplier(new Supplier(UUID.randomUUID().toString(), tenantId, command.name(),
                command.companyName(), command.contactName(), command.countryCode(), command.creditRating(),
                command.leadTimeDays(), command.moq(), Supplier.SupplierStatus.ACTIVE.name(), now, now));
    }

    public PurchaseOrder createPurchaseOrder(String tenantId, CreatePurchaseOrderCommand command) {
        purchaseStore.findSupplier(tenantId, command.supplierId()).orElseThrow(() -> new BizException("SUPPLIER_NOT_FOUND", "供应商不存在"));
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BizException("PO_LINE_REQUIRED", "采购明细不能为空");
        }
        BigDecimal total = command.lines().stream()
                .map(PurchaseOrderLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant now = Instant.now();
        String poNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return purchaseStore.savePurchaseOrder(new PurchaseOrder(UUID.randomUUID().toString(), tenantId, command.supplierId(),
                poNumber, command.currency(), total, PurchaseOrderStatus.DRAFT, command.paymentTerms(),
                command.shippingTerms(), command.purchaseType(), command.expectedDeliveryDate(), null,
                command.notes(), null, null, command.lines(), now, now));
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
            return new PurchaseOrderLine(line.lineId(), line.productId(), line.sellerSku(), line.quantity(),
                    totalReceived, line.unitCost(), line.totalPrice(), line.expectedDate());
        }).toList();
        boolean allReceived = lines.stream().allMatch(line -> line.receivedQuantity() == line.quantity());
        PurchaseOrder updated = update(po, allReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED, lines);
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
        return purchaseStore.saveSuggestion(new ReplenishmentSuggestion(UUID.randomUUID().toString(), tenantId,
                command.sellerSku(), command.warehouseId(), command.currentStock(), command.avgDailySales(),
                command.leadTimeDays(), safetyStock, suggestedQty, priority,
                ReplenishmentSuggestion.SuggestionStatus.PENDING, now, now));
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
        BigDecimal overall = command.qualityScore().multiply(new BigDecimal("0.3"))
                .add(command.deliveryScore().multiply(new BigDecimal("0.3"))
                .add(command.priceScore().multiply(new BigDecimal("0.2"))
                .add(command.serviceScore().multiply(new BigDecimal("0.2")))));
        overall = overall.setScale(2, RoundingMode.HALF_UP);
        return purchaseStore.saveEvaluation(new SupplierEvaluation(UUID.randomUUID().toString(), tenantId,
                command.supplierId(), command.qualityScore(), command.deliveryScore(), command.priceScore(),
                command.serviceScore(), overall, command.comment(), Instant.now()));
    }

    public List<SupplierEvaluation> listSupplierEvaluations(String tenantId, String supplierId) {
        return purchaseStore.listEvaluations(tenantId, supplierId);
    }

    public List<Supplier> listSuppliers(String tenantId) {
        return purchaseStore.listSuppliers(tenantId);
    }

    public Supplier updateSupplier(Supplier supplier) {
        return purchaseStore.saveSupplier(supplier);
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
        return purchaseStore.findPurchaseOrder(tenantId, poId).orElseThrow(() -> new BizException("PO_NOT_FOUND", "采购单不存在"));
    }

    private ReplenishmentSuggestion getSuggestion(String tenantId, String suggestionId) {
        return purchaseStore.findSuggestion(tenantId, suggestionId)
                .orElseThrow(() -> new BizException("SUGGESTION_NOT_FOUND", "补货建议不存在"));
    }

    private void receiveInventoryAfterPurchase(PurchaseOrder po, ReceiveCommand command) {
        if (command.warehouseId() == null || command.warehouseId().isBlank()) {
            return;
        }
        for (ReceiptLine receipt : command.receipts()) {
            po.lines().stream()
                    .filter(line -> line.lineId().equals(receipt.lineId()))
                    .findFirst()
                    .ifPresent(line -> receiveInventory(po, command.warehouseId(), line, receipt.quantity()));
        }
    }

    private void receiveInventory(PurchaseOrder po, String warehouseId, PurchaseOrderLine line, int receivedQuantity) {
        try {
            wmsClient.receive(new com.aidotnet.erp.scm.client.WmsClient.StockRequest(
                    warehouseId, line.sellerSku(), receivedQuantity));
        } catch (Exception ex) {
            log.warn("Receive inventory after purchase failed. poId={}, warehouseId={}, sellerSku={}",
                    po.poId(), warehouseId, line.sellerSku(), ex);
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
        return purchaseStore.savePurchaseOrder(new PurchaseOrder(po.poId(), po.tenantId(), po.supplierId(), po.poNumber(),
                po.currency(), po.totalAmount(), status, po.paymentTerms(), po.shippingTerms(), po.purchaseType(),
                po.expectedDeliveryDate(), po.actualDeliveryDate(), po.notes(), po.createdBy(), po.approvedBy(),
                lines, po.createdAt(), Instant.now()));
    }

    private ReplenishmentSuggestion updateSuggestion(ReplenishmentSuggestion s, ReplenishmentSuggestion.SuggestionStatus status) {
        return purchaseStore.saveSuggestion(new ReplenishmentSuggestion(s.suggestionId(), s.tenantId(), s.sellerSku(),
                s.warehouseId(), s.currentStock(), s.avgDailySales(), s.leadTimeDays(), s.safetyStock(),
                s.suggestedQuantity(), s.priority(), status, s.createdAt(), Instant.now()));
    }

    private int calculateSafetyStock(int avgDailySales, int leadTimeDays) {
        return (int) Math.ceil(avgDailySales * leadTimeDays * 0.5);
    }

    private int calculateReorderPoint(int avgDailySales, int leadTimeDays, int safetyStock) {
        return avgDailySales * leadTimeDays + safetyStock;
    }

    public record CreateSupplierCommand(String name, String companyName, String contactName,
                                        String countryCode, String creditRating, int leadTimeDays, int moq) {}

    public record CreatePurchaseOrderCommand(String supplierId, String currency, String paymentTerms,
                                             String shippingTerms, String purchaseType,
                                             Instant expectedDeliveryDate, String notes,
                                             List<PurchaseOrderLine> lines) {}

    public record ReceiveCommand(String warehouseId, List<ReceiptLine> receipts) {}

    public record ReceiptLine(String lineId, int quantity) {}

    public record GenerateSuggestionCommand(String sellerSku, String warehouseId, int currentStock,
                                            int avgDailySales, int leadTimeDays) {}

    public record EvaluateSupplierCommand(String supplierId, BigDecimal qualityScore, BigDecimal deliveryScore,
                                          BigDecimal priceScore, BigDecimal serviceScore, String comment) {}
}
