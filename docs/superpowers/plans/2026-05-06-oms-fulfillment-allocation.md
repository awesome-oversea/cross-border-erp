# OMS Fulfillment Allocation Implementation Plan

**Goal:** 为 `P2-022` 补齐当前履约计划包裹的分仓与物流优选能力。
**Architecture:** 只处理当前履约计划中的待分配包裹；复用现有 `WmsClient`、`TmsClient`、库存预占与物流推荐抽象。
**Tech Stack:** Java 17, Spring Boot 3.2.x, JUnit 5, Mockito

---

### Task 1: 建立 `P2-022` 失败测试

**Files:**
- Modify: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderServiceTest.java`

- [ ] Step 1: 增加分仓成功并物流优选成功测试
- [ ] Step 2: 增加无物流方案进入异常测试
- [ ] Step 3: 运行 `mvn -pl erp-domain-oms -am -Dtest=OrderServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`，确认红灯

### Task 2: 实现分仓与物流优选

**Files:**
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderFulfillmentController.java`

- [ ] Step 1: 增加履约计划分仓入口方法
- [ ] Step 2: 增加包裹级选仓与库存预占逻辑
- [ ] Step 3: 增加物流推荐排序与异常状态处理
- [ ] Step 4: 暴露入站 API

### Task 3: 重新验证 OMS

**Files:**
- Verify only

- [ ] Step 1: 运行 `mvn -pl erp-domain-oms -am -Dtest=OrderServiceTest,OrderImportAuditServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] Step 2: 只根据本次输出声明结果
