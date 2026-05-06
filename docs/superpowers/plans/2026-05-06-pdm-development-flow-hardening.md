# PDM Development Flow Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 `P2-010` 产品开发流程的审核前置、阶段流转、批量分配和统计能力。

**Architecture:** 在现有 `ProductService/ProductController` 上做最小补强，不新增新表；阶段规则和统计逻辑都放在应用层，持久化层继续复用 `ProductStore` 现有读写能力。

**Tech Stack:** Java 17, Spring Boot 3.2.x, MyBatis-Plus, JUnit 5, Mockito

---

### Task 1: 建立产品开发流程失败测试

**Files:**
- Create: `erp-domain-pdm/src/test/java/com/aidotnet/erp/pdm/application/ProductDevelopmentFlowServiceTest.java`
- Modify: `erp-domain-pdm/pom.xml`

- [ ] Step 1: 为“未审批提报不可创建开发流程”“阶段不可跳级”“完成态不可再推进”“批量分配和统计结果正确”写失败测试
- [ ] Step 2: 为 `erp-domain-pdm` 增加测试依赖
- [ ] Step 3: 运行 `mvn -pl erp-domain-pdm -am -Dtest=ProductDevelopmentFlowServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`，确认失败

### Task 2: 实现产品开发流程补强

**Files:**
- Modify: `erp-domain-pdm/src/main/java/com/aidotnet/erp/pdm/application/ProductService.java`
- Modify: `erp-domain-pdm/src/main/java/com/aidotnet/erp/pdm/api/ProductController.java`

- [ ] Step 1: 在 `createDevelopment` 增加“提报必须已审批”校验
- [ ] Step 2: 在 `updateDevStage` 增加顺序流转校验和完成态控制
- [ ] Step 3: 新增批量团队分配应用服务
- [ ] Step 4: 新增开发统计聚合结果与查询接口

### Task 3: 重新验证本轮 PDM 能力

**Files:**
- Verify only

- [ ] Step 1: 运行 `mvn -pl erp-domain-pdm -am -Dtest=ProductDevelopmentFlowServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] Step 2: 记录测试数量、失败数、错误数
- [ ] Step 3: 只基于本次新鲜输出声明结果
