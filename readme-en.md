# Cross-Border E-Commerce ERP

**Cloud-Native · DDD · 14-Domain · Multi-Tenant SaaS**

English | [中文](README.md)

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📖 Personal Showcase

> This is a **personal architecture demonstration project**, not a commercial product.
>
> It covers the **complete cross-border e-commerce chain across 14 business domains**, from product development to financial accounting, demonstrating deep understanding of cross-border e-commerce operations.
> Built with **DDD modular monolith architecture** — 18 Maven modules, strict 4-layer DDD layering, event-driven cross-domain collaboration.
>
> - **Architecture Overview**: [docs/architecture-overview.md](docs/architecture-overview.md)
> - **Requirements Specification**: [docs/requirements-specification.md](docs/requirements-specification.md)
> - **Architecture Decision Records**: [docs/adr/](docs/adr/)

---

## Project Overview

A **full-chain SaaS ERP platform** for cross-border e-commerce sellers, covering the complete business loop from product development, store operations, advertising, order fulfillment, supply chain procurement, warehousing & logistics, financial accounting to business intelligence.

This project demonstrates:

- **Business Architecture**: Domain modeling across 14 business domains, cross-domain process orchestration, business middle-platform abstraction
- **Technical Architecture**: Modular monolith to microservice evolution path, event-driven architecture, dual-layer gateway, multi-tenant isolation
- **Data Architecture**: Schema-isolated multi-tenancy, Outbox event sourcing, Canal + Flink real-time data pipeline
- **Security Architecture**: JWT + RBAC + 10-dimension data permissions, API gateway security layer, data masking
- **AI Architecture**: AI-powered product selection / pricing / replenishment / risk control, PMS integration, intelligent recommendation workflows
- **Engineering Architecture**: DDD layered conventions, CI/CD pipeline, Docker / K8s cloud-native deployment

---

## System Architecture

```
                              ┌─────────────────────────────────────────────────┐
                              │                   Client Layer                   │
                              │   Next.js 14 (SSR/SSG) + React 18 + Ant Design │
                              └──────────────────────┬──────────────────────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────────┐
                              │              Dual-Layer API Gateway              │
                              │  Kong 3.6 (SSL / Domain / Global Rate Limit)    │
                              │     → Spring Cloud Gateway (Auth / Tenant / Route)│
                              └──────────────────────┬──────────────────────────┘
                                                     │
          ┌────────────┬────────────┬────────────────┼────────────────┬────────────┬────────────┐
          │            │            │                │                │            │            │
    ┌─────▼─────┐┌─────▼─────┐┌────▼────┐  ┌───────▼───────┐┌──────▼──────┐┌─────▼─────┐┌─────▼─────┐
    │   IAM     ││   PDM     ││  SOM    │  │     OMS       ││    SCM      ││   WMS     ││   FBA     │
    │  Identity ││  Product  ││  Sales  │  │    Order      ││  Supply     ││ Warehouse ││Amazon FBA │
    │ & Access  ││   Dev     ││  Ops    │  │  Management  ││   Chain     ││ Mgmt      ││ Logistics │
    └─────┬─────┘└─────┬─────┘└────┬────┘  └───────┬───────┘└──────┬──────┘└─────┬─────┘└─────┬─────┘
          │            │            │                │                │            │            │
    ┌─────▼─────┐┌─────▼─────┐┌────▼────┐  ┌───────▼───────┐┌──────▼──────┐┌─────▼─────┐┌─────▼─────┐
    │   ADS     ││   TMS     ││  CRM    │  │     FMS       ││     BI      ││    SYS    ││ Dashboard │
    │Advertising││ Logistics ││Customer │  │   Finance     ││  Business   ││  System   ││  Workbench│
    │           ││  Tracking ││ Service │  │  Management   ││Intelligence ││  Settings ││           │
    └───────────┘└───────────┘└─────────┘  └───────────────┘└─────────────┘└───────────┘└───────────┘
          │            │            │                │                │            │            │
          └────────────┴────────────┴────────────────┼────────────────┴────────────┴────────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────────┐
                              │         Technical Middle Platform & Infra        │
                              │  Event-Driven (Kafka/RocketMQ) · Cache (Redis)  │
                              │  Search (ES) · Storage (MinIO) · Notification   │
                              │  Audit · Workflow · Scheduling · Data Masking    │
                              └──────────────────────┬──────────────────────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────────┐
                              │                 Data Layer                       │
                              │  PostgreSQL 16 (Schema Multi-Tenant)            │
                              │  ClickHouse (OLAP) · Canal → Kafka → Flink      │
                              └─────────────────────────────────────────────────┘
```

---

## Domain-Driven Design

### 14 Bounded Contexts

| # | Domain | Bounded Context | Core Aggregates | AI-Enhanced |
|---|--------|----------------|-----------------|-------------|
| 1 | Dashboard | Workbench | KPIs, To-Do Items | ★AI Dashboard |
| 2 | IAM | Identity & Access | User, Role, Permission, Tenant | — |
| 3 | PDM | Product Development | SPU, SKU, Category, Brand | ★AI Product Selection |
| 4 | SOM | Sales Operations | Store, Listing, Price Rule | ★AI Pricing |
| 5 | ADS | Advertising | Campaign, Ad Group, Keyword | ★AI Optimization |
| 6 | OMS | Order Management | Sales Order, Order Line, Refund | ★AI Risk Control |
| 7 | SCM | Supply Chain | Supplier, Purchase Order, Plan | ★AI Replenishment |
| 8 | WMS | Warehouse Management | Warehouse, Inventory, In/Out Order | ★AI Forecasting |
| 9 | FBA | Amazon FBA Logistics | Inbound Plan, Shipment, Restock | ★AI FBA |
| 10 | TMS | Transport & Logistics | Carrier, Shipment, Tracking | ★AI Logistics |
| 11 | CRM | Customer Service | Customer, Ticket, Email Rule | ★AI Sentiment |
| 12 | FMS | Finance Management | Cost Event, Receivable, Reconciliation | ★AI Cost Aggregation |
| 13 | BI | Business Intelligence | Metric, KPI, Report, Cockpit | ★KPI |
| 14 | SYS | System Settings | Config, Webhook, Business Rule | — |

### DDD Layered Architecture (Inside Each Domain Module)

```
erp-domain-xxx/
└── src/main/java/com/aidotnet/erp/xxx/
    ├── interfaces/          ← Interface Layer (REST Controller, DTO, OpenAPI)
    ├── application/         ← Application Layer (Use Case Coordination, Transaction Orchestration)
    ├── domain/              ← Domain Core (Pure Java, Zero Framework Dependency)
    │   ├── model/           ← Aggregate Root, Entity, Value Object
    │   ├── service/         ← Domain Service
    │   ├── event/           ← Domain Event
    │   └── repository/      ← Repository Interface
    └── infrastructure/      ← Infrastructure (MyBatis Mapper, Messaging, External Client)
```

**Core Constraint**: The domain layer must not import any Spring / MyBatis classes, depending only on domain abstractions in `erp-common`.

---

## Cross-Domain Business Loops

The core architectural capability of this system is reflected in **cross-domain process orchestration**:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    Sales Fulfillment Loop                                │
│  OMS(Order Import→Payment) → WMS(Reserve→Pick→Ship) → TMS(Track) → FMS│
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                    Procurement Inbound Loop                              │
│  SCM(Supplier→PO→Approval→Receive) → WMS(Inbound→QC) → FMS(Auto Cost) │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                    Procurement Planning Loop                             │
│  OMS(Order Demand) → SCM(Restock→Plan→Approval→PO) → WMS(Inbound)     │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                    Inbound QC Loop                                       │
│  WMS(Receive→Freeze→QC→Pass Good/Freeze Defect) → Inventory Available  │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                    Outbound Fulfillment Loop                             │
│  WMS(Reserve→Pick→Pack→Weigh→Ship) → Inventory Deduct → TMS(Shipment) │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
erp/
├── pom.xml                        # Parent POM (Dependency Management)
├── erp-common/                    # Shared Module (Security/Event/Cache/Search/Storage/Notification/Audit/Workflow)
├── erp-gateway/                   # Spring Cloud Gateway (Internal Gateway)
├── erp-domain-dashboard/          # Workbench Domain
├── erp-domain-iam/                # Identity & Access Domain (Auth/RBAC/10-Dim Data Scope)
├── erp-domain-pdm/                # Product Development Domain
├── erp-domain-som/                # Sales Operations Domain
├── erp-domain-ads/                # Advertising Domain
├── erp-domain-oms/                # Order Management Domain
├── erp-domain-scm/                # Supply Chain Domain
├── erp-domain-wms/                # Warehouse Management Domain
├── erp-domain-fba/                # FBA / Overseas Warehouse Domain
├── erp-domain-tms/                # Transport & Logistics Domain
├── erp-domain-crm/                # Customer Service Domain
├── erp-domain-fms/                # Finance Management Domain
├── erp-domain-bi/                 # Business Intelligence Domain
├── erp-domain-sys/                # System Settings Domain
├── erp-app/                       # Bootstrap Module (Single-Process Aggregation)
├── erp-web/                       # Frontend (Next.js 14 + React 18 + Ant Design 5)
├── docker-compose.yml             # Local Development Environment
├── Dockerfile                     # Container Image Build
├── docs/adr/                      # Architecture Decision Records (ADR)
└── .github/workflows/ci.yml      # CI/CD Pipeline
```

---

## Technology Stack

### Backend — Spring Cloud Java Cloud-Native

| Category | Technology | Version |
|----------|-----------|---------|
| Framework | Spring Boot + Spring Cloud + Spring Cloud Alibaba | 3.2.x / 2023.x |
| JDK | Eclipse Temurin | 17 |
| Service Governance | Nacos (Registry / Config Center) | 2.3.x |
| Circuit Breaker | Sentinel | 1.8.x |
| Remote Call | OpenFeign + LoadBalancer | — |
| Dual-Layer Gateway | Kong (External) → Spring Cloud Gateway (Internal) | 3.6 |
| Async Messaging | Spring Cloud Stream + Kafka + RocketMQ | 3.6 / 5.x |
| Distributed TX | Seata (AT Mode, Reserved) | 1.8.x |
| Security | Spring Security 6 + JWT + BCrypt | — |
| ORM | MyBatis-Plus (Multi-Tenant Plugin / Logical Delete / Pagination) | 3.5.x |
| Database | PostgreSQL | 16 |
| Cache | Redis | 7.x |
| Search | Elasticsearch | 8.x |
| Object Storage | MinIO (S3 Compatible) | — |
| DB Migration | Flyway | — |
| API Docs | SpringDoc OpenAPI (14-Domain Grouped) | — |
| Code Style | EditorConfig + Spotless | — |

### Data Architecture

| Component | Responsibility |
|-----------|---------------|
| Canal | binlog Real-Time Capture → Kafka |
| Flink | Stream Computing, Real-Time Aggregation → ES/Redis/PG |
| ClickHouse | OLAP Analytics Engine |
| EFK | Elasticsearch + Fluentd + Kibana Logging Stack |

### Frontend — SSR / SSG

| Category | Technology | Version |
|----------|-----------|---------|
| Framework | Next.js (App Router) | 14.2 |
| UI Library | React + Ant Design | 18 / 5.x |
| Language | TypeScript | 5.4 |
| State Management | Zustand | 4.5 |
| Data Fetching | SWR | 2.2 |
| Charts | ECharts | 5.5 |
| HTTP | Axios | 1.7 |

---

## Security Architecture

### Multi-Tenant Isolation

- **Schema-Level Isolation**: Each tenant has an independent PostgreSQL Schema for physical data isolation
- **Request-Level Isolation**: `X-Tenant-Id` Header propagated across the entire chain, MyBatis-Plus auto-injects tenant conditions
- **Index Isolation**: Elasticsearch indices isolated by tenant prefix

### 10-Dimension Data Permission Model

| Dimension | Description |
|-----------|-------------|
| tenant | Tenant isolation |
| org | Organization scope |
| department | Department scope |
| store | Store scope |
| marketplace | Marketplace scope (Amazon US/JP etc.) |
| channel | Channel scope (Amazon/TikTok/Walmart etc.) |
| warehouse | Warehouse scope (Local/FBA/Overseas) |
| supplier | Supplier scope |
| category | Category scope |
| data_level | Detail / Summary / Masked level |

### Authentication & Authorization

```
Client → Kong(SSL Termination) → SCG(JWT Verify / Tenant Header Injection) → Service(@RequirePermission)
                                                                              ↓
                                                                    PermissionAspect(AOP)
                                                                    → 10-Dim DataScope Filter
```

---

## Event-Driven Architecture

```
Domain Event → Outbox Table → Polling → Kafka/RocketMQ → Consumer → Cross-Domain Sync
                                            ↓
                                      Topic: erp.{domain}.{aggregate}.{event}.v1
```

The **Outbox Pattern** ensures zero event loss and eventual consistency. Strong consistency scenarios use RocketMQ transaction messages (half-message + callback).

---

## Middle Platform Architecture

### 14 Business Middle Platforms

| # | Platform | Responsibility |
|---|----------|---------------|
| 1 | Content Moderation Center | Product / Listing content compliance review |
| 2 | Currency & Forex Center | Real-time exchange rate fetching and caching |
| 3 | Payment Aggregation Center | Stripe / Alipay / Ping++ multi-channel payment |
| 4 | Order Strategy Center | Split / Merge / Ship-Available-First strategy engine |
| 5 | Logistics Strategy Center | Carrier selection rules, freight rule engine |
| 6 | Billing Strategy Center | Platform fee calculation, commission rule engine |
| 7 | Customer Data Platform (CDP) | Customer profiling, tag system, behavior analytics |
| 8 | Invoice & Tax Platform | Invoice templates, VAT calculation |
| 9 | Compliance & Risk Platform | Platform compliance detection, trade compliance rules |
| 10 | Product Selection Analytics | Market trend analysis, competitor data aggregation |
| 11 | Ad Optimization Platform | Ad strategy templates, bidding algorithm interface |
| 12 | Cost Aggregation Engine | Multi-dimension cost auto-aggregation rules |
| 13 | Profit Calculation Engine | Profit calculation, forex conversion, allocation rules |
| 14 | Inventory Voucher Engine | Auto voucher generation, accounting subject mapping |

### 11 Technical Middle Platforms

| # | Platform | Implementation |
|---|----------|---------------|
| 1 | Notification Center | SMS / Feishu / DingTalk / WeCom / Email unified interface |
| 2 | File Processing Center | Image compression / format conversion / doc generation → MinIO |
| 3 | Workflow Engine | `ApprovalStateMachine` multi-level approval / countersign / add-sign |
| 4 | Task Scheduling Center | `@ScheduledTask` + Quartz / XXL-JOB |
| 5 | Permission Management Center | OAuth2 + RBAC + 10-dimension data permissions |
| 6 | Audit Log Center | `@Audited` AOP + Fluentd collection |
| 7 | API Gateway | Kong + Spring Cloud Gateway dual-layer |
| 8 | Translation Center | Cloud translation API + i18n resource management |
| 9 | Data Masking Center | Jackson serialization interception + `data_level` dimension |
| 10 | API Management Platform | SpringDoc OpenAPI 14-domain grouping |
| 11 | Connector Management Platform | Plugin architecture, unified external API integration |

---

## Quick Start

### Prerequisites

- JDK 17+ (Eclipse Temurin)
- Maven 3.9+
- Node.js 18+ / npm
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
docker compose up -d postgres redis kafka kafka-init minio elasticsearch
```

### 2. Start Backend

```bash
cd erp-app
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Backend starts at http://localhost:8080
```

### 3. Start Frontend

```bash
cd erp-web
npm install
npm run dev
# Frontend starts at http://localhost:3000
```

### 4. Access the System

- Frontend: http://localhost:3000
- Default Tenant: `tenant-demo`
- Admin Account: `admin` / `admin123`
- API Docs: http://localhost:8080/swagger-ui.html

---

## Frontend Business Capabilities

> The following pages are strictly aligned with the Cross-Border E-Commerce ERP Requirements Specification V4, covering the full chain from product development → listing operations → order processing → multi-warehouse replenishment → warehouse execution → logistics shipping → customer service → financial accounting → business analytics.

### Dashboard — Workbench Domain ★AI Dashboard

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Operations Cockpit | `/dashboard` | Core KPI dashboard (today's orders / revenue / inventory alerts / pending tickets), sales trend chart, platform distribution pie chart, recent orders list, workflow notification aggregation |

### IAM — Identity & Access Domain

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| User Management | `/iam/users` | User CRUD, role assignment, tenant isolation, 10-dimension data permissions |
| Role Management | `/iam/roles` | Role definition, menu + data permission configuration, object-level permissions |
| Position Management | `/iam/positions` | Position definition, position hierarchy, position-department association |
| Object Permissions | `/iam/permissions` | Fine-grained permission control for business objects (Product / Listing / Ad) |

### PDM — Product Development Domain ★AI Product Selection

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Product Management | `/pdm/products` | SPU/SKU management, category/brand, variants (parent/child), product lifecycle (DRAFT→ACTIVE), development review, channel SKU mapping, bundle products, packaging materials, product access control, issue tracking |
| Listing Management | `/pdm/listings` | Channel SKU mapping, pre-listing configuration (price limits / title library / image library), UPC management, sensitive word library, IP management |

### SOM — Sales Operations Domain ★AI Listing Optimization

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Store Management | `/som/stores` | Multi-platform store authorization (Amazon / Shopify / TikTok etc.), connection status monitoring, store code uniqueness validation |
| Listing Management | `/som/listings` | Multi-platform listing, batch price adjustment / promotion / quantity update, one-click multi-language translation, balanced inventory strategy, slow-moving clearance, Reviews sync |
| Price Rules | `/som/price-rules` | Listing price calculator, Buybox capture & auto-adjust, platform price limits to prevent internal price wars, auto-adjustment rules |
| Listing Monitoring | `/som/monitors` | Sales / price / Buybox / listing status / FBA inventory age / star rating anomaly monitoring, SMS / email / DingTalk alerts, operations calendar |

### ADS — Advertising Domain ★AI Optimization

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Ad Campaigns | `/ads/campaigns` | SP / SB / SD / SBV ad types, batch multi-store campaign management, ACoS / impressions / clicks / conversions / spend, batch status / budget / bid changes, PMS ad optimization integration |
| Ad Strategies | `/ads/strategies` | Search term extraction, negative keyword management, auto-bidding (based on impression / click conditions), ad dashboard, grouped analysis view |

### OMS — Order Management Domain ★AI Risk Control

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Order List | `/oms/orders` | Three-layer order separation (Channel Order / ERP Sales Order / Fulfillment Order / Package), platform order sync, batch import, split / merge / ship-available-first, customs declaration rules, platform ship confirmation, risk order detection, plug adapter rules, refund / return / exchange |
| Order Audit | `/oms/audit` | Order audit strategy, risk control validation, profit margin check, anomaly order interception, blacklist management |

### SCM — Supply Chain Domain ★AI Replenishment

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Suppliers | `/scm/suppliers` | Supplier management (contacts / qualifications / ratings), supplier portal (online order acceptance), disabled supplier validation, 1688 procurement integration |
| Purchase Orders | `/scm/purchase-orders` | 5 procurement modes (Market / Factory / Tmall-Taobao / 1688 / Processing), multi-level / auto approval workflow, purchase contracts, order tracking, delivery-receipt reconciliation, three-warehouse replenishment analysis, PMS AI replenishment suggestions |

### WMS — Warehouse Management Domain ★AI Forecasting

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Warehouse Management | `/wms/warehouses` | Warehouse basic info, zone / bin management, bin group statistics, custom label templates |
| Inventory Ledger | `/wms/inventory` | Inventory transaction ledger with 5 statuses (on-hand / reserved / available / in-transit / damaged), receive → QC → inbound full process, defective return / repair / supplier buyback, outbound QC, transfer / stocktake, Removal orders, manual in/out, cross-domain navigation (→ FBA Inventory / Purchase Orders) |

### FBA — Amazon FBA Logistics Domain

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| FBA Inventory | `/fba/inventory` | EFN / NARF inventory view, inventory age analysis, stock alerts (available days < 14 highlighted in red) |
| Restock Suggestions | `/fba/restock` | FBA restock suggestions (customizable daily avg / lead time / sales noise filter), replenishment cart, one-click shipment plan generation |
| Shipment Plans | `/fba/shipments` | Three creation modes (Sync from Amazon / Load ShipmentID / Create without ID), packing SOP, weigh & ship, FBA first-leg 5 exception handling |

### TMS — Transport & Logistics Domain

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Carriers | `/tms/carriers` | Carrier API authorization, shipping method management, freight algorithm maintenance, freight estimation |
| Tracking | `/tms/tracking` | 2000+ carrier tracking, online time analysis, delivery statistics, logistics curation, performance comparison, post-shipment freight adjustment |

### CRM — Customer Service Domain ★AI Sentiment

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Customer Management | `/crm/customers` | Customer profiling, tag system, behavior analytics (CDP) |
| Ticket Management | `/crm/tickets` | Multi-platform email / message management, intelligent agent assignment, auto-reply on holidays, new-hire reply review, ticket status flow (OPEN→ASSIGNED→RESOLVED→CLOSED) |
| Email Rules | `/crm/email-rules` | Email templates, quick reply, bilingual (EN/CN), negative review & dispute handling |
| Email Marketing | `/crm/campaigns` | Auto-promotion, request reviews (excluding refund / negative review orders) |
| Quality Issues | `/crm/quality` | Quality issue tracking with PDM / WMS integration, product issue categorization analysis |

### FMS — Finance Management Domain ★AI Cost Aggregation

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Cost Events | `/fms/cost-events` | 8-category cost aggregation (Procurement / First-Leg / Warehousing / Commission / Advertising / Payment / Last-Leg / Other), auto cost event generation (SCM receipt → FMS integration), anomaly cost detection |
| Profit Reports | `/fms/profit` | Profit calculation (FIFO), multi-dimension profit reports (platform / store / personnel / product / currency), profit trend chart, reconciliation (supplier / carrier), receivable / payment confirmation, forex rate snapshot |

### BI — Business Intelligence Domain ★KPI

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| Operations Cockpit | `/bi/cockpit` | Comprehensive dashboard (store sales / profit / ads / inventory / account health), operations monitoring (anomaly auto-alerts) |
| KPI Metrics | `/bi/kpis` | KPI setting / tracking / alerting, developer commission reports, warehouse staff KPI |
| Report Center | `/bi/reports` | 500+ business reports, order profit reports, product sales reports, stockout analysis, FBA shipment analysis |
| Trend Analysis | `/bi/trends` | Real-time sales multi-dimension analysis, product performance 11 deep metrics, custom reports |

### SYS — System Settings Domain

| Page | Route | Business Capabilities |
|------|-------|----------------------|
| System Config | `/sys/configs` | System parameter configuration (procurement approval / profit check business switches), AI feature toggles, enable / disable control |
| Webhooks | `/sys/webhooks` | Platform connector management, webhook event subscription, test push |
| Business Rules | `/sys/rules` | Customs declaration rules, plug adapter rules, approval workflow configuration, invoice / contract templates, data import / export |

### Page Statistics

| Dimension | Count |
|-----------|-------|
| Business Domains | 14 |
| Route Pages | 45 |
| Business Capability Points | 200+ |
| AI-Enhanced Features | 9 domains (Selection / Pricing / Optimization / Risk Control / Replenishment / Forecasting / Sentiment / Cost Aggregation / KPI) |

**TypeScript type check all passed**, zero-error build.

---

## Testing

### Backend Integration Tests

```bash
cd erp-app
mvn test -Dspring.profiles.active=test
```

Covering 14 business domains, key test scenarios:

| Test Class | Verification |
|------------|-------------|
| `IamApiTests` | Login / RBAC / Tenant Disable / Audit Log |
| `OmsApiTests` | Order Import Idempotency / Status Flow / Fulfillment Loop / Inventory Linkage |
| `ScmApiTests` | Supplier / PO Approval / Receipt → WMS/FMS Integration |
| `WmsApiTests` | Inventory CRUD / Inbound QC / Outbound Fulfillment Full Flow |
| `FbaApiTests` | Inbound Plan / Shipment / Inventory Flow |
| `TmsApiTests` | Carrier / Tracking / Delivery / Tenant Isolation |
| `FmsApiTests` | Receivable / Payment Confirmation / Duplicate Check |
| `CrmApiTests` | Customer / Ticket Assignment → Resolution → Close Flow |
| `BiApiTests` | Metric / KPI / Cockpit / Tenant Isolation |
| `DashboardApiTests` | Metric CRUD / Tenant Isolation |

### Frontend Build Verification

```bash
cd erp-web
npm run build
# 45 pages built successfully, 0 errors
```

---

## Architecture Decision Records (ADR)

| ID | Title | Decision |
|----|-------|----------|
| ADR-0001 | Modular Monolith Baseline | Phase-1 modular monolith organized by 14 domain modules, ready for future microservice extraction |
| ADR-001~003 | Technology Selection | Spring Boot 3 + MyBatis-Plus + PostgreSQL |
| ADR-004 | Message Middleware Selection | Kafka (high throughput) + RocketMQ (transaction messages) dual-stack |
| ADR-005 | Distributed Transaction Strategy | Strong consistency → Seata / transaction messages; eventual consistency → Saga + Outbox |
| ADR-006 | Database Isolation Strategy | Schema-level multi-tenant isolation |
| ADR-007 | CDC Data Sync Architecture | Canal → Kafka → Flink real-time data pipeline |
| ADR-008 | Frontend Technology Selection | Next.js 14 (App Router) + Ant Design 5 |

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Kubernetes Cluster                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Ingress  │  │  Kong    │  │  SCG     │  │  ERP App │   │
│  │  (Nginx)  │→│  Gateway │→│  Gateway │→│  Pods     │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ PG RDS   │  │  Redis   │  │  Kafka   │  │  MinIO   │   │
│  │ Cluster  │  │ Cluster  │  │ Cluster  │  │ Cluster  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │   ES     │  │ ClickHouse│  │  Nacos   │                  │
│  │ Cluster  │  │ Cluster  │  │ Cluster  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

Multi-Environment Support:
- **Local Development**: H2 + Embedded Redis/Kafka, zero-container startup
- **Test Environment**: Docker Compose one-command orchestration
- **Production**: K8s + Helm, multi-cloud deployment (Huawei Cloud / AWS / Alibaba Cloud)

---

## Project Progress

| Phase | Status | Deliverables |
|-------|--------|-------------|
| P0 Engineering Baseline | Done | 18-module skeleton, DDD layers, Docker Compose, Flyway, CI/CD |
| P1 Technical Middle Platform | Done | JWT auth, RBAC, 10-dim permissions, event-driven, Outbox, cache, search, approval |
| P2 Core Domain Business | Done | 14-domain business logic + 45 frontend pages + frontend-backend integration |
| P3 Integration Testing | Done | 14-domain backend integration tests + cross-domain loop verification |
| P4 Deployment & Launch | In Progress | K8s Helm Charts, CI/CD refinement, monitoring & alerting |

---

## Contributing

Issues and Pull Requests are welcome. Please ensure:

1. Code passes `mvn spotless:check` format check
2. New APIs follow `/{service-name}/api/{direction}/v1/{resource}` path convention
3. Domain layer code does not depend on Spring framework
4. All business data retains `tenantId` and audit fields

---

## License

MIT License - See [LICENSE](LICENSE) file for details
