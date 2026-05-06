package com.aidotnet.erp.common.ads;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdsOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(AdsOptimizationService.class);
    private final Map<String, OptimizationLog> optimizationLogs = new ConcurrentHashMap<>();
    private final Map<String, PmsInstruction> pmsInstructions = new ConcurrentHashMap<>();
    private final Map<String, List<PmsInstruction>> campaignInstructions = new ConcurrentHashMap<>();
    private final Map<String, List<EffectSnapshot>> effectSnapshots = new ConcurrentHashMap<>();

    public BudgetAllocationResult allocateBudget(String campaignId, BigDecimal totalBudget,
                                                  String strategy, Map<String, BigDecimal> campaignMetrics) {
        BigDecimal acos = campaignMetrics.getOrDefault("acos", BigDecimal.valueOf(30));
        BigDecimal roas = campaignMetrics.getOrDefault("roas", BigDecimal.valueOf(3.3));
        BigDecimal suggestedBudget = totalBudget;

        if ("ACOS_OPTIMIZE".equals(strategy)) {
            if (acos.compareTo(BigDecimal.valueOf(25)) > 0) {
                suggestedBudget = totalBudget.multiply(BigDecimal.valueOf(0.8)).setScale(2, RoundingMode.HALF_UP);
            } else if (acos.compareTo(BigDecimal.valueOf(15)) < 0) {
                suggestedBudget = totalBudget.multiply(BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP);
            }
        } else if ("ROAS_OPTIMIZE".equals(strategy)) {
            if (roas.compareTo(BigDecimal.valueOf(4)) < 0) {
                suggestedBudget = totalBudget.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP);
            } else {
                suggestedBudget = totalBudget.multiply(BigDecimal.valueOf(1.15)).setScale(2, RoundingMode.HALF_UP);
            }
        }

        String logId = "opt-" + System.currentTimeMillis();
        optimizationLogs.put(logId, new OptimizationLog(logId, campaignId, "BUDGET",
                totalBudget, suggestedBudget, strategy, Instant.now()));

        log.info("Budget allocated: campaign={}, strategy={}, from={}, to={}",
                campaignId, strategy, totalBudget, suggestedBudget);
        return new BudgetAllocationResult(campaignId, totalBudget, suggestedBudget, strategy);
    }

    public PmsInstruction executePmsInstruction(String campaignId, String instructionType,
                                                  Map<String, Object> params) {
        String instructionId = "PMS-" + System.currentTimeMillis();
        Map<String, Object> beforeState = captureState(campaignId);

        PmsInstruction instruction = new PmsInstruction(instructionId, campaignId, instructionType,
                params, beforeState, null, "EXECUTED", Instant.now(), null);
        pmsInstructions.put(instructionId, instruction);
        campaignInstructions.computeIfAbsent(campaignId, k -> new ArrayList<>()).add(instruction);

        captureEffectSnapshot(campaignId, "BEFORE_" + instructionType, beforeState);

        log.info("Executed PMS instruction: id={}, campaign={}, type={}", instructionId, campaignId, instructionType);
        return instruction;
    }

    public PmsInstruction rollbackPmsInstruction(String instructionId) {
        PmsInstruction instruction = pmsInstructions.get(instructionId);
        if (instruction == null) {
            throw new IllegalArgumentException("PMS instruction not found: " + instructionId);
        }
        if (!"EXECUTED".equals(instruction.status())) {
            throw new IllegalStateException("Only EXECUTED instructions can be rolled back");
        }

        Map<String, Object> rollbackState = instruction.beforeState();
        PmsInstruction rolledBack = new PmsInstruction(instruction.instructionId(), instruction.campaignId(),
                instruction.instructionType(), instruction.params(), instruction.beforeState(),
                rollbackState, "ROLLED_BACK", instruction.executedAt(), Instant.now());
        pmsInstructions.put(instructionId, rolledBack);

        captureEffectSnapshot(instruction.campaignId(), "ROLLBACK_" + instruction.instructionType(), rollbackState);

        log.info("Rolled back PMS instruction: id={}, campaign={}", instructionId, instruction.campaignId());
        return rolledBack;
    }

    public List<PmsInstruction> getCampaignInstructions(String campaignId) {
        return campaignInstructions.getOrDefault(campaignId, List.of());
    }

    public EffectAnalysis analyzeEffect(String campaignId, Map<String, BigDecimal> beforeMetrics,
                                         Map<String, BigDecimal> afterMetrics) {
        Map<String, BigDecimal> changes = new HashMap<>();
        Map<String, String> changeDirections = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : beforeMetrics.entrySet()) {
            String metric = entry.getKey();
            BigDecimal before = entry.getValue();
            BigDecimal after = afterMetrics.getOrDefault(metric, before);
            if (before.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = after.subtract(before).divide(before, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                changes.put(metric, change);
                changeDirections.put(metric, change.compareTo(BigDecimal.ZERO) > 0 ? "UP" :
                        change.compareTo(BigDecimal.ZERO) < 0 ? "DOWN" : "FLAT");
            }
        }

        BigDecimal acosBefore = beforeMetrics.getOrDefault("acos", BigDecimal.ZERO);
        BigDecimal acosAfter = afterMetrics.getOrDefault("acos", BigDecimal.ZERO);
        BigDecimal roasBefore = beforeMetrics.getOrDefault("roas", BigDecimal.ZERO);
        BigDecimal roasAfter = afterMetrics.getOrDefault("roas", BigDecimal.ZERO);
        BigDecimal ctrBefore = beforeMetrics.getOrDefault("ctr", BigDecimal.ZERO);
        BigDecimal ctrAfter = afterMetrics.getOrDefault("ctr", BigDecimal.ZERO);

        List<String> insights = new ArrayList<>();
        if (acosAfter.compareTo(acosBefore) < 0) {
            insights.add("ACOS improved from " + acosBefore + "% to " + acosAfter + "%");
        }
        if (roasAfter.compareTo(roasBefore) > 0) {
            insights.add("ROAS improved from " + roasBefore + " to " + roasAfter);
        }
        if (ctrAfter.compareTo(ctrBefore) > 0) {
            insights.add("CTR improved from " + ctrBefore + "% to " + ctrAfter + "%");
        }
        if (acosAfter.compareTo(acosBefore) > 0) {
            insights.add("WARNING: ACOS increased from " + acosBefore + "% to " + acosAfter + "%");
        }

        String overallEffect = "NEUTRAL";
        if (acosAfter.compareTo(acosBefore) < 0 && roasAfter.compareTo(roasBefore) > 0) {
            overallEffect = "POSITIVE";
        } else if (acosAfter.compareTo(acosBefore) > 0 && roasAfter.compareTo(roasBefore) < 0) {
            overallEffect = "NEGATIVE";
        }

        EffectAnalysis analysis = new EffectAnalysis(campaignId, beforeMetrics, afterMetrics,
                changes, changeDirections, insights, overallEffect, Instant.now());

        captureEffectSnapshot(campaignId, "ANALYSIS", Map.of("analysis", analysis));

        log.info("Effect analysis: campaign={}, effect={}, insights={}", campaignId, overallEffect, insights.size());
        return analysis;
    }

    public List<EffectSnapshot> getEffectHistory(String campaignId) {
        return effectSnapshots.getOrDefault(campaignId, List.of());
    }

    private Map<String, Object> captureState(String campaignId) {
        Map<String, Object> state = new HashMap<>();
        state.put("campaignId", campaignId);
        state.put("capturedAt", Instant.now().toString());
        return state;
    }

    private void captureEffectSnapshot(String campaignId, String eventType, Map<String, Object> data) {
        EffectSnapshot snapshot = new EffectSnapshot("SNAP-" + System.currentTimeMillis(),
                campaignId, eventType, data, Instant.now());
        effectSnapshots.computeIfAbsent(campaignId, k -> new ArrayList<>()).add(snapshot);
    }

    public record BudgetAllocationResult(String campaignId, BigDecimal originalBudget,
                                          BigDecimal suggestedBudget, String strategy) {}
    public record OptimizationLog(String id, String campaignId, String type,
                                   BigDecimal fromValue, BigDecimal toValue, String strategy,
                                   Instant timestamp) {}
    public record PmsInstruction(String instructionId, String campaignId, String instructionType,
                                  Map<String, Object> params, Map<String, Object> beforeState,
                                  Map<String, Object> afterState, String status,
                                  Instant executedAt, Instant rolledBackAt) {}
    public record EffectSnapshot(String snapshotId, String campaignId, String eventType,
                                  Map<String, Object> data, Instant capturedAt) {}
    public record EffectAnalysis(String campaignId, Map<String, BigDecimal> beforeMetrics,
                                  Map<String, BigDecimal> afterMetrics,
                                  Map<String, BigDecimal> changes, Map<String, String> changeDirections,
                                  List<String> insights, String overallEffect, Instant analyzedAt) {}
}
