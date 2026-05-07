package com.aidotnet.erp.ads.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.ads.infrastructure.AdsExtStore;
import com.aidotnet.erp.ads.infrastructure.CampaignRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 广告域事件处理器，监听跨域事件并触发广告域相关业务逻辑。
 * <p>
 * 描述: 订阅PDM产品创建事件和OMS订单交付事件，
 *       分别触发自动广告活动建议和ROAS计算。
 * </p>
 * <p>
 * 事件订阅:
 *   1. erp.pdm.product.created - PDM产品创建 → 自动广告活动建议
 *   2. erp.oms.order.delivered - OMS订单交付 → ROAS计算触发
 * </p>
 * <p>
 * 跨域关联:
 *   - ADS ↔ PDM: 新产品上架时自动建议创建广告活动
 *   - ADS ↔ OMS: 订单交付后触发广告ROAS(广告投资回报率)重新计算
 * </p>
 *
 * @author ERP系统
 */
@Component
public class AdsEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(AdsEventHandlers.class);

    private final DomainEventDispatcher dispatcher;
    private final CampaignRepository campaignRepository;
    private final AdsExtStore adsExtStore;

    public AdsEventHandlers(DomainEventDispatcher dispatcher, CampaignRepository campaignRepository, AdsExtStore adsExtStore) {
        this.dispatcher = dispatcher;
        this.campaignRepository = campaignRepository;
        this.adsExtStore = adsExtStore;
    }

    /**
     * 注册事件处理器。
     * <p>
     * 在Spring容器初始化后自动注册所有事件订阅。
     * </p>
     */
    @PostConstruct
    public void register() {
        dispatcher.register("erp.pdm.product.created", this::handleProductCreated);
        dispatcher.register("erp.oms.order.delivered", this::handleOrderDelivered);
    }

    /**
     * 处理PDM产品创建事件。
     * <p>
     * 当PDM域有新产品上架时，自动建议为该产品创建广告活动。
     * 后续可扩展为自动创建DRAFT状态广告活动。
     * </p>
     *
     * @param event PDM产品创建领域事件
     */
    private void handleProductCreated(DomainEvent event) {
        log.info("[ADS] Product created - auto-campaign suggestion: tenant={}, productId={}",
                event.tenantId(), event.aggregateId());
    }

    /**
     * 处理OMS订单交付事件。
     * <p>
     * 当OMS域有订单交付时，触发广告ROAS(广告投资回报率)重新计算。
     * 后续可扩展为自动更新关联广告活动的效果数据。
     * </p>
     *
     * @param event OMS订单交付领域事件
     */
    private void handleOrderDelivered(DomainEvent event) {
        log.info("[ADS] Order delivered - ROAS calculation trigger: tenant={}, orderId={}",
                event.tenantId(), event.aggregateId());
    }
}
