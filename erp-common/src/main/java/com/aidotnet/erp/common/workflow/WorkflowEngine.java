package com.aidotnet.erp.common.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, WorkflowInstance> instances = new ConcurrentHashMap<>();

    public String createDefinition(String name, List<String> steps, Map<String, Object> config) {
        String defId = "wf-def-" + idGenerator.getAndIncrement();
        definitions.put(defId, new WorkflowDefinition(defId, name, steps, config));
        log.info("Created workflow definition: id={}, name={}, steps={}", defId, name, steps);
        return defId;
    }

    public String startInstance(String definitionId, Map<String, Object> variables) {
        WorkflowDefinition def = definitions.get(definitionId);
        if (def == null) {
            throw new IllegalArgumentException("Workflow definition not found: " + definitionId);
        }
        String instanceId = "wf-inst-" + idGenerator.getAndIncrement();
        WorkflowInstance instance = new WorkflowInstance(
                instanceId, definitionId, def.name(),
                "PENDING", 0, def.steps(),
                variables != null ? variables : new HashMap<>(),
                Instant.now(), null
        );
        instances.put(instanceId, instance);
        log.info("Started workflow instance: id={}, definition={}", instanceId, definitionId);
        return instanceId;
    }

    public WorkflowInstance getInstance(String instanceId) {
        return instances.get(instanceId);
    }

    public boolean completeTask(String instanceId, String action, Map<String, Object> taskVariables) {
        WorkflowInstance instance = instances.get(instanceId);
        if (instance == null) {
            return false;
        }

        Map<String, Object> vars = new HashMap<>(instance.variables());
        if (taskVariables != null) {
            vars.putAll(taskVariables);
        }

        int nextStep = instance.currentStep() + 1;
        String newStatus;
        if (nextStep >= instance.steps().size()) {
            newStatus = "COMPLETED";
        } else {
            newStatus = "APPROVED".equals(action) ? "IN_PROGRESS" : "REJECTED";
        }

        if ("REJECTED".equals(action)) {
            newStatus = "REJECTED";
            nextStep = instance.currentStep();
        }

        WorkflowInstance updated = new WorkflowInstance(
                instance.id(), instance.definitionId(), instance.name(),
                newStatus, nextStep, instance.steps(), vars,
                instance.createdAt(), Instant.now()
        );
        instances.put(instanceId, updated);
        log.info("Workflow task completed: instanceId={}, action={}, newStatus={}", instanceId, action, newStatus);
        return true;
    }

    public List<WorkflowInstance> getPendingInstances(String definitionId) {
        return instances.values().stream()
                .filter(i -> definitionId == null || definitionId.equals(i.definitionId()))
                .filter(i -> "PENDING".equals(i.status()) || "IN_PROGRESS".equals(i.status()))
                .toList();
    }

    public record WorkflowDefinition(String id, String name, List<String> steps, Map<String, Object> config) {}
    public record WorkflowInstance(String id, String definitionId, String name, String status,
                                   int currentStep, List<String> steps, Map<String, Object> variables,
                                   Instant createdAt, Instant updatedAt) {}
}
