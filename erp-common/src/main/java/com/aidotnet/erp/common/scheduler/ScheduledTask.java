package com.aidotnet.erp.common.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledTask {

    String name();

    String cron() default "";

    long fixedDelayMs() default 0;

    String description() default "";
}
