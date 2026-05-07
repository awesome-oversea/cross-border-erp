package com.aidotnet.erp.fms.infrastructure.connector;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.ExternalFinanceConnector;
import com.aidotnet.erp.fms.domain.ExternalFinanceVoucher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class YonyouFinanceConnector implements ExternalFinanceConnector {

    private static final Logger log = LoggerFactory.getLogger(YonyouFinanceConnector.class);
    private static final String FINANCE_SYSTEM = "YONYOU";
    private static final String FINANCE_SYSTEM_NAME = "用友U8Cloud";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YonyouFinanceConnector() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String getFinanceSystem() {
        return FINANCE_SYSTEM;
    }

    @Override
    public String getFinanceSystemName() {
        return FINANCE_SYSTEM_NAME;
    }

    @Override
    public boolean testConnection(String apiUrl, String apiKey, String apiSecret, String accountSet) {
        log.info("Testing Yonyou connection: url={}", apiUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/u8cloud/api/v1/ping"))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", apiKey)
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Yonyou connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String pushVoucher(String apiUrl, String apiKey, String apiSecret, String accountSet,
                               ExternalFinanceVoucher voucher) {
        log.info("Pushing voucher to Yonyou: tenant={} ref={}", voucher.tenantId(), voucher.erpReferenceId());
        try {
            Map<String, Object> payload = Map.of(
                    "voucherType", voucher.voucherType(),
                    "voucherNumber", voucher.voucherNumber(),
                    "accountSet", accountSet,
                    "data", voucher.voucherData());
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/u8cloud/api/v1/vouchers"))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", apiKey)
                    .header("X-API-Secret", apiSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BizException("YONYOU_PUSH_FAILED", "用友推送凭证失败: " + response.statusCode());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return String.valueOf(result.getOrDefault("voucherNumber", voucher.voucherNumber()));
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("YONYOU_REQUEST_ERROR", "用友请求异常: " + e.getMessage());
        }
    }

    @Override
    public ExternalFinanceVoucher queryVoucher(String apiUrl, String apiKey, String apiSecret,
                                                String accountSet, String voucherNumber) {
        log.info("Querying Yonyou voucher: number={}", voucherNumber);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/u8cloud/api/v1/vouchers/" + voucherNumber + "?accountSet=" + accountSet))
                    .header("X-API-Key", apiKey)
                    .header("X-API-Secret", apiSecret)
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return new ExternalFinanceVoucher(
                    UUID.randomUUID().toString(), "", null, FINANCE_SYSTEM, "VOUCHER",
                    voucherNumber, "", "", result, "SYNCED", null, Instant.now(), Instant.now(), Instant.now());
        } catch (Exception e) {
            throw new BizException("YONYOU_QUERY_ERROR", "用友查询凭证异常: " + e.getMessage());
        }
    }

    @Override
    public boolean cancelVoucher(String apiUrl, String apiKey, String apiSecret,
                                  String accountSet, String voucherNumber) {
        log.info("Cancelling Yonyou voucher: number={}", voucherNumber);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/u8cloud/api/v1/vouchers/" + voucherNumber + "/cancel?accountSet=" + accountSet))
                    .header("X-API-Key", apiKey)
                    .header("X-API-Secret", apiSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 400;
        } catch (Exception e) {
            throw new BizException("YONYOU_CANCEL_ERROR", "用友作废凭证异常: " + e.getMessage());
        }
    }
}
