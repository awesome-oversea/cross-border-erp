package com.aidotnet.erp.fms.infrastructure.connector;

import com.aidotnet.erp.common.connector.PaymentConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StripeConnector implements PaymentConnector {

    private static final Logger log = LoggerFactory.getLogger(StripeConnector.class);
    private static final String CODE = "STRIPE";
    private static final String NAME = "Stripe";

    @Override
    public String getPaymentCode() { return CODE; }

    @Override
    public String getPaymentName() { return NAME; }

    @Override
    public Map<String, Object> createPayment(Map<String, Object> paymentParams) {
        log.info("Creating Stripe payment intent");
        Object amount = paymentParams.get("amount");
        String currency = (String) paymentParams.getOrDefault("currency", "usd");
        if (amount == null) {
            throw new BizException("STRIPE_PARAM_MISSING", "amount不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("paymentIntentId", "pi_" + System.currentTimeMillis());
        result.put("status", "requires_payment_method");
        result.put("currency", currency);
        result.put("amount", amount);
        result.put("clientSecret", "pi_mock_secret");
        return result;
    }

    @Override
    public Map<String, Object> queryPayment(String paymentId) {
        log.info("Querying Stripe payment={}", paymentId);
        Map<String, Object> result = new HashMap<>();
        result.put("paymentIntentId", paymentId);
        result.put("status", "succeeded");
        return result;
    }

    @Override
    public Map<String, Object> refund(String paymentId, Map<String, Object> refundParams) {
        log.info("Refunding Stripe payment={} amount={}", paymentId, refundParams.get("amount"));
        Map<String, Object> result = new HashMap<>();
        result.put("refundId", "re_" + System.currentTimeMillis());
        result.put("paymentIntentId", paymentId);
        result.put("status", "succeeded");
        result.put("amount", refundParams.get("amount"));
        return result;
    }

    @Override
    public Map<String, Object> getBalance(String accountId) {
        log.info("Getting Stripe balance for account={}", accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", accountId);
        result.put("available", java.util.List.of(Map.of("currency", "usd", "amount", "0")));
        result.put("pending", java.util.List.of(Map.of("currency", "usd", "amount", "0")));
        return result;
    }

    @Override
    public Map<String, Object> reconcile(Map<String, String> params) {
        log.info("Reconciling Stripe payouts from={} to={}", params.get("start_date"), params.get("end_date"));
        Map<String, Object> result = new HashMap<>();
        result.put("payouts", java.util.List.of());
        result.put("balanceTransactions", java.util.List.of());
        return result;
    }
}
