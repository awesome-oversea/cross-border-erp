package com.aidotnet.erp.common.job;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.persistence.entity.JobExecutionLogEntity;
import com.aidotnet.erp.common.persistence.entity.ScheduledJobEntity;
import com.aidotnet.erp.common.persistence.mapper.JobExecutionLogMapper;
import com.aidotnet.erp.common.persistence.mapper.ScheduledJobMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistentJobScheduler {

    private final ScheduledJobMapper jobMapper;
    private final JobExecutionLogMapper logMapper;

    public PersistentJobScheduler(ScheduledJobMapper jobMapper, JobExecutionLogMapper logMapper) {
        this.jobMapper = jobMapper;
        this.logMapper = logMapper;
    }

    @Transactional
    public ScheduledJob register(String jobCode, String cronExpression) {
        LocalDateTime now = LocalDateTime.now();
        ScheduledJobEntity entity = new ScheduledJobEntity();
        entity.setJobCode(jobCode);
        entity.setCronExpression(cronExpression);
        entity.setEnabled(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        jobMapper.insert(entity);
        return new ScheduledJob(jobCode, cronExpression, true);
    }

    @Transactional
    public ScheduledJob enable(String jobCode) {
        return setEnabled(jobCode, true);
    }

    @Transactional
    public ScheduledJob disable(String jobCode) {
        return setEnabled(jobCode, false);
    }

    @Transactional
    public JobExecutionLog trigger(String jobCode, Runnable task) {
        ScheduledJobEntity job = mustGet(jobCode);
        if (!Boolean.TRUE.equals(job.getEnabled())) {
            throw new BizException("JOB_DISABLED", "scheduled job is disabled");
        }
        LocalDateTime now = LocalDateTime.now();
        try {
            task.run();
            return appendLog(jobCode, true, null, now);
        } catch (RuntimeException ex) {
            appendLog(jobCode, false, ex.getMessage(), now);
            throw ex;
        }
    }

    public List<JobExecutionLog> logs(String jobCode) {
        return logMapper.selectList(new LambdaQueryWrapper<JobExecutionLogEntity>()
                        .eq(JobExecutionLogEntity::getJobCode, jobCode)
                        .orderByAsc(JobExecutionLogEntity::getId))
                .stream()
                .map(entity -> new JobExecutionLog(entity.getJobCode(), Boolean.TRUE.equals(entity.getSuccess()),
                        entity.getErrorMessage(), entity.getExecutedAt().toInstant(ZoneOffset.UTC)))
                .toList();
    }

    private ScheduledJob setEnabled(String jobCode, boolean enabled) {
        ScheduledJobEntity entity = mustGet(jobCode);
        entity.setEnabled(enabled);
        entity.setUpdatedAt(LocalDateTime.now());
        jobMapper.updateById(entity);
        return new ScheduledJob(entity.getJobCode(), entity.getCronExpression(), enabled);
    }

    private JobExecutionLog appendLog(String jobCode, boolean success, String errorMessage, LocalDateTime executedAt) {
        JobExecutionLogEntity entity = new JobExecutionLogEntity();
        entity.setJobCode(jobCode);
        entity.setSuccess(success);
        entity.setErrorMessage(errorMessage);
        entity.setExecutedAt(executedAt);
        logMapper.insert(entity);
        return new JobExecutionLog(jobCode, success, errorMessage, executedAt.toInstant(ZoneOffset.UTC));
    }

    private ScheduledJobEntity mustGet(String jobCode) {
        ScheduledJobEntity entity = jobMapper.selectOne(new LambdaQueryWrapper<ScheduledJobEntity>()
                .eq(ScheduledJobEntity::getJobCode, jobCode));
        if (entity == null) {
            throw new BizException("NOT_FOUND", "scheduled job not found");
        }
        return entity;
    }
}
