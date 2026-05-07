package com.aidotnet.erp.scm.domain;

import java.time.Instant;
import java.util.List;

public record PurchasePlan(
        String planId,
        String tenantId,
        String planNumber,
        PurchasePlanStatus status,
        List<PurchasePlanLine> lines,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean hasActionableLines() {
        return lines != null && lines.stream().anyMatch(PurchasePlanLine::isActionable);
    }

    public boolean allActionableLinesOrdered() {
        return lines != null
                && lines.stream()
                        .filter(PurchasePlanLine::isActionable)
                        .allMatch(line -> line.lineStatus() == PurchasePlanLineStatus.ORDERED);
    }
}
