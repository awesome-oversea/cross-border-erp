package com.aidotnet.erp.scm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.domain.PurchaseException;
import com.aidotnet.erp.scm.domain.PurchaseExceptionStatus;
import com.aidotnet.erp.scm.domain.PurchaseExceptionType;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.PurchaseTracking;
import com.aidotnet.erp.scm.domain.PurchaseTrackingStatus;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import com.aidotnet.erp.scm.infrastructure.ScmExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 采购跟单与采购异常应用服务
 * <p>
 * 描述: 负责采购单跟单初始化、收货回写、不良/退回登记、异常创建与处理闭环。
 *       该服务保持“采购执行域”的业务口径，不直接替代 WMS 的入库质检流程。
 * </p>
 */
@Service
public class PurchaseTrackingService {

    private final ScmExtStore scmExtStore;
    private final PurchaseStore purchaseStore;

    public PurchaseTrackingService(ScmExtStore scmExtStore, PurchaseStore purchaseStore) {
        this.scmExtStore = scmExtStore;
        this.purchaseStore = purchaseStore;
    }

    @Transactional
    public PurchaseTracking initTracking(String tenantId, String poId) {
        PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, poId)
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "purchase order not found"));
        if (po.status() != PurchaseOrderStatus.APPROVED
                && po.status() != PurchaseOrderStatus.PARTIALLY_RECEIVED
                && po.status() != PurchaseOrderStatus.RECEIVED) {
            throw new BizException("PO_STATUS_INVALID", "only approved purchase orders can initialize tracking");
        }
        for (PurchaseOrderLine line : po.lines()) {
            scmExtStore.findTracking(tenantId, poId, line.lineId()).ifPresent(existing -> {
                throw new BizException("TRACKING_ALREADY_EXISTS", "purchase tracking already exists: " + line.lineId());
            });
            PurchaseTracking tracking = new PurchaseTracking(
                    UUID.randomUUID().toString(),
                    tenantId,
                    poId,
                    line.lineId(),
                    line.sellerSku(),
                    line.quantity(),
                    0,
                    line.quantity(),
                    0,
                    0,
                    line.unitCost(),
                    line.unitCost(),
                    PurchaseTrackingStatus.PENDING_RECEIPT,
                    null,
                    Instant.now(),
                    Instant.now());
            scmExtStore.saveTracking(tracking);
        }
        return scmExtStore.findTracking(tenantId, poId, po.lines().get(0).lineId())
                .orElseThrow(() -> new BizException("TRACKING_INIT_FAILED", "purchase tracking init failed"));
    }

    /**
     * 登记实收数量与实际单价。
     * 业务规则:
     * 1. 超收超过 10% 自动生成 OVER_DELIVERED 异常；
     * 2. 实际单价较下单单价上涨超过 5% 自动生成 PRICE_INCREASED 异常；
     * 3. 跟单状态按已收、待收、不良、退回综合判断。
     */
    @Transactional
    public PurchaseTracking recordReceipt(String tenantId, String poId, String lineId, int receivedQuantity, BigDecimal actualUnitCost) {
        PurchaseTracking tracking = scmExtStore.findTracking(tenantId, poId, lineId)
                .orElseThrow(() -> new BizException("TRACKING_NOT_FOUND", "purchase tracking not found"));
        int newReceived = tracking.receivedQuantity() + receivedQuantity;
        if (newReceived > tracking.orderedQuantity() + (int) (tracking.orderedQuantity() * 0.1)) {
            createException(
                    tenantId,
                    poId,
                    lineId,
                    tracking.sellerSku(),
                    PurchaseExceptionType.OVER_DELIVERED,
                    BigDecimal.valueOf(tracking.orderedQuantity()),
                    BigDecimal.valueOf(newReceived),
                    "received quantity exceeds ordered quantity by more than 10%");
        }
        int newPending =
                Math.max(0, tracking.orderedQuantity() - newReceived - tracking.damagedQuantity() - tracking.returnedQuantity());
        PurchaseTrackingStatus newStatus = resolveStatus(
                tracking.orderedQuantity(), newReceived, tracking.damagedQuantity(), tracking.returnedQuantity(), newPending);
        if (actualUnitCost != null && actualUnitCost.compareTo(tracking.orderedUnitCost()) > 0) {
            BigDecimal increaseRate = actualUnitCost.subtract(tracking.orderedUnitCost())
                    .divide(tracking.orderedUnitCost(), 4, RoundingMode.HALF_UP);
            if (increaseRate.compareTo(new BigDecimal("0.05")) > 0) {
                createException(
                        tenantId,
                        poId,
                        lineId,
                        tracking.sellerSku(),
                        PurchaseExceptionType.PRICE_INCREASED,
                        tracking.orderedUnitCost(),
                        actualUnitCost,
                        "actual unit cost increased by more than 5%");
            }
        }
        PurchaseTracking updated = new PurchaseTracking(
                tracking.trackingId(),
                tracking.tenantId(),
                tracking.poId(),
                tracking.lineId(),
                tracking.sellerSku(),
                tracking.orderedQuantity(),
                newReceived,
                newPending,
                tracking.damagedQuantity(),
                tracking.returnedQuantity(),
                tracking.orderedUnitCost(),
                actualUnitCost != null ? actualUnitCost : tracking.actualUnitCost(),
                newStatus,
                Instant.now(),
                tracking.createdAt(),
                Instant.now());
        return scmExtStore.saveTracking(updated);
    }

    /** 登记质损/破损数量，并生成 DAMAGED 采购异常。 */
    @Transactional
    public PurchaseTracking recordDamage(String tenantId, String poId, String lineId, int damagedQuantity, String reason) {
        PurchaseTracking tracking = scmExtStore.findTracking(tenantId, poId, lineId)
                .orElseThrow(() -> new BizException("TRACKING_NOT_FOUND", "purchase tracking not found"));
        createException(
                tenantId,
                poId,
                lineId,
                tracking.sellerSku(),
                PurchaseExceptionType.DAMAGED,
                BigDecimal.ZERO,
                BigDecimal.valueOf(damagedQuantity),
                "damaged items: " + reason);
        int newDamaged = tracking.damagedQuantity() + damagedQuantity;
        int newPending =
                Math.max(0, tracking.orderedQuantity() - tracking.receivedQuantity() - newDamaged - tracking.returnedQuantity());
        PurchaseTracking updated = new PurchaseTracking(
                tracking.trackingId(),
                tracking.tenantId(),
                tracking.poId(),
                tracking.lineId(),
                tracking.sellerSku(),
                tracking.orderedQuantity(),
                tracking.receivedQuantity(),
                newPending,
                newDamaged,
                tracking.returnedQuantity(),
                tracking.orderedUnitCost(),
                tracking.actualUnitCost(),
                resolveStatus(
                        tracking.orderedQuantity(),
                        tracking.receivedQuantity(),
                        newDamaged,
                        tracking.returnedQuantity(),
                        newPending),
                tracking.lastReceivedAt(),
                tracking.createdAt(),
                Instant.now());
        return scmExtStore.saveTracking(updated);
    }

    /** 登记供应商退回数量，回写跟单未结数量。 */
    @Transactional
    public PurchaseTracking recordReturn(String tenantId, String poId, String lineId, int returnedQuantity, String reason) {
        PurchaseTracking tracking = scmExtStore.findTracking(tenantId, poId, lineId)
                .orElseThrow(() -> new BizException("TRACKING_NOT_FOUND", "purchase tracking not found"));
        int newReceived = Math.max(0, tracking.receivedQuantity() - returnedQuantity);
        int newReturned = tracking.returnedQuantity() + returnedQuantity;
        int newPending =
                Math.max(0, tracking.orderedQuantity() - newReceived - tracking.damagedQuantity() - newReturned);
        PurchaseTracking updated = new PurchaseTracking(
                tracking.trackingId(),
                tracking.tenantId(),
                tracking.poId(),
                tracking.lineId(),
                tracking.sellerSku(),
                tracking.orderedQuantity(),
                newReceived,
                newPending,
                tracking.damagedQuantity(),
                newReturned,
                tracking.orderedUnitCost(),
                tracking.actualUnitCost(),
                resolveStatus(
                        tracking.orderedQuantity(),
                        newReceived,
                        tracking.damagedQuantity(),
                        newReturned,
                        newPending),
                tracking.lastReceivedAt(),
                tracking.createdAt(),
                Instant.now());
        return scmExtStore.saveTracking(updated);
    }

    /** 创建采购异常记录，供采购跟单、审批和后续处理闭环使用。 */
    public PurchaseException createException(
            String tenantId,
            String poId,
            String lineId,
            String sellerSku,
            PurchaseExceptionType type,
            BigDecimal expectedValue,
            BigDecimal actualValue,
            String description) {
        PurchaseException exception = new PurchaseException(
                UUID.randomUUID().toString(),
                tenantId,
                poId,
                lineId,
                sellerSku,
                type,
                expectedValue,
                actualValue,
                description,
                PurchaseExceptionStatus.PENDING,
                null,
                null,
                Instant.now(),
                null);
        return scmExtStore.saveException(exception);
    }

    /**
     * 处理采购异常。
     * 当前已落地规则:
     * - 少收/缺货在确认处理后，将采购跟单行按异常结案，避免无限待收。
     */
    @Transactional
    public PurchaseException handleException(
            String tenantId,
            String exceptionId,
            String handlerId,
            String handlerNote,
            PurchaseExceptionStatus resolution) {
        PurchaseException exception = scmExtStore.findException(tenantId, exceptionId)
                .orElseThrow(() -> new BizException("EXCEPTION_NOT_FOUND", "purchase exception not found"));
        if (exception.status() != PurchaseExceptionStatus.PENDING && exception.status() != PurchaseExceptionStatus.PROCESSING) {
            throw new BizException("EXCEPTION_STATUS_INVALID", "purchase exception already handled");
        }
        PurchaseException updated = new PurchaseException(
                exception.exceptionId(),
                exception.tenantId(),
                exception.poId(),
                exception.lineId(),
                exception.sellerSku(),
                exception.exceptionType(),
                exception.expectedValue(),
                exception.actualValue(),
                exception.description(),
                resolution,
                handlerId,
                handlerNote,
                exception.createdAt(),
                Instant.now());
        PurchaseException saved = scmExtStore.saveException(updated);
        if (resolution == PurchaseExceptionStatus.RESOLVED) {
            applyResolvedExceptionToTracking(saved);
        }
        return saved;
    }

    public List<PurchaseTracking> listTrackings(String tenantId, String poId) {
        return scmExtStore.listTrackings(tenantId, poId);
    }

    public List<PurchaseException> listExceptions(String tenantId, String poId) {
        return scmExtStore.listExceptions(tenantId, poId);
    }

    public List<PurchaseException> listPendingExceptions(String tenantId) {
        return scmExtStore.listPendingExceptions(tenantId);
    }

    public boolean isTrackingComplete(String tenantId, String poId) {
        List<PurchaseTracking> trackings = scmExtStore.listTrackings(tenantId, poId);
        return trackings.stream().allMatch(PurchaseTracking::isComplete);
    }

    /** 将已解决的少收/缺货异常同步到跟单状态，形成采购执行闭环。 */
    private void applyResolvedExceptionToTracking(PurchaseException exception) {
        if (exception.lineId() == null || exception.lineId().isBlank()) {
            return;
        }
        if (exception.exceptionType() != PurchaseExceptionType.UNDER_DELIVERED
                && exception.exceptionType() != PurchaseExceptionType.MISSING_ITEM) {
            return;
        }
        scmExtStore.findTracking(exception.tenantId(), exception.poId(), exception.lineId()).ifPresent(tracking -> {
            PurchaseTracking updated = new PurchaseTracking(
                    tracking.trackingId(),
                    tracking.tenantId(),
                    tracking.poId(),
                    tracking.lineId(),
                    tracking.sellerSku(),
                    tracking.orderedQuantity(),
                    tracking.receivedQuantity(),
                    0,
                    tracking.damagedQuantity(),
                    tracking.returnedQuantity(),
                    tracking.orderedUnitCost(),
                    tracking.actualUnitCost(),
                    PurchaseTrackingStatus.CLOSED_WITH_EXCEPTION,
                    tracking.lastReceivedAt(),
                    tracking.createdAt(),
                    Instant.now());
            scmExtStore.saveTracking(updated);
        });
    }

    /** 统一根据收货、不良、退回和待收数量推导采购跟单状态。 */
    private PurchaseTrackingStatus resolveStatus(
            int orderedQuantity, int receivedQuantity, int damagedQuantity, int returnedQuantity, int pendingQuantity) {
        if (receivedQuantity >= orderedQuantity) {
            return PurchaseTrackingStatus.FULLY_RECEIVED;
        }
        if (pendingQuantity == 0 || receivedQuantity + damagedQuantity + returnedQuantity >= orderedQuantity) {
            return PurchaseTrackingStatus.CLOSED_WITH_EXCEPTION;
        }
        return receivedQuantity > 0 || damagedQuantity > 0 || returnedQuantity > 0
                ? PurchaseTrackingStatus.PARTIALLY_RECEIVED
                : PurchaseTrackingStatus.PENDING_RECEIPT;
    }
}
