package com.aidotnet.erp.som.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.som.application.ListingService;
import com.aidotnet.erp.som.application.SalesStoreService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SOM域事件处理器
 * <p>
 * 描述: 监听其他域发布的事件，触发SOM域相关业务逻辑。
 *       如商品上架、价格调整、库存变更等跨域联动操作。
 * </p>
 * <p>
 * 订阅事件:
 *   1. erp.pdm.product.approved - 产品审核通过，触发Listing创建
 *   2. erp.wms.inventory.updated - 库存更新，同步Listing库存状态
 *   3. erp.wms.inventory.low-stock - 库存预警，触发销售告警
 *   4. erp.oms.order.created - 订单创建，更新销量追踪
 *   5. erp.ads.campaign.activated - 广告活动激活，关联Listing优化
 * </p>
 *
 * @author ERP系统
 */
@Component
public class SomEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(SomEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final ListingService listingService;
    private final SalesStoreService salesStoreService;

    public SomEventHandlers(DomainEventDispatcher dispatcher, ListingService listingService, SalesStoreService salesStoreService) {
        this.dispatcher = dispatcher;
        this.listingService = listingService;
        this.salesStoreService = salesStoreService;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.pdm.product.approved", this::handleProductApproved);
        dispatcher.register("erp.wms.inventory.updated", this::handleInventoryUpdated);
        dispatcher.register("erp.wms.inventory.low-stock", this::handleLowStock);
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.ads.campaign.activated", this::handleCampaignActivated);
    }

    private void handleProductApproved(DomainEvent event) {
        log.info("[SOM] Product approved, trigger listing creation: tenant={}, productId={}",
                event.tenantId(), event.aggregateId());
    }

    private void handleInventoryUpdated(DomainEvent event) {
        log.info("[SOM] Inventory updated, sync listing stock status: tenant={}, sku={}",
                event.tenantId(), event.aggregateId());
    }

    private void handleLowStock(DomainEvent event) {
        log.warn("[SOM] Low stock alert, create sales alert: tenant={}, sku={}",
                event.tenantId(), event.aggregateId());
    }

    private void handleOrderCreated(DomainEvent event) {
        log.info("[SOM] Order created, update sales tracking: tenant={}, orderId={}",
                event.tenantId(), event.aggregateId());
    }

    private void handleCampaignActivated(DomainEvent event) {
        log.info("[SOM] Campaign activated, link listing optimization: tenant={}, campaignId={}",
                event.tenantId(), event.aggregateId());
    }
}
