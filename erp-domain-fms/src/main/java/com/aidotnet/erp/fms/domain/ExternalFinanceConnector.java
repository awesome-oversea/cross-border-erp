package com.aidotnet.erp.fms.domain;

public interface ExternalFinanceConnector {

    String getFinanceSystem();

    String getFinanceSystemName();

    boolean testConnection(String apiUrl, String apiKey, String apiSecret, String accountSet);

    String pushVoucher(String apiUrl, String apiKey, String apiSecret, String accountSet,
                       ExternalFinanceVoucher voucher);

    ExternalFinanceVoucher queryVoucher(String apiUrl, String apiKey, String apiSecret,
                                         String accountSet, String voucherNumber);

    boolean cancelVoucher(String apiUrl, String apiKey, String apiSecret,
                           String accountSet, String voucherNumber);
}
