package com.aidotnet.erp.som.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.som.domain.ChannelSku;
import com.aidotnet.erp.som.domain.ChannelSkuStatus;
import com.aidotnet.erp.som.infrastructure.SomRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SOM渠道SKU映射测试")
class ChannelSkuMappingServiceTest {

    @Test
    @DisplayName("同店铺同渠道同SellerSKU重复创建时应拦截")
    void shouldRejectDuplicateActiveChannelSkuMappingInSameScope() {
        SomRepository repository = mock(SomRepository.class);
        when(repository.findChannelSkuByExternalSku("T1", "AMAZON", "STORE-1", "US", "SELLER-SKU-1"))
                .thenReturn(Optional.of(mapping("MAP-1", "SKU-1", "AMAZON", "SELLER-SKU-1",
                        "STORE-1", "US", ChannelSkuStatus.ACTIVE)));
        ListingService service = new ListingService(repository, mock(DomainEventPublisher.class));

        BizException ex = assertThrows(BizException.class, () -> service.createChannelSku(
                "T1",
                new ListingService.CreateChannelSkuCommand("SKU-1", "AMAZON", "SELLER-SKU-1", "STORE-1", "US")));

        assertEquals("CHANNEL_SKU_DUPLICATED", ex.getCode());
    }

    @Test
    @DisplayName("同店铺同渠道同SellerSKU已绑定其他内部SKU时应拦截冲突")
    void shouldRejectConflictingActiveChannelSkuBinding() {
        SomRepository repository = mock(SomRepository.class);
        when(repository.findChannelSkuByExternalSku("T1", "AMAZON", "STORE-1", "US", "SELLER-SKU-1"))
                .thenReturn(Optional.of(mapping("MAP-2", "SKU-OTHER", "AMAZON", "SELLER-SKU-1",
                        "STORE-1", "US", ChannelSkuStatus.ACTIVE)));
        ListingService service = new ListingService(repository, mock(DomainEventPublisher.class));

        BizException ex = assertThrows(BizException.class, () -> service.createChannelSku(
                "T1",
                new ListingService.CreateChannelSkuCommand("SKU-1", "AMAZON", "SELLER-SKU-1", "STORE-1", "US")));

        assertEquals("CHANNEL_SKU_CONFLICT", ex.getCode());
    }

    @Test
    @DisplayName("已停用映射再次建立时应重启原映射")
    void shouldReactivateInactiveMapping() {
        SomRepository repository = mock(SomRepository.class);
        when(repository.findChannelSkuByExternalSku("T1", "AMAZON", "STORE-1", "US", "SELLER-SKU-1"))
                .thenReturn(Optional.of(mapping("MAP-3", "SKU-1", "AMAZON", "SELLER-SKU-1",
                        "STORE-1", "US", ChannelSkuStatus.INACTIVE)));
        when(repository.saveChannelSku(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ListingService service = new ListingService(repository, mock(DomainEventPublisher.class));

        ChannelSku result = service.createChannelSku(
                "T1",
                new ListingService.CreateChannelSkuCommand("SKU-1", "AMAZON", "SELLER-SKU-1", "STORE-1", "US"));

        assertEquals("MAP-3", result.channelSkuId());
        assertEquals(ChannelSkuStatus.ACTIVE, result.status());
    }

    @Test
    @DisplayName("解除映射应将渠道SKU状态置为INACTIVE")
    void shouldDeactivateChannelSkuMapping() {
        SomRepository repository = mock(SomRepository.class);
        when(repository.findChannelSku("T1", "MAP-4"))
                .thenReturn(Optional.of(mapping("MAP-4", "SKU-1", "AMAZON", "SELLER-SKU-2",
                        "STORE-1", "US", ChannelSkuStatus.ACTIVE)));
        when(repository.saveChannelSku(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ListingService service = new ListingService(repository, mock(DomainEventPublisher.class));

        ChannelSku result = service.deactivateChannelSku("T1", "MAP-4");

        assertEquals(ChannelSkuStatus.INACTIVE, result.status());
    }

    private ChannelSku mapping(String mappingId, String productSku, String channel, String channelSku,
                               String storeId, String marketplaceId, ChannelSkuStatus status) {
        return new ChannelSku(
                mappingId,
                "T1",
                productSku,
                channel,
                channelSku,
                storeId,
                marketplaceId,
                status,
                Instant.now(),
                Instant.now());
    }
}
