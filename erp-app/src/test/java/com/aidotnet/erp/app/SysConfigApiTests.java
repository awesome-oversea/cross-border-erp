package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
class SysConfigApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageSystemConfigCreateUpdateDisableEnableAndTenantIsolation() throws Exception {
        String response = mockMvc.perform(post("/sys/api/in/v1/configs")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configKey\":\"order.auto.audit\",\"configValue\":\"true\",\"description\":\"auto audit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andReturn().getResponse().getContentAsString();
        String configId = objectMapper.readTree(response).at("/data/configId").asText();

        mockMvc.perform(post("/sys/api/in/v1/configs")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configKey\":\"order.auto.audit\",\"configValue\":\"false\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONFIG_DUPLICATED"));

        mockMvc.perform(put("/sys/api/in/v1/configs/" + configId)
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configKey\":\"order.auto.audit\",\"configValue\":\"false\",\"description\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configValue").value("false"));

        mockMvc.perform(patch("/sys/api/in/v1/configs/" + configId + "/disable")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(get("/sys/api/in/v1/configs/key/order.auto.audit")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONFIG_NOT_FOUND"));

        mockMvc.perform(patch("/sys/api/in/v1/configs/" + configId + "/enable")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));

        mockMvc.perform(get("/sys/api/in/v1/configs")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
