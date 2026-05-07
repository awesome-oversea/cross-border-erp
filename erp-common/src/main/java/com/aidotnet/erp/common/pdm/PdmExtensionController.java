package com.aidotnet.erp.common.pdm;

import com.aidotnet.erp.common.api.Result;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/pdm/api/v1")
public class PdmExtensionController {

    private final PdmExtensionService pdmExtService;

    public PdmExtensionController(PdmExtensionService pdmExtService) {
        this.pdmExtService = pdmExtService;
    }

    @PostMapping("/selection-proposals")
    public Result<PdmExtensionService.SelectionProposal> createSelectionProposal(@RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.createSelectionProposal(
                (String) request.get("tenantId"),
                (String) request.get("productName"),
                (String) request.getOrDefault("category", ""),
                (String) request.getOrDefault("targetMarket", ""),
                (String) request.getOrDefault("sourceType", "MANUAL"),
                (String) request.getOrDefault("sourceLink", ""),
                (String) request.getOrDefault("submitter", ""),
                (String) request.getOrDefault("analysisData", "")
        ));
    }

    @PutMapping("/selection-proposals/{proposalId}/submit")
    public Result<PdmExtensionService.SelectionProposal> submitProposal(@PathVariable String proposalId) {
        return Result.ok(pdmExtService.submitProposal(proposalId));
    }

    @PutMapping("/selection-proposals/{proposalId}/review")
    public Result<PdmExtensionService.SelectionProposal> reviewProposal(
            @PathVariable String proposalId, @RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.reviewProposal(
                proposalId,
                (String) request.getOrDefault("reviewer", ""),
                (String) request.get("decision"),
                (String) request.getOrDefault("comments", "")
        ));
    }

    @GetMapping("/selection-proposals")
    public Result<List<PdmExtensionService.SelectionProposal>> listSelectionProposals(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status) {
        return Result.ok(pdmExtService.listSelectionProposals(tenantId, status));
    }

    @PostMapping("/development-processes")
    public Result<PdmExtensionService.DevelopmentProcess> createDevelopmentProcess(@RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.createDevelopmentProcess(
                (String) request.get("tenantId"),
                (String) request.get("productName"),
                (String) request.getOrDefault("proposalId", ""),
                (String) request.getOrDefault("developer", ""),
                (String) request.getOrDefault("milestones", "")
        ));
    }

    @PatchMapping("/development-processes/{processId}/status")
    public Result<PdmExtensionService.DevelopmentProcess> updateProcessStatus(
            @PathVariable String processId, @RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.updateProcessStatus(processId, (String) request.get("status")));
    }

    @GetMapping("/development-processes")
    public Result<List<PdmExtensionService.DevelopmentProcess>> listDevelopmentProcesses(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status) {
        return Result.ok(pdmExtService.listDevelopmentProcesses(tenantId, status));
    }

    @PostMapping("/intellectual-properties")
    public Result<PdmExtensionService.IntellectualProperty> createIntellectualProperty(@RequestBody Map<String, Object> request) {
        Instant filingDate = request.containsKey("filingDate") ? Instant.parse((String) request.get("filingDate")) : null;
        Instant expiryDate = request.containsKey("expiryDate") ? Instant.parse((String) request.get("expiryDate")) : null;
        return Result.ok(pdmExtService.createIntellectualProperty(
                (String) request.get("tenantId"),
                (String) request.get("productName"),
                (String) request.getOrDefault("ipType", "TRADEMARK"),
                (String) request.getOrDefault("ipNumber", ""),
                (String) request.getOrDefault("status", "PENDING"),
                (String) request.getOrDefault("owner", ""),
                filingDate, expiryDate,
                (String) request.getOrDefault("description", "")
        ));
    }

    @PatchMapping("/intellectual-properties/{ipId}/status")
    public Result<PdmExtensionService.IntellectualProperty> updateIpStatus(
            @PathVariable String ipId, @RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.updateIpStatus(ipId, (String) request.get("status")));
    }

    @GetMapping("/intellectual-properties")
    public Result<List<PdmExtensionService.IntellectualProperty>> listIntellectualProperties(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String ipType) {
        return Result.ok(pdmExtService.listIntellectualProperties(tenantId, ipType));
    }

    @PostMapping("/quality-standards")
    public Result<PdmExtensionService.QualityStandard> createQualityStandard(@RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.createQualityStandard(
                (String) request.get("tenantId"),
                (String) request.get("standardName"),
                (String) request.getOrDefault("category", ""),
                (String) request.getOrDefault("applicableProducts", ""),
                (String) request.getOrDefault("inspectionItems", ""),
                (String) request.getOrDefault("acceptanceCriteria", ""),
                (String) request.getOrDefault("version", "1.0")
        ));
    }

    @PatchMapping("/quality-standards/{standardId}/active")
    public Result<PdmExtensionService.QualityStandard> updateQualityStandard(
            @PathVariable String standardId, @RequestBody Map<String, Object> request) {
        return Result.ok(pdmExtService.updateQualityStandard(standardId, (Boolean) request.getOrDefault("active", true)));
    }

    @GetMapping("/quality-standards")
    public Result<List<PdmExtensionService.QualityStandard>> listQualityStandards(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String category) {
        return Result.ok(pdmExtService.listQualityStandards(tenantId, category));
    }
}
