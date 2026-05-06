package com.aidotnet.erp.fms.infrastructure.connector;

import com.aidotnet.erp.common.connector.PaymentConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PingPongConnector implements PaymentConnector {

    private static final Logger log = LoggerFactory.getLogger(PingPongConnector.class);
    private static final String CODE = "PINGPONG";
    private static final String NAME = "PingPong";

    @Override
    public String getPaymentCode() { return CODE; }

    @Override
    public String getPaymentName() { return NAME; }

    @Override
    public Map<String, Object> createPayment(Map<String, Object> paymentParams) {
        log.info("Creating PingPong payment");
        Object amount = paymentParams.get("amount");
        String currency = (String) paymentParams.getOrDefault("currency", "USD");
        if (amount == null) {
            throw new BizException("PINGPONG_PARAM_MISSING", "amount不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", "pp_" + System.currentTimeMillis());
        result.put("status", "PENDING");
        result.put("currency", currency);
        result.put("amount", amount);
        return result;
    }

    @Override
    public Map<String, Object> queryPayment(String paymentId) {
        log.info("Querying PingPong payment={}", paymentId);
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", paymentId);
        result.put("status", "SUCCESS");
        return result;
    }

    @Override
    public Map<String, Object> refund(String paymentId, Map<String, Object> refundParams) {
        log.info("Refunding PingPong payment={} amount={}", paymentId, refundParams.get("amount"));
        Map<String, Object> result = new HashMap<>();
        result.put("refundId", "pp_refund_" + System.currentTimeMillis());
        result.put("paymentId", paymentId);
        result.put("status", "SUCCESS");
        result.put("amount", refundParams.get("amount"));
        return result;
    }

    @Override
    public Map<String, Object> getBalance(String accountId) {
        log.info("Getting PingPong balance for account={}", accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", accountId);
        result.put("balances", java.util.List.of(
                Map.of("currency", "USD", "amount", "0.00"),
                Map.of("currency", "EUR", "amount", "0.00"),
                Map.of("currency", "GBP", "amount", "0.00"),
                Map.of("currency", "JPY", "amount", "0")));
        return result;
    }

    @Override
    public Map<String, Object> reconcile(Map<String, String> params) {
        log.info("Reconciling PingPong settlements from={} to={}", params.get("start_date"), params.get("end_date"));
        Map<String, Object> result = new HashMap<>();
        result.put("settlements", java.util.List.of());
        result.put("totalCount", 0);
        return result;
    }
}
