# 跨境电商 ERP 系统 — 中台详细设计参考

## 1. 总体设计原则

1. **模块化与分层**
   - 业务中台和技术中台独立部署，模块接口清晰，避免跨域数据库直接访问。
   - DDD 分层：`interfaces → application → domain ← infrastructure`，domain 层不依赖 Spring。
2. **事件驱动与数据一致性**
   - 核心业务使用 Kafka + Outbox 模式，关键业务强一致性，非关键业务最终一致性。
   - 聚合根冗余低频字段，保证秒级最终一致性。
3. **PMS AI 集成**
   - PMS 仅生成建议（Recommendation / Draft / InsightCard / 风险预警），ERP 审批后执行。
   - ERP 作为业务数据真相源，确保审计完整性。
4. **多租户与安全**
   - 租户隔离：物理或逻辑多 Schema 方案。
   - 对象级权限控制，RBAC + 数据权限 + 审计。
5. **可观测性**
   - 全链路日志（EFK）、指标监控（Prometheus/Grafana）、链路追踪（OpenTelemetry/Jaeger）。

------

## 2. 中台架构总览

```
┌─────────────────────────────┐
│        展示层               │
│ Next.js / PDA / OpenAPI     │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│        网关层               │
│ 外部网关 Kong → 内部网关 SCG│
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│       业务域层（14大业务域） │
│ DASHBOARD / IAM / PDM / SOM │
│ ADS / OMS / SCM / WMS / FBA │
│ TMS / CRM / FMS / BI / SYS  │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│       业务中台层（14业务中台）│
│ 审核 / 汇率 / 支付 / 订单策略 │
│ 物流策略 / 计费 / CDP / 税务 │
│ 合规 / 选品 / 广告 / 成本 / 利润 / 凭证 │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│       技术中台层（11技术中台）│
│ 通知 / 文件 / 工作流 / 调度  │
│ 权限 / 审计 / 网关 / 翻译 / 脱敏 / API管理 / 连接器 │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│       事件驱动层            │
│ Kafka / RocketMQ + Outbox  │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│       连接器层              │
│ 电商平台 / 物流 / 支付 / PMS │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│       数据层                │
│ PostgreSQL / Redis / ES / MinIO / ClickHouse │
└─────────────────────────────┘
```

> 可参考可视化架构图展示各业务域、业务中台、技术中台及 PMS 闭环对应开源项目。

------

## 3. 技术栈选型

### 3.1 后端

- Spring Boot 3.2.x, Spring Cloud 2023.x, MyBatis-Plus, PostgreSQL 15+, Redis 7.x, Kafka 3.6+, RocketMQ 5.x, Temporal Java SDK 1.x
- API 文档：SpringDoc OpenAPI
- 安全：Spring Security + JWT

### 3.2 前端

- Next.js 14+, React 18+, TypeScript 5.x, Ant Design 5.x, Zustand, Axios + SWR

### 3.3 数据分析

- Canal → Kafka → Flink → ClickHouse / ES / Redis

### 3.4 运维

- Docker + Kubernetes + Helm
- CI/CD：GitLab CI / GitHub Actions
- 监控告警：Prometheus / Grafana
- 链路追踪：OpenTelemetry + Jaeger
- 日志集中：EFK

------

## 4. DDD 分层与模块结构

```
erp-domain-xxx/
├── interfaces/      # REST Controller + DTO + Assembler
├── application/     # 用例协调、事务编排
├── domain/          # 聚合根、实体、值对象、领域服务、事件、仓储接口、网关接口
└── infrastructure/  # 持久化实现、消息发送、外部 API 客户端、Spring 配置
```

- 模块依赖：`interfaces → application → domain ← infrastructure`
- 域间调用通过 application 层接口

------

## 5. 中台业务模块 + 技术中台 + 工作流中台 + 自动化中台开源项目参考

| 模块分类     | 项目名称                           | 所属组织      | Star数 | 开发语言   | 上手难度 | 社区活跃度 | 性能 | 开源协议   | 商业风险          | 适用场景                                      | 开源地址                                                  |
| ------------ | ---------------------------------- | ------------- | ------ | ---------- | -------- | ---------- | ---- | ---------- | ----------------- | --------------------------------------------- | --------------------------------------------------------- |
| 中台业务模块 | Ant Financial mid-platform         | Ant Financial | 50k+   | Java/Scala | ⭐⭐⭐⭐☆    | ⭐⭐⭐⭐☆      | 高   | 不公开     | 高（商业授权）    | 中台业务模块独立，事件驱动解耦                | https://github.com/ant-financial                          |
| 中台业务模块 | mall-cloud                         | macrozheng    | 14k+   | Java       | ⭐⭐⭐☆☆    | ⭐⭐⭐☆       | 中高 | MIT        | 低                | DDD + 微服务分层参考                          | https://github.com/macrozheng/mall                        |
| 中台业务模块 | Apache ShardingSphere              | Apache        | 16k+   | Java       | ⭐⭐⭐☆☆    | ⭐⭐⭐⭐☆      | 高   | Apache-2.0 | 低                | 多租户与对象级权限管理                        | https://github.com/apache/shardingsphere                  |
| 中台业务模块 | Metasfresh                         | metasfresh    | 4k+    | Java       | ⭐⭐⭐☆☆    | ⭐⭐☆☆☆      | 中   | GPLv2      | 中等              | ERP 后端模块，库存/订单/财务参考              | https://github.com/metasfresh                             |
| 中台业务模块 | Spree Commerce                     | spree         | 8k+    | Ruby       | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | Headless 电商平台，商品/订单模块              | https://github.com/spree/spree                            |
| 技术中台模块 | Supabase                           | supabase      | 101k+  | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐☆☆      | 高   | Apache-2.0 | 低                | 后端 BaaS，中台基础框架                       | https://github.com/supabase/supabase                      |
| 技术中台模块 | awesome-oss-saas                   | vihar         | 3k+    | -          | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | 多领域 SaaS 工具集合，中台参考                | https://github.com/vihar/awesome-oss-saas                 |
| 技术中台模块 | open-source-saas                   | toolworks-dev | 2k+    | -          | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | SaaS 项目集合，中台工具参考                   | https://github.com/toolworks-dev/open-source-saas         |
| 工作流中台   | Apache Airflow                     | Apache        | 30k+   | Python     | ⭐⭐⭐☆☆    | ⭐⭐⭐⭐☆      | 高   | Apache-2.0 | 低                | 批处理任务、数据管道、定时任务调度            | https://github.com/apache/airflow                         |
| 工作流中台   | Argo Workflows                     | argoproj      | 16.6k+ | Go         | ⭐⭐⭐☆☆    | ⭐⭐⭐☆☆      | 高   | Apache-2.0 | 低                | 云原生容器任务、K8s 工作流编排                | https://github.com/argoproj/argo-workflows                |
| 工作流中台   | Kestra                             | kestra-io     | 5k+    | Java       | ⭐⭐⭐☆☆    | ⭐⭐⭐☆☆      | 高   | Apache-2.0 | 低                | 工作流编排引擎，中台复杂流程                  | https://github.com/kestra-io/kestra                       |
| 工作流中台   | Imixs-Workflow                     | imixs         | 1k+    | Java       | ⭐⭐⭐☆☆    | ⭐⭐☆☆☆      | 中   | Apache-2.0 | 低                | BPMN 工作流引擎 / BPM 平台                    | https://github.com/imixs/imixs-workflow                   |
| 自动化中台   | n8n                                | n8n-io        | 186k+  | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐⭐☆      | 高   | Fair-code  | 中等（Fair-code） | 可视化工作流自动化，跨系统任务执行            | https://github.com/n8n-io/n8n                             |
| 自动化中台   | Activepieces                       | Activepieces  | 21k+   | TypeScript | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | 无代码自动化引擎                              | https://github.com/Activepieces/activepieces              |
| 自动化中台   | Flowise                            | FlowiseAI     | 2k+    | TypeScript | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | AI 工作流工具，智能流程自动化                 | https://github.com/FlowiseAI/Flowise                      |
| 自动化中台   | ToolJet                            | ToolJet       | 25k+   | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐☆       | 中   | Apache-2.0 | 低                | 低代码内部工具，快速构建业务界面 & 平台逻辑   | https://github.com/ToolJet/ToolJet                        |
| 自动化中台   | Hoppscotch                         | hoppscotch    | 25k+   | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐☆       | 中   | MIT        | 低                | API 联调 & 内部工具生态，自动化接口测试与调度 | https://github.com/hoppscotch/hoppscotch                  |
| 自动化中台   | middleware (DORA metrics platform) | middlewarehq  | 1k+    | JavaScript | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | 工程性能度量、中台指标采集                    | https://github.com/middlewarehq/middleware                |
| 自动化中台   | Awesome Microservices              | mfornos       | -      | -          | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低                | 微服务生态收集，中台服务体系构建              | https://project-awesome.org/mfornos/awesome-microservices |

------

## 6. 可视化架构图参考

 参考项目根目录：ERP 中台模块对应开源项目可视化架构图.png

- 图中展示了 **14业务域 + 14业务中台 + 11技术中台 + PMS 闭环**，并标注每个模块推荐的开源项目。
- 可作为 ERP 中台设计文档的直观参考，用于规划模块依赖、事件流、数据流和技术选型。

------

## 7. 使用建议

1. **业务中台**：优先参考 mall-cloud、ShardingSphere、Metasfresh、Spree Commerce 的模块化设计。
2. **技术中台**：Supabase、OSS SaaS 工具集合可作为基础支撑。
3. **工作流中台**：Airflow / Argo / Kestra / Imixs-Workflow，可快速实现任务调度与流程管理。
4. **自动化中台**：n8n、Activepieces、Flowise、ToolJet、Hoppscotch 提供低代码/无代码自动化方案。
5. **PMS 集成闭环**：建议以事件驱动模式引入，AI 仅提供建议，ERP 审批后执行。