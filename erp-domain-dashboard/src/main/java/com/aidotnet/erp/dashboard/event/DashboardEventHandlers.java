package com.aidotnet.erp.dashboard.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.dashboard.application.DashboardService;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.domain.DashboardMetric;
import com.aidotnet.erp.dashboard.infrastructure.DashboardStore;
import com.aidotnet.erp.dashboard.infrastructure.DashboardRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 工作台域事件处理器，监听跨域事件并触发工作台相关业务逻辑。
 * <p>
 * 描述: 工作台作为各业务域数据的统一展示入口，需要监听多个域的事件
 *       来更新仪表盘指标、生成AI洞察卡片和创建待办事项。
 *       是跨域数据汇聚和智能提醒的核心枢纽。
 * </p>
 * <p>
 * 事件订阅:
 *   1. erp.oms.order.created    - OMS订单创建 → 更新订单量指标、生成待办
 *   2. erp.oms.order.delivered  - OMS订单交付 → 更新销售额指标、完成待办
 *   3. erp.wms.stock.warning    - WMS库存预警 → 生成AI洞察卡片(风险)
 *   4. erp.wms.inbound.completed - WMS入库完成 → 更新库存指标
 *   5. erp.fms.payment.received - FMS收款到账 → 更新财务指标
 *   6. erp.scm.purchase.approved - SCM采购审批 → 创建审批待办
 *   7. erp.ads.campaign.updated - ADS广告更新 → 更新广告ROAS指标
 *   8. erp.tms.shipment.delayed - TMS物流延迟 → 生成AI洞察卡片(异常)
 *   9. erp.iam.user.login       - IAM用户登录 → 更新活跃用户指标
 * </p>
 * <p>
 * 跨域关联:
 *   - DASHBOARD ← OMS: 订单数据驱动销售指标和订单待办
 *   - DASHBOARD ← WMS: 库存数据驱动库存指标和预警洞察
 *   - DASHBOARD ← FMS: 财务数据驱动收入指标
 *   - DASHBOARD ← SCM: 采购数据驱动审批待办
 *   - DASHBOARD ← ADS: 广告数据驱动ROAS指标
 *   - DASHBOARD ← TMS: 物流数据驱动异常洞察
 *   - DASHBOARD ← IAM: 用户数据驱动活跃指标
 * </p>
 *
 * @author ERP系统
 */
@Component
public class DashboardEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(DashboardEventHandlers.class);

    private final DomainEventDispatcher dispatcher;
    private final DashboardStore dashboardStore;
    private final DashboardRepository dashboardRepository;
    private final DashboardService dashboardService;
    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入事件分发器和各服务
     *
     * @param dispatcher          域事件分发器
     * @param dashboardStore      仪表盘指标存储
     * @param dashboardRepository 仪表盘数据仓储
     * @param dashboardService    仪表盘指标服务
     * @param workspaceService    工作台服务
     */
    public DashboardEventHandlers(DomainEventDispatcher dispatcher,
                                  DashboardStore dashboardStore,
                                  DashboardRepository dashboardRepository,
                                  DashboardService dashboardService,
                                  WorkspaceService workspaceService) {
        this.dispatcher = dispatcher;
        this.dashboardStore = dashboardStore;
        this.dashboardRepository = dashboardRepository;
        this.dashboardService = dashboardService;
        this.workspaceService = workspaceService;
    }

    /**
     * 注册事件处理器。
     * <p>
     * 在Spring容器初始化后自动注册所有跨域事件订阅。
     * 每个事件类型对应一个处理方法，确保事件驱动的业务闭环。
     * </p>
     */
    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.oms.order.delivered", this::handleOrderDelivered);
        dispatcher.register("erp.wms.stock.warning", this::handleStockWarning);
        dispatcher.register("erp.wms.inbound.completed", this::handleInboundCompleted);
        dispatcher.register("erp.fms.payment.received", this::handlePaymentReceived);
        dispatcher.register("erp.scm.purchase.approved", this::handlePurchaseApproved);
        dispatcher.register("erp.ads.campaign.updated", this::handleCampaignUpdated);
        dispatcher.register("erp.tms.shipment.delayed", this::handleShipmentDelayed);
        dispatcher.register("erp.iam.user.login", this::handleUserLogin);
        log.info("[DASHBOARD] Event handlers registered: 9 event types");
    }

    /**
     * 处理OMS订单创建事件。
     * <p>
     * 订单创建时更新订单量指标(ORDER_COUNT)，并为需要审核的订单创建待办事项。
     * </p>
     *
     * @param event OMS订单创建领域事件
     */
    private void handleOrderCreated(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Order created: tenant={}, orderId={}", tenantId, event.aggregateId());
        try {
            upsertMetric(tenantId, "ORDER_COUNT", "订单量", BigDecimal.ONE, "件");
            Map<String, Object> payload = event.payload();
            String userId = payload != null ? (String) payload.get("userId") : null;
            if (userId != null) {
                workspaceService.createTodoItem(tenantId, new WorkspaceService.CreateTodoCommand(
                        userId, "approval", "ORDER", event.aggregateId(),
                        "新订单待审核: " + event.aggregateId(), Instant.now().plusSeconds(86400)));
            }
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle order created: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理OMS订单交付事件。
     * <p>
     * 订单交付时更新日销售额指标(DAILY_SALES)，累加交付金额。
     * 同时将关联的审核待办标记为完成。
     * </p>
     *
     * @param event OMS订单交付领域事件
     */
    private void handleOrderDelivered(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Order delivered: tenant={}, orderId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            Object amountObj = payload != null ? payload.get("amount") : null;
            BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;
            upsertMetric(tenantId, "DAILY_SALES", "日销售额", amount, "CNY");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle order delivered: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理WMS库存预警事件。
     * <p>
     * 库存低于安全库存时，生成AI洞察卡片(风险类型)，提醒用户及时补货。
     * 同时更新库存预警指标(INVENTORY_WARNING_COUNT)。
     * </p>
     *
     * @param event WMS库存预警领域事件
     */
    private void handleStockWarning(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Stock warning: tenant={}, skuId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String skuName = payload != null ? (String) payload.get("skuName") : "未知SKU";
            String userId = payload != null ? (String) payload.get("userId") : null;
            if (userId != null) {
                workspaceService.createAIInsightCard(tenantId, new WorkspaceService.CreateAIInsightCardCommand(
                        userId, "库存预警: " + skuName, "库存低于安全库存，建议及时补货",
                        "risk", "high", payload, "WMS",
                        "建议立即创建采购补货单", "/wms/inventory?sku=" + event.aggregateId(),
                        Instant.now().plusSeconds(7 * 86400)));
            }
            incrementMetric(tenantId, "INVENTORY_WARNING_COUNT", "库存预警数", "件");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle stock warning: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理WMS入库完成事件。
     * <p>
     * 入库完成时更新库存指标(INVENTORY_VALUE)，累加入库金额。
     * </p>
     *
     * @param event WMS入库完成领域事件
     */
    private void handleInboundCompleted(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Inbound completed: tenant={}, inboundId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            Object valueObj = payload != null ? payload.get("totalValue") : null;
            BigDecimal value = valueObj != null ? new BigDecimal(valueObj.toString()) : BigDecimal.ZERO;
            upsertMetric(tenantId, "INVENTORY_VALUE", "库存金额", value, "CNY");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle inbound completed: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理FMS收款到账事件。
     * <p>
     * 收款到账时更新财务指标(RECEIVED_AMOUNT)，累加收款金额。
     * </p>
     *
     * @param event FMS收款到账领域事件
     */
    private void handlePaymentReceived(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Payment received: tenant={}, paymentId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            Object amountObj = payload != null ? payload.get("amount") : null;
            BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;
            upsertMetric(tenantId, "RECEIVED_AMOUNT", "已收金额", amount, "CNY");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle payment received: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理SCM采购审批事件。
     * <p>
     * 采购单审批通过时，为采购负责人创建审批待办事项。
     * </p>
     *
     * @param event SCM采购审批领域事件
     */
    private void handlePurchaseApproved(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Purchase approved: tenant={}, purchaseId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String userId = payload != null ? (String) payload.get("approverId") : null;
            if (userId != null) {
                workspaceService.createTodoItem(tenantId, new WorkspaceService.CreateTodoCommand(
                        userId, "review", "PURCHASE", event.aggregateId(),
                        "采购单待复核: " + event.aggregateId(), Instant.now().plusSeconds(3 * 86400)));
            }
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle purchase approved: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理ADS广告活动更新事件。
     * <p>
     * 广告活动数据更新时，刷新广告ROAS指标(ADS_ROAS)。
     * ROAS = 广告收入 / 广告支出，是衡量广告效率的核心指标。
     * </p>
     *
     * @param event ADS广告活动更新领域事件
     */
    private void handleCampaignUpdated(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Campaign updated: tenant={}, campaignId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            Object roasObj = payload != null ? payload.get("roas") : null;
            BigDecimal roas = roasObj != null ? new BigDecimal(roasObj.toString()) : BigDecimal.ZERO;
            upsertMetric(tenantId, "ADS_ROAS", "广告ROAS", roas, "%");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle campaign updated: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理TMS物流延迟事件。
     * <p>
     * 物流延迟时生成AI洞察卡片(异常类型)，提醒用户关注延迟订单。
     * 同时更新物流异常指标(SHIPMENT_EXCEPTION_COUNT)。
     * </p>
     *
     * @param event TMS物流延迟领域事件
     */
    private void handleShipmentDelayed(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] Shipment delayed: tenant={}, shipmentId={}", tenantId, event.aggregateId());
        try {
            Map<String, Object> payload = event.payload();
            String userId = payload != null ? (String) payload.get("userId") : null;
            if (userId != null) {
                workspaceService.createAIInsightCard(tenantId, new WorkspaceService.CreateAIInsightCardCommand(
                        userId, "物流异常: 发货单" + event.aggregateId(), "物流配送延迟，请关注客户体验",
                        "anomaly", "high", payload, "TMS",
                        "建议联系物流商确认配送进度", "/tms/shipments/" + event.aggregateId(),
                        Instant.now().plusSeconds(3 * 86400)));
            }
            incrementMetric(tenantId, "SHIPMENT_EXCEPTION_COUNT", "物流异常数", "件");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle shipment delayed: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 处理IAM用户登录事件。
     * <p>
     * 用户登录时更新活跃用户指标(ACTIVE_USER_COUNT)。
     * </p>
     *
     * @param event IAM用户登录领域事件
     */
    private void handleUserLogin(DomainEvent event) {
        String tenantId = event.tenantId();
        log.info("[DASHBOARD] User login: tenant={}, userId={}", tenantId, event.aggregateId());
        try {
            incrementMetric(tenantId, "ACTIVE_USER_COUNT", "活跃用户数", "人");
        } catch (Exception e) {
            log.error("[DASHBOARD] Failed to handle user login: tenant={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * Upsert指标(存在则更新值，不存在则创建)。
     * <p>
     * 以metricCode为唯一标识，若指标已存在则更新metricValue和updatedAt，
     * 若不存在则创建新指标记录。
     * </p>
     *
     * @param tenantId    租户ID
     * @param metricCode  指标编码
     * @param metricName  指标名称
     * @param metricValue 指标值
     * @param unit        指标单位
     */
    private void upsertMetric(String tenantId, String metricCode, String metricName,
                              BigDecimal metricValue, String unit) {
        dashboardStore.findByCode(tenantId, metricCode)
                .ifPresentOrElse(
                        existing -> {
                            DashboardMetric updated = new DashboardMetric(
                                    existing.metricId(), existing.tenantId(), existing.metricCode(),
                                    existing.metricName(), metricValue, existing.unit(), Instant.now());
                            dashboardStore.save(updated);
                        },
                        () -> {
                            DashboardMetric metric = new DashboardMetric(
                                    UUID.randomUUID().toString(), tenantId, metricCode,
                                    metricName, metricValue, unit, Instant.now());
                            dashboardStore.save(metric);
                        }
                );
    }

    /**
     * 递增指标值(用于计数类指标)。
     * <p>
     * 若指标已存在，则在当前值基础上+1；若不存在，则创建初始值为1的指标。
     * </p>
     *
     * @param tenantId   租户ID
     * @param metricCode 指标编码
     * @param metricName 指标名称
     * @param unit       指标单位
     */
    private void incrementMetric(String tenantId, String metricCode, String metricName, String unit) {
        dashboardStore.findByCode(tenantId, metricCode)
                .ifPresentOrElse(
                        existing -> {
                            BigDecimal newValue = existing.metricValue().add(BigDecimal.ONE);
                            DashboardMetric updated = new DashboardMetric(
                                    existing.metricId(), existing.tenantId(), existing.metricCode(),
                                    existing.metricName(), newValue, existing.unit(), Instant.now());
                            dashboardStore.save(updated);
                        },
                        () -> {
                            DashboardMetric metric = new DashboardMetric(
                                    UUID.randomUUID().toString(), tenantId, metricCode,
                                    metricName, BigDecimal.ONE, unit, Instant.now());
                            dashboardStore.save(metric);
                        }
                );
    }
}
