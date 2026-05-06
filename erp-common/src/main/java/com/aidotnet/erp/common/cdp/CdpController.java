package com.aidotnet.erp.common.cdp;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("commonCdpController")
@RequestMapping("/crm/api/v1/cdp")
public class CdpController {

    private final CustomerDataPlatformService cdpService;

    public CdpController(CustomerDataPlatformService cdpService) {
        this.cdpService = cdpService;
    }

    @GetMapping("/customers/{id}/profile")
    public Result<CustomerDataPlatformService.CustomerProfile> getProfile(@PathVariable String id) {
        return Result.ok(cdpService.getProfile(id));
    }

    @GetMapping("/segments")
    public Result<List<CustomerDataPlatformService.CustomerSegment>> getSegments() {
        return Result.ok(cdpService.getSegments());
    }

    @PostMapping("/segments")
    public Result<Map<String, String>> createSegment(@RequestBody Map<String, Object> request) {
        String id = cdpService.createSegment(
                (String) request.get("name"),
                (String) request.getOrDefault("description", ""),
                request
        );
        return Result.ok(Map.of("id", id));
    }
}
