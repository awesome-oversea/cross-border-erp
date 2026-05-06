# PDM-SOM-OMS A闭环 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 `PDM -> SOM -> OMS` 纯 ERP 内闭环中价格计算器、原始订单留痕与审核规则可验证实现。

**Architecture:** 保持 `PDM` 商品主数据、`SOM` 渠道映射与 Listing、`OMS` 标准订单三段分层不变，在 `SOM` 增加价格测算接口，在 `OMS` 增加原始订单快照聚合和导入留痕。优先走 TDD，先补失败测试，再补服务与持久化。

**Tech Stack:** Java 17, Spring Boot 3.2.x, MyBatis-Plus, JUnit 5, Mockito

---

### Task 1: 为 SOM 价格测算器建立失败测试

**Files:**
- Create: `erp-app/src/test/java/com/aidotnet/erp/som/application/ListingPricingServiceTest.java`
- Modify: `erp-domain-som/src/main/java/com/aidotnet/erp/som/application/ListingService.java`
- Modify: `erp-domain-som/src/main/java/com/aidotnet/erp/som/api/ListingController.java`

- [ ] Step 1: 写价格测算失败测试
- [ ] Step 2: 运行 `mvn -pl erp-app -Dtest=ListingPricingServiceTest test`，确认失败
- [ ] Step 3: 在 `ListingService` 中实现价格测算与规则叠加最小代码
- [ ] Step 4: 暴露 `POST /som/api/in/v1/price-rules/calculate`
- [ ] Step 5: 重跑测试，确认转绿

### Task 2: 为 OMS 原始订单快照建立失败测试

**Files:**
- Create: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderImportAuditServiceTest.java`
- Create: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/domain/OriginalOrderSnapshot.java`
- Create: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/infrastructure/data/OriginalOrderSnapshotDO.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/infrastructure/OrderStore.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/infrastructure/mapper/OrderMapper.java`
- Modify: `erp-domain-oms/src/main/resources/mapper/OrderMapper.xml`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderController.java`
- Modify: `db/ddl/postgresql/000_erp_baseline.sql`

- [ ] Step 1: 写“导入订单时必须保存原始快照”和“重复订单仍保留快照”的失败测试
- [ ] Step 2: 运行 `mvn -pl erp-domain-oms -Dtest=OrderImportAuditServiceTest test`，确认失败
- [ ] Step 3: 新增 `oms_original_order` 持久化模型与 Mapper
- [ ] Step 4: 在 `OrderService.importOrder` 先保存原始快照，再执行去重与标准化
- [ ] Step 5: 重跑 OMS 测试，确认转绿

### Task 3: 为 OMS 审核规则补齐回归测试

**Files:**
- Modify: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderServiceTest.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`

- [ ] Step 1: 新增低利润、黑名单、异常地址、重复订单的失败测试
- [ ] Step 2: 运行 `mvn -pl erp-domain-oms -Dtest=OrderServiceTest test`，确认存在红灯
- [ ] Step 3: 只补足最小业务代码，不扩大规则范围
- [ ] Step 4: 重跑 `OrderServiceTest`

### Task 4: 端到端验证本轮闭环

**Files:**
- Verify only

- [ ] Step 1: 运行 `mvn -pl erp-domain-oms -Dtest=OrderServiceTest,OrderImportAuditServiceTest test`
- [ ] Step 2: 运行 `mvn -pl erp-app -Dtest=ListingPricingServiceTest test`
- [ ] Step 3: 如有需要，运行 `mvn -pl erp-domain-oms,erp-domain-som,erp-app test -DskipITs`
- [ ] Step 4: 记录实际通过/失败情况，不做未经验证的完成声明
