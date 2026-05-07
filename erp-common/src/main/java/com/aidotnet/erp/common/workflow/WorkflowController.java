package com.aidotnet.erp.common.workflow;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/sys/api/v1/workflow")
public class WorkflowController {

    private final WorkflowEngine workflowEngine;

    public WorkflowController(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @PostMapping("/definitions")
    public Result<Map<String, String>> createDefinition(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<String> steps = (List<String>) request.get("steps");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) request.get("config");
        String id = workflowEngine.createDefinition(name, steps, config);
        return Result.ok(Map.of("id", id, "name", name));
    }

    @PostMapping("/instances")
    public Result<Map<String, String>> startInstance(@RequestBody Map<String, Object> request) {
        String definitionId = (String) request.get("definitionId");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");
        String instanceId = workflowEngine.startInstance(definitionId, variables);
        return Result.ok(Map.of("instanceId", instanceId));
    }

    @GetMapping("/instances/{id}")
    public Result<WorkflowEngine.WorkflowInstance> getInstance(@PathVariable String id) {
        return Result.ok(workflowEngine.getInstance(id));
    }

    @PostMapping("/tasks/{id}/complete")
    public Result<Boolean> completeTask(@PathVariable String id, @RequestBody Map<String, Object> request) {
        String action = (String) request.getOrDefault("action", "APPROVED");
        @SuppressWarnings("unchecked")
        Map<String, Object> taskVars = (Map<String, Object>) request.get("variables");
        return Result.ok(workflowEngine.completeTask(id, action, taskVars));
    }
}
