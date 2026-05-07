package com.aidotnet.erp.oms.infrastructure;

import com.aidotnet.erp.oms.domain.BuyerBlacklistEntry;
import com.aidotnet.erp.oms.domain.OriginalOrderSnapshot;
import com.aidotnet.erp.oms.domain.OrderLine;
import com.aidotnet.erp.oms.domain.OrderRefund;
import com.aidotnet.erp.oms.domain.OrderRiskCheck;
import com.aidotnet.erp.oms.domain.OrderStatus;
import com.aidotnet.erp.oms.domain.OrderStrategy;
import com.aidotnet.erp.oms.domain.OrderSyncLog;
import com.aidotnet.erp.oms.domain.PmsRiskAlertReviewLog;
import com.aidotnet.erp.oms.domain.PmsRiskAlert;
import com.aidotnet.erp.oms.domain.Promotion;
import com.aidotnet.erp.oms.domain.SalesOrder;
import com.aidotnet.erp.oms.infrastructure.data.BuyerBlacklistDO;
import com.aidotnet.erp.oms.infrastructure.data.OriginalOrderSnapshotDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderLineDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderRefundDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderRiskCheckDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderStrategyDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderSyncLogDO;
import com.aidotnet.erp.oms.infrastructure.data.PmsRiskAlertDO;
import com.aidotnet.erp.oms.infrastructure.data.PmsRiskAlertReviewLogDO;
import com.aidotnet.erp.oms.infrastructure.data.PromotionDO;
import com.aidotnet.erp.oms.infrastructure.mapper.OrderMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * OMS域订单数据存储
 * <p>
 * 描述: 订单域核心数据存储层，负责订单、订单行、退款、风控、策略、
 *       同步日志、黑名单、促销、PMS告警等实体的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class OrderStore {

    /** 订单数据MyBatis映射器 */
    private final OrderMapper orderMapper;

    /**
     * 构造函数 - 依赖注入映射器
     *
     * @param orderMapper 订单MyBatis映射器
     */
    public OrderStore(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /** 保存订单，存在则更新，不存在则新增 */
    public SalesOrder save(SalesOrder order) {
        OrderDO existing = orderMapper.selectById(order.orderId(), order.tenantId());
        OrderDO data = toData(order);
        if (existing != null) {
            data.setUpdatedAt(order.updatedAt());
            orderMapper.update(data);
        } else {
            data.setCreatedAt(order.createdAt());
            data.setUpdatedAt(order.updatedAt());
            orderMapper.insert(data);
        }
        orderMapper.deleteOrderLines(order.orderId());
        for (OrderLine line : order.lines()) {
            orderMapper.insertOrderLine(toLineData(order.orderId(), line));
        }
        return order;
    }

    public Optional<SalesOrder> find(String tenantId, String orderId) {
        return Optional.ofNullable(orderMapper.selectById(orderId, tenantId)).map(this::toDomain);
    }

    public Optional<SalesOrder> findByPlatformOrderNo(String tenantId, String platform, String platformOrderNo) {
        return Optional.ofNullable(orderMapper.selectByPlatformOrderNo(tenantId, platform, platformOrderNo))
                .map(this::toDomain);
    }

    public List<SalesOrder> list(String tenantId) {
        return orderMapper.selectByTenant(tenantId).stream().map(this::toDomain).toList();
    }

    public List<SalesOrder> listRecentOrdersByBuyer(String tenantId, String platform, String buyerName, Instant createdAfter) {
        return orderMapper.selectRecentOrdersByBuyer(tenantId, platform, buyerName, createdAfter).stream().map(this::toDomain).toList();
    }

    public OriginalOrderSnapshot saveOriginalOrderSnapshot(OriginalOrderSnapshot snapshot) {
        OriginalOrderSnapshotDO existing = orderMapper.selectOriginalOrderSnapshot(snapshot.tenantId(), snapshot.snapshotId());
        OriginalOrderSnapshotDO data = toOriginalOrderSnapshotData(snapshot);
        if (existing == null) {
            orderMapper.insertOriginalOrderSnapshot(data);
        } else {
            orderMapper.updateOriginalOrderSnapshot(data);
        }
        return snapshot;
    }

    public OrderRefund saveRefund(OrderRefund refund) {
        OrderRefundDO existing = orderMapper.selectRefundById(refund.tenantId(), refund.refundId());
        OrderRefundDO data = toRefundData(refund);
        if (existing == null) {
            orderMapper.insertRefund(data);
        } else {
            orderMapper.updateRefund(data);
        }
        return refund;
    }

    public Optional<OrderRefund> findRefund(String tenantId, String refundId) {
        return Optional.ofNullable(orderMapper.selectRefundById(tenantId, refundId)).map(this::toRefundDomain);
    }

    public List<OrderRefund> listRefunds(String tenantId, String orderId) {
        return orderMapper.selectRefunds(tenantId, orderId).stream().map(this::toRefundDomain).toList();
    }

    public OrderRiskCheck saveRiskCheck(OrderRiskCheck check) {
        orderMapper.insertRiskCheck(toRiskCheckData(check));
        return check;
    }

    public List<OrderRiskCheck> listRiskChecks(String tenantId, String orderId) {
        return orderMapper.selectRiskChecks(tenantId, orderId).stream().map(this::toRiskCheckDomain).toList();
    }

    public BuyerBlacklistEntry saveBuyerBlacklist(BuyerBlacklistEntry entry) {
        orderMapper.insertBuyerBlacklist(toBuyerBlacklistData(entry));
        return entry;
    }

    public Optional<BuyerBlacklistEntry> findBuyerBlacklist(String tenantId, String entryId) {
        return Optional.ofNullable(orderMapper.selectBuyerBlacklist(tenantId, entryId)).map(this::toBuyerBlacklistDomain);
    }

    public List<BuyerBlacklistEntry> listBuyerBlacklist(String tenantId) {
        return orderMapper.selectBuyerBlacklistByTenant(tenantId).stream().map(this::toBuyerBlacklistDomain).toList();
    }

    public void deleteBuyerBlacklist(String tenantId, String entryId) {
        orderMapper.deleteBuyerBlacklist(tenantId, entryId);
    }

    public Promotion savePromotion(Promotion promotion) {
        orderMapper.insertPromotion(toPromotionData(promotion));
        return promotion;
    }

    public List<Promotion> listPromotions(String tenantId, String orderId) {
        return orderMapper.selectPromotions(tenantId, orderId).stream().map(this::toPromotionDomain).toList();
    }

    public PmsRiskAlert savePmsRiskAlert(PmsRiskAlert alert) {
        PmsRiskAlertDO existing = orderMapper.selectPmsRiskAlertByIdempotencyKey(alert.idempotencyKey());
        if (existing != null) {
            return toPmsRiskAlertDomain(existing);
        }
        orderMapper.insertPmsRiskAlert(toPmsRiskAlertData(alert));
        return alert;
    }

    public Optional<PmsRiskAlert> findPmsRiskAlert(String tenantId, String alertId) {
        return Optional.ofNullable(orderMapper.selectPmsRiskAlert(tenantId, alertId)).map(this::toPmsRiskAlertDomain);
    }

    public List<PmsRiskAlert> listPmsRiskAlertsByOrder(String tenantId, String orderId) {
        return orderMapper.selectPmsRiskAlertsByOrder(tenantId, orderId).stream().map(this::toPmsRiskAlertDomain).toList();
    }

    public List<PmsRiskAlert> listPendingPmsRiskAlerts(String tenantId) {
        return orderMapper.selectPendingPmsRiskAlerts(tenantId).stream().map(this::toPmsRiskAlertDomain).toList();
    }

    public void updatePmsRiskAlertStatus(String alertId, String status) {
        orderMapper.updatePmsRiskAlertStatus(alertId, status);
    }

    public PmsRiskAlertReviewLog savePmsRiskAlertReviewLog(PmsRiskAlertReviewLog reviewLog) {
        orderMapper.insertPmsRiskAlertReviewLog(toPmsRiskAlertReviewLogData(reviewLog));
        return reviewLog;
    }

    public List<PmsRiskAlertReviewLog> listPmsRiskAlertReviewLogs(String tenantId, String alertId) {
        return orderMapper.selectPmsRiskAlertReviewLogs(tenantId, alertId).stream()
                .map(this::toPmsRiskAlertReviewLogDomain)
                .toList();
    }

    public OrderSyncLog saveOrderSyncLog(OrderSyncLog syncLog) {
        orderMapper.insertOrderSyncLog(toOrderSyncLogData(syncLog));
        return syncLog;
    }

    public List<OrderSyncLog> listOrderSyncLogs(String tenantId, String platform) {
        return orderMapper.selectOrderSyncLogs(tenantId, platform).stream().map(this::toOrderSyncLogDomain).toList();
    }

    public OrderStrategy saveOrderStrategy(OrderStrategy strategy) {
        OrderStrategyDO existing = orderMapper.selectOrderStrategy(strategy.tenantId(), strategy.strategyId());
        OrderStrategyDO data = toOrderStrategyData(strategy);
        if (existing == null) {
            orderMapper.insertOrderStrategy(data);
        } else {
            orderMapper.updateOrderStrategy(data);
        }
        return strategy;
    }

    public Optional<OrderStrategy> findOrderStrategy(String tenantId, String strategyId) {
        return Optional.ofNullable(orderMapper.selectOrderStrategy(tenantId, strategyId)).map(this::toOrderStrategyDomain);
    }

    public List<OrderStrategy> listOrderStrategies(String tenantId, String strategyType) {
        return orderMapper.selectOrderStrategies(tenantId, strategyType).stream().map(this::toOrderStrategyDomain).toList();
    }

    private OrderDO toData(SalesOrder order) {
        OrderDO data = new OrderDO();
        data.setOrderId(order.orderId());
        data.setTenantId(order.tenantId());
        data.setListingId(order.listingId());
        data.setStoreId(order.storeId());
        data.setPlatform(order.platform());
        data.setMarketplace(order.marketplace());
        data.setPlatformOrderNo(order.platformOrderNo());
        data.setBuyerName(order.buyerName());
        data.setCustomerId(order.customerId());
        data.setCountryCode(order.countryCode());
        data.setShippingAddress(order.shippingAddress());
        data.setCurrency(order.currency());
        data.setTotalAmount(order.totalAmount());
        data.setTaxAmount(order.taxAmount());
        data.setShippingAmount(order.shippingAmount());
        data.setDiscountAmount(order.discountAmount());
        data.setStatus(order.status().name());
        data.setPaymentStatus(order.paymentStatus());
        data.setFulfillmentStatus(order.fulfillmentStatus());
        data.setRiskLevel(order.riskLevel());
        data.setProfitMargin(order.profitMargin());
        data.setOrderDate(order.orderDate());
        data.setCreatedAt(order.createdAt());
        data.setUpdatedAt(order.updatedAt());
        return data;
    }

    private SalesOrder toDomain(OrderDO data) {
        List<OrderLine> lines = orderMapper.selectOrderLines(data.getOrderId()).stream().map(this::toOrderLineDomain).toList();
        List<Promotion> promotions = orderMapper.selectPromotions(data.getTenantId(), data.getOrderId()).stream().map(this::toPromotionDomain).toList();
        return new SalesOrder(data.getOrderId(), data.getTenantId(), data.getListingId(), data.getStoreId(),
                data.getPlatform(), data.getMarketplace(), data.getPlatformOrderNo(), data.getBuyerName(),
                data.getCustomerId(), data.getCountryCode(), data.getShippingAddress(),
                data.getCurrency(), data.getTotalAmount(), data.getTaxAmount(),
                data.getShippingAmount(), data.getDiscountAmount(),
                OrderStatus.valueOf(data.getStatus()), data.getPaymentStatus(),
                data.getFulfillmentStatus(), data.getRiskLevel(), data.getProfitMargin(),
                lines, promotions, data.getOrderDate(), data.getCreatedAt(), data.getUpdatedAt());
    }

    private OrderLineDO toLineData(String orderId, OrderLine line) {
        OrderLineDO data = new OrderLineDO();
        data.setLineId(line.lineId());
        data.setOrderId(orderId);
        data.setProductId(line.productId());
        data.setSellerSku(line.sellerSku());
        data.setTitle(line.title());
        data.setQuantity(line.quantity());
        data.setUnitPrice(line.unitPrice());
        data.setTotalPrice(line.totalPrice());
        data.setTaxAmount(line.taxAmount());
        data.setEstimatedUnitCost(line.estimatedUnitCost());
        return data;
    }

    private OrderLine toOrderLineDomain(OrderLineDO data) {
        return new OrderLine(data.getLineId(), data.getProductId(), data.getSellerSku(), data.getTitle(),
                data.getQuantity(), data.getUnitPrice(), data.getTotalPrice(), data.getTaxAmount(),
                data.getEstimatedUnitCost());
    }

    private OriginalOrderSnapshotDO toOriginalOrderSnapshotData(OriginalOrderSnapshot snapshot) {
        OriginalOrderSnapshotDO data = new OriginalOrderSnapshotDO();
        data.setSnapshotId(snapshot.snapshotId());
        data.setTenantId(snapshot.tenantId());
        data.setPlatform(snapshot.platform());
        data.setMarketplace(snapshot.marketplace());
        data.setStoreId(snapshot.storeId());
        data.setPlatformOrderNo(snapshot.platformOrderNo());
        data.setBuyerName(snapshot.buyerName());
        data.setCountryCode(snapshot.countryCode());
        data.setShippingAddress(snapshot.shippingAddress());
        data.setCurrency(snapshot.currency());
        data.setImportMode(snapshot.importMode());
        data.setRawPayload(snapshot.rawPayload());
        data.setProcessingStatus(snapshot.processingStatus());
        data.setStandardOrderId(snapshot.standardOrderId());
        data.setDuplicateOrderId(snapshot.duplicateOrderId());
        data.setCreatedAt(snapshot.createdAt());
        data.setUpdatedAt(snapshot.updatedAt());
        return data;
    }

    private OrderRefundDO toRefundData(OrderRefund refund) {
        OrderRefundDO data = new OrderRefundDO();
        data.setRefundId(refund.refundId());
        data.setTenantId(refund.tenantId());
        data.setOrderId(refund.orderId());
        data.setReason(refund.reason());
        data.setRefundAmount(refund.refundAmount());
        data.setRefundType(refund.refundType().name());
        data.setStatus(refund.status().name());
        data.setCreatedAt(refund.createdAt());
        data.setUpdatedAt(refund.updatedAt());
        return data;
    }

    private OrderRefund toRefundDomain(OrderRefundDO data) {
        return new OrderRefund(data.getRefundId(), data.getTenantId(), data.getOrderId(), data.getReason(),
                data.getRefundAmount(), OrderRefund.RefundType.valueOf(data.getRefundType()),
                OrderRefund.RefundStatus.valueOf(data.getStatus()), data.getCreatedAt(), data.getUpdatedAt());
    }

    private OrderRiskCheckDO toRiskCheckData(OrderRiskCheck check) {
        OrderRiskCheckDO data = new OrderRiskCheckDO();
        data.setCheckId(check.checkId());
        data.setTenantId(check.tenantId());
        data.setOrderId(check.orderId());
        data.setRiskLevel(check.riskLevel().name());
        data.setRiskType(check.riskType());
        data.setDescription(check.description());
        data.setSuggestedAction(check.suggestedAction());
        data.setCheckedAt(check.checkedAt());
        return data;
    }

    private OrderRiskCheck toRiskCheckDomain(OrderRiskCheckDO data) {
        return new OrderRiskCheck(data.getCheckId(), data.getTenantId(), data.getOrderId(),
                OrderRiskCheck.RiskLevel.valueOf(data.getRiskLevel()), data.getRiskType(), data.getDescription(),
                data.getSuggestedAction(), data.getCheckedAt());
    }

    private BuyerBlacklistDO toBuyerBlacklistData(BuyerBlacklistEntry entry) {
        BuyerBlacklistDO data = new BuyerBlacklistDO();
        data.setEntryId(entry.entryId());
        data.setTenantId(entry.tenantId());
        data.setBuyerName(entry.buyerName());
        data.setReason(entry.reason());
        data.setCreatedAt(entry.createdAt());
        return data;
    }

    private BuyerBlacklistEntry toBuyerBlacklistDomain(BuyerBlacklistDO data) {
        return new BuyerBlacklistEntry(data.getEntryId(), data.getTenantId(), data.getBuyerName(), data.getReason(), data.getCreatedAt());
    }

    private PromotionDO toPromotionData(Promotion promotion) {
        PromotionDO data = new PromotionDO();
        data.setPromoId(promotion.promoId());
        data.setTenantId(promotion.tenantId());
        data.setOrderId(promotion.orderId());
        data.setPromoType(promotion.promoType());
        data.setPromoCode(promotion.promoCode());
        data.setDiscount(promotion.discount());
        data.setDescription(promotion.description());
        data.setAppliedAt(promotion.appliedAt());
        return data;
    }

    private Promotion toPromotionDomain(PromotionDO data) {
        return new Promotion(data.getPromoId(), data.getTenantId(), data.getOrderId(), data.getPromoType(),
                data.getPromoCode(), data.getDiscount(), data.getDescription(), data.getAppliedAt());
    }

    private PmsRiskAlertDO toPmsRiskAlertData(PmsRiskAlert alert) {
        PmsRiskAlertDO data = new PmsRiskAlertDO();
        data.setAlertId(alert.alertId());
        data.setTenantId(alert.tenantId());
        data.setOrderId(alert.orderId());
        data.setRiskType(alert.riskType());
        data.setRiskScore(alert.riskScore());
        data.setRiskLevel(alert.riskLevel());
        data.setDescription(alert.description());
        data.setSuggestedAction(alert.suggestedAction());
        data.setTraceId(alert.traceId());
        data.setIdempotencyKey(alert.idempotencyKey());
        data.setStatus(alert.status());
        data.setCreatedAt(alert.createdAt());
        return data;
    }

    private PmsRiskAlert toPmsRiskAlertDomain(PmsRiskAlertDO data) {
        return new PmsRiskAlert(data.getAlertId(), data.getTenantId(), data.getOrderId(), data.getRiskType(),
                data.getRiskScore(), data.getRiskLevel(), data.getDescription(), data.getSuggestedAction(),
                data.getTraceId(), data.getIdempotencyKey(), data.getStatus(), data.getCreatedAt());
    }

    private PmsRiskAlertReviewLogDO toPmsRiskAlertReviewLogData(PmsRiskAlertReviewLog reviewLog) {
        PmsRiskAlertReviewLogDO data = new PmsRiskAlertReviewLogDO();
        data.setLogId(reviewLog.logId());
        data.setTenantId(reviewLog.tenantId());
        data.setAlertId(reviewLog.alertId());
        data.setOrderId(reviewLog.orderId());
        data.setAction(reviewLog.action());
        data.setReviewerNote(reviewLog.reviewerNote());
        data.setRiskLevel(reviewLog.riskLevel());
        data.setCreatedAt(reviewLog.createdAt());
        return data;
    }

    private PmsRiskAlertReviewLog toPmsRiskAlertReviewLogDomain(PmsRiskAlertReviewLogDO data) {
        return new PmsRiskAlertReviewLog(
                data.getLogId(),
                data.getTenantId(),
                data.getAlertId(),
                data.getOrderId(),
                data.getAction(),
                data.getReviewerNote(),
                data.getRiskLevel(),
                data.getCreatedAt());
    }

    private OrderSyncLogDO toOrderSyncLogData(OrderSyncLog syncLog) {
        OrderSyncLogDO data = new OrderSyncLogDO();
        data.setSyncId(syncLog.syncId());
        data.setTenantId(syncLog.tenantId());
        data.setPlatform(syncLog.platform());
        data.setSyncType(syncLog.syncType());
        data.setStatus(syncLog.status());
        data.setSyncedCount(syncLog.syncedCount());
        data.setFailedCount(syncLog.failedCount());
        data.setErrorMessage(syncLog.errorMessage());
        data.setStartedAt(syncLog.startedAt());
        data.setCompletedAt(syncLog.completedAt());
        return data;
    }

    private OrderSyncLog toOrderSyncLogDomain(OrderSyncLogDO data) {
        return new OrderSyncLog(data.getSyncId(), data.getTenantId(), data.getPlatform(), data.getSyncType(),
                data.getStatus(), data.getSyncedCount(), data.getFailedCount(), data.getErrorMessage(),
                data.getStartedAt(), data.getCompletedAt());
    }

    private OrderStrategyDO toOrderStrategyData(OrderStrategy strategy) {
        OrderStrategyDO data = new OrderStrategyDO();
        data.setStrategyId(strategy.strategyId());
        data.setTenantId(strategy.tenantId());
        data.setStrategyType(strategy.strategyType());
        data.setName(strategy.name());
        data.setDescription(strategy.description());
        data.setRules(strategy.rules());
        data.setEnabled(strategy.enabled());
        data.setPriority(strategy.priority());
        data.setCreatedAt(strategy.createdAt());
        data.setUpdatedAt(strategy.updatedAt());
        return data;
    }

    private OrderStrategy toOrderStrategyDomain(OrderStrategyDO data) {
        return new OrderStrategy(data.getStrategyId(), data.getTenantId(), data.getStrategyType(),
                data.getName(), data.getDescription(), data.getRules(), data.getEnabled(),
                data.getPriority(), data.getCreatedAt(), data.getUpdatedAt());
    }
}
