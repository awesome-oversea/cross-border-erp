# SCM Supplier Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐 `P2-026` 供应商管理能力，支持供应商档案、联系人、资质、评分维护，并阻止停用供应商新建采购单。
**Architecture:** 保持现有 `PurchaseService + PurchaseStore + MyBatis XML` 结构不变，新增 `SupplierProfile` 聚合视图及联系人、资质、评分的持久化映射。采购建单在应用服务层增加供应商状态门禁。
**Tech Stack:** Java 17, Spring Boot 3.2.x, MyBatis XML Mapper, Flyway, JUnit 5, Mockito

---

### Task 1: 建立 P2-026 红灯测试

**Files:**
- Create: `erp-domain-scm/src/test/java/com/aidotnet/erp/scm/application/PurchaseServiceTest.java`
- Modify: `erp-domain-scm/pom.xml`

- [ ] Step 1: 为 SCM 模块补 `spring-boot-starter-test`
- [ ] Step 2: 新增“停用供应商不可建采购单”测试
- [ ] Step 3: 新增“评估供应商后刷新评分汇总”测试
- [ ] Step 4: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-scm -am '-Dtest=PurchaseServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`，确认测试先失败

### Task 2: 实现供应商档案聚合

**Files:**
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/SupplierContact.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/SupplierQualification.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/domain/SupplierProfile.java`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/application/PurchaseService.java`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/api/PurchaseController.java`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/PurchaseStore.java`

- [ ] Step 1: 新增联系人、资质、供应商档案领域模型
- [ ] Step 2: 为 `PurchaseService` 增加创建、查询、更新 `SupplierProfile` 的命令和方法
- [ ] Step 3: 将 `/scm/api/in/v1/suppliers` 改为返回完整档案视图
- [ ] Step 4: 保持旧采购流程不变，仅替换供应商查询与更新入口

### Task 3: 实现持久化与汇总评分

**Files:**
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/data/SupplierContactDO.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/data/SupplierQualificationDO.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/mapper/SupplierContactMapper.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/mapper/SupplierQualificationMapper.java`
- Create: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/mapper/SupplierScoreMapper.java`
- Create: `erp-domain-scm/src/main/resources/mapper/SupplierContactMapper.xml`
- Create: `erp-domain-scm/src/main/resources/mapper/SupplierQualificationMapper.xml`
- Create: `erp-domain-scm/src/main/resources/mapper/SupplierScoreMapper.xml`
- Create: `erp-domain-scm/src/main/resources/mapper/SupplierEvaluationMapper.xml`
- Modify: `erp-domain-scm/src/main/java/com/aidotnet/erp/scm/infrastructure/PurchaseStore.java`

- [ ] Step 1: 为联系人、资质、评分汇总补 DO/Mapper/XML
- [ ] Step 2: 在 `PurchaseStore` 中实现档案读取、全量替换联系人/资质、评分 upsert
- [ ] Step 3: 在评估供应商后同步刷新 `SupplierScore`
- [ ] Step 4: 重新运行定向测试，确认转绿

### Task 4: 补数据库脚本与最终验证

**Files:**
- Modify: `erp-app/src/main/resources/db/schema.sql`
- Create: `erp-app/src/main/resources/db/migration/V38__scm_supplier_management_enhancement.sql`

- [ ] Step 1: 为供应商、采购单、采购单行补齐缺失字段
- [ ] Step 2: 新增联系人、资质、评分汇总表
- [ ] Step 3: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-scm -am '-Dtest=PurchaseServiceTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`
- [ ] Step 4: 运行 `D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd -pl erp-domain-scm -am test`
- [ ] Step 5: 根据测试输出确认结果并记录剩余风险
