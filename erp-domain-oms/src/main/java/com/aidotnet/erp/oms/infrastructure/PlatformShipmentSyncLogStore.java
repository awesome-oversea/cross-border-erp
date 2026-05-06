package com.aidotnet.erp.oms.infrastructure;

import com.aidotnet.erp.oms.domain.PlatformShipmentSyncLog;
import com.aidotnet.erp.oms.domain.PlatformShipmentSyncStatus;
import com.aidotnet.erp.oms.infrastructure.data.PlatformShipmentSyncLogDO;
import com.aidotnet.erp.oms.infrastructure.mapper.PlatformShipmentSyncLogMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PlatformShipmentSyncLogStore {

    private final PlatformShipmentSyncLogMapper mapper;

    public PlatformShipmentSyncLogStore(PlatformShipmentSyncLogMapper mapper) {
        this.mapper = mapper;
    }

    public PlatformShipmentSyncLog save(PlatformShipmentSyncLog log) {
        mapper.insert(toData(log));
        return log;
    }

    public List<PlatformShipmentSyncLog> listByOrderId(String tenantId, String orderId) {
        return mapper.selectByOrderId(tenantId, orderId).stream()
                .map(this::toDomain)
                .toList();
    }

    private PlatformShipmentSyncLogDO toData(PlatformShipmentSyncLog log) {
        PlatformShipmentSyncLogDO data = new PlatformShipmentSyncLogDO();
        data.setLogId(log.logId());
        data.setTenantId(log.tenantId());
        data.setOrderId(log.orderId());
        data.setPackageId(log.packageId());
        data.setPlatform(log.platform());
        data.setPlatformOrderNo(log.platformOrderNo());
        data.setTrackingNo(log.trackingNo());
        data.setStatus(log.status().name());
        data.setAttemptNo(log.attemptNo());
        data.setErrorMessage(log.errorMessage());
        data.setSyncedAt(log.syncedAt());
        return data;
    }

    private PlatformShipmentSyncLog toDomain(PlatformShipmentSyncLogDO data) {
        return new PlatformShipmentSyncLog(
                data.getLogId(),
                data.getTenantId(),
                data.getOrderId(),
                data.getPackageId(),
                data.getPlatform(),
                data.getPlatformOrderNo(),
                data.getTrackingNo(),
                PlatformShipmentSyncStatus.valueOf(data.getStatus()),
                data.getAttemptNo() != null ? data.getAttemptNo() : 0,
                data.getErrorMessage(),
                data.getSyncedAt());
    }
}
