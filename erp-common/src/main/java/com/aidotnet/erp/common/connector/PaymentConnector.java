package com.aidotnet.erp.common.connector;

import java.util.Map;

public interface PaymentConnector {

    String getPaymentCode();

    String getPaymentName();

    Map<String, Object> createPayment(Map<String, Object> paymentParams);

    Map<String, Object> queryPayment(String paymentId);

    Map<String, Object> refund(String paymentId, Map<String, Object> refundParams);

    Map<String, Object> getBalance(String accountId);

    Map<String, Object> reconcile(Map<String, String> params);
}
