package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.MetricCaliber;
import com.aidotnet.erp.bi.domain.MetricCaliberValue;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricCaliberService {

    private final BiExtStore extStore;

    public MetricCaliberService(BiExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public MetricCaliber createCaliber(String tenantId, String metricCode, String metricName,
                                        String category, String caliberType, String formula,
                                        String formulaDescription, String numeratorMetric,
                                        String denominatorMetric, String unit, String dataSource,
                                        String calculationScope, List<String> dimensions,
                                        List<String> excludeConditions, String permissionCode,
                                        String dataLevel, String description) {
        validateCaliberType(caliberType);
        validateFormula(caliberType, formula, numeratorMetric, denominatorMetric);
        extStore.findMetricCaliberByCode(tenantId, metricCode).ifPresent(existing -> {
            throw new BizException("CALIBER_EXISTS", "指标口径已存在: " + metricCode);
        });
        Instant now = Instant.now();
        MetricCaliber caliber = new MetricCaliber(
                UUID.randomUUID().toString(), tenantId, metricCode, metricName, category,
                caliberType, formula, formulaDescription, numeratorMetric, denominatorMetric,
                unit, dataSource, calculationScope, dimensions, excludeConditions,
                permissionCode, dataLevel, true, "1", description, now, now);
        extStore.saveMetricCaliber(caliber);
        return caliber;
    }

    @Transactional
    public MetricCaliber updateCaliber(String tenantId, String caliberId, String formula,
                                        String formulaDescription, String numeratorMetric,
                                        String denominatorMetric, String unit, String dataSource,
                                        String calculationScope, List<String> dimensions,
                                        List<String> excludeConditions, String description) {
        MetricCaliber existing = extStore.findMetricCaliber(tenantId, caliberId)
                .orElseThrow(() -> new BizException("CALIBER_NOT_FOUND", "指标口径不存在"));
        int newVersion = Integer.parseInt(existing.version()) + 1;
        Instant now = Instant.now();
        MetricCaliber updated = new MetricCaliber(
                existing.caliberId(), existing.tenantId(), existing.metricCode(),
                existing.metricName(), existing.category(), existing.caliberType(),
                formula != null ? formula : existing.formula(),
                formulaDescription != null ? formulaDescription : existing.formulaDescription(),
                numeratorMetric != null ? numeratorMetric : existing.numeratorMetric(),
                denominatorMetric != null ? denominatorMetric : existing.denominatorMetric(),
                unit != null ? unit : existing.unit(),
                dataSource != null ? dataSource : existing.dataSource(),
                calculationScope != null ? calculationScope : existing.calculationScope(),
                dimensions != null ? dimensions : existing.dimensions(),
                excludeConditions != null ? excludeConditions : existing.excludeConditions(),
                existing.permissionCode(), existing.dataLevel(), existing.enabled(),
                String.valueOf(newVersion),
                description != null ? description : existing.description(),
                existing.createdAt(), now);
        extStore.saveMetricCaliber(updated);
        return updated;
    }

    @Transactional
    public MetricCaliber toggleCaliber(String tenantId, String caliberId, boolean enabled) {
        MetricCaliber existing = extStore.findMetricCaliber(tenantId, caliberId)
                .orElseThrow(() -> new BizException("CALIBER_NOT_FOUND", "指标口径不存在"));
        MetricCaliber updated = new MetricCaliber(
                existing.caliberId(), existing.tenantId(), existing.metricCode(),
                existing.metricName(), existing.category(), existing.caliberType(),
                existing.formula(), existing.formulaDescription(), existing.numeratorMetric(),
                existing.denominatorMetric(), existing.unit(), existing.dataSource(),
                existing.calculationScope(), existing.dimensions(), existing.excludeConditions(),
                existing.permissionCode(), existing.dataLevel(), enabled, existing.version(),
                existing.description(), existing.createdAt(), Instant.now());
        extStore.saveMetricCaliber(updated);
        return updated;
    }

    public MetricCaliberValue calculateMetric(String tenantId, String metricCode,
                                               String dimensionKey, String dimensionValue,
                                               BigDecimal numeratorValue, BigDecimal denominatorValue) {
        MetricCaliber caliber = extStore.findMetricCaliberByCode(tenantId, metricCode)
                .orElseThrow(() -> new BizException("CALIBER_NOT_FOUND", "指标口径不存在: " + metricCode));
        if (!caliber.enabled()) {
            throw new BizException("CALIBER_DISABLED", "指标口径已禁用");
        }
        BigDecimal result = switch (caliber.caliberType()) {
            case "DIRECT" -> numeratorValue;
            case "RATIO" -> {
                if (denominatorValue == null || denominatorValue.compareTo(BigDecimal.ZERO) == 0) {
                    throw new BizException("DENOMINATOR_ZERO", "分母不能为零");
                }
                yield numeratorValue.divide(denominatorValue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            case "FORMULA" -> evaluateFormula(caliber.formula(), numeratorValue, denominatorValue);
            default -> throw new BizException("UNSUPPORTED_TYPE", "不支持的口径类型: " + caliber.caliberType());
        };
        MetricCaliberValue value = new MetricCaliberValue(
                UUID.randomUUID().toString(), tenantId, caliber.caliberId(), metricCode,
                result, dimensionKey, dimensionValue, Instant.now());
        extStore.saveMetricCaliberValue(value);
        return value;
    }

    public List<MetricCaliber> listCalibers(String tenantId, String category) {
        return extStore.listMetricCalibers(tenantId, category);
    }

    public List<MetricCaliberValue> listCaliberValues(String tenantId, String metricCode) {
        return extStore.listMetricCaliberValues(tenantId, metricCode);
    }

    private void validateCaliberType(String caliberType) {
        if (!List.of("DIRECT", "RATIO", "FORMULA").contains(caliberType)) {
            throw new BizException("INVALID_CALIBER_TYPE", "口径类型必须是DIRECT/RATIO/FORMULA之一");
        }
    }

    private void validateFormula(String caliberType, String formula,
                                  String numeratorMetric, String denominatorMetric) {
        if ("RATIO".equals(caliberType)) {
            if (numeratorMetric == null || numeratorMetric.isBlank()) {
                throw new BizException("NUMERATOR_REQUIRED", "比率类型必须指定分子指标");
            }
            if (denominatorMetric == null || denominatorMetric.isBlank()) {
                throw new BizException("DENOMINATOR_REQUIRED", "比率类型必须指定分母指标");
            }
        }
        if ("FORMULA".equals(caliberType) && (formula == null || formula.isBlank())) {
            throw new BizException("FORMULA_REQUIRED", "公式类型必须指定计算公式");
        }
    }

    private BigDecimal evaluateFormula(String formula, BigDecimal numerator, BigDecimal denominator) {
        if (formula.contains("{numerator}") && formula.contains("{denominator}")) {
            String expr = formula.replace("{numerator}", numerator.toPlainString())
                    .replace("{denominator}", denominator.toPlainString());
            try {
                return new BigDecimal(expr).setScale(4, BigDecimal.ROUND_HALF_UP);
            } catch (Exception e) {
                return numerator.divide(denominator, 4, BigDecimal.ROUND_HALF_UP);
            }
        }
        return numerator;
    }
}
