package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 采购订单领域模型
 * <p>
 * 描述: SCM域核心实体，表示向供应商发出的采购订单。包含采购行、
 *       金额、交付日期和审批信息。是采购闭环的起点。
 * </p>
 * <p>
 * 状态流转: DRAFT → SUBMITTED → APPROVED → PARTIALLY_RECEIVED → RECEIVED
 *           取消分支: DRAFT/SUBMITTED → CANCELLED
 * </p>
 *
 * @author ERP系统
 */
public record PurchaseOrder(
        String poId,
        String tenantId,
        String supplierId,
        String poNumber,
        String currency,
        BigDecimal totalAmount,
        PurchaseOrderStatus status,
        String paymentTerms,
        String shippingTerms,
        String purchaseType,
        Instant expectedDeliveryDate,
        Instant actualDeliveryDate,
        String notes,
        String createdBy,
        String approvedBy,
        List<PurchaseOrderLine> lines,
        Instant createdAt,
        Instant updatedAt
) {
    /** 计算采购订单总金额(所有行合计) */
    public BigDecimal calculateTotal() {
        return lines.stream()
                .map(PurchaseOrderLine::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 是否可审批(草稿或已提交状态) */
    public boolean canApprove() {
        return status == PurchaseOrderStatus.DRAFT || status == PurchaseOrderStatus.SUBMITTED;
    }

    /** 是否可收货(已审批或部分收货状态) */
    public boolean canReceive() {
        return status == PurchaseOrderStatus.APPROVED || status == PurchaseOrderStatus.PARTIALLY_RECEIVED;
    }

    /** 计算收货完成率 */
    public double getCompletionRate() {
        int total = lines.stream().mapToInt(PurchaseOrderLine::quantity).sum();
        int received = lines.stream().mapToInt(PurchaseOrderLine::receivedQuantity).sum();
        return total > 0 ? (double) received / total : 0.0;
    }
}
