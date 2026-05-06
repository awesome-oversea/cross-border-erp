# OMS Fulfillment Split Merge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `P2-021` 补齐 `OMS` 履约计划层的手工拆包与合包能力，并用测试锁定数量金额守恒。

**Architecture:** 不改 `SalesOrder` 结构，直接在 `OrderFulfillmentPlan` 和 `OrderFulfillmentPackage` 上操作；拆包/合包后的包裹回退到待分配状态，交由后续分仓和物流优选继续处理。

**Tech Stack:** Java 17, Spring Boot 3.2.x, JUnit 5, Mockito

---

### Task 1: 建立 OMS 拆包合包失败测试

**Files:**
- Modify: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderServiceTest.java`

- [ ] Step 1: 增加拆包数量金额守恒测试
- [ ] Step 2: 增加拆包数量不守恒失败测试
- [ ] Step 3: 增加合包数量金额守恒测试
- [ ] Step 4: 运行 `mvn -pl erp-domain-oms -am -Dtest=OrderServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`，确认红灯

### Task 2: 实现履约计划层拆包合包

**Files:**
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderFulfillmentController.java`

- [ ] Step 1: 新增拆包命令和拆包逻辑
- [ ] Step 2: 新增合包命令和合包逻辑
- [ ] Step 3: 增加包裹守恒校验与状态重置
- [ ] Step 4: 暴露入站 API

### Task 3: 重新验证 OMS

**Files:**
- Verify only

- [ ] Step 1: 运行 `mvn -pl erp-domain-oms -am -Dtest=OrderServiceTest,OrderImportAuditServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] Step 2: 记录本次通过数与失败数
- [ ] Step 3: 只按本次输出声明结果
