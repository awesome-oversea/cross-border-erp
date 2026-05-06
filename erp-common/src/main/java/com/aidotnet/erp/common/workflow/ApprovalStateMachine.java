package com.aidotnet.erp.common.workflow;

import com.aidotnet.erp.common.exception.BizException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ApprovalStateMachine {

    private final Map<ApprovalStatus, Set<ApprovalStatus>> transitions = new EnumMap<>(ApprovalStatus.class);

    public ApprovalStateMachine() {
        transitions.put(ApprovalStatus.DRAFT, Set.of(ApprovalStatus.PENDING, ApprovalStatus.CANCELLED));
        transitions.put(ApprovalStatus.PENDING, Set.of(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED, ApprovalStatus.CANCELLED));
        transitions.put(ApprovalStatus.APPROVED, Set.of(ApprovalStatus.EXECUTING, ApprovalStatus.CANCELLED));
        transitions.put(ApprovalStatus.REJECTED, Set.of(ApprovalStatus.DRAFT));
        transitions.put(ApprovalStatus.EXECUTING, Set.of(ApprovalStatus.COMPLETED, ApprovalStatus.FAILED));
        transitions.put(ApprovalStatus.FAILED, Set.of(ApprovalStatus.EXECUTING));
        transitions.put(ApprovalStatus.COMPLETED, Set.of());
        transitions.put(ApprovalStatus.CANCELLED, Set.of());
    }

    public ApprovalStatus transition(ApprovalStatus current, ApprovalStatus target) {
        Set<ApprovalStatus> allowed = transitions.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BizException("INVALID_TRANSITION",
                    "不允许从 " + current + " 转换到 " + target);
        }
        return target;
    }

    public boolean canTransition(ApprovalStatus current, ApprovalStatus target) {
        return transitions.getOrDefault(current, Set.of()).contains(target);
    }

    public Set<ApprovalStatus> nextStatuses(ApprovalStatus current) {
        return transitions.getOrDefault(current, Set.of());
    }
}
