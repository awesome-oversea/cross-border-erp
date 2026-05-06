package com.aidotnet.erp.sys.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.sys.application.BusinessAlertService;
import com.aidotnet.erp.sys.application.PmsIntegrationService;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 系统设置域事件处理器，监听跨域事件并触发SYS域相关业务逻辑。
 * <p>
 * 描述: SYS域作为系统配置中枢和PMS集成桥梁，需要监听各域事件来：
 *       1. 自动生成业务预警(库存不足、物流延迟、财务异常等)
 *       2. 触发PMS智能推荐反馈(执行结果回传)
 *       3. 记录跨域操作日志(关键业务操作审计)
 *       4. 更新AI功能开关状态(根据业务指标自动调整)
 * </p>
 * <p>
 * 事件订阅:
 *   1. erp.oms.order.cancelled     - OMS订单取消 → 生成业务预警
 *   2. erp.wms.stock.warning       - WMS库存预警 → 升级为系统级预警
 *   3. erp.fms.payment.overdue     - FMS付款逾期 → 生成财务预警
 *   4. erp.tms.shipment.delayed    - TMS物流延迟 → 生成物流预警
 *   5. erp.ads.campaign.overspend  - ADS广告超支 → 生成广告预警
 *   6. erp.scm.supplier.risk       - SCM供应商风险 → 生成供应链预警
 *   7. erp.pms.recommendation.executed - PMS建议执行完成 → 自动反馈
 * </p>
 * <p>
 * 跨域关联:
 *   - SYS ← OMS: 订单异常触发业务预警
 *   - SYS ← WMS: 库存预警升级为系统级预警
 *   - SYS ← FMS: 财务异常触发业务预警
 *   - SYS ← TMS: 物流异常触发业务预警
 *   - SYS ← ADS: 广告超支触发业务预警
 *   - SYS ← SCM: 供应商风险触发业务预警
 *   - SYS ← PMS: 建议执行结果自动反馈
 * </p>
 *
 * @author ERP系统
 */
@Component
public class SysEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(SysEventHandlers.class);

    private final DomainEventDispatcher dispatcher;
    private final SysExtStore extStore;
    private final BusinessAlertService alertService;
    private final PmsIntegrationService pmsIntegrationService;

    public SysEventHandlers(DomainEventDispatcher dispatcher,
                            SysExtStore extStore,
                            BusinessAlertService alertService,
                            PmsIntegrationService pmsIntegrationService) {
        this.dispatcher = dispatcher;
        this.extStore = extStore;
        this.alertService = alertService;
        this.pmsIntegrationService = pmsIntegrationService;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.cancelled", this::handleOrderCancelled);
        dispatcher.register("erp.wms.stock.warning", this::handleStockWarning);
        dispatcher.register("erp.fms.payment.overdue", this::handlePaymentOverdue);
        dispatcher.register("erp.tms.shipment.delayed", this::handleShipmentDelayed);
        dispatcher.register("erp.ads.campaign.overspend", this::handleCampaignOverspend);
        dispatcher.register("erp.scm.supplier.risk", this::handleSupplierRisk);
        dispatcher.register("erp.pms.recommendation.executed", this::handlePmsRecommendationExecuted);
        log.info("[SYS] Event handlers registered: 7 event types");
    }

    private void handleOrderCancelled(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] Order cancelled: tenant={}, orderId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String reason = payload != null ? (String) payload.get("reason") : "未知原因";
            alertService.createAlert(tenantId, new BusinessAlertService.CreateAlertCommand(
                    "ORDER_CANCELLED", "ORDER_CANCELLED", "medium", "OMS",
                    "订单取消预警", "订单 " + event.aggregateId() + " 已取消，原因: " + reason,
                    "ORDER", event.aggregateId()));
        } catch (Exception e) {
            log.error("[SYS] Failed to handle order cancelled: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    private void handleStockWarning(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] Stock warning escalated: tenant={}, skuId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String skuName = payload != null ? (String) payload.get("skuName") : "未知SKU";
            Object quantity = payload != null ? payload.get("quantity") : "0";
            alertService.createAlert(tenantId, new BusinessAlertService.CreateAlertCommand(
                    "STOCK_WARNING", "STOCK_WARNING", "high", "WMS",
                    "库存预警", "SKU " + skuName + " 库存不足，当前库存: " + quantity,
                    "SKU", event.aggregateId()));
        } catch (Exception e) {
            log.error("[SYS] Failed to handle stock warning: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    private void handlePaymentOverdue(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] Payment overdue: tenant={}, paymentId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            Object amount = payload != null ? payload.get("amount") : "0";
            String currency = payload != null ? (String) payload.get("currency") : "CNY";
            alertService.createAlert(tenantId, new BusinessAlertService.CreateAlertCommand(
                    "PAYMENT_OVERDUE", "PAYMENT_OVERDUE", "high", "FMS",
                    "付款逾期预警", "付款单 " + event.aggregateId() + " 已逾期，金额: " + amount + " " + currency,
                    "PAYMENT", event.aggregateId()));
        } catch (Exception e) {
            log.error("[SYS] Failed to handle payment overdue: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    private void handleShipmentDelayed(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] Shipment delayed: tenant={}, shipmentId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String carrier = payload != null ? (String) payload.get("carrier") : "未知物流商";
            Integer delayDays = payload != null && payload.get("delayDays") != null
                    ? ((Number) payload.get("delayDays")).intValue() : 0;
            alertService.createAlert(tenantId, new BusinessAlertService.CreateAlertCommand(
                    "SHIPMENT_DELAYED", "SHIPMENT_DELAYED", "medium", "TMS",
                    "物流延迟预警", "发货单 " + event.aggregateId() + " 延迟 " + delayDays + " 天，物流商: " + carrier,
                    "SHIPMENT", event.aggregateId()));
        } catch (Exception e) {
            log.error("[SYS] Failed to handle shipment delayed: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    private void handleCampaignOverspend(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] Campaign overspend: tenant={}, campaignId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            Object overspendAmount = payload != null ? payload.get("overspendAmount") : "0";
            alertService.createAlert(tenantId, new BusinessAlertService.CreateAlertCommand(
                    "CAMPAIGN_OVERSPEND", "CAMPAIGN_OVERSPEND", "high", "ADS",
                    "广告超支预警", "广告活动 " + event.aggregateId() + " 超出预算，超支金额: " + overspendAmount,
                    "CAMPAIGN", event.aggregateId()));
        } catch (Exception e) {
            log.error("[SYS] Failed to handle campaign overspend: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    private void handleSupplierRisk(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] Supplier risk: tenant={}, supplierId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String riskType = payload != null ? (String) payload.get("riskType") : "未知风险";
            String supplierName = payload != null ? (String) payload.get("supplierName") : "未知供应商";
            alertService.createAlert(tenantId, new BusinessAlertService.CreateAlertCommand(
                    "SUPPLIER_RISK", "SUPPLIER_RISK", "high", "SCM",
                    "供应商风险预警", "供应商 " + supplierName + " 存在风险: " + riskType,
                    "SUPPLIER", event.aggregateId()));
        } catch (Exception e) {
            log.error("[SYS] Failed to handle supplier risk: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    private void handlePmsRecommendationExecuted(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[SYS] PMS recommendation executed: tenant={}, recommendationId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String erpReferenceId = payload != null ? (String) payload.get("erpReferenceId") : null;
            String executionStatus = payload != null ? (String) payload.get("executionStatus") : "UNKNOWN";
            String executionResult = payload != null ? (String) payload.get("executionResult") : null;
            if (erpReferenceId != null) {
                pmsIntegrationService.sendFeedback(tenantId, erpReferenceId,
                        "EXECUTION_RESULT", executionStatus, executionResult,
                        null, null, "SYSTEM", event.traceId());
            }
        } catch (Exception e) {
            log.error("[SYS] Failed to handle PMS recommendation executed: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }
}
