# OMS Shipment Sync Implementation Plan

**Goal:** 补齐 `P2-023` 发货后订单/包裹回写与平台标记发货失败重试能力。
**Architecture:** 保持现有 `ship` / `retryPlatformShipmentSync` 入口，在订单回写与同步重试语义上补齐验收行为。
**Tech Stack:** Java 17, Spring Boot 3.2.x, JUnit 5, Mockito

---

### Task 1: 建立 `P2-023` 红灯测试

**Files:**
- Modify: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderServiceTest.java`

- [ ] Step 1: 增加发货后订单履约状态与包裹追踪号回写测试
- [ ] Step 2: 增加平台标记发货失败后重试测试
- [ ] Step 3: 运行 `mvn -pl erp-domain-oms -am -Dtest=OrderServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`，确认红灯

### Task 2: 实现订单回写与同步重试细化

**Files:**
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`

- [ ] Step 1: 发货后按履约计划状态回写订单 `fulfillmentStatus`
- [ ] Step 2: 保持包裹追踪号、同步状态、同步日志语义一致
- [ ] Step 3: 重新验证新增测试转绿

### Task 3: 重新验证 OMS

**Files:**
- Verify only

- [ ] Step 1: 运行 `mvn -pl erp-domain-oms -am -Dtest=OrderServiceTest,OrderImportAuditServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] Step 2: 只根据本次输出声明结果
