package com.aidotnet.erp.common.api;

import java.time.Instant;

public record Result<T>(boolean success, String code, String message, T data, Instant timestamp) {

    private static final String SUCCESS_CODE = "SUCCESS";

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, SUCCESS_CODE, "success", data, Instant.now());
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(false, code, message, null, Instant.now());
    }
}
