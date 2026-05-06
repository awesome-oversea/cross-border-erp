package com.aidotnet.erp.common.event;

import java.util.Locale;

public final class DomainEventNames {

    private static final String VERSION = "v1";

    private DomainEventNames() {}

    public static String v1(String domain, String aggregate, String event) {
        return String.join(".", "erp", normalize(domain), normalize(aggregate), normalize(event), VERSION);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("domain event name segment must not be blank");
        }
        return value.trim().replace('_', '-').toLowerCase(Locale.ROOT);
    }
}
