package com.aidotnet.erp.app;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ErpAppTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BiApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String uniqueTenant(String scenario) {
        return "tenant-bi-" + scenario + "-" + UUID.randomUUID();
    }

    @Test
    void manageMetricsKpisWidgetsAndCockpitSummary() throws Exception {
        String tenantId = uniqueTenant("core");
        String otherTenantId = uniqueTenant("isolation");

        String metricResponse = mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "sales")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].metricId").value(metricId));

        mockMvc.perform(get("/bi/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricName").value("Sales GMV"));

        mockMvc.perform(get("/bi/api/in/v1/metrics/by-code/sales_gmv")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricId").value(metricId));

        mockMvc.perform(patch("/bi/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.snapshotName").value("Profit Board"))
                .andExpect(jsonPath("$.data.snapshotData").value("{\"dataSource\":\"fms_profit_statement\",\"rows\":0}"))
                .andExpect(jsonPath("$.data.format").value("json"));

        String kpiResponse = mockMvc.perform(post("/bi/api/in/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetValue":90.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetValue").value(90.00));

        mockMvc.perform(get("/bi/api/in/v1/kpis/by-code/sales_goal")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiId").value(kpiId));

        mockMvc.perform(get("/bi/api/in/v1/kpis/by-code/sales_goal/achievement")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.achieved").value(true))
                .andExpect(jsonPath("$.data.kpi.kpiCode").value("sales_goal"));

        String widgetResponse = mockMvc.perform(post("/bi/api/in/v1/widgets")
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].widgetId").value(widgetId));

        mockMvc.perform(get("/bi/api/in/v1/dashboard/summary")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCount").value(1))
                .andExpect(jsonPath("$.data.reportCount").value(1))
                .andExpect(jsonPath("$.data.widgetCount").value(1))
                .andExpect(jsonPath("$.data.cockpitCards.sales.kpiCode").value("sales_goal"))
                .andExpect(jsonPath("$.data.cockpitCards.sales.achieved").value(true));

        mockMvc.perform(get("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/bi/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("METRIC_DEFINITION_NOT_FOUND"));
    }

    @Test
    void exposeUnifiedV1CoreReportMetricAndDashboardSummary() throws Exception {
        String tenantId = uniqueTenant("core-v1");
        String otherTenantId = uniqueTenant("core-v1-other");

        String metricResponse = mockMvc.perform(post("/bi/api/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"sales_gmv_v1",
                                  "metricName":"Sales GMV V1",
                                  "category":"sales",
                                  "formula":"sum(order_amount)",
                                  "unit":"USD",
                                  "permissionCode":"bi:metric:read",
                                  "dataLevel":"DETAIL",
                                  "description":"Unified v1 sales metric",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("sales_gmv_v1"))
                .andReturn().getResponse().getContentAsString();
        String metricId = objectMapper.readTree(metricResponse).at("/data/metricId").asText();

        mockMvc.perform(get("/bi/api/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "sales")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].metricId").value(metricId));

        mockMvc.perform(post("/bi/api/v1/reports")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportCode":"profit_board_v1",
                                  "reportName":"Profit Board V1",
                                  "dataSource":"fms_profit_statement",
                                  "queryText":"select * from bi_profit_statement_v1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCode").value("profit_board_v1"));

        mockMvc.perform(post("/bi/api/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"sales_goal_v1",
                                  "kpiName":"Sales Goal V1",
                                  "category":"sales",
                                  "value":120.00,
                                  "targetValue":100.00,
                                  "unit":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiCode").value("sales_goal_v1"));

        mockMvc.perform(get("/bi/api/v1/dashboard/summary")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCount").value(1))
                .andExpect(jsonPath("$.data.reportCount").value(1))
                .andExpect(jsonPath("$.data.widgetCount").value(0))
                .andExpect(jsonPath("$.data.cockpitCards.sales.kpiCode").value("sales_goal_v1"))
                .andExpect(jsonPath("$.data.cockpitCards.sales.achieved").value(true));

        mockMvc.perform(get("/bi/api/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("METRIC_DEFINITION_NOT_FOUND"));
    }

    @Test
    void exposeUnifiedV1BiExtAlertSnapshotCockpitTemplateTrendAndRanking() throws Exception {
        String tenantId = uniqueTenant("ext-v1");
        String otherTenantId = uniqueTenant("ext-v1-other");

        String alertRuleResponse = mockMvc.perform(post("/bi/api/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Unified Profit Alert",
                                  "metricCode":"gross_profit_rate_v1",
                                  "domain":"finance",
                                  "condition":"LESS_THAN",
                                  "threshold":"15",
                                  "severity":"WARNING",
                                  "notifyChannel":"EMAIL",
                                  "notifyTargets":"finance@erp.test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleName").value("Unified Profit Alert"))
                .andReturn().getResponse().getContentAsString();
        String ruleId = objectMapper.readTree(alertRuleResponse).at("/data/ruleId").asText();

        mockMvc.perform(get("/bi/api/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .param("domain", "finance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ruleId").value(ruleId));

        mockMvc.perform(get("/bi/api/v1/alert-rules/" + ruleId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("gross_profit_rate_v1"));

        String snapshotResponse = mockMvc.perform(post("/bi/api/v1/report-snapshots")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportId":"profit_report_v1",
                                  "snapshotName":"Profit Snapshot V1",
                                  "snapshotData":"{\\"grossProfit\\":1250.35}",
                                  "format":"json"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value("profit_report_v1"))
                .andReturn().getResponse().getContentAsString();
        String snapshotId = objectMapper.readTree(snapshotResponse).at("/data/snapshotId").asText();

        mockMvc.perform(get("/bi/api/v1/report-snapshots")
                        .header("X-Tenant-Id", tenantId)
                        .param("reportId", "profit_report_v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].snapshotId").value(snapshotId));

        mockMvc.perform(get("/bi/api/v1/report-snapshots/" + snapshotId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.snapshotName").value("Profit Snapshot V1"));

        mockMvc.perform(get("/bi/api/v1/cockpit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.alerts.length()").value(1))
                .andExpect(jsonPath("$.data.alerts[0].ruleId").value(ruleId));

        String templateResponse = mockMvc.perform(post("/bi/api/v1/kpi-templates")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateCode":"profit_margin_tpl_v1",
                                  "templateName":"Profit Margin Template V1",
                                  "category":"finance",
                                  "defaultUnit":"PERCENT",
                                  "defaultTargetFormula":"gross_profit/revenue",
                                  "description":"Unified v1 KPI template"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateCode").value("profit_margin_tpl_v1"))
                .andReturn().getResponse().getContentAsString();
        String templateId = objectMapper.readTree(templateResponse).at("/data/templateId").asText();

        mockMvc.perform(get("/bi/api/v1/kpi-templates")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].templateId").value(templateId));

        mockMvc.perform(get("/bi/api/v1/kpi-templates/" + templateId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateName").value("Profit Margin Template V1"));

        String trendResponse = mockMvc.perform(post("/bi/api/v1/trends")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"gross_profit_rate_v1",
                                  "metricName":"Gross Profit Rate V1",
                                  "period":"P7D",
                                  "dataPoints":3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("gross_profit_rate_v1"))
                .andExpect(jsonPath("$.data.dataPoints.length()").value(3))
                .andReturn().getResponse().getContentAsString();
        String trendId = objectMapper.readTree(trendResponse).at("/data/trendId").asText();

        mockMvc.perform(get("/bi/api/v1/trends")
                        .header("X-Tenant-Id", tenantId)
                        .param("metricCode", "gross_profit_rate_v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].trendId").value(trendId));

        String rankingResponse = mockMvc.perform(post("/bi/api/v1/rankings")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rankingType":"profit-store",
                                  "dimension":"store",
                                  "items":[
                                    {
                                      "rankKey":"AMZ-US-001",
                                      "label":"Amazon US Store",
                                      "value":580.25,
                                      "rank":1
                                    },
                                    {
                                      "rankKey":"AMZ-EU-001",
                                      "label":"Amazon EU Store",
                                      "value":420.15,
                                      "rank":2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rankingType").value("profit-store"))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String rankingId = objectMapper.readTree(rankingResponse).at("/data/rankingId").asText();

        mockMvc.perform(get("/bi/api/v1/rankings")
                        .header("X-Tenant-Id", tenantId)
                        .param("rankingType", "profit-store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].rankingId").value(rankingId))
                .andExpect(jsonPath("$.data[0].items.length()").value(2));

        mockMvc.perform(get("/bi/api/v1/cockpit")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alerts.length()").value(0));
    }

    @Test
    void aggregateUnifiedCockpitMetricsFromOperationsFinanceAndServiceSignals() throws Exception {
        String tenantId = uniqueTenant("cockpit-live");
        String otherTenantId = uniqueTenant("cockpit-live-other");

        mockMvc.perform(post("/bi/api/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Cockpit Inventory Alert",
                                  "metricCode":"inventory_available",
                                  "domain":"cockpit",
                                  "condition":"LESS_THAN",
                                  "threshold":"5",
                                  "severity":"WARNING",
                                  "notifyChannel":"EMAIL",
                                  "notifyTargets":"ops@erp.test"
                                }
                                """))
                .andExpect(status().isOk());

        String customerResponse = mockMvc.perform(post("/crm/api/in/v1/customers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Cockpit Customer",
                                  "email":"cockpit.customer@erp.test",
                                  "phone":"13800138000",
                                  "countryCode":"US",
                                  "platform":"amazon"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String customerId = objectMapper.readTree(customerResponse).at("/data/customerId").asText();

        mockMvc.perform(post("/crm/api/in/v1/tickets")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"%s",
                                  "subject":"Delayed order follow-up",
                                  "description":"Customer is waiting for the FBA shipment to arrive."
                                }
                                """.formatted(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        String storeResponse = mockMvc.perform(post("/som/api/in/v1/stores")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform":"amazon",
                                  "storeCode":"AMZ-COCKPIT-US",
                                  "storeName":"Amazon Cockpit US"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String storeId = objectMapper.readTree(storeResponse).at("/data/storeId").asText();

        mockMvc.perform(patch("/som/api/in/v1/stores/" + storeId + "/connect")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        String listingResponse = mockMvc.perform(post("/som/api/in/v1/listings")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId":"PROD-COCKPIT-100",
                                  "storeId":"%s",
                                  "title":"Cockpit Listing",
                                  "description":"Unified cockpit aggregation validation listing",
                                  "price":25.00,
                                  "originalPrice":30.00,
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "marketplaceListingId":"LISTING-COCKPIT-100"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String listingId = objectMapper.readTree(listingResponse).at("/data/listingId").asText();

        mockMvc.perform(patch("/som/api/in/v1/listings/" + listingId + "/publish")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/som/api/in/v1/channel-skus")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productSku":"PROD-COCKPIT-100",
                                  "channel":"amazon",
                                  "channelSku":"SKU-COCKPIT-100",
                                  "storeId":"%s",
                                  "marketplaceId":"AMAZON-US"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelSku").value("SKU-COCKPIT-100"));

        mockMvc.perform(post("/som/api/in/v1/listings/" + listingId + "/performance")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId":"%s",
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "impressions":2100,
                                  "clicks":75,
                                  "ctr":3.57,
                                  "spend":42.10,
                                  "sales":350.00,
                                  "acos":12.03,
                                  "orders":7,
                                  "conversionRate":9.33,
                                  "periodStart":"2026-05-01T00:00:00Z",
                                  "periodEnd":"2026-05-07T23:59:59Z"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sales").value(350.0))
                .andExpect(jsonPath("$.data.orders").value(7));

        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"WH-COCKPIT-US",
                                  "name":"Cockpit Warehouse US",
                                  "type":"SELF",
                                  "countryCode":"US",
                                  "address":"Los Angeles",
                                  "contactPerson":"cockpit-ops",
                                  "phone":"123456789"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"SKU-COCKPIT-100",
                                  "quantity":4
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(4));

        String shipmentResponse = mockMvc.perform(post("/fba/api/in/v1/shipments")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amazonShipmentId":"FBA-COCKPIT-100",
                                  "destinationFc":"ONT8",
                                  "carrier":"UPS",
                                  "plannedQuantity":5,
                                  "items":[
                                    {
                                      "productId":"PROD-COCKPIT-100",
                                      "sellerSku":"SKU-COCKPIT-100",
                                      "fnsku":"FNSKU-COCKPIT-100",
                                      "quantity":5,
                                      "boxQuantity":2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/fbaShipmentId").asText();

        mockMvc.perform(patch("/fba/api/in/v1/shipments/" + shipmentId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/pack")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartonCount":2,
                                  "totalWeight":12.50,
                                  "carrier":"UPS"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/ship")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrier":"UPS",
                                  "trackingNo":"1Z-COCKPIT-100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        mockMvc.perform(get("/bi/api/v1/cockpit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.metrics.totalOrders").value(7))
                .andExpect(jsonPath("$.data.metrics.totalRevenue").value(350.0))
                .andExpect(jsonPath("$.data.metrics.pendingShipments").value(1))
                .andExpect(jsonPath("$.data.metrics.activeListings").value(1))
                .andExpect(jsonPath("$.data.metrics.lowStockSkus").value(1))
                .andExpect(jsonPath("$.data.metrics.openTickets").value(1))
                .andExpect(jsonPath("$.data.alerts.length()").value(1));

        mockMvc.perform(get("/bi/api/v1/cockpit")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.totalOrders").value(0))
                .andExpect(jsonPath("$.data.metrics.totalRevenue").value(0))
                .andExpect(jsonPath("$.data.metrics.pendingShipments").value(0))
                .andExpect(jsonPath("$.data.metrics.activeListings").value(0))
                .andExpect(jsonPath("$.data.metrics.lowStockSkus").value(0))
                .andExpect(jsonPath("$.data.metrics.openTickets").value(0))
                .andExpect(jsonPath("$.data.alerts.length()").value(0));
    }

    @Test
    void exposeUnifiedV1DataAnalysisCrossAndExportLifecycle() throws Exception {
        String tenantId = uniqueTenant("analysis-v1");
        String otherTenantId = uniqueTenant("analysis-v1-other");

        mockMvc.perform(post("/bi/api/v1/analysis/cross")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "analysisName":"Unified Cross Analysis",
                                  "rowDimensions":["channel"],
                                  "columnDimensions":["marketplace"],
                                  "metrics":["gross_profit"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisName").value("Unified Cross Analysis"))
                .andExpect(jsonPath("$.data.rows.length()").value(12))
                .andExpect(jsonPath("$.data.totals.gross_profit").isNumber());

        String exportResponse = mockMvc.perform(post("/bi/api/v1/analysis/exports")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "exportName":"Unified Analysis Export",
                                  "exportType":"CROSS_ANALYSIS",
                                  "format":"CSV"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exportName").value("Unified Analysis Export"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String taskId = objectMapper.readTree(exportResponse).at("/data/taskId").asText();

        mockMvc.perform(post("/bi/api/v1/analysis/exports/" + taskId + "/execute")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.fileUrl").value(containsString("/exports/" + tenantId + "/" + taskId)));

        mockMvc.perform(get("/bi/api/v1/analysis/exports/" + taskId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/bi/api/v1/analysis/exports")
                        .header("X-Tenant-Id", tenantId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].taskId").value(taskId));

        mockMvc.perform(get("/bi/api/v1/analysis/exports")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/bi/api/v1/analysis/exports/" + taskId)
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EXPORT_TASK_NOT_FOUND"));
    }

    @Test
    void manageMetricCaliberAndKpiAssessmentLifecycle() throws Exception {
        String tenantId = uniqueTenant("caliber-kpi");
        String otherTenantId = uniqueTenant("caliber-kpi-isolation");

        String caliberResponse = mockMvc.perform(post("/bi/api/in/v1/metrics/calibers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"inventory_turnover",
                                  "metricName":"Inventory Turnover",
                                  "category":"inventory",
                                  "caliberType":"RATIO",
                                  "formula":"{numerator}/{denominator}",
                                  "formulaDescription":"Inventory turnover ratio",
                                  "numeratorMetric":"inventory_outbound_qty",
                                  "denominatorMetric":"average_inventory_qty",
                                  "unit":"PERCENT",
                                  "dataSource":"wms_inventory_fact",
                                  "calculationScope":"WAREHOUSE",
                                  "dimensions":["warehouse_id"],
                                  "excludeConditions":["status=FREEZE"],
                                  "permissionCode":"bi:inventory:read",
                                  "dataLevel":"SUMMARY",
                                  "description":"Inventory turnover caliber"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("inventory_turnover"))
                .andExpect(jsonPath("$.data.version").value("1"))
                .andReturn().getResponse().getContentAsString();
        String caliberId = objectMapper.readTree(caliberResponse).at("/data/caliberId").asText();

        mockMvc.perform(get("/bi/api/in/v1/metrics/calibers")
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].caliberId").value(caliberId));

        mockMvc.perform(get("/bi/api/in/v1/metrics/calibers/inventory_turnover")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caliberId").value(caliberId))
                .andExpect(jsonPath("$.data.calculationScope").value("WAREHOUSE"));

        mockMvc.perform(patch("/bi/api/in/v1/metrics/calibers/" + caliberId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formulaDescription":"Inventory turnover ratio by warehouse and store",
                                  "calculationScope":"WAREHOUSE_STORE",
                                  "dimensions":["warehouse_id","store_id"],
                                  "excludeConditions":["status=FREEZE","warehouse_type=TRANSIT"],
                                  "description":"Updated inventory turnover caliber"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caliberId").value(caliberId))
                .andExpect(jsonPath("$.data.version").value("2"))
                .andExpect(jsonPath("$.data.calculationScope").value("WAREHOUSE_STORE"))
                .andExpect(jsonPath("$.data.dimensions.length()").value(2));

        mockMvc.perform(post("/bi/api/in/v1/metrics/calculate")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"inventory_turnover",
                                  "dimensionKey":"warehouse_id",
                                  "dimensionValue":"WH-US-001",
                                  "numeratorValue":120.00,
                                  "denominatorValue":30.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("inventory_turnover"))
                .andExpect(jsonPath("$.data.dimensionValue").value("WH-US-001"))
                .andExpect(jsonPath("$.data.calculatedValue").value(400.0));

        mockMvc.perform(get("/bi/api/in/v1/metrics/values")
                        .header("X-Tenant-Id", tenantId)
                        .param("metricCode", "inventory_turnover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dimensionKey").value("warehouse_id"));

        String targetResponse = mockMvc.perform(post("/bi/api/in/v1/kpis/targets")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"warehouse_turnover_kpi",
                                  "kpiName":"Warehouse Turnover KPI",
                                  "department":"WMS",
                                  "role":"MANAGER",
                                  "period":"2026-05",
                                  "targetValue":300.00,
                                  "warningValue":240.00,
                                  "excellentValue":360.00,
                                  "unit":"PERCENT",
                                  "metricCode":"inventory_turnover",
                                  "caliberId":"%s",
                                  "applicableRoles":["WMS_MANAGER","BI_ANALYST"],
                                  "scoringRule":"LINEAR",
                                  "weight":50.00
                                }
                                """.formatted(caliberId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiCode").value("warehouse_turnover_kpi"))
                .andExpect(jsonPath("$.data.caliberId").value(caliberId))
                .andReturn().getResponse().getContentAsString();
        String targetId = objectMapper.readTree(targetResponse).at("/data/targetId").asText();

        mockMvc.perform(get("/bi/api/in/v1/kpis/targets")
                        .header("X-Tenant-Id", tenantId)
                        .param("department", "WMS")
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].targetId").value(targetId));

        mockMvc.perform(get("/bi/api/in/v1/kpis/targets/" + targetId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("inventory_turnover"))
                .andExpect(jsonPath("$.data.weight").value(50.0));

        mockMvc.perform(patch("/bi/api/in/v1/kpis/targets/" + targetId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetValue":320.00,
                                  "warningValue":250.00,
                                  "excellentValue":380.00,
                                  "weight":60.00,
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetId").value(targetId))
                .andExpect(jsonPath("$.data.targetValue").value(320.0))
                .andExpect(jsonPath("$.data.weight").value(60.0));

        String assessmentResponse = mockMvc.perform(post("/bi/api/in/v1/kpis/targets/" + targetId + "/assess")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"wms-user-001",
                                  "actualValue":352.00,
                                  "assessorId":"bi-manager-001",
                                  "comment":"Warehouse turnover exceeded target"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetId").value(targetId))
                .andExpect(jsonPath("$.data.status").value("ON_TRACK"))
                .andExpect(jsonPath("$.data.score").value(110.0))
                .andReturn().getResponse().getContentAsString();
        String assessmentId = objectMapper.readTree(assessmentResponse).at("/data/assessmentId").asText();

        mockMvc.perform(get("/bi/api/in/v1/kpis/assessments")
                        .header("X-Tenant-Id", tenantId)
                        .param("userId", "wms-user-001")
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].assessmentId").value(assessmentId));

        mockMvc.perform(get("/bi/api/in/v1/kpis/assessments/" + assessmentId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assessmentId").value(assessmentId))
                .andExpect(jsonPath("$.data.comment").value("Warehouse turnover exceeded target"));

        mockMvc.perform(get("/bi/api/in/v1/kpis/targets/" + targetId + "/assessments")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].assessmentId").value(assessmentId));

        mockMvc.perform(get("/bi/api/in/v1/kpis/departments/WMS/score")
                        .header("X-Tenant-Id", tenantId)
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(110.0));

        mockMvc.perform(get("/bi/api/in/v1/metrics/values")
                        .header("X-Tenant-Id", otherTenantId)
                        .param("metricCode", "inventory_turnover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/bi/api/in/v1/kpis/targets/" + targetId)
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TARGET_NOT_FOUND"));
    }

    @Test
    void exposeUnifiedV1MetricCaliberAndKpiStatistics() throws Exception {
        String tenantId = uniqueTenant("kpi-stats");
        String otherTenantId = uniqueTenant("kpi-stats-other");

        String caliberResponse = mockMvc.perform(post("/bi/api/v1/metrics/calibers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"inventory_turnover_stat",
                                  "metricName":"Inventory Turnover Stat",
                                  "category":"inventory",
                                  "caliberType":"RATIO",
                                  "formula":"{numerator}/{denominator}",
                                  "formulaDescription":"Inventory turnover ratio for KPI statistics",
                                  "numeratorMetric":"inventory_outbound_qty",
                                  "denominatorMetric":"average_inventory_qty",
                                  "unit":"PERCENT",
                                  "dataSource":"wms_inventory_fact",
                                  "calculationScope":"WAREHOUSE",
                                  "dimensions":["warehouse_id"],
                                  "excludeConditions":["status=FREEZE"],
                                  "permissionCode":"bi:inventory:read",
                                  "dataLevel":"SUMMARY",
                                  "description":"Inventory turnover caliber for unified v1 statistics"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("inventory_turnover_stat"))
                .andReturn().getResponse().getContentAsString();
        String caliberId = objectMapper.readTree(caliberResponse).at("/data/caliberId").asText();

        mockMvc.perform(get("/bi/api/v1/metrics/calibers")
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].caliberId").value(caliberId));

        String managerTargetResponse = mockMvc.perform(post("/bi/api/v1/kpis/targets")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"warehouse_turnover_manager",
                                  "kpiName":"Warehouse Turnover Manager KPI",
                                  "department":"WMS",
                                  "role":"MANAGER",
                                  "period":"2026-05",
                                  "targetValue":300.00,
                                  "warningValue":240.00,
                                  "excellentValue":360.00,
                                  "unit":"PERCENT",
                                  "metricCode":"inventory_turnover_stat",
                                  "caliberId":"%s",
                                  "applicableRoles":["WMS_MANAGER"],
                                  "scoringRule":"LINEAR",
                                  "weight":60.00
                                }
                                """.formatted(caliberId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiCode").value("warehouse_turnover_manager"))
                .andReturn().getResponse().getContentAsString();
        String managerTargetId = objectMapper.readTree(managerTargetResponse).at("/data/targetId").asText();

        String operatorTargetResponse = mockMvc.perform(post("/bi/api/v1/kpis/targets")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"warehouse_turnover_operator",
                                  "kpiName":"Warehouse Turnover Operator KPI",
                                  "department":"WMS",
                                  "role":"OPERATOR",
                                  "period":"2026-05",
                                  "targetValue":100.00,
                                  "warningValue":80.00,
                                  "excellentValue":120.00,
                                  "unit":"PERCENT",
                                  "metricCode":"inventory_turnover_stat",
                                  "caliberId":"%s",
                                  "applicableRoles":["WMS_OPERATOR"],
                                  "scoringRule":"LINEAR",
                                  "weight":40.00
                                }
                                """.formatted(caliberId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kpiCode").value("warehouse_turnover_operator"))
                .andReturn().getResponse().getContentAsString();
        String operatorTargetId = objectMapper.readTree(operatorTargetResponse).at("/data/targetId").asText();

        mockMvc.perform(post("/bi/api/v1/kpis/targets/" + managerTargetId + "/assess")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"wms-manager-001",
                                  "actualValue":360.00,
                                  "assessorId":"bi-director-001",
                                  "comment":"Manager target reached excellent line"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXCELLENT"))
                .andExpect(jsonPath("$.data.score").value(120.0));

        mockMvc.perform(post("/bi/api/v1/kpis/targets/" + operatorTargetId + "/assess")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"wms-operator-001",
                                  "actualValue":75.00,
                                  "assessorId":"bi-director-001",
                                  "comment":"Operator target below warning line"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CRITICAL"))
                .andExpect(jsonPath("$.data.score").value(75.0));

        mockMvc.perform(get("/bi/api/v1/kpis/statistics")
                        .header("X-Tenant-Id", tenantId)
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetCount").value(2))
                .andExpect(jsonPath("$.data.assessedCount").value(2))
                .andExpect(jsonPath("$.data.assessedTargetCount").value(2))
                .andExpect(jsonPath("$.data.completionRate").value(100.0))
                .andExpect(jsonPath("$.data.avgAchievementRate").value(97.5))
                .andExpect(jsonPath("$.data.avgScore").value(97.5))
                .andExpect(jsonPath("$.data.statusCounts.EXCELLENT").value(1))
                .andExpect(jsonPath("$.data.statusCounts.CRITICAL").value(1))
                .andExpect(jsonPath("$.data.departmentStats.length()").value(1))
                .andExpect(jsonPath("$.data.departmentStats[0].dimensionValue").value("WMS"))
                .andExpect(jsonPath("$.data.departmentStats[0].avgScore").value(97.5))
                .andExpect(jsonPath("$.data.teamStats[0].dimensionValue").value("WMS"))
                .andExpect(jsonPath("$.data.roleStats.length()").value(2))
                .andExpect(jsonPath("$.data.roleStats[0].dimensionValue").value("MANAGER"))
                .andExpect(jsonPath("$.data.roleStats[0].avgScore").value(120.0))
                .andExpect(jsonPath("$.data.roleStats[1].dimensionValue").value("OPERATOR"))
                .andExpect(jsonPath("$.data.roleStats[1].avgScore").value(75.0))
                .andExpect(jsonPath("$.data.userStats.length()").value(2))
                .andExpect(jsonPath("$.data.userStats[0].dimensionValue").value("wms-manager-001"))
                .andExpect(jsonPath("$.data.userStats[0].avgScore").value(120.0))
                .andExpect(jsonPath("$.data.userStats[1].dimensionValue").value("wms-operator-001"))
                .andExpect(jsonPath("$.data.userStats[1].avgScore").value(75.0));

        mockMvc.perform(get("/bi/api/v1/kpis/statistics")
                        .header("X-Tenant-Id", tenantId)
                        .param("period", "2026-05")
                        .param("role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetCount").value(1))
                .andExpect(jsonPath("$.data.avgScore").value(120.0))
                .andExpect(jsonPath("$.data.roleStats.length()").value(1))
                .andExpect(jsonPath("$.data.roleStats[0].dimensionValue").value("MANAGER"));

        mockMvc.perform(get("/bi/api/v1/kpis/statistics")
                        .header("X-Tenant-Id", otherTenantId)
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetCount").value(0))
                .andExpect(jsonPath("$.data.assessedCount").value(0))
                .andExpect(jsonPath("$.data.departmentStats.length()").value(0))
                .andExpect(jsonPath("$.data.userStats.length()").value(0));
    }

    @Test
    void manageCustomReportsAndDeveloperCommission() throws Exception {
        String tenantId = uniqueTenant("advanced");
        String otherTenantId = uniqueTenant("advanced-isolation");

        mockMvc.perform(post("/bi/api/in/v1/dimensions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionCode":"store_id",
                                  "dimensionName":"Store",
                                  "dimensionType":"STORE",
                                  "sourceField":"store_id",
                                  "description":"Store dimension"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionCode").value("store_id"));

        mockMvc.perform(post("/bi/api/in/v1/dimensions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionCode":"marketplace_id",
                                  "dimensionName":"Marketplace",
                                  "dimensionType":"MARKETPLACE",
                                  "sourceField":"marketplace_id",
                                  "description":"Marketplace dimension"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionCode").value("marketplace_id"));

        mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"gross_profit",
                                  "metricName":"Gross Profit",
                                  "category":"profit",
                                  "formula":"sum(gross_profit)",
                                  "unit":"USD",
                                  "permissionCode":"bi:profit:read",
                                  "dataLevel":"SUMMARY",
                                  "description":"Gross profit metric",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("gross_profit"));

        mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"gross_margin",
                                  "metricName":"Gross Margin",
                                  "category":"profit",
                                  "formula":"avg(gross_margin)",
                                  "unit":"PERCENT",
                                  "permissionCode":"bi:profit:read",
                                  "dataLevel":"SUMMARY",
                                  "description":"Gross margin metric",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("gross_margin"));

        String customReportResponse = mockMvc.perform(post("/bi/api/in/v1/custom-reports")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportCode":"profit_overview",
                                  "reportName":"Profit Overview",
                                  "subjectArea":"FMS_PROFIT",
                                  "dimensions":["store_id","marketplace_id"],
                                  "metrics":["gross_profit","gross_margin"],
                                  "filters":{"period":"2026-05","currency":"USD"},
                                  "sorts":["gross_profit:DESC"],
                                  "visibility":"SHARED",
                                  "permissionCode":"bi:report:finance",
                                  "dataLevel":"SUMMARY",
                                  "description":"Finance profit report"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCode").value("profit_overview"))
                .andExpect(jsonPath("$.data.subjectArea").value("FMS_PROFIT"))
                .andExpect(jsonPath("$.data.dimensions.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String customReportId = objectMapper.readTree(customReportResponse).at("/data/reportId").asText();

        mockMvc.perform(post("/bi/api/in/v1/custom-reports")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportCode":"profit_overview",
                                  "reportName":"Duplicate Profit Overview",
                                  "subjectArea":"FMS_PROFIT",
                                  "dimensions":["store_id"],
                                  "metrics":["gross_profit"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CUSTOM_REPORT_DUPLICATED"));

        mockMvc.perform(get("/bi/api/in/v1/custom-reports")
                        .header("X-Tenant-Id", tenantId)
                        .param("subjectArea", "FMS_PROFIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reportId").value(customReportId));

        mockMvc.perform(get("/bi/api/in/v1/custom-reports/" + customReportId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportName").value("Profit Overview"))
                .andExpect(jsonPath("$.data.filters.period").value("2026-05"));

        mockMvc.perform(post("/bi/api/in/v1/custom-reports/" + customReportId + "/run")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.snapshotName").value("Profit Overview"))
                .andExpect(jsonPath("$.data.snapshotData").value(containsString("\"reportCode\":\"profit_overview\"")))
                .andExpect(jsonPath("$.data.snapshotData").value(containsString("\"subjectArea\":\"FMS_PROFIT\"")));

        mockMvc.perform(post("/bi/api/in/v1/custom-reports/" + customReportId + "/exports")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "format":"CSV"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exportType").value("CUSTOM_REPORT"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        String commissionResponse = mockMvc.perform(post("/bi/api/in/v1/developer-commission")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"dev-100",
                                  "userName":"Alice",
                                  "teamCode":"PDM-A",
                                  "period":"2026-05",
                                  "skuCount":20,
                                  "orderCount":10,
                                  "salesProfit":10000.00,
                                  "kpiScore":105.00,
                                  "baseCommissionRate":8.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("dev-100"))
                .andExpect(jsonPath("$.data.orderRate").value(50.0))
                .andExpect(jsonPath("$.data.commissionCoefficient").value(0.5250))
                .andExpect(jsonPath("$.data.commissionAmount").value(420.0))
                .andReturn().getResponse().getContentAsString();
        String commissionReportId = objectMapper.readTree(commissionResponse).at("/data/reportId").asText();

        mockMvc.perform(get("/bi/api/in/v1/developer-commission")
                        .header("X-Tenant-Id", tenantId)
                        .param("period", "2026-05")
                        .param("userId", "dev-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reportId").value(commissionReportId));

        mockMvc.perform(get("/bi/api/in/v1/developer-commission/" + commissionReportId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamCode").value("PDM-A"))
                .andExpect(jsonPath("$.data.period").value("2026-05"));

        mockMvc.perform(get("/bi/api/in/v1/custom-reports")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/bi/api/in/v1/developer-commission")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void exportBiOutboundReportKpisMetricsDashboardAndAlerts() throws Exception {
        String tenantId = uniqueTenant("outbound");

        mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"sales_gmv_export",
                                  "metricName":"Sales GMV Export",
                                  "category":"sales",
                                  "formula":"sum(order_amount)",
                                  "unit":"USD",
                                  "permissionCode":"bi:metric:read",
                                  "dataLevel":"SUMMARY",
                                  "description":"Outbound metric export",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("sales_gmv_export"));

        mockMvc.perform(post("/bi/api/in/v1/reports")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportCode":"sales_outbound_board",
                                  "reportName":"Sales Outbound Board",
                                  "reportType":"COCKPIT",
                                  "dataSource":"som_sales_fact",
                                  "queryText":"select * from som_sales_fact"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCode").value("sales_outbound_board"));

        String dashboardResponse = mockMvc.perform(post("/bi/api/in/v1/dashboards")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dashboardName":"Sales Cockpit",
                                  "dashboardType":"sales",
                                  "config":{
                                    "layout":"two-column",
                                    "reportCode":"sales_outbound_board"
                                  },
                                  "owner":"ops-owner"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dashboardName").value("Sales Cockpit"))
                .andReturn().getResponse().getContentAsString();
        String dashboardId = objectMapper.readTree(dashboardResponse).at("/data/dashboardId").asText();

        mockMvc.perform(put("/bi/api/in/v1/dashboards/" + dashboardId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dashboardName":"Sales Cockpit Updated",
                                  "dashboardType":"sales",
                                  "config":{
                                    "layout":"focus",
                                    "reportCode":"sales_outbound_board",
                                    "highlight":"profit"
                                  },
                                  "owner":"ops-owner"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dashboardName").value("Sales Cockpit Updated"))
                .andExpect(jsonPath("$.data.config.layout").value("focus"))
                .andExpect(jsonPath("$.data.config.highlight").value("profit"));

        mockMvc.perform(post("/bi/api/in/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"sales_goal_export",
                                  "kpiName":"Sales Goal Export",
                                  "category":"sales",
                                  "value":60.00,
                                  "targetValue":100.00,
                                  "unit":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFF_TRACK"));

        mockMvc.perform(get("/bi/api/out/v1/reports/sales_outbound_board/data")
                        .header("X-Tenant-Id", tenantId)
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCode").value("sales_outbound_board"))
                .andExpect(jsonPath("$.data.reportName").value("Sales Outbound Board"))
                .andExpect(jsonPath("$.data.period").value("2026-05"))
                .andExpect(jsonPath("$.data.dataSource").value("som_sales_fact"))
                .andExpect(jsonPath("$.data.format").value("json"))
                .andExpect(jsonPath("$.data.snapshotData").value(containsString("\"dataSource\":\"som_sales_fact\"")));

        mockMvc.perform(get("/bi/api/out/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].kpiCode").value("sales_goal_export"))
                .andExpect(jsonPath("$.data[0].status").value("OFF_TRACK"))
                .andExpect(jsonPath("$.data[0].achievementRate").value(60.0));

        mockMvc.perform(get("/bi/api/out/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].metricCode").value("sales_gmv_export"));

        mockMvc.perform(get("/bi/api/out/v1/dashboards/" + dashboardId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dashboardId").value(dashboardId))
                .andExpect(jsonPath("$.data.dashboardName").value("Sales Cockpit Updated"))
                .andExpect(jsonPath("$.data.config.layout").value("focus"))
                .andExpect(jsonPath("$.data.config.highlight").value("profit"))
                .andExpect(jsonPath("$.data.cockpitSummary.metricCount").value(1))
                .andExpect(jsonPath("$.data.cockpitSummary.reportCount").value(1));

        mockMvc.perform(get("/bi/api/out/v1/alerts")
                        .header("X-Tenant-Id", tenantId)
                        .param("alertType", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].kpiCode").value("sales_goal_export"))
                .andExpect(jsonPath("$.data[0].severity").value("CRITICAL"));
    }

    @Test
    void submitPmsTrendPredictionApproveAndSyncDashboardLifecycle() throws Exception {
        String tenantId = uniqueTenant("pms-trend");

        mockMvc.perform(post("/api/pms/v1/ai-toggles")
                        .header("tenant_id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "feature_code":"BI_AI_TREND",
                                  "feature_name":"BI AI Trend",
                                  "domain":"BI",
                                  "enabled":true,
                                  "description":"Enable PMS to submit BI insight and trend prediction recommendations.",
                                  "config_json":"{}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("BI"))
                .andExpect(jsonPath("$.data.enabled").value(true));

        String recommendationResponse = mockMvc.perform(pmsBiWriteHeaders(post("/api/pms/v1/recommendations"),
                                tenantId, "idem-bi-trend-rec-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recommendation_id":"pms-bi-trend-001",
                                  "domain":"BI",
                                  "recommendation_type":"AI_TREND_PREDICTION",
                                  "object_type":"PENDING_ACTION",
                                  "target_object_type":"CATEGORY",
                                  "target_object_id":"home",
                                  "content":"PMS predicts a home-category GMV downtrend and recommends distributing an AI insight card after BI approval.",
                                  "score":93.4,
                                  "confidence":0.91,
                                  "evidence_chain_id":"ev-bi-trend-001",
                                  "data_sources":["ERP_BI_FACT","PMS_TREND_AGENT"],
                                  "risk_flags":["BI_APPROVAL_REQUIRED"],
                                  "explainability":"based on week-over-week GMV decline and traffic conversion weakening",
                                  "requested_action":"CREATE_BI_TREND_INSIGHT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andReturn().getResponse().getContentAsString();
        String erpReferenceId = objectMapper.readTree(recommendationResponse).at("/data/erpReferenceId").asText();

        String submitResponse = mockMvc.perform(pmsBiWriteHeaders(post("/bi/api/in/v1/pms/trend-predict"),
                                tenantId, "idem-bi-trend-submit-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "erpReferenceId":"%s",
                                  "title":"Home Sales GMV may weaken next week",
                                  "summary":"PMS predicts Amazon US home-category GMV will continue declining unless budget and creative are adjusted within 24 hours.",
                                  "insightType":"trend",
                                  "severity":"high",
                                  "category":"home",
                                  "metricCode":"home_sales_gmv",
                                  "metricName":"Home Sales GMV",
                                  "targetUserId":"ops-bi-001",
                                  "trendPeriod":"P7D",
                                  "predictionPoints":[
                                    {"timestamp":"2026-05-01T00:00:00Z","value":1200.00,"targetValue":1300.00},
                                    {"timestamp":"2026-05-02T00:00:00Z","value":1100.00,"targetValue":1300.00},
                                    {"timestamp":"2026-05-03T00:00:00Z","value":980.00,"targetValue":1300.00}
                                  ],
                                  "suggestion":"Increase sponsored-products budget by 15%% and refresh the top three creatives immediately.",
                                  "actionUrl":"/platform/bi/api/v1/cockpit/home-sales",
                                  "insightData":{
                                    "channel":"AMAZON",
                                    "marketplace":"AMAZON-US",
                                    "predictedDeviationRate":-0.18
                                  }
                                }
                                """.formatted(erpReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.erpReferenceId").value(erpReferenceId))
                .andExpect(jsonPath("$.data.metricCode").value("home_sales_gmv"))
                .andExpect(jsonPath("$.data.targetUserId").value("ops-bi-001"))
                .andReturn().getResponse().getContentAsString();
        String insightId = objectMapper.readTree(submitResponse).at("/data/insightId").asText();

        mockMvc.perform(pmsBiWriteHeaders(post("/bi/api/in/v1/pms/trend-predict"),
                                tenantId, "idem-bi-trend-submit-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "erpReferenceId":"%s",
                                  "title":"Home Sales GMV may weaken next week",
                                  "summary":"PMS predicts Amazon US home-category GMV will continue declining unless budget and creative are adjusted within 24 hours.",
                                  "insightType":"trend",
                                  "severity":"high",
                                  "category":"home",
                                  "metricCode":"home_sales_gmv",
                                  "metricName":"Home Sales GMV",
                                  "targetUserId":"ops-bi-001",
                                  "trendPeriod":"P7D",
                                  "predictionPoints":[
                                    {"timestamp":"2026-05-01T00:00:00Z","value":1200.00,"targetValue":1300.00},
                                    {"timestamp":"2026-05-02T00:00:00Z","value":1100.00,"targetValue":1300.00},
                                    {"timestamp":"2026-05-03T00:00:00Z","value":980.00,"targetValue":1300.00}
                                  ],
                                  "suggestion":"Increase sponsored-products budget by 15%% and refresh the top three creatives immediately.",
                                  "actionUrl":"/platform/bi/api/v1/cockpit/home-sales",
                                  "insightData":{
                                    "channel":"AMAZON",
                                    "marketplace":"AMAZON-US",
                                    "predictedDeviationRate":-0.18
                                  }
                                }
                                """.formatted(erpReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.insightId").value(insightId))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        mockMvc.perform(get("/bi/api/in/v1/pms/trend-predict/" + insightId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.insightId").value(insightId))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        String approveResponse = mockMvc.perform(post("/bi/api/in/v1/pms/trend-predict/" + insightId + "/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approvedBy":"bi.ai.manager"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.approvedBy").value("bi.ai.manager"))
                .andExpect(jsonPath("$.data.generatedTrendId").isNotEmpty())
                .andExpect(jsonPath("$.data.dashboardCardId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String generatedTrendId = objectMapper.readTree(approveResponse).at("/data/generatedTrendId").asText();
        String dashboardCardId = objectMapper.readTree(approveResponse).at("/data/dashboardCardId").asText();

        mockMvc.perform(get("/bi/api/in/v1/trends")
                        .header("X-Tenant-Id", tenantId)
                        .param("metricCode", "home_sales_gmv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].trendId").value(generatedTrendId))
                .andExpect(jsonPath("$.data[0].metricCode").value("home_sales_gmv"))
                .andExpect(jsonPath("$.data[0].dataPoints.length()").value(3));

        mockMvc.perform(get("/dashboard/api/in/v1/ai-insights/cards")
                        .header("X-Tenant-Id", tenantId)
                        .param("userId", "ops-bi-001")
                        .param("insightType", "trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].cardId").value(dashboardCardId))
                .andExpect(jsonPath("$.data[0].title").value("Home Sales GMV may weaken next week"))
                .andExpect(jsonPath("$.data[0].sourceDomain").value("BI"))
                .andExpect(jsonPath("$.data[0].data.metricCode").value("home_sales_gmv"))
                .andExpect(jsonPath("$.data[0].data.generatedTrendId").value(generatedTrendId));

        mockMvc.perform(pmsBiWriteHeaders(get("/api/pms/v1/feedbacks/pending"), tenantId,
                        "idem-bi-trend-feedback-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].erpReferenceId").value(erpReferenceId))
                .andExpect(jsonPath("$.data[0].domain").value("BI"))
                .andExpect(jsonPath("$.data[0].executionStatus").value("EXECUTED"));
    }

    @Test
    void analyzeFbaShipmentLeadTimeCostAndExceptions() throws Exception {
        String tenantId = uniqueTenant("fba-analysis");
        String otherTenantId = uniqueTenant("fba-analysis-other");

        String shipmentResponse = mockMvc.perform(post("/fba/api/in/v1/shipments")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amazonShipmentId":"FBA-ANALYSIS-100",
                                  "destinationFc":"ONT8",
                                  "carrier":"UPS",
                                  "plannedQuantity":6,
                                  "items":[
                                    {
                                      "productId":"PROD-FBA-100",
                                      "sellerSku":"SKU-FBA-100",
                                      "fnsku":"FNSKU-FBA-100",
                                      "quantity":6,
                                      "boxQuantity":3
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/fbaShipmentId").asText();

        mockMvc.perform(patch("/fba/api/in/v1/shipments/" + shipmentId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/pack")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartonCount":3,
                                  "totalWeight":18.20,
                                  "carrier":"UPS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PACKED"));

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/ship")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrier":"UPS",
                                  "trackingNo":"1Z-FBA-ANALYSIS-100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        mockMvc.perform(patch("/fba/api/in/v1/shipments/" + shipmentId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivedQuantity":6
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"SHIPPING_COST",
                                  "sourceType":"SHIPMENT",
                                  "sourceId":"%s",
                                  "sellerSku":"SKU-FBA-100",
                                  "storeId":"AMZ-US-01",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":35.50
                                }
                                """.formatted(shipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costType").value("SHIPPING_COST"));

        mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"FBA_FEE",
                                  "sourceType":"SHIPMENT",
                                  "sourceId":"%s",
                                  "sellerSku":"SKU-FBA-100",
                                  "storeId":"AMZ-US-01",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":12.75
                                }
                                """.formatted(shipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costType").value("FBA_FEE"));

        String damagedResponse = mockMvc.perform(post("/fba/api/in/v1/shipment-exceptions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shipmentId":"%s",
                                  "type":"DAMAGED",
                                  "qty":1,
                                  "description":"one carton damaged in transit"
                                }
                                """.formatted(shipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        String damagedExceptionId = objectMapper.readTree(damagedResponse).at("/data/exceptionId").asText();

        String lostResponse = mockMvc.perform(post("/fba/api/in/v1/shipment-exceptions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shipmentId":"%s",
                                  "type":"LOST",
                                  "qty":1,
                                  "description":"one carton lost during handover"
                                }
                                """.formatted(shipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        String lostExceptionId = objectMapper.readTree(lostResponse).at("/data/exceptionId").asText();

        mockMvc.perform(patch("/fba/api/in/v1/shipment-exceptions/" + lostExceptionId + "/resolve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resolvedBy":"fba.ops.manager"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(get("/bi/api/v1/fba-shipment-analysis")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.shipmentCount").value(1))
                .andExpect(jsonPath("$.data.summary.closedShipmentCount").value(1))
                .andExpect(jsonPath("$.data.summary.exceptionShipmentCount").value(1))
                .andExpect(jsonPath("$.data.summary.exceptionCount").value(2))
                .andExpect(jsonPath("$.data.summary.totalPlannedQuantity").value(6))
                .andExpect(jsonPath("$.data.summary.totalReceivedQuantity").value(6))
                .andExpect(jsonPath("$.data.summary.averageReceiveRate").value(100.0))
                .andExpect(jsonPath("$.data.summary.totalShippingCost").value(35.5))
                .andExpect(jsonPath("$.data.summary.totalFbaFee").value(12.75))
                .andExpect(jsonPath("$.data.summary.totalLogisticsCost").value(48.25))
                .andExpect(jsonPath("$.data.shipments.length()").value(1))
                .andExpect(jsonPath("$.data.shipments[0].shipmentId").value(shipmentId))
                .andExpect(jsonPath("$.data.shipments[0].amazonShipmentId").value("FBA-ANALYSIS-100"))
                .andExpect(jsonPath("$.data.shipments[0].status").value("CLOSED"))
                .andExpect(jsonPath("$.data.shipments[0].receiveRate").value(100.0))
                .andExpect(jsonPath("$.data.shipments[0].shippingCost").value(35.5))
                .andExpect(jsonPath("$.data.shipments[0].fbaFee").value(12.75))
                .andExpect(jsonPath("$.data.shipments[0].totalCost").value(48.25))
                .andExpect(jsonPath("$.data.shipments[0].exceptionCount").value(2))
                .andExpect(jsonPath("$.data.shipments[0].openExceptionCount").value(1))
                .andExpect(jsonPath("$.data.shipments[0].exceptionTypes.length()").value(2))
                .andExpect(jsonPath("$.data.shipments[0].exceptionTypes[0]").value("DAMAGED"))
                .andExpect(jsonPath("$.data.shipments[0].exceptionTypes[1]").value("LOST"))
                .andExpect(jsonPath("$.data.shipments[0].processingHours").isNumber())
                .andExpect(jsonPath("$.data.shipments[0].transitHours").isNumber());

        mockMvc.perform(get("/bi/api/v1/fba-shipment-analysis")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.shipmentCount").value(0))
                .andExpect(jsonPath("$.data.shipments.length()").value(0));
    }

    @Test
    void monitorOperationSignalsAcrossSalesPriceBuyboxInventoryAndReviews() throws Exception {
        String tenantId = uniqueTenant("operation-monitor");
        String otherTenantId = uniqueTenant("operation-monitor-other");

        mockMvc.perform(post("/bi/api/in/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Listing Orders Alert",
                                  "metricCode":"listing_orders",
                                  "domain":"operation-monitor",
                                  "condition":"LESS_THAN",
                                  "threshold":"1",
                                  "severity":"CRITICAL",
                                  "notifyChannel":"DINGTALK",
                                  "notifyTargets":"ops-group"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Price Gap Alert",
                                  "metricCode":"price_gap_rate",
                                  "domain":"operation-monitor",
                                  "condition":"GREATER_THAN",
                                  "threshold":"5",
                                  "severity":"WARNING",
                                  "notifyChannel":"EMAIL",
                                  "notifyTargets":"pricing@erp.test"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Buybox Hijacker Alert",
                                  "metricCode":"buybox_hijacker_count",
                                  "domain":"operation-monitor",
                                  "condition":"GREATER_THAN",
                                  "threshold":"0",
                                  "severity":"CRITICAL",
                                  "notifyChannel":"SMS",
                                  "notifyTargets":"ops-manager"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Inventory Alert",
                                  "metricCode":"inventory_available",
                                  "domain":"operation-monitor",
                                  "condition":"LESS_THAN",
                                  "threshold":"5",
                                  "severity":"CRITICAL",
                                  "notifyChannel":"DINGTALK",
                                  "notifyTargets":"wms-group"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Rating Alert",
                                  "metricCode":"average_rating",
                                  "domain":"operation-monitor",
                                  "condition":"LESS_THAN",
                                  "threshold":"4",
                                  "severity":"WARNING",
                                  "notifyChannel":"EMAIL",
                                  "notifyTargets":"crm@erp.test"
                                }
                                """))
                .andExpect(status().isOk());

        String storeResponse = mockMvc.perform(post("/som/api/in/v1/stores")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform":"amazon",
                                  "storeCode":"AMZ-OPS-US",
                                  "storeName":"Amazon Ops US"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String storeId = objectMapper.readTree(storeResponse).at("/data/storeId").asText();

        mockMvc.perform(patch("/som/api/in/v1/stores/" + storeId + "/connect")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        String listingResponse = mockMvc.perform(post("/som/api/in/v1/listings")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId":"PROD-OPS-100",
                                  "storeId":"%s",
                                  "title":"Operations Monitor Listing",
                                  "description":"BI monitor validation listing",
                                  "price":20.00,
                                  "originalPrice":30.00,
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "marketplaceListingId":"LISTING-OPS-100"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String listingId = objectMapper.readTree(listingResponse).at("/data/listingId").asText();

        mockMvc.perform(patch("/som/api/in/v1/listings/" + listingId + "/publish")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/som/api/in/v1/channel-skus")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productSku":"PROD-OPS-100",
                                  "channel":"amazon",
                                  "channelSku":"SKU-OPS-100",
                                  "storeId":"%s",
                                  "marketplaceId":"AMAZON-US"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelSku").value("SKU-OPS-100"));

        mockMvc.perform(post("/som/api/in/v1/listings/" + listingId + "/performance")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId":"%s",
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "impressions":1200,
                                  "clicks":42,
                                  "ctr":3.50,
                                  "spend":18.60,
                                  "sales":0.00,
                                  "acos":0.00,
                                  "orders":0,
                                  "conversionRate":0.00,
                                  "periodStart":"2026-05-01T00:00:00Z",
                                  "periodEnd":"2026-05-07T23:59:59Z"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orders").value(0));

        mockMvc.perform(post("/som/api/in/v1/alerts")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId":"%s",
                                  "alertType":"price_anomaly",
                                  "severity":"WARNING",
                                  "message":"Price exceeds competitive band",
                                  "relatedSku":"SKU-OPS-100",
                                  "metricValue":20.00,
                                  "thresholdValue":18.00
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acknowledged").value(false));

        mockMvc.perform(post("/som/api/v1/buybox-monitors")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId":"%s",
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "winnerName":"competitor-A",
                                  "winnerPrice":18.00,
                                  "ourPrice":20.00,
                                  "hijackerCount":2
                                }
                                """.formatted(listingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hijackerCount").value(2));

        mockMvc.perform(post("/som/api/v1/hijack-alerts")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId":"%s",
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "hijackerName":"competitor-A",
                                  "hijackerPrice":18.00,
                                  "ourPrice":20.00,
                                  "severity":"CRITICAL"
                                }
                                """.formatted(listingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"WH-OPS-US",
                                  "name":"Operations Warehouse US",
                                  "type":"SELF",
                                  "countryCode":"US",
                                  "address":"Los Angeles",
                                  "contactPerson":"ops",
                                  "phone":"123456789"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"SKU-OPS-100",
                                  "quantity":3
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(3));

        mockMvc.perform(post("/crm/api/in/v1/review-analyses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerSku":"SKU-OPS-100",
                                  "marketplaceId":"AMAZON-US",
                                  "averageRating":3.4,
                                  "totalReviews":18,
                                  "positiveCount":8,
                                  "neutralCount":4,
                                  "negativeCount":6,
                                  "sentimentSummary":"negative delivery and packaging feedback"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(3.4));

        mockMvc.perform(get("/bi/api/v1/operation-monitor")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.monitoredListingCount").value(1))
                .andExpect(jsonPath("$.data.summary.abnormalListingCount").value(1))
                .andExpect(jsonPath("$.data.summary.criticalListingCount").value(1))
                .andExpect(jsonPath("$.data.summary.salesAnomalyCount").value(1))
                .andExpect(jsonPath("$.data.summary.priceAnomalyCount").value(1))
                .andExpect(jsonPath("$.data.summary.buyboxAnomalyCount").value(1))
                .andExpect(jsonPath("$.data.summary.inventoryAnomalyCount").value(1))
                .andExpect(jsonPath("$.data.summary.reviewAnomalyCount").value(1))
                .andExpect(jsonPath("$.data.summary.pendingAlertCount").value(2))
                .andExpect(jsonPath("$.data.summary.enabledRuleCount").value(5))
                .andExpect(jsonPath("$.data.activeRules.length()").value(5))
                .andExpect(jsonPath("$.data.listings.length()").value(1))
                .andExpect(jsonPath("$.data.listings[0].listingId").value(listingId))
                .andExpect(jsonPath("$.data.listings[0].sellerSku").value("SKU-OPS-100"))
                .andExpect(jsonPath("$.data.listings[0].inventoryAvailable").value(3))
                .andExpect(jsonPath("$.data.listings[0].averageRating").value(3.4))
                .andExpect(jsonPath("$.data.listings[0].hijackerCount").value(2))
                .andExpect(jsonPath("$.data.listings[0].anomalyTypes.length()").value(5))
                .andExpect(jsonPath("$.data.listings[0].anomalies.length()").value(5));

        mockMvc.perform(get("/bi/api/v1/operation-monitor")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.monitoredListingCount").value(0))
                .andExpect(jsonPath("$.data.summary.abnormalListingCount").value(0))
                .andExpect(jsonPath("$.data.summary.enabledRuleCount").value(0))
                .andExpect(jsonPath("$.data.listings.length()").value(0));
    }

    @Test
    void aggregateAlertCenterAcrossOperationProfitKpiAndLogisticsSignals() throws Exception {
        String tenantId = uniqueTenant("alert-center");
        String otherTenantId = uniqueTenant("alert-center-other");

        mockMvc.perform(post("/bi/api/in/v1/alert-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"Inventory Critical Alert",
                                  "metricCode":"inventory_available",
                                  "domain":"operation-monitor",
                                  "condition":"LESS_THAN",
                                  "threshold":"5",
                                  "severity":"CRITICAL",
                                  "notifyChannel":"DINGTALK",
                                  "notifyTargets":"wms-alert-group"
                                }
                                """))
                .andExpect(status().isOk());

        String storeResponse = mockMvc.perform(post("/som/api/in/v1/stores")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform":"amazon",
                                  "storeCode":"AMZ-ALERT-US",
                                  "storeName":"Amazon Alert US"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String storeId = objectMapper.readTree(storeResponse).at("/data/storeId").asText();

        mockMvc.perform(patch("/som/api/in/v1/stores/" + storeId + "/connect")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        String listingResponse = mockMvc.perform(post("/som/api/in/v1/listings")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId":"PROD-ALERT-100",
                                  "storeId":"%s",
                                  "title":"Alert Center Listing",
                                  "description":"Unified alert center validation listing",
                                  "price":25.00,
                                  "originalPrice":35.00,
                                  "platform":"amazon",
                                  "marketplace":"AMAZON-US",
                                  "marketplaceListingId":"LISTING-ALERT-100"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String listingId = objectMapper.readTree(listingResponse).at("/data/listingId").asText();

        mockMvc.perform(patch("/som/api/in/v1/listings/" + listingId + "/publish")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/som/api/in/v1/channel-skus")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productSku":"PROD-ALERT-100",
                                  "channel":"amazon",
                                  "channelSku":"SKU-ALERT-100",
                                  "storeId":"%s",
                                  "marketplaceId":"AMAZON-US"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelSku").value("SKU-ALERT-100"));

        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"WH-ALERT-US",
                                  "name":"Alert Warehouse US",
                                  "type":"SELF",
                                  "countryCode":"US",
                                  "address":"Seattle",
                                  "contactPerson":"alert-ops",
                                  "phone":"123456789"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"SKU-ALERT-100",
                                  "quantity":3
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(3));

        String productCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-ALERT-100",
                                  "sellerSku":"SKU-ALERT-100",
                                  "storeId":"%s",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":120.00
                                }
                                """.formatted(storeId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String productCostEventId = objectMapper.readTree(productCostResponse).at("/data/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"alert-center-store-product-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDimension").value("STORE"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + productCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].targetDimension").value("STORE"))
                .andExpect(jsonPath("$.data[0].targetId").value(storeId));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"STORE",
                                  "dimensionId":"%s",
                                  "sellerSku":"SKU-ALERT-100",
                                  "orderId":"ORD-ALERT-100",
                                  "storeId":"%s",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """.formatted(storeId, storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grossMargin").value(-0.2));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/detect")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "threshold":0.10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dimensionId").value(storeId))
                .andExpect(jsonPath("$.data[0].severity").value("CRITICAL"));

        mockMvc.perform(post("/bi/api/in/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"ads_acos_target",
                                  "kpiName":"Ads ACOS Control",
                                  "category":"ads",
                                  "value":40.00,
                                  "targetValue":100.00,
                                  "unit":"PERCENT"
                                }
                                """))
                .andExpect(status().isOk());

        String shipmentResponse = mockMvc.perform(post("/fba/api/in/v1/shipments")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amazonShipmentId":"FBA-ALERT-100",
                                  "destinationFc":"ONT8",
                                  "carrier":"UPS",
                                  "plannedQuantity":6,
                                  "items":[
                                    {
                                      "productId":"PROD-ALERT-100",
                                      "sellerSku":"SKU-ALERT-100",
                                      "fnsku":"FNSKU-ALERT-100",
                                      "quantity":6,
                                      "boxQuantity":3
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/fbaShipmentId").asText();

        mockMvc.perform(patch("/fba/api/in/v1/shipments/" + shipmentId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/pack")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartonCount":3,
                                  "totalWeight":18.20,
                                  "carrier":"UPS"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/ship")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrier":"UPS",
                                  "trackingNo":"1Z-FBA-ALERT-100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        mockMvc.perform(post("/fba/api/in/v1/shipment-exceptions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shipmentId":"%s",
                                  "type":"DAMAGED",
                                  "qty":1,
                                  "description":"damaged carton still under investigation"
                                }
                                """.formatted(shipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        mockMvc.perform(get("/bi/api/v1/alert-center")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalAlertCount").value(4))
                .andExpect(jsonPath("$.data.summary.openAlertCount").value(4))
                .andExpect(jsonPath("$.data.summary.criticalAlertCount").value(4))
                .andExpect(jsonPath("$.data.summary.categoryCounts.inventory").value(1))
                .andExpect(jsonPath("$.data.summary.categoryCounts.profit").value(1))
                .andExpect(jsonPath("$.data.summary.categoryCounts.kpi").value(1))
                .andExpect(jsonPath("$.data.summary.categoryCounts.logistics").value(1))
                .andExpect(jsonPath("$.data.alerts.length()").value(4));

        mockMvc.perform(get("/bi/api/v1/alert-center")
                        .header("X-Tenant-Id", tenantId)
                        .param("category", "profit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalAlertCount").value(1))
                .andExpect(jsonPath("$.data.alerts.length()").value(1))
                .andExpect(jsonPath("$.data.alerts[0].category").value("profit"))
                .andExpect(jsonPath("$.data.alerts[0].sourceDomain").value("FMS"))
                .andExpect(jsonPath("$.data.alerts[0].dimensionId").value(storeId))
                .andExpect(jsonPath("$.data.alerts[0].sellerSku").value("SKU-ALERT-100"));

        mockMvc.perform(get("/bi/api/v1/alert-center")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalAlertCount").value(0))
                .andExpect(jsonPath("$.data.alerts.length()").value(0));
    }

    @Test
    void exportPmsBiReadOnlyDataWithHeaderValidationScopeFilteringAndMasking() throws Exception {
        String tenantId = uniqueTenant("pms-read");

        mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"pms_home_metric",
                                  "metricName":"PMS Home Metric",
                                  "category":"home",
                                  "formula":"sum(home_order_amount)",
                                  "unit":"USD",
                                  "permissionCode":"bi:metric:read",
                                  "dataLevel":"DETAIL",
                                  "description":"Home scope metric",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "metricCode":"pms_beauty_metric",
                                  "metricName":"PMS Beauty Metric",
                                  "category":"beauty",
                                  "formula":"sum(beauty_order_amount)",
                                  "unit":"USD",
                                  "permissionCode":"bi:metric:read",
                                  "dataLevel":"DETAIL",
                                  "description":"Beauty scope metric",
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"pms_home_kpi",
                                  "kpiName":"PMS Home KPI",
                                  "category":"home",
                                  "value":86.50,
                                  "targetValue":100.00,
                                  "unit":"USD"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bi/api/in/v1/kpis")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kpiCode":"pms_beauty_kpi",
                                  "kpiName":"PMS Beauty KPI",
                                  "category":"beauty",
                                  "value":45.00,
                                  "targetValue":100.00,
                                  "unit":"USD"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bi/api/out/v1/pms/kpi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PMS_HEADER_MISSING"));

        mockMvc.perform(pmsBiHeaders(get("/bi/api/out/v1/pms/kpi"), tenantId, "pms-bi-kpi-001")
                        .param("data_level", "MASKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].kpiCode").value("pms_home_kpi"))
                .andExpect(jsonPath("$.data[0].category").value("home"))
                .andExpect(jsonPath("$.data[0].masked").value(true))
                .andExpect(jsonPath("$.data[0].valueDisplay").value("******"))
                .andExpect(jsonPath("$.data[0].targetValueDisplay").value("******"))
                .andExpect(jsonPath("$.data[0].value").isEmpty())
                .andExpect(jsonPath("$.data[0].targetValue").isEmpty())
                .andExpect(jsonPath("$.data[0].achievementRate").isEmpty());

        mockMvc.perform(pmsBiHeaders(get("/bi/api/out/v1/pms/metrics"), tenantId, "pms-bi-metric-001")
                        .param("data_level", "MASKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].metricCode").value("pms_home_metric"))
                .andExpect(jsonPath("$.data[0].category").value("home"))
                .andExpect(jsonPath("$.data[0].masked").value(true))
                .andExpect(jsonPath("$.data[0].formula").value("******"))
                .andExpect(jsonPath("$.data[0].permissionCode").value("******"));
    }

    private static MockHttpServletRequestBuilder pmsBiHeaders(MockHttpServletRequestBuilder builder,
                                                              String tenantId,
                                                              String idempotencyKey) {
        return builder.header("tenant_id", tenantId)
                .header("actor_id", "agent-bi-001")
                .header("actor_type", "agent")
                .header("agent_id", "pms-agent-bi-001")
                .header("scope", "store:amazon-us,category:home,data_level:MASKED")
                .header("purpose", "ai_bi_read")
                .header("trace_id", "trace-" + idempotencyKey)
                .header("idempotency_key", idempotencyKey)
                .header("source_system", "PMS")
                .header("signature", "mock-signature");
    }

    private static MockHttpServletRequestBuilder pmsBiWriteHeaders(MockHttpServletRequestBuilder builder,
                                                                   String tenantId,
                                                                   String idempotencyKey) {
        return builder.header("tenant_id", tenantId)
                .header("actor_id", "agent-bi-001")
                .header("actor_type", "agent")
                .header("agent_id", "pms-bi-agent-001")
                .header("scope", "category:home,data_level:DETAIL")
                .header("purpose", "ai_bi_write")
                .header("trace_id", "trace-" + idempotencyKey)
                .header("idempotency_key", idempotencyKey)
                .header("source_system", "PMS")
                .header("signature", "mock-signature");
    }
}
