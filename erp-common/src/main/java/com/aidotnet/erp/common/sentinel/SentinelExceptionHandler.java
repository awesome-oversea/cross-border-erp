package com.aidotnet.erp.common.sentinel;

import com.aidotnet.erp.common.api.Result;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SentinelExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SentinelExceptionHandler.class);

    @ExceptionHandler(FlowException.class)
    public ResponseEntity<Result<Void>> handleFlowException(FlowException e) {
        log.warn("Sentinel flow limit triggered: resource={}", e.getRule().getResource());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Result.fail("RATE_LIMITED", "系统繁忙，请稍后重试"));
    }

    @ExceptionHandler(DegradeException.class)
    public ResponseEntity<Result<Void>> handleDegradeException(DegradeException e) {
        log.warn("Sentinel degrade triggered: resource={}", e.getRule().getResource());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Result.fail("SERVICE_DEGRADED", "服务暂时不可用，请稍后重试"));
    }

    @ExceptionHandler(SystemBlockException.class)
    public ResponseEntity<Result<Void>> handleSystemBlockException(SystemBlockException e) {
        log.warn("Sentinel system protection triggered: resource={}", e.getResourceName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Result.fail("SYSTEM_PROTECTED", "系统负载过高，请稍后重试"));
    }

    @ExceptionHandler(BlockException.class)
    public ResponseEntity<Result<Void>> handleBlockException(BlockException e) {
        log.warn("Sentinel block triggered: rule={}", e.getRule());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Result.fail("BLOCKED", "请求被限流，请稍后重试"));
    }
}
