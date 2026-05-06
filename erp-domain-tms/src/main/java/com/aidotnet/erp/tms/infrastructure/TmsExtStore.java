package com.aidotnet.erp.tms.infrastructure;

import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.LogisticsStrategy;
import com.aidotnet.erp.tms.domain.ShippingBatch;
import com.aidotnet.erp.tms.domain.ShippingBatch.ShippingBatchStatus;
import com.aidotnet.erp.tms.domain.ShippingRate;
import com.aidotnet.erp.tms.infrastructure.data.CarrierDO;
import com.aidotnet.erp.tms.infrastructure.data.LogisticsStrategyDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingBatchDO;
import com.aidotnet.erp.tms.infrastructure.data.ShippingRateDO;
import com.aidotnet.erp.tms.infrastructure.mapper.TmsExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class TmsExtStore {

    private final TmsExtMapper mapper;
    private final ObjectMapper objectMapper;

    public TmsExtStore(TmsExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public ShippingBatch saveShippingBatch(ShippingBatch batch) {
        ShippingBatchDO existing = mapper.selectShippingBatch(batch.tenantId(), batch.batchId());
        ShippingBatchDO data = toData(batch);
        if (existing == null) {
            mapper.insertShippingBatch(data);
        } else {
            mapper.updateShippingBatch(data);
        }
        return batch;
    }

    public Optional<ShippingBatch> findShippingBatch(String tenantId, String batchId) {
        return Optional.ofNullable(mapper.selectShippingBatch(tenantId, batchId)).map(this::toDomain);
    }

    public List<ShippingBatch> listShippingBatches(String tenantId, String carrierId) {
        return mapper.selectShippingBatches(tenantId, carrierId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private ShippingBatchDO toData(ShippingBatch b) {
        ShippingBatchDO data = new ShippingBatchDO();
        data.setBatchId(b.batchId());
        data.setTenantId(b.tenantId());
        data.setCarrierId(b.carrierId());
        data.setShipmentIds(toJson(b.shipmentIds()));
        data.setStatus(b.status().name());
        data.setCreatedAt(b.createdAt() != null ? b.createdAt() : Instant.now());
        data.setUpdatedAt(b.updatedAt() != null ? b.updatedAt() : Instant.now());
        return data;
    }

    private ShippingBatch toDomain(ShippingBatchDO d) {
        List<String> shipmentIds = fromJson(d.getShipmentIds());
        return new ShippingBatch(d.getBatchId(), d.getTenantId(), d.getCarrierId(), shipmentIds,
                ShippingBatchStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private <T> T fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return (T) List.of();
        }
    }

    public LogisticsStrategy saveLogisticsStrategy(LogisticsStrategy strategy) {
        LogisticsStrategyDO existing = mapper.selectLogisticsStrategy(strategy.tenantId(), strategy.strategyId());
        LogisticsStrategyDO data = toLogisticsStrategyData(strategy);
        if (existing == null) {
            mapper.insertLogisticsStrategy(data);
        } else {
            mapper.updateLogisticsStrategy(data);
        }
        return strategy;
    }

    public Optional<LogisticsStrategy> findLogisticsStrategy(String tenantId, String strategyId) {
        return Optional.ofNullable(mapper.selectLogisticsStrategy(tenantId, strategyId)).map(this::toLogisticsStrategyDomain);
    }

    public List<LogisticsStrategy> listLogisticsStrategies(String tenantId, String strategyType) {
        return mapper.selectLogisticsStrategies(tenantId, strategyType).stream().map(this::toLogisticsStrategyDomain).collect(Collectors.toList());
    }

    public List<Carrier> listCarriers(String tenantId) {
        return mapper.selectCarriers(tenantId).stream().map(this::toCarrierDomain).collect(Collectors.toList());
    }

    public List<ShippingRate> listShippingRates(String tenantId, String originCountry, String destinationCountry) {
        return mapper.selectShippingRatesByRoute(tenantId, originCountry, destinationCountry).stream().map(this::toShippingRateDomain).collect(Collectors.toList());
    }

    private LogisticsStrategyDO toLogisticsStrategyData(LogisticsStrategy s) {
        LogisticsStrategyDO data = new LogisticsStrategyDO();
        data.setStrategyId(s.strategyId());
        data.setTenantId(s.tenantId());
        data.setStrategyName(s.strategyName());
        data.setStrategyType(s.strategyType());
        data.setOriginCountry(s.originCountry());
        data.setDestinationCountry(s.destinationCountry());
        data.setPreferredCarrier(s.preferredCarrier());
        data.setRules(s.rules());
        data.setEnabled(s.enabled());
        data.setPriority(s.priority());
        data.setCreatedAt(s.createdAt());
        data.setUpdatedAt(s.updatedAt());
        return data;
    }

    private LogisticsStrategy toLogisticsStrategyDomain(LogisticsStrategyDO d) {
        return new LogisticsStrategy(d.getStrategyId(), d.getTenantId(), d.getStrategyName(), d.getStrategyType(),
                d.getOriginCountry(), d.getDestinationCountry(), d.getPreferredCarrier(), d.getRules(),
                d.isEnabled(), d.getPriority(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private Carrier toCarrierDomain(CarrierDO d) {
        return new Carrier(d.getCarrierId(), d.getTenantId(), d.getCode(), d.getName(),
                d.getCountryCode(), d.getType(), "ACTIVE", null, null, true,
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ShippingRate toShippingRateDomain(ShippingRateDO d) {
        return new ShippingRate(d.getRateId(), d.getTenantId(), d.getMethodId(),
                d.getOrigin(), d.getDestination(),
                BigDecimal.ZERO, BigDecimal.valueOf(99999), d.getRate(), "USD",
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
