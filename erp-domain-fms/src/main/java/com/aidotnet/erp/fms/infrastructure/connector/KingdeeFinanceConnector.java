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
public class KingdeeFinanceConnector implements ExternalFinanceConnector {

    private static final Logger log = LoggerFactory.getLogger(KingdeeFinanceConnector.class);
    private static final String FINANCE_SYSTEM = "KINGDEE";
    private static final String FINANCE_SYSTEM_NAME = "金蝶云星空";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public KingdeeFinanceConnector() {
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
        log.info("Testing Kingdee connection: url={}", apiUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/k3cloud/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.ExecuteBillQuery.common.kdsvc"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"FormId\":\"BD_Account\",\"TopRowCount\":1}"))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Kingdee connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String pushVoucher(String apiUrl, String apiKey, String apiSecret, String accountSet,
                               ExternalFinanceVoucher voucher) {
        log.info("Pushing voucher to Kingdee: tenant={} ref={}", voucher.tenantId(), voucher.erpReferenceId());
        try {
            Map<String, Object> payload = Map.of(
                    "FormId", "GL_VOUCHER",
                    "Data", Map.of(
                            "VOUCHERDATE", voucher.voucherData().getOrDefault("date", ""),
                            "VOUCHERNUMBER", voucher.voucherNumber(),
                            "ENTRIES", voucher.voucherData().getOrDefault("entries", "")
                    ));
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/k3cloud/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Save.common.kdsvc"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BizException("KINGDEE_PUSH_FAILED", "金蝶推送凭证失败: " + response.statusCode());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return String.valueOf(result.getOrDefault("Number", voucher.voucherNumber()));
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("KINGDEE_REQUEST_ERROR", "金蝶请求异常: " + e.getMessage());
        }
    }

    @Override
    public ExternalFinanceVoucher queryVoucher(String apiUrl, String apiKey, String apiSecret,
                                                String accountSet, String voucherNumber) {
        log.info("Querying Kingdee voucher: number={}", voucherNumber);
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "FormId", "GL_VOUCHER",
                    "FilterString", "FVOUCHERNUMBER='" + voucherNumber + "'"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/k3cloud/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.ExecuteBillQuery.common.kdsvc"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return new ExternalFinanceVoucher(
                    UUID.randomUUID().toString(), "", null, FINANCE_SYSTEM, "GL_VOUCHER",
                    voucherNumber, "", "", result, "SYNCED", null, Instant.now(), Instant.now(), Instant.now());
        } catch (Exception e) {
            throw new BizException("KINGDEE_QUERY_ERROR", "金蝶查询凭证异常: " + e.getMessage());
        }
    }

    @Override
    public boolean cancelVoucher(String apiUrl, String apiKey, String apiSecret,
                                  String accountSet, String voucherNumber) {
        log.info("Cancelling Kingdee voucher: number={}", voucherNumber);
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "FormId", "GL_VOUCHER",
                    "Data", Map.of("Id", voucherNumber, "IsDeleteEntry", true)));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/k3cloud/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Delete.common.kdsvc"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 400;
        } catch (Exception e) {
            throw new BizException("KINGDEE_CANCEL_ERROR", "金蝶删除凭证异常: " + e.getMessage());
        }
    }
}
