package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BiApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageMetricsKpisWidgetsAndCockpitSummary() throws Exception {
        String metricResponse = mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"sales_gmv",
                                  "metricName":"Sales GMV",
                                  "category":"sales",
                                  "formula":"sum(order_amount)",
                                  "unit":"USD",
                                  "permissionCode":"bi:metric:read",
                                  "dataLevel":"DETAIL",
                                  "description":"Gross merchandise value",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricCode").value("sales_gmv"))
                .andReturn().getResponse().getContentAsString();
        String metricId = objectMapper.readTree(metricResponse).at("/data/metricId").asText();

        mockMvc.perform(get("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", "tenant-bi")
                        .param("category", "sales")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].metricId").value(metricId));

        mockMvc.perform(get("/bi/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricName").value("Sales GMV"));

        mockMvc.perform(get("/bi/api/in/v1/metrics/by-code/sales_gmv")
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricId").value(metricId));

        mockMvc.perform(patch("/bi/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricName":"Sales GMV Daily",
                                  "formula":"sum(order_amount_daily)",
                                  "description":"Daily gross merchandise value",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricName").value("Sales GMV Daily"))
                .andExpect(jsonPath("$.data.formula").value("sum(order_amount_daily)"));

        String reportResponse = mockMvc.perform(post("/bi/api/in/v1/reports")
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportCode":"profit_board",
                                  "reportName":"Profit Board",
                                  "dataSource":"fms_profit_statement",
                                  "queryText":"select * from bi_profit_statement"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCode").value("profit_board"))
                .andReturn().getResponse().getContentAsString();
        String reportCode = objectMapper.readTree(reportResponse).at("/data/reportCode").asText();

        // 运行接口返回的是报表快照，而不是报表定义本身。
        mockMvc.perform(post("/bi/api/in/v1/reports/" + reportCode + "/run")
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("tenant-bi"))
                .andExpect(jsonPath("$.data.snapshotName").value("Profit Board"))
                .andExpect(jsonPath("$.data.snapshotData").value("{\"dataSource\":\"fms_profit_statement\",\"rows\":0}"))
                .andExpect(jsonPath("$.data.format").value("json"));

        String kpiResponse = mockMvc.perform(post("/bi/api/in/v1/kpis")
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"sales_goal",
                                  "kpiName":"Sales Goal",
                                  "category":"sales",
                                  "value":95.00,
                                  "targetValue":100.00,
                                  "unit":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiCode").value("sales_goal"))
                .andReturn().getResponse().getContentAsString();
        String kpiId = objectMapper.readTree(kpiResponse).at("/data/kpiId").asText();

        mockMvc.perform(patch("/bi/api/in/v1/kpis/" + kpiId + "/target")
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetValue":90.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetValue").value(90.00));

        mockMvc.perform(get("/bi/api/in/v1/kpis/by-code/sales_goal")
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiId").value(kpiId));

        mockMvc.perform(get("/bi/api/in/v1/kpis/by-code/sales_goal/achievement")
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.achieved").value(true))
                .andExpect(jsonPath("$.data.kpi.kpiCode").value("sales_goal"));

        String widgetResponse = mockMvc.perform(post("/bi/api/in/v1/widgets")
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "widgetCode":"sales-overview",
                                  "widgetName":"Sales Overview",
                                  "widgetType":"line-chart",
                                  "config":{
                                    "metricCode":"sales_gmv",
                                    "period":"P7D"
                                  },
                                  "sortOrder":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.widgetCode").value("sales-overview"))
                .andReturn().getResponse().getContentAsString();
        String widgetId = objectMapper.readTree(widgetResponse).at("/data/widgetId").asText();

        mockMvc.perform(patch("/bi/api/in/v1/widgets/" + widgetId + "/config")
                        .header("X-Tenant-Id", "tenant-bi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "config":{
                                    "metricCode":"sales_gmv",
                                    "period":"P30D",
                                    "chart":"line"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.config.period").value("P30D"))
                .andExpect(jsonPath("$.data.config.chart").value("line"));

        mockMvc.perform(get("/bi/api/in/v1/widgets")
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].widgetId").value(widgetId));

        mockMvc.perform(get("/bi/api/in/v1/dashboard/summary")
                        .header("X-Tenant-Id", "tenant-bi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCount").value(1))
                .andExpect(jsonPath("$.data.reportCount").value(1))
                .andExpect(jsonPath("$.data.widgetCount").value(1))
                .andExpect(jsonPath("$.data.cockpitCards.sales.kpiCode").value("sales_goal"))
                .andExpect(jsonPath("$.data.cockpitCards.sales.achieved").value(true));

        mockMvc.perform(get("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/bi/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("METRIC_DEFINITION_NOT_FOUND"));
    }
}
