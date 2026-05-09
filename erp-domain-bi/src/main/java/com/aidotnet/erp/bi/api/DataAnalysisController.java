package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.DataAnalysisService;
import com.aidotnet.erp.bi.application.DataAnalysisService.CreateExportCommand;
import com.aidotnet.erp.bi.application.DataAnalysisService.CrossAnalysisCommand;
import com.aidotnet.erp.bi.domain.CrossAnalysisResult;
import com.aidotnet.erp.bi.domain.DataExportTask;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/bi/api/in/v1/analysis", "/bi/api/v1/analysis"})
public class DataAnalysisController {

    private final DataAnalysisService analysisService;

    public DataAnalysisController(DataAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/cross")
    public Result<CrossAnalysisResult> crossAnalysis(@Valid @RequestBody CrossAnalysisRequest request) {
        return Result.ok(analysisService.crossAnalysis(currentTenant(), new CrossAnalysisCommand(
                request.analysisName(), request.rowDimensions(), request.columnDimensions(), request.metrics())));
    }

    @PostMapping("/exports")
    public Result<DataExportTask> createExportTask(@Valid @RequestBody CreateExportRequest request) {
        return Result.ok(analysisService.createExportTask(currentTenant(), new CreateExportCommand(
                request.exportName(), request.exportType(), request.format())));
    }

    @PostMapping("/exports/{taskId}/execute")
    public Result<DataExportTask> executeExport(@PathVariable String taskId) {
        return Result.ok(analysisService.executeExport(currentTenant(), taskId));
    }

    @GetMapping("/exports/{taskId}")
    public Result<DataExportTask> getExportTask(@PathVariable String taskId) {
        return Result.ok(analysisService.getExportTask(currentTenant(), taskId));
    }

    @GetMapping("/exports")
    public Result<List<DataExportTask>> listExportTasks(@RequestParam(required = false) String status) {
        return Result.ok(analysisService.listExportTasks(currentTenant(), status));
    }

    private String currentTenant() { return TenantContext.getTenantId(); }

    public record CrossAnalysisRequest(String analysisName, @NotEmpty List<String> rowDimensions,
                                       @NotEmpty List<String> columnDimensions, @NotEmpty List<String> metrics) {}
    public record CreateExportRequest(@NotBlank String exportName, @NotBlank String exportType, String format) {}
}
