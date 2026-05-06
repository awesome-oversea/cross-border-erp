package com.aidotnet.erp.common.job;

import com.aidotnet.erp.common.exception.BizException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryJobScheduler {

    private final Map<String, ScheduledJob> jobs = new ConcurrentHashMap<>();
    private final List<JobExecutionLog> logs = new ArrayList<>();

    public ScheduledJob register(String jobCode, String cronExpression) {
        ScheduledJob job = new ScheduledJob(jobCode, cronExpression, true);
        jobs.put(jobCode, job);
        return job;
    }

    public ScheduledJob enable(String jobCode) {
        ScheduledJob job = mustGet(jobCode).withEnabled(true);
        jobs.put(jobCode, job);
        return job;
    }

    public ScheduledJob disable(String jobCode) {
        ScheduledJob job = mustGet(jobCode).withEnabled(false);
        jobs.put(jobCode, job);
        return job;
    }

    public JobExecutionLog trigger(String jobCode, Runnable task) {
        ScheduledJob job = mustGet(jobCode);
        if (!job.enabled()) {
            throw new BizException("JOB_DISABLED", "scheduled job is disabled");
        }
        try {
            task.run();
            JobExecutionLog log = new JobExecutionLog(jobCode, true, null, Instant.now());
            logs.add(log);
            return log;
        } catch (RuntimeException ex) {
            JobExecutionLog log = new JobExecutionLog(jobCode, false, ex.getMessage(), Instant.now());
            logs.add(log);
            throw ex;
        }
    }

    public List<JobExecutionLog> logs() {
        return List.copyOf(logs);
    }

    private ScheduledJob mustGet(String jobCode) {
        ScheduledJob job = jobs.get(jobCode);
        if (job == null) {
            throw new BizException("NOT_FOUND", "scheduled job not found");
        }
        return job;
    }
}
