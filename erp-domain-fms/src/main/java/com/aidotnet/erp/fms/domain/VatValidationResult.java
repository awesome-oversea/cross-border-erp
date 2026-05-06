package com.aidotnet.erp.fms.domain;

public record VatValidationResult(String vatNumber, String countryCode, boolean valid,
                                  String name, String address, String serviceProvider,
                                  java.time.Instant validatedAt) {}
