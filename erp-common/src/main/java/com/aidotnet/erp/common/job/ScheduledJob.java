package com.aidotnet.erp.common.job;

public record ScheduledJob(String jobCode, String cronExpression, boolean enabled) {

    public ScheduledJob withEnabled(boolean enabled) {
        return new ScheduledJob(jobCode, cronExpression, enabled);
    }
}
