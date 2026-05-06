package com.aidotnet.erp.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class InMemoryDomainEventPublisherTests {

    @Test
    void publishAndSubscribeDomainEvent() {
        InMemoryDomainEventPublisher publisher = new InMemoryDomainEventPublisher();
        ArrayList<DomainEvent> received = new ArrayList<>();
        publisher.subscribe("iam.user.created", received::add);

        DomainEvent event = new SimpleDomainEvent("evt-1", "tenant-demo", "trace-demo", "iam.user.created", "u-1", Instant.now());
        publisher.publish(event);

        assertThat(received).containsExactly(event);
        assertThat(publisher.publishedEvents()).containsExactly(event);
        assertThat(event.eventId()).isEqualTo("evt-1");
        assertThat(event.tenantId()).isEqualTo("tenant-demo");
        assertThat(event.traceId()).isEqualTo("trace-demo");
        assertThat(event.occurredAt()).isNotNull();
    }

    record SimpleDomainEvent(String eventId, String tenantId, String traceId, String eventType, String aggregateId,
                             Instant occurredAt) implements DomainEvent {
    }
}
