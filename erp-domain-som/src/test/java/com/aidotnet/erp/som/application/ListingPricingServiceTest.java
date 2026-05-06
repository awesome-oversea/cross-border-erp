package com.aidotnet.erp.som.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.som.domain.PriceRule;
import com.aidotnet.erp.som.domain.PriceRuleStatus;
import com.aidotnet.erp.som.domain.PriceRuleType;
import com.aidotnet.erp.som.infrastructure.SomRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SOM价格测算测试")
class ListingPricingServiceTest {

    @Test
    @DisplayName("应按成本佣金运费和目标利润率计算建议售价")
    void shouldCalculateSuggestedPriceFromCostStructure() {
        SomRepository repository = mock(SomRepository.class);
        when(repository.listActivePriceRules("T1")).thenReturn(List.of());
        ListingService service = new ListingService(repository, mock(DomainEventPublisher.class));

        ListingService.PriceQuote result = service.calculatePriceQuote(
                "T1",
                new ListingService.CalculatePriceCommand(
                        "AMAZON",
                        "US",
                        new BigDecimal("50"),
                        new BigDecimal("5"),
                        new BigDecimal("2"),
                        new BigDecimal("0.15"),
                        new BigDecimal("0.20")));

        assertEquals(new BigDecimal("87.69"), result.suggestedPrice());
        assertEquals(new BigDecimal("17.54"), result.estimatedProfit());
        assertEquals(new BigDecimal("0.2000"), result.estimatedProfitRate());
        assertFalse(result.lowProfit());
        assertTrue(result.appliedRules().isEmpty());
    }

    @Test
    @DisplayName("规则压低售价后应给出低利润预警")
    void shouldMarkLowProfitWhenRuleCapsSuggestedPrice() {
        SomRepository repository = mock(SomRepository.class);
        when(repository.listActivePriceRules("T1")).thenReturn(List.of(
                new PriceRule(
                        "R1",
                        "T1",
                        "US cap",
                        PriceRuleType.FIXED,
                        null,
                        "{\"maxPrice\":80}",
                        null,
                        new BigDecimal("80"),
                        PriceRuleStatus.ACTIVE,
                        Instant.now(),
                        Instant.now())));
        ListingService service = new ListingService(repository, mock(DomainEventPublisher.class));

        ListingService.PriceQuote result = service.calculatePriceQuote(
                "T1",
                new ListingService.CalculatePriceCommand(
                        "AMAZON",
                        "US",
                        new BigDecimal("50"),
                        new BigDecimal("5"),
                        new BigDecimal("2"),
                        new BigDecimal("0.15"),
                        new BigDecimal("0.20")));

        assertEquals(new BigDecimal("80.00"), result.suggestedPrice());
        assertEquals(new BigDecimal("11.00"), result.estimatedProfit());
        assertEquals(new BigDecimal("0.1375"), result.estimatedProfitRate());
        assertTrue(result.lowProfit());
        assertEquals(List.of("R1"), result.appliedRules());
    }
}
