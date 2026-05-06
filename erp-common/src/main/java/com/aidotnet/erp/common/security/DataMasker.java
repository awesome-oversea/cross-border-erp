package com.aidotnet.erp.common.security;

public final class DataMasker {

    private DataMasker() {
    }

    public static String email(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int at = value.indexOf('@');
        if (at <= 1) {
            return "***" + value.substring(Math.max(at, 0));
        }
        return value.charAt(0) + "***" + value.substring(at);
    }

    public static String phone(String value) {
        if (value == null || value.length() < 7) {
            return "***";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }
}
