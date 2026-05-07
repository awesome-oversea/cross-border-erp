package com.aidotnet.erp.scm.domain;

import java.time.Instant;
import java.util.List;

public record PurchasePlanLine(
        String lineId,
        String sellerSku,
        int orderDemandQuantity,
        int replenishmentDemandQuantity,
        int availableInventoryQuantity,
        int inPurchasingQuantity,
        int suggestedPurchaseQuantity,
        List<String> orderSourceRefs,
        List<String> suggestionSourceRefs,
        PurchasePlanLineStatus lineStatus,
        String linkedPoId,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActionable() {
        return suggestedPurchaseQuantity > 0;
    }

    public boolean canCreatePurchaseOrder() {
        return isActionable() && lineStatus == PurchasePlanLineStatus.GENERATED;
    }
}
