# OMS Refund And Cancel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 `P2-024` 退款与取消订单流程的库存释放、财务成本事件回写、状态恢复与领域事件。

**Architecture:** 保持现有 `OrderService` / `OrderController` 入口不变，在取消与退款流转中补充规则化的 side effects。库存仅处理未发货预占释放，财务通过 FMS `recordCostEvent` 以订单行维度分摊退款损耗。

**Tech Stack:** Java 17, Spring Boot 3.2.x, JUnit 5, Mockito

---

### Task 1: 建立 P2-024 红灯测试

**Files:**
- Modify: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderServiceTest.java`

- [ ] Step 1: 增加取消已支付订单释放预占库存测试
- [ ] Step 2: 增加退款驳回恢复正确订单状态测试
- [ ] Step 3: 增加退款完成写入财务成本事件测试
- [ ] Step 4: 增加退款/取消发布事件测试
- [ ] Step 5: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-oms -am '-Dtest=OrderServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`，确认新增测试先失败

### Task 2: 实现退款与取消规则

**Files:**
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`

- [ ] Step 1: 在取消流程中补充履约包裹预占库存释放
- [ ] Step 2: 在退款驳回流程中按履约状态恢复原订单状态
- [ ] Step 3: 在退款完成与已支付取消流程中按订单行分摊写入 `RETURN_COST`
- [ ] Step 4: 为退款申请、审批、驳回、完成补齐领域事件
- [ ] Step 5: 重新运行定向测试，确认转绿

### Task 3: 重新验证 OMS

**Files:**
- Verify only

- [ ] Step 1: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-oms -am '-Dtest=OrderServiceTest,OrderImportAuditServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`
- [ ] Step 2: 仅根据本次测试输出确认结果并汇报剩余风险
