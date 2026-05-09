package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.KpiAssessment;
import com.aidotnet.erp.bi.domain.KpiStatus;
import com.aidotnet.erp.bi.domain.KpiTarget;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KPI考核评估应用服务
 * <p>
 * 描述: BI域KPI考核服务，负责KPI目标设定、考核评估、部门评分、达成率计算。
 * </p>
 *
 * @author ERP系统
 */
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

    /**
     * KPI 统计视图，面向 BI 域统一 `/bi/api/v1/kpis/statistics` 读模型。
     * 当前团队(team)口径沿用 department 责任主体，保证未上线阶段不引入新的主数据耦合。
     */
    public KpiStatisticsResult buildStatistics(String tenantId, String period, String department,
                                               String role, String userId) {
        List<KpiTarget> filteredTargets = extStore.listKpiTargets(tenantId, department, period).stream()
                .filter(target -> matchesFilter(role, target.role()))
                .sorted(Comparator.comparing(KpiTarget::department, Comparator.nullsLast(String::compareTo))
                        .thenComparing(KpiTarget::role, Comparator.nullsLast(String::compareTo))
                        .thenComparing(KpiTarget::targetId))
                .toList();
        Map<String, KpiTarget> targetIndex = filteredTargets.stream()
                .collect(Collectors.toMap(KpiTarget::targetId, Function.identity(),
                        (left, right) -> left, LinkedHashMap::new));
        List<KpiAssessment> filteredAssessments = extStore.listKpiAssessments(tenantId, userId, period).stream()
                .filter(assessment -> targetIndex.containsKey(assessment.targetId()))
                .sorted(Comparator.comparing(KpiAssessment::userId, Comparator.nullsLast(String::compareTo))
                        .thenComparing(KpiAssessment::assessmentId))
                .toList();
        long assessedTargetCount = filteredAssessments.stream()
                .map(KpiAssessment::targetId)
                .distinct()
                .count();
        List<KpiDimensionStatistics> departmentStats = buildTargetDimensionStatistics(
                filteredTargets, filteredAssessments, targetIndex, KpiTarget::department, "department");
        return new KpiStatisticsResult(
                period,
                department,
                role,
                userId,
                filteredTargets.size(),
                filteredAssessments.size(),
                assessedTargetCount,
                toPercent(BigDecimal.valueOf(assessedTargetCount), BigDecimal.valueOf(filteredTargets.size())),
                average(filteredAssessments.stream().map(KpiAssessment::achievementRate).toList()),
                average(filteredAssessments.stream().map(KpiAssessment::score).toList()),
                buildStatusCounts(filteredAssessments),
                departmentStats,
                aliasDimensionStatistics(departmentStats, "team"),
                buildTargetDimensionStatistics(filteredTargets, filteredAssessments, targetIndex, KpiTarget::role, "role"),
                buildAssessmentDimensionStatistics(filteredAssessments, "user"),
                Instant.now());
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

    private List<KpiDimensionStatistics> buildTargetDimensionStatistics(List<KpiTarget> targets,
                                                                       List<KpiAssessment> assessments,
                                                                       Map<String, KpiTarget> targetIndex,
                                                                       Function<KpiTarget, String> dimensionExtractor,
                                                                       String dimension) {
        Map<String, KpiStatisticsAccumulator> grouped = new LinkedHashMap<>();
        for (KpiTarget target : targets) {
            String dimensionValue = resolveDimensionValue(dimensionExtractor.apply(target));
            grouped.computeIfAbsent(dimensionValue, ignored -> new KpiStatisticsAccumulator())
                    .registerTarget(target.targetId());
        }
        for (KpiAssessment assessment : assessments) {
            KpiTarget target = targetIndex.get(assessment.targetId());
            if (target == null) {
                continue;
            }
            String dimensionValue = resolveDimensionValue(dimensionExtractor.apply(target));
            grouped.computeIfAbsent(dimensionValue, ignored -> new KpiStatisticsAccumulator())
                    .registerAssessment(assessment);
        }
        return toDimensionStatistics(grouped, dimension);
    }

    private List<KpiDimensionStatistics> buildAssessmentDimensionStatistics(List<KpiAssessment> assessments,
                                                                           String dimension) {
        Map<String, KpiStatisticsAccumulator> grouped = new LinkedHashMap<>();
        for (KpiAssessment assessment : assessments) {
            String dimensionValue = resolveDimensionValue(assessment.userId());
            grouped.computeIfAbsent(dimensionValue, ignored -> new KpiStatisticsAccumulator())
                    .registerAssessment(assessment);
        }
        return toDimensionStatistics(grouped, dimension);
    }

    private List<KpiDimensionStatistics> toDimensionStatistics(Map<String, KpiStatisticsAccumulator> grouped,
                                                               String dimension) {
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toResult(dimension, entry.getKey()))
                .toList();
    }

    private List<KpiDimensionStatistics> aliasDimensionStatistics(List<KpiDimensionStatistics> source,
                                                                  String dimension) {
        return source.stream()
                .map(item -> new KpiDimensionStatistics(
                        dimension,
                        item.dimensionValue(),
                        item.targetCount(),
                        item.assessedCount(),
                        item.avgAchievementRate(),
                        item.avgScore(),
                        item.statusCounts(),
                        item.targetIds(),
                        item.assessmentIds()))
                .toList();
    }

    private Map<String, Long> buildStatusCounts(List<KpiAssessment> assessments) {
        Map<String, Long> statusCounts = emptyStatusCounts();
        for (KpiAssessment assessment : assessments) {
            String status = assessment.status() != null ? assessment.status().name() : KpiStatus.NOT_STARTED.name();
            statusCounts.merge(status, 1L, Long::sum);
        }
        return statusCounts;
    }

    private Map<String, Long> emptyStatusCounts() {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        Arrays.stream(KpiStatus.values()).forEach(status -> statusCounts.put(status.name(), 0L));
        return statusCounts;
    }

    private BigDecimal average(List<BigDecimal> values) {
        List<BigDecimal> filtered = values.stream()
                .filter(value -> value != null)
                .toList();
        if (filtered.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = filtered.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(filtered.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toPercent(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean matchesFilter(String filter, String actualValue) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        if (actualValue == null || actualValue.isBlank()) {
            return false;
        }
        return filter.trim().equalsIgnoreCase(actualValue.trim());
    }

    private String resolveDimensionValue(String dimensionValue) {
        if (dimensionValue == null || dimensionValue.isBlank()) {
            return "UNSPECIFIED";
        }
        return dimensionValue.trim();
    }

    public record KpiStatisticsResult(String period,
                                      String department,
                                      String role,
                                      String userId,
                                      long targetCount,
                                      long assessedCount,
                                      long assessedTargetCount,
                                      BigDecimal completionRate,
                                      BigDecimal avgAchievementRate,
                                      BigDecimal avgScore,
                                      Map<String, Long> statusCounts,
                                      List<KpiDimensionStatistics> departmentStats,
                                      List<KpiDimensionStatistics> teamStats,
                                      List<KpiDimensionStatistics> roleStats,
                                      List<KpiDimensionStatistics> userStats,
                                      Instant generatedAt) {}

    public record KpiDimensionStatistics(String dimension,
                                         String dimensionValue,
                                         long targetCount,
                                         long assessedCount,
                                         BigDecimal avgAchievementRate,
                                         BigDecimal avgScore,
                                         Map<String, Long> statusCounts,
                                         List<String> targetIds,
                                         List<String> assessmentIds) {}

    private static final class KpiStatisticsAccumulator {

        private final Set<String> targetIds = new TreeSet<>();
        private final List<String> assessmentIds = new ArrayList<>();
        private final Map<String, Long> statusCounts = new LinkedHashMap<>();
        private BigDecimal totalAchievementRate = BigDecimal.ZERO;
        private BigDecimal totalScore = BigDecimal.ZERO;

        private KpiStatisticsAccumulator() {
            Arrays.stream(KpiStatus.values()).forEach(status -> statusCounts.put(status.name(), 0L));
        }

        private void registerTarget(String targetId) {
            if (targetId != null && !targetId.isBlank()) {
                targetIds.add(targetId);
            }
        }

        private void registerAssessment(KpiAssessment assessment) {
            registerTarget(assessment.targetId());
            if (assessment.assessmentId() != null && !assessment.assessmentId().isBlank()) {
                assessmentIds.add(assessment.assessmentId());
            }
            totalAchievementRate = totalAchievementRate.add(assessment.achievementRate() != null
                    ? assessment.achievementRate() : BigDecimal.ZERO);
            totalScore = totalScore.add(assessment.score() != null ? assessment.score() : BigDecimal.ZERO);
            String status = assessment.status() != null ? assessment.status().name() : KpiStatus.NOT_STARTED.name();
            statusCounts.merge(status, 1L, Long::sum);
        }

        private KpiDimensionStatistics toResult(String dimension, String dimensionValue) {
            long assessedCount = assessmentIds.size();
            BigDecimal avgAchievementRate = assessedCount == 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : totalAchievementRate.divide(BigDecimal.valueOf(assessedCount), 2, RoundingMode.HALF_UP);
            BigDecimal avgScore = assessedCount == 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : totalScore.divide(BigDecimal.valueOf(assessedCount), 2, RoundingMode.HALF_UP);
            return new KpiDimensionStatistics(
                    dimension,
                    dimensionValue,
                    targetIds.size(),
                    assessedCount,
                    avgAchievementRate,
                    avgScore,
                    new LinkedHashMap<>(statusCounts),
                    List.copyOf(targetIds),
                    List.copyOf(assessmentIds));
        }
    }

    private void validateWeight(BigDecimal weight) {
        if (weight != null && (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BizException("INVALID_WEIGHT", "权重必须在0到100之间");
        }
    }
}
