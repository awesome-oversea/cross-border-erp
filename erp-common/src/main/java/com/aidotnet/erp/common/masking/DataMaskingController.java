package com.aidotnet.erp.common.masking;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/v1/masking")
public class DataMaskingController {

    private final DataMaskingService maskingService;

    public DataMaskingController(DataMaskingService maskingService) {
        this.maskingService = maskingService;
    }

    @PostMapping("/mask")
    public Result<Map<String, String>> mask(@RequestBody MaskRequest request) {
        Map<String, String> masked = maskingService.maskMap(request.data(), request.fieldRules());
        return Result.ok(masked);
    }

    @GetMapping("/rules")
    public Result<List<String>> getRules() {
        return Result.ok(List.of("phone", "email", "idCard", "bankCard", "name", "address"));
    }

    public record MaskRequest(Map<String, String> data, Map<String, String> fieldRules) {}
}
