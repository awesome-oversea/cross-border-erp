package com.aidotnet.erp.common.exception;

public class BizException extends RuntimeException {

    private final String code;

    public BizException(ErrorCode errorCode) {
        this(errorCode.name(), null);
    }

    public BizException(ErrorCode errorCode, String message) {
        this(errorCode.name(), message);
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
