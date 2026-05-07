package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.KpiAssessment;
import com.aidotnet.erp.bi.domain.KpiStatus;
import com.aidotnet.erp.bi.domain.KpiTarget;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KpiAssessmentService {

    private final BiExtStore extStore;

    public KpiAssessmentService(BiExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public KpiTarget createTarget(String tenantId, String kpiCode, String kpiName,
                                  String department, String role, String period,
                                  BigDecimal targetValue, BigDecimal warningValue,
                                  BigDecimal excellentValue, String unit, String metricCode,
                                  String caliberId, List<String> applicableRoles,
                                  String scoringRule, BigDecimal weight) {
        validateTargetParams(department, period, targetValue);
        validateWeight(weight);
        Instant now = Instant.now();
        KpiTarget target = new KpiTarget(
                UUID.randomUUID().toString(), tenantId, kpiCode, kpiName, department, role,
                period, targetValue, warningValue, excellentValue, unit, metricCode, caliberId,
                applicableRoles, scoringRule, weight != null ? weight : BigDecimal.TEN,
                true, now, now);
        extStore.saveKpiTarget(target);
        return target;
    }

    @Transactional
    public KpiTarget updateTarget(String tenantId, String targetId, BigDecimal targetValue,
                                  BigDecimal warningValue, BigDecimal excellentValue,
                                  String scoringRule, BigDecimal weight, Boolean enabled) {
        KpiTarget existing = getTarget(tenantId, targetId);
        BigDecimal resolvedTargetValue = targetValue != null ? targetValue : existing.targetValue();
        validateTargetParams(existing.department(), existing.period(), resolvedTargetValue);
        validateWeight(weight);
        Instant now = Instant.now();
        KpiTarget updated = new KpiTarget(
                existing.targetId(), existing.tenantId(), existing.kpiCode(),
                existing.kpiName(), existing.department(), existing.role(), existing.period(),
                resolvedTargetValue,
                warningValue != null ? warningValue : existing.warningValue(),
                excellentValue != null ? excellentValue : existing.excellentValue(),
                existing.unit(), existing.metricCode(), existing.caliberId(),
                existing.applicableRoles(),
                scoringRule != null ? scoringRule : existing.scoringRule(),
                weight != null ? weight : existing.weight(),
                enabled != null ? enabled : existing.enabled(),
                existing.createdAt(), now);
        extStore.saveKpiTarget(updated);
        return updated;
    }

    @Transactional
    public KpiAssessment assess(String tenantId, String targetId, String userId,
                                BigDecimal actualValue, String assessorId, String comment) {
        KpiTarget target = getTarget(tenantId, targetId);
        if (!target.enabled()) {
            throw new BizException("TARGET_DISABLED", "KPI目标已禁用");
        }
        BigDecimal achievementRate = calculateAchievementRate(actualValue, target.targetValue());
        BigDecimal score = calculateScore(achievementRate, target);
        KpiStatus status = determineStatus(achievementRate, target);
        KpiAssessment assessment = new KpiAssessment(
                UUID.randomUUID().toString(), tenantId, targetId, target.kpiCode(),
                target.kpiName(), target.department(), userId, target.period(),
                actualValue, target.targetValue(), achievementRate, score, status,
                assessorId, comment, Instant.now(), Instant.now());
        extStore.saveKpiAssessment(assessment);
        return assessment;
    }

    public KpiTarget getTarget(String tenantId, String targetId) {
        return extStore.findKpiTarget(tenantId, targetId)
                .orElseThrow(() -> new BizException("TARGET_NOT_FOUND", "KPI目标不存在"));
    }

    public List<KpiTarget> listTargets(String tenantId, String department, String period) {
        return extStore.listKpiTargets(tenantId, department, period);
    }

    public KpiAssessment getAssessment(String tenantId, String assessmentId) {
        return extStore.findKpiAssessment(tenantId, assessmentId)
                .orElseThrow(() -> new BizException("ASSESSMENT_NOT_FOUND", "KPI考核记录不存在"));
    }

    public List<KpiAssessment> listAssessments(String tenantId, String userId, String period) {
        return extStore.listKpiAssessments(tenantId, userId, period);
    }

    public List<KpiAssessment> listAssessmentsByTarget(String tenantId, String targetId) {
        getTarget(tenantId, targetId);
        return extStore.listKpiAssessmentsByTarget(tenantId, targetId);
    }

    public BigDecimal calculateDepartmentScore(String tenantId, String department, String period) {
        List<KpiTarget> targets = extStore.listKpiTargets(tenantId, department, period);
        if (targets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalWeight = targets.stream()
                .map(KpiTarget::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal weightedScore = BigDecimal.ZERO;
        for (KpiTarget target : targets) {
            List<KpiAssessment> assessments = extStore.listKpiAssessmentsByTarget(tenantId, target.targetId());
            if (!assessments.isEmpty()) {
                BigDecimal avgScore = assessments.stream()
                        .map(KpiAssessment::score)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(assessments.size()), 2, RoundingMode.HALF_UP);
                weightedScore = weightedScore.add(avgScore.multiply(target.weight()));
            }
        }
        return weightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAchievementRate(BigDecimal actualValue, BigDecimal targetValue) {
        if (actualValue == null || targetValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actualValue.divide(targetValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateScore(BigDecimal achievementRate, KpiTarget target) {
        if ("LINEAR".equals(target.scoringRule())) {
            return achievementRate.setScale(2, RoundingMode.HALF_UP);
        }
        if ("THRESHOLD".equals(target.scoringRule())) {
            if (achievementRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
                return BigDecimal.valueOf(100);
            }
            if (target.warningValue() != null && target.targetValue() != null
                    && target.targetValue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal warningRate = target.warningValue()
                        .divide(target.targetValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                if (achievementRate.compareTo(warningRate) >= 0) {
                    return BigDecimal.valueOf(60).add(
                            achievementRate.subtract(warningRate)
                                    .max(BigDecimal.ZERO)
                                    .setScale(2, RoundingMode.HALF_UP));
                }
            }
            return BigDecimal.ZERO;
        }
        return achievementRate.min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private KpiStatus determineStatus(BigDecimal achievementRate, KpiTarget target) {
        BigDecimal excellentRate = target.excellentValue() != null
                ? target.excellentValue().divide(target.targetValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(120);
        BigDecimal warningRate = target.warningValue() != null
                ? target.warningValue().divide(target.targetValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(80);
        if (achievementRate.compareTo(excellentRate) >= 0) {
            return KpiStatus.EXCELLENT;
        }
        if (achievementRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return KpiStatus.ON_TRACK;
        }
        if (achievementRate.compareTo(warningRate) >= 0) {
            return KpiStatus.WARNING;
        }
        return KpiStatus.CRITICAL;
    }

    private void validateTargetParams(String department, String period, BigDecimal targetValue) {
        if (department == null || department.isBlank()) {
            throw new BizException("DEPARTMENT_REQUIRED", "部门不能为空");
        }
        if (period == null || period.isBlank()) {
            throw new BizException("PERIOD_REQUIRED", "考核周期不能为空");
        }
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("INVALID_TARGET_VALUE", "目标值必须大于0");
        }
    }

    private void validateWeight(BigDecimal weight) {
        if (weight != null && (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BizException("INVALID_WEIGHT", "权重必须在0到100之间");
        }
    }
}
