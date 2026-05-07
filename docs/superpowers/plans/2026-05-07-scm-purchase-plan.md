# SCM Purchase Plan Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 `P2-027` 采购需求汇总、采购计划生成和按计划转采购单草稿能力。
**Architecture:** 在 OMS 增加采购需求聚合接口，在 SCM 新增采购计划聚合模型与持久化，使用 WMS 可用库存和 SCM 采购中数量做扣减计算，最后复用现有采购单模型生成采购单草稿。
**Tech Stack:** Java 17, Spring Boot 3.2.x, OpenFeign, MyBatis XML Mapper, Flyway, JUnit 5, Mockito, MockMvc

---

### Task 1: 建立 P2-027 红灯测试

**Files:**
- Create: `erp-domain-scm/src/test/java/com/aidotnet/erp/scm/application/PurchasePlanningServiceTest.java`
- Modify: `erp-app/src/test/java/com/aidotnet/erp/app/ScmApiTests.java`

- [ ] Step 1: 增加“订单需求 + 补货建议 - 可用库存 - 采购中数量”生成计划的单元测试
- [ ] Step 2: 增加“按计划生成采购单草稿并回写计划状态”的单元测试
- [ ] Step 3: 在集成测试中补导入订单、接受补货建议、生成采购计划、转采购单的端到端场景
- [ ] Step 4: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-scm -am '-Dtest=PurchasePlanningServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`，确认测试先失败

### Task 2: 实现 OMS 采购需求聚合接口

**Files:**
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderController.java`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/OmsClient.java`

- [ ] Step 1: 在 OMS 中新增按 SKU 汇总开放订单需求的方法和响应模型
- [ ] Step 2: 新增 `GET /oms/api/in/v1/orders/procurement-demand` 接口
- [ ] Step 3: 调整 SCM `OmsClient` 使用新接口而不是读取订单列表
- [ ] Step 4: 运行受影响测试，确认接口与客户端联通

### Task 3: 实现 SCM 采购计划核心能力

**Files:**
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/PurchasePlan.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/PurchasePlanLine.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/PurchasePlanStatus.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/PurchasePlanLineStatus.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/data/PurchasePlanDO.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/data/PurchasePlanLineDO.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/mapper/PurchasePlanMapper.java`
- Create: `erp-domain-scm/src/main/resources/mapper/PurchasePlanMapper.xml`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/application/PurchasePlanningService.java`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/PurchaseStore.java`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/WmsClient.java`

- [ ] Step 1: 新增采购计划领域模型和数据对象
- [ ] Step 2: 在 `PurchaseStore` 中补采购计划与采购中数量汇总持久化能力
- [ ] Step 3: 在 `PurchasePlanningService` 中实现需求汇总、扣减计算、计划生成
- [ ] Step 4: 实现按计划行生成采购单草稿并回写计划状态
- [ ] Step 5: 重新运行定向单元测试，确认转绿

### Task 4: 暴露接口与数据库支持

**Files:**
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/api/PurchaseController.java`
- Modify: `erp-app/src/main/resources/db/schema.sql`
- Create: `erp-app/src/main/resources/db/migration/V39__scm_purchase_plan.sql`

- [ ] Step 1: 暴露采购计划生成、列表、详情、转采购单接口
- [ ] Step 2: 新增采购计划和采购计划行表结构
- [ ] Step 3: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-scm -am test`
- [ ] Step 4: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-app -am '-Dtest=ScmApiTests' '-Dsurefire.failIfNoSpecifiedTests=false' test`
- [ ] Step 5: 根据测试输出确认结果并记录剩余风险
