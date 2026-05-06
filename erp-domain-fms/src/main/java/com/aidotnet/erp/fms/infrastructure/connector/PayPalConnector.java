package com.aidotnet.erp.fms.infrastructure.connector;

import com.aidotnet.erp.common.connector.PaymentConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayPalConnector implements PaymentConnector {

    private static final Logger log = LoggerFactory.getLogger(PayPalConnector.class);
    private static final String CODE = "PAYPAL";
    private static final String NAME = "PayPal";

    @Override
    public String getPaymentCode() { return CODE; }

    @Override
    public String getPaymentName() { return NAME; }

    @Override
    public Map<String, Object> createPayment(Map<String, Object> paymentParams) {
        log.info("Creating PayPal payment");
        String currency = (String) paymentParams.getOrDefault("currency", "USD");
        Object amount = paymentParams.get("amount");
        if (amount == null) {
            throw new BizException("PAYPAL_PARAM_MISSING", "amount不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", "paypal_" + System.currentTimeMillis());
        result.put("status", "CREATED");
        result.put("currency", currency);
        result.put("amount", amount);
        result.put("approveUrl", "https://www.paypal.com/checkout?token=mock");
        return result;
    }

    @Override
    public Map<String, Object> queryPayment(String paymentId) {
        log.info("Querying PayPal payment={}", paymentId);
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", paymentId);
        result.put("status", "COMPLETED");
        return result;
    }

    @Override
    public Map<String, Object> refund(String paymentId, Map<String, Object> refundParams) {
        log.info("Refunding PayPal payment={} amount={}", paymentId, refundParams.get("amount"));
        Map<String, Object> result = new HashMap<>();
        result.put("refundId", "paypal_refund_" + System.currentTimeMillis());
        result.put("paymentId", paymentId);
        result.put("status", "REFUNDED");
        result.put("amount", refundParams.get("amount"));
        return result;
    }

    @Override
    public Map<String, Object> getBalance(String accountId) {
        log.info("Getting PayPal balance for account={}", accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", accountId);
        result.put("balances", java.util.List.of(
                Map.of("currency", "USD", "amount", "0.00"),
                Map.of("currency", "EUR", "amount", "0.00")));
        return result;
    }

    @Override
    public Map<String, Object> reconcile(Map<String, String> params) {
        log.info("Reconciling PayPal transactions from={} to={}", params.get("start_date"), params.get("end_date"));
        Map<String, Object> result = new HashMap<>();
        result.put("transactions", java.util.List.of());
        result.put("totalCount", 0);
        return result;
    }
}
