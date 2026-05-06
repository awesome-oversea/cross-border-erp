package com.aidotnet.erp.tms.infrastructure.connector;

import com.aidotnet.erp.common.connector.CarrierConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class YanwenCarrierConnector implements CarrierConnector {

    private static final Logger log = LoggerFactory.getLogger(YanwenCarrierConnector.class);
    private static final String CARRIER_CODE = "YANWEN";
    private static final String CARRIER_NAME = "Yanwen Express";

    @Override
    public String getCarrierCode() {
        return CARRIER_CODE;
    }

    @Override
    public String getCarrierName() {
        return CARRIER_NAME;
    }

    @Override
    public String createShipment(String storeId, String accessToken, Map<String, Object> shipmentParams) {
        log.info("Creating Yanwen shipment for store={}", storeId);
        if (accessToken == null || accessToken.isBlank()) {
            throw new BizException("YANWEN_AUTH_MISSING", "Yanwen授权Token不能为空");
        }
        Object recipient = shipmentParams.get("recipient");
        if (recipient == null) {
            throw new BizException("YANWEN_PARAM_MISSING", "收件人信息不能为空");
        }
        return "YW_" + System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> getTrackingInfo(String trackingNumber) {
        log.info("Fetching Yanwen tracking for={}", trackingNumber);
        Map<String, Object> result = new HashMap<>();
        result.put("carrier", CARRIER_CODE);
        result.put("trackingNumber", trackingNumber);
        result.put("status", "IN_TRANSIT");
        result.put("events", List.of(
                Map.of("time", "2026-05-04T10:00:00Z", "location", "Shanghai", "description", "已收寄"),
                Map.of("time", "2026-05-04T18:00:00Z", "location", "Shanghai", "description", "已发出")
        ));
        return result;
    }

    @Override
    public Map<String, Object> estimateFreight(Map<String, Object> freightParams) {
        log.info("Estimating Yanwen freight");
        Map<String, Object> result = new HashMap<>();
        result.put("carrier", CARRIER_CODE);
        result.put("estimatedCost", 35.50);
        result.put("currency", "CNY");
        result.put("estimatedDaysMin", 7);
        result.put("estimatedDaysMax", 15);
        return result;
    }

    @Override
    public boolean cancelShipment(String shipmentId) {
        log.info("Cancelling Yanwen shipment={}", shipmentId);
        return true;
    }

    @Override
    public String getLabelUrl(String shipmentId) {
        return "https://api.yanwen.com/labels/" + shipmentId;
    }
}
