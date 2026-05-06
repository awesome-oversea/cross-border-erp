package com.aidotnet.erp.fba.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fba.domain.RemovalItem;
import com.aidotnet.erp.fba.domain.RemovalOrder;
import com.aidotnet.erp.fba.domain.RemovalOrder.RemovalStatus;
import com.aidotnet.erp.fba.domain.RemovalOrder.RemovalType;
import com.aidotnet.erp.fba.infrastructure.FbaExtStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FbaRemovalService {

    private final FbaExtStore extStore;

    public FbaRemovalService(FbaExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public RemovalOrder createRemovalOrder(String tenantId, CreateRemovalCommand command) {
        if (command.removalType() == RemovalType.RETURN && command.returnAddressId() == null) {
            throw new BizException("RETURN_ADDRESS_REQUIRED", "退回类型必须指定退回地址");
        }
        Instant now = Instant.now();
        List<RemovalItem> items = command.items().stream()
                .map(i -> new RemovalItem(UUID.randomUUID().toString(), null, i.fbaSku(), i.quantity(),
                        i.disposalFee(), i.liquidationRevenue()))
                .toList();
        RemovalOrder order = new RemovalOrder(UUID.randomUUID().toString(), tenantId, command.fbaSku(),
                command.quantity(), command.removalType(), RemovalStatus.PENDING,
                command.returnAddressId(), command.reason(), items, now, now);
        return extStore.saveRemovalOrder(order);
    }

    @Transactional
    public RemovalOrder processRemoval(String tenantId, String removalId) {
        RemovalOrder order = getRemovalOrder(tenantId, removalId);
        if (order.status() != RemovalStatus.PENDING) {
            throw new BizException("REMOVAL_STATUS_INVALID", "移除单状态不允许处理");
        }
        return extStore.saveRemovalOrder(new RemovalOrder(order.removalId(), order.tenantId(), order.fbaSku(),
                order.quantity(), order.removalType(), RemovalStatus.PROCESSING,
                order.returnAddressId(), order.reason(), order.items(), order.createdAt(), Instant.now()));
    }

    @Transactional
    public RemovalOrder completeRemoval(String tenantId, String removalId) {
        RemovalOrder order = getRemovalOrder(tenantId, removalId);
        if (order.status() != RemovalStatus.PROCESSING) {
            throw new BizException("REMOVAL_STATUS_INVALID", "移除单状态不允许完成");
        }
        return extStore.saveRemovalOrder(new RemovalOrder(order.removalId(), order.tenantId(), order.fbaSku(),
                order.quantity(), order.removalType(), RemovalStatus.COMPLETED,
                order.returnAddressId(), order.reason(), order.items(), order.createdAt(), Instant.now()));
    }

    @Transactional
    public RemovalOrder cancelRemoval(String tenantId, String removalId) {
        RemovalOrder order = getRemovalOrder(tenantId, removalId);
        if (order.status() == RemovalStatus.COMPLETED) {
            throw new BizException("REMOVAL_STATUS_INVALID", "已完成的移除单不可取消");
        }
        return extStore.saveRemovalOrder(new RemovalOrder(order.removalId(), order.tenantId(), order.fbaSku(),
                order.quantity(), order.removalType(), RemovalStatus.CANCELLED,
                order.returnAddressId(), order.reason(), order.items(), order.createdAt(), Instant.now()));
    }

    public RemovalOrder getRemovalOrder(String tenantId, String removalId) {
        return extStore.findRemovalOrder(tenantId, removalId)
                .orElseThrow(() -> new BizException("REMOVAL_NOT_FOUND", "移除单不存在"));
    }

    public List<RemovalOrder> listRemovalOrders(String tenantId, RemovalStatus status) {
        return extStore.listRemovalOrders(tenantId, status);
    }

    public record CreateRemovalCommand(String fbaSku, int quantity, RemovalType removalType,
                                        String returnAddressId, String reason,
                                        List<RemovalItemCommand> items) {}
    public record RemovalItemCommand(String fbaSku, int quantity,
                                      java.math.BigDecimal disposalFee,
                                      java.math.BigDecimal liquidationRevenue) {}
}
