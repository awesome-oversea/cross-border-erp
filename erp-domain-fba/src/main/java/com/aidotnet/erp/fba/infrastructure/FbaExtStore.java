package com.aidotnet.erp.fba.infrastructure;

import com.aidotnet.erp.fba.domain.RemovalItem;
import com.aidotnet.erp.fba.domain.RemovalOrder;
import com.aidotnet.erp.fba.domain.RemovalOrder.RemovalStatus;
import com.aidotnet.erp.fba.infrastructure.data.RemovalOrderDO;
import com.aidotnet.erp.fba.infrastructure.mapper.FbaExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class FbaExtStore {

    private final FbaExtMapper mapper;
    private final ObjectMapper objectMapper;

    public FbaExtStore(FbaExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public RemovalOrder saveRemovalOrder(RemovalOrder order) {
        RemovalOrderDO existing = mapper.selectRemovalOrder(order.tenantId(), order.removalId());
        RemovalOrderDO data = toData(order);
        if (existing == null) {
            mapper.insertRemovalOrder(data);
        } else {
            mapper.updateRemovalOrder(data);
        }
        return order;
    }

    public Optional<RemovalOrder> findRemovalOrder(String tenantId, String removalId) {
        return Optional.ofNullable(mapper.selectRemovalOrder(tenantId, removalId)).map(this::toDomain);
    }

    public List<RemovalOrder> listRemovalOrders(String tenantId, RemovalStatus status) {
        return mapper.selectRemovalOrders(tenantId, status != null ? status.name() : null).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private RemovalOrderDO toData(RemovalOrder o) {
        RemovalOrderDO data = new RemovalOrderDO();
        data.setRemovalId(o.removalId());
        data.setTenantId(o.tenantId());
        data.setFbaSku(o.fbaSku());
        data.setQuantity(o.quantity());
        data.setRemovalType(o.removalType().name());
        data.setStatus(o.status().name());
        data.setReturnAddressId(o.returnAddressId());
        data.setReason(o.reason());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setUpdatedAt(o.updatedAt() != null ? o.updatedAt() : Instant.now());
        return data;
    }

    private RemovalOrder toDomain(RemovalOrderDO d) {
        return new RemovalOrder(d.getRemovalId(), d.getTenantId(), d.getFbaSku(), d.getQuantity(),
                RemovalOrder.RemovalType.valueOf(d.getRemovalType()), RemovalStatus.valueOf(d.getStatus()),
                d.getReturnAddressId(), d.getReason(), List.of(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
