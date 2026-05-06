package com.aidotnet.erp.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aidotnet.erp.app.ErpApplication;
import com.aidotnet.erp.common.approval.ApprovalStatus;
import com.aidotnet.erp.common.approval.PersistentApprovalService;
import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.idempotency.PersistentIdempotencyService;
import com.aidotnet.erp.common.integration.PersistentIntegrationRegistry;
import com.aidotnet.erp.common.job.PersistentJobScheduler;
import com.aidotnet.erp.common.message.PlatformMessageProperties;
import com.aidotnet.erp.common.notify.PersistentNotificationService;
import com.aidotnet.erp.common.persistence.entity.OutboxEventEntity;
import com.aidotnet.erp.common.persistence.mapper.NotificationRecordMapper;
import com.aidotnet.erp.common.persistence.mapper.OutboxEventMapper;
import com.aidotnet.erp.common.translation.PersistentTranslationService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = ErpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.stream.default-binder=test",
                "spring.cloud.stream.binders.test.type=test"
        }
)
@ActiveProfiles("test")
class PersistenceAndMessagingTests {

    @Autowired
    private PersistentIdempotencyService idempotencyService;
    @Autowired
    private PersistentApprovalService approvalService;
    @Autowired
    private PersistentJobScheduler jobScheduler;
    @Autowired
    private PersistentNotificationService notificationService;
    @Autowired
    private PersistentTranslationService translationService;
    @Autowired
    private PersistentIntegrationRegistry integrationRegistry;
    @Autowired
    private DomainEventPublisher domainEventPublisher;
    @Autowired
    private PlatformMessageProperties messageProperties;
    @Autowired
    private NotificationRecordMapper notificationRecordMapper;
    @Autowired
    private OutboxEventMapper outboxEventMapper;

    @Test
    void mybatisPlusServicesPersistPlatformCapabilities() {
        AtomicInteger counter = new AtomicInteger();
        String idemKey = "idem-" + UUID.randomUUID();
        String first = idempotencyService.execute("tenant-a", idemKey, "body-a", String.class,
                () -> "created-" + counter.incrementAndGet());
        String second = idempotencyService.execute("tenant-a", idemKey, "body-a", String.class,
                () -> "created-" + counter.incrementAndGet());

        var approval = approvalService.start("tenant-a", "PURCHASE_ORDER", "po-1", "buyer");
        var approved = approvalService.approve("tenant-a", approval.approvalId(), "manager", "ok");

        String jobCode = "sync-orders-" + UUID.randomUUID();
        jobScheduler.register(jobCode, "0 */5 * * * ?");
        var log = jobScheduler.trigger(jobCode, counter::incrementAndGet);

        var notification = notificationService.send("tenant-a", "WEBHOOK", "ops", "Order ${orderNo} failed",
                Map.of("orderNo", "A001"));
        var translation = translationService.translate("tenant-a", "en", "zh-CN", "wireless mouse");
        var connector = integrationRegistry.registerConnector("tenant-a", "amazon-" + UUID.randomUUID(),
                "Amazon SP-API");
        var client = integrationRegistry.createApiClient("tenant-a", "pms-" + UUID.randomUUID());
        integrationRegistry.authenticate("tenant-a", client.clientCode(), client.plainSecretOnce());

        assertThat(first).isEqualTo("created-1");
        assertThat(second).isEqualTo(first);
        assertThat(counter).hasValue(2);
        assertThat(approved.status()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(approved.history()).hasSize(2);
        assertThat(log.success()).isTrue();
        assertThat(jobScheduler.logs(jobCode)).hasSize(1);
        assertThat(notification.content()).isEqualTo("Order A001 failed");
        assertThat(notificationRecordMapper.selectCount(null)).isPositive();
        assertThat(translation.translatedText()).isEqualTo("[zh-CN] wireless mouse");
        assertThat(connector.enabled()).isTrue();
        assertThat(client.secretHash()).isNotEqualTo(client.plainSecretOnce());
        assertThatThrownBy(() -> integrationRegistry.authenticate("tenant-a", client.clientCode(), "wrong"))
                .isInstanceOf(BizException.class);
    }

    @Test
    void springCloudStreamRoutesEventsToKafkaAndRocketMqBindingsAndPersistsOutbox() {
        assertThat(messageProperties.bindingFor("OMS.ORDER_CREATED")).isEqualTo("erpKafkaOutbound-out-0");
        assertThat(messageProperties.bindingFor("WMS.INVENTORY_CHANGED")).isEqualTo("erpRocketOutbound-out-0");

        DomainEvent event = new TestDomainEvent("evt-" + UUID.randomUUID(), "tenant-a", "trace-a",
                "WMS.INVENTORY_CHANGED", "sku-1", Instant.now());
        domainEventPublisher.publish(event);

        OutboxEventEntity stored = outboxEventMapper.selectById(outboxEventMapper.selectCount(null));
        assertThat(stored).isNotNull();
        assertThat(stored.getEventType()).isEqualTo("WMS.INVENTORY_CHANGED");
        assertThat(stored.getStatus()).isEqualTo("PUBLISHED");
    }

    record TestDomainEvent(String eventId, String tenantId, String traceId, String eventType, String aggregateId,
                           Instant occurredAt) implements DomainEvent {
    }
}
