package com.aidotnet.erp.scm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.domain.PurchaseException;
import com.aidotnet.erp.scm.domain.PurchaseExceptionStatus;
import com.aidotnet.erp.scm.domain.PurchaseExceptionType;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseTracking;
import com.aidotnet.erp.scm.domain.PurchaseTrackingStatus;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import com.aidotnet.erp.scm.infrastructure.ScmExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseTrackingService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseTrackingService.class);

    private final ScmExtStore scmExtStore;
    private final PurchaseStore purchaseStore;

    public PurchaseTrackingService(ScmExtStore scmExtStore, PurchaseStore purchaseStore) {
        this.scmExtStore = scmExtStore;
        this.purchaseStore = purchaseStore;
    }

    @Transactional
    public PurchaseTracking initTracking(String tenantId, String poId) {
        PurchaseOrder po = purchaseStore.findPurchaseOrder(tenantId, poId)
                .orElseThrow(() -> new BizException("PO_NOT_FOUND", "采购单不存在"));
        for (PurchaseOrderLine line : po.lines()) {
            scmExtStore.findTracking(tenantId, poId, line.lineId()).ifPresent(existing -> {
                throw new BizException("TRACKING_ALREADY_EXISTS", "采购跟单已存在: " + line.lineId());
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
                .orElseThrow(() -> new BizException("TRACKING_INIT_FAILED", "跟单初始化失败"));
    }

    @Transactional
    public PurchaseTracking recordReceipt(String tenantId, String poId, String lineId,
                                          int receivedQuantity, BigDecimal actualUnitCost) {
        PurchaseTracking tracking = scmExtStore.findTracking(tenantId, poId, lineId)
                .orElseThrow(() -> new BizException("TRACKING_NOT_FOUND", "采购跟单不存在"));
        int newReceived = tracking.receivedQuantity() + receivedQuantity;
        if (newReceived > tracking.orderedQuantity() + (int) (tracking.orderedQuantity() * 0.1)) {
            createException(tenantId, poId, lineId, tracking.sellerSku(),
                    PurchaseExceptionType.OVER_DELIVERED,
                    BigDecimal.valueOf(tracking.orderedQuantity()),
                    BigDecimal.valueOf(newReceived),
                    "收货数量超出订单数量10%以上");
        }
        int newPending = Math.max(0, tracking.orderedQuantity() - newReceived);
        PurchaseTrackingStatus newStatus;
        if (newReceived >= tracking.orderedQuantity()) {
            newStatus = PurchaseTrackingStatus.FULLY_RECEIVED;
        } else {
            newStatus = PurchaseTrackingStatus.PARTIALLY_RECEIVED;
        }
        if (actualUnitCost != null && actualUnitCost.compareTo(tracking.orderedUnitCost()) > 0) {
            BigDecimal increaseRate = actualUnitCost.subtract(tracking.orderedUnitCost())
                    .divide(tracking.orderedUnitCost(), 4, BigDecimal.ROUND_HALF_UP);
            if (increaseRate.compareTo(new BigDecimal("0.05")) > 0) {
                createException(tenantId, poId, lineId, tracking.sellerSku(),
                        PurchaseExceptionType.PRICE_INCREASED,
                        tracking.orderedUnitCost(),
                        actualUnitCost,
                        "采购单价上涨超过5%");
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

    @Transactional
    public PurchaseTracking recordDamage(String tenantId, String poId, String lineId,
                                         int damagedQuantity, String reason) {
        PurchaseTracking tracking = scmExtStore.findTracking(tenantId, poId, lineId)
                .orElseThrow(() -> new BizException("TRACKING_NOT_FOUND", "采购跟单不存在"));
        createException(tenantId, poId, lineId, tracking.sellerSku(),
                PurchaseExceptionType.DAMAGED,
                BigDecimal.ZERO,
                BigDecimal.valueOf(damagedQuantity),
                "损坏: " + reason);
        PurchaseTracking updated = new PurchaseTracking(
                tracking.trackingId(),
                tracking.tenantId(),
                tracking.poId(),
                tracking.lineId(),
                tracking.sellerSku(),
                tracking.orderedQuantity(),
                tracking.receivedQuantity(),
                tracking.pendingQuantity(),
                tracking.damagedQuantity() + damagedQuantity,
                tracking.returnedQuantity(),
                tracking.orderedUnitCost(),
                tracking.actualUnitCost(),
                tracking.status(),
                tracking.lastReceivedAt(),
                tracking.createdAt(),
                Instant.now());
        return scmExtStore.saveTracking(updated);
    }

    @Transactional
    public PurchaseTracking recordReturn(String tenantId, String poId, String lineId,
                                         int returnedQuantity, String reason) {
        PurchaseTracking tracking = scmExtStore.findTracking(tenantId, poId, lineId)
                .orElseThrow(() -> new BizException("TRACKING_NOT_FOUND", "采购跟单不存在"));
        PurchaseTracking updated = new PurchaseTracking(
                tracking.trackingId(),
                tracking.tenantId(),
                tracking.poId(),
                tracking.lineId(),
                tracking.sellerSku(),
                tracking.orderedQuantity(),
                tracking.receivedQuantity() - returnedQuantity,
                tracking.pendingQuantity() + returnedQuantity,
                tracking.damagedQuantity(),
                tracking.returnedQuantity() + returnedQuantity,
                tracking.orderedUnitCost(),
                tracking.actualUnitCost(),
                tracking.status(),
                tracking.lastReceivedAt(),
                tracking.createdAt(),
                Instant.now());
        return scmExtStore.saveTracking(updated);
    }

    public PurchaseException createException(String tenantId, String poId, String lineId,
                                             String sellerSku, PurchaseExceptionType type,
                                             BigDecimal expectedValue, BigDecimal actualValue,
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

    @Transactional
    public PurchaseException handleException(String tenantId, String exceptionId,
                                             String handlerId, String handlerNote,
                                             PurchaseExceptionStatus resolution) {
        PurchaseException exception = scmExtStore.findException(tenantId, exceptionId)
                .orElseThrow(() -> new BizException("EXCEPTION_NOT_FOUND", "采购异常不存在"));
        if (exception.status() != PurchaseExceptionStatus.PENDING && exception.status() != PurchaseExceptionStatus.PROCESSING) {
            throw new BizException("EXCEPTION_STATUS_INVALID", "异常已处理");
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
        return scmExtStore.saveException(updated);
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
}
