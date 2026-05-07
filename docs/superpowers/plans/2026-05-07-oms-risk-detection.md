# OMS Risk Detection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 `P2-025` 订单风险检测的统一风险记录和人工处理留痕能力。

**Architecture:** 保持现有 OMS 风控入口不变，在 `OrderService` 中把 PMS 风险预警沉淀到 `OrderRiskCheck`，并新增独立审核日志模型、存储与查询接口。主预警表保存当前状态，日志表保存审核历史。

**Tech Stack:** Java 17, Spring Boot 3.2.x, MyBatis XML Mapper, Flyway, JUnit 5, Mockito

---

### Task 1: 建立 P2-025 红灯测试

**Files:**
- Modify: `erp-domain-oms/src/test/java/com/aidotnet/erp/oms/application/OrderServiceTest.java`

- [ ] Step 1: 增加接收 PMS 风险预警时写入订单风险检查记录测试
- [ ] Step 2: 增加人工审核写入风险处理日志测试
- [ ] Step 3: 增加非法审核动作拒绝测试
- [ ] Step 4: 增加风险处理日志查询测试
- [ ] Step 5: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-oms -am '-Dtest=OrderServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`，确认新增测试先失败

### Task 2: 实现风险记录与处理留痕

**Files:**
- Create: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/domain/PmsRiskAlertReviewLog.java`
- Create: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/infrastructure/data/PmsRiskAlertReviewLogDO.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/infrastructure/OrderStore.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/infrastructure/mapper/OrderMapper.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
- Modify: `erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderController.java`
- Modify: `erp-domain-oms/src/main/resources/mapper/OrderMapper.xml`
- Modify: `erp-app/src/main/resources/db/schema.sql`
- Create: `erp-app/src/main/resources/db/migration/V37__oms_pms_risk_review_log.sql`

- [ ] Step 1: 新增风险处理日志领域模型、DO、Mapper 和存储方法
- [ ] Step 2: 接收 PMS 风险预警时同步写入 `OrderRiskCheck`
- [ ] Step 3: 审核风险预警时校验动作、写入日志、保留备注
- [ ] Step 4: 增加风险处理日志查询服务与接口
- [ ] Step 5: 重新运行定向测试，确认转绿

### Task 3: 重新验证 OMS

**Files:**
- Verify only

- [ ] Step 1: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-oms -am '-Dtest=OrderServiceTest,OrderImportAuditServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`
- [ ] Step 2: 仅根据本次测试输出确认结果并汇报剩余风险
