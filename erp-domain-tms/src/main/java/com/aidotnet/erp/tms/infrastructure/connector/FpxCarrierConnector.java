package com.aidotnet.erp.tms.infrastructure.connector;

import com.aidotnet.erp.common.connector.CarrierConnector;
import com.aidotnet.erp.common.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FpxCarrierConnector implements CarrierConnector {

    private static final Logger log = LoggerFactory.getLogger(FpxCarrierConnector.class);
    private static final String CARRIER_CODE = "FPX";
    private static final String CARRIER_NAME = "4PX Express";
    private static final String BASE_URL = "https://open.4px.com/api/v1";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FpxCarrierConnector() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

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
        log.info("Creating 4PX shipment for store={}", storeId);
        try {
            String body = objectMapper.writeValueAsString(shipmentParams);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/shipments"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BizException("FPX_CREATE_FAILED", "4PX创建运单失败: " + response.statusCode());
            }
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return String.valueOf(result.getOrDefault("shipment_id", ""));
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("FPX_REQUEST_ERROR", "4PX请求异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTrackingInfo(String trackingNumber) {
        log.info("Fetching 4PX tracking for={}", trackingNumber);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/tracking/" + trackingNumber))
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return result;
        } catch (Exception e) {
            throw new BizException("FPX_TRACKING_ERROR", "4PX物流追踪异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> estimateFreight(Map<String, Object> freightParams) {
        log.info("Estimating 4PX freight");
        try {
            String body = objectMapper.writeValueAsString(freightParams);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/freight/estimate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return result;
        } catch (Exception e) {
            throw new BizException("FPX_FREIGHT_ERROR", "4PX运费估算异常: " + e.getMessage());
        }
    }

    @Override
    public boolean cancelShipment(String shipmentId) {
        log.info("Cancelling 4PX shipment={}", shipmentId);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/shipments/" + shipmentId + "/cancel"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 400;
        } catch (Exception e) {
            throw new BizException("FPX_CANCEL_ERROR", "4PX取消运单异常: " + e.getMessage());
        }
    }

    @Override
    public String getLabelUrl(String shipmentId) {
        log.info("Getting 4PX label for shipment={}", shipmentId);
        return BASE_URL + "/shipments/" + shipmentId + "/label";
    }
}
