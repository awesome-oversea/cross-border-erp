package com.aidotnet.erp.common.risk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ComplianceRiskService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceRiskService.class);
    private final Map<String, ComplianceRule> complianceRules = new ConcurrentHashMap<>();
    private final Map<String, RiskRule> riskRules = new ConcurrentHashMap<>();

    public ComplianceCheckResult check(String category, String content, Map<String, Object> context) {
        List<String> violations = new ArrayList<>();

        for (ComplianceRule rule : complianceRules.values()) {
            if (rule.category().equals(category) && rule.enabled()) {
                if (!rule.check(content, context)) {
                    violations.add(rule.id() + ":" + rule.description());
                }
            }
        }

        boolean passed = violations.isEmpty();
        if (!passed) {
            log.warn("Compliance check failed: category={}, violations={}", category, violations);
        }
        return new ComplianceCheckResult(passed, violations, passed ? "PASSED" : "FAILED");
    }

    public RiskAssessmentResult assessRisk(String entityType, String entityId, Map<String, Object> riskFactors) {
        double riskScore = 0.0;
        List<String> riskItems = new ArrayList<>();

        for (RiskRule rule : riskRules.values()) {
            if (rule.entityType().equals(entityType) && rule.enabled()) {
                double score = rule.evaluate(riskFactors);
                if (score > 0) {
                    riskScore += score;
                    riskItems.add(rule.id() + ":" + rule.description());
                }
            }
        }

        String level;
        if (riskScore >= 80) level = "HIGH";
        else if (riskScore >= 40) level = "MEDIUM";
        else level = "LOW";

        return new RiskAssessmentResult(entityId, entityType, riskScore, level, riskItems);
    }

    public void addComplianceRule(String id, String category, String description, ComplianceRule.Checker checker) {
        complianceRules.put(id, new ComplianceRule(id, category, description, true, checker));
    }

    public void addRiskRule(String id, String entityType, String description, RiskRule.Evaluator evaluator) {
        riskRules.put(id, new RiskRule(id, entityType, description, true, evaluator));
    }

    public record ComplianceCheckResult(boolean passed, List<String> violations, String status) {}
    public record RiskAssessmentResult(String entityId, String entityType, double riskScore, String level, List<String> riskItems) {}

    public record ComplianceRule(String id, String category, String description, boolean enabled, Checker checker) {
        public boolean check(String content, Map<String, Object> context) {
            return checker.check(content, context);
        }
        @FunctionalInterface
        public interface Checker {
            boolean check(String content, Map<String, Object> context);
        }
    }

    public record RiskRule(String id, String entityType, String description, boolean enabled, Evaluator evaluator) {
        public double evaluate(Map<String, Object> factors) {
            return evaluator.evaluate(factors);
        }
        @FunctionalInterface
        public interface Evaluator {
            double evaluate(Map<String, Object> factors);
        }
    }
}
