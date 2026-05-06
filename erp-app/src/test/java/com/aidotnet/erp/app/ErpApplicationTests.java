package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
class ErpApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsTenantAndTraceContext() throws Exception {
        mockMvc.perform(get("/sys/api/in/v1/health")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Trace-Id", "trace-demo"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "trace-demo"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andExpect(jsonPath("$.data.traceId").value("trace-demo"));
    }
}
