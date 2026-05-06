import { get, post, put, patch, del } from './client';
import type {
  User, Role, Position, ObjectPermission,
  Customer, ServiceTicket, EmailRule, EmailCampaign, QualityIssue,
  Order, Product, Warehouse, InventoryBalance,
  Supplier, PurchaseOrder, CostEvent, Carrier, ShipmentPlan,
  AlertRule, ReportDefinition, KpiMetric, CockpitTrend, RankingData,
  WebhookEndpoint,
} from '@/types';

export const iamApi = {
  listUsers: () => get<User[]>('/iam/api/in/v1/users'),
  createUser: (data: Partial<User>) => post<User>('/iam/api/in/v1/users', data),
  updateUser: (id: string, data: Partial<User>) => put<User>(`/iam/api/in/v1/users/${id}`, data),
  listRoles: () => get<Role[]>('/iam/api/in/v1/roles'),
  createRole: (data: Partial<Role>) => post<Role>('/iam/api/in/v1/roles', data),
  listPositions: () => get<Position[]>('/iam/api/in/v1/positions'),
  createPosition: (data: Partial<Position>) => post<Position>('/iam/api/in/v1/positions', data),
  listObjectPermissions: (userId: string) => get<ObjectPermission[]>(`/iam/api/in/v1/object-permissions?userId=${userId}`),
  grantObjectPermission: (data: Partial<ObjectPermission>) => post<ObjectPermission>('/iam/api/in/v1/object-permissions', data),
};

export const omsApi = {
  listOrders: (params?: Record<string, string>) => get<Order[]>('/oms/api/in/v1/orders', params),
  getOrder: (id: string) => get<Order>(`/oms/api/in/v1/orders/${id}`),
  auditOrder: (id: string, data: { action: string; reason?: string }) => patch<Order>(`/oms/api/in/v1/orders/${id}/audit`, data),
};

export const pdmApi = {
  listProducts: () => get<Product[]>('/pdm/api/in/v1/products'),
  getProduct: (id: string) => get<Product>(`/pdm/api/in/v1/products/${id}`),
  createProduct: (data: Partial<Product>) => post<Product>('/pdm/api/in/v1/products', data),
  updateProduct: (id: string, data: Partial<Product>) => put<Product>(`/pdm/api/in/v1/products/${id}`, data),
};

export const wmsApi = {
  listWarehouses: () => get<Warehouse[]>('/wms/api/in/v1/warehouses'),
  createWarehouse: (data: Partial<Warehouse>) => post<Warehouse>('/wms/api/in/v1/warehouses', data),
  listInventory: (warehouseId?: string) => get<InventoryBalance[]>('/wms/api/in/v1/inventory', warehouseId ? { warehouseId } : undefined),
};

export const scmApi = {
  listSuppliers: () => get<Supplier[]>('/scm/api/in/v1/suppliers'),
  createSupplier: (data: Partial<Supplier>) => post<Supplier>('/scm/api/in/v1/suppliers', data),
  listPurchaseOrders: () => get<PurchaseOrder[]>('/scm/api/in/v1/purchase-orders'),
  createPurchaseOrder: (data: Partial<PurchaseOrder>) => post<PurchaseOrder>('/scm/api/in/v1/purchase-orders', data),
};

export const fmsApi = {
  listCostEvents: (params?: Record<string, string>) => get<CostEvent[]>('/fms/api/in/v1/cost-events', params),
  listVatStatuses: () => get<unknown[]>('/fms/api/in/v1/vat-statuses'),
};

export const crmApi = {
  listCustomers: () => get<Customer[]>('/crm/api/in/v1/customers'),
  createCustomer: (data: Partial<Customer>) => post<Customer>('/crm/api/in/v1/customers', data),
  listTickets: () => get<ServiceTicket[]>('/crm/api/in/v1/tickets'),
  createTicket: (data: Partial<ServiceTicket>) => post<ServiceTicket>('/crm/api/in/v1/tickets', data),
  resolveTicket: (id: string, data: { resolution: string }) => patch<ServiceTicket>(`/crm/api/in/v1/tickets/${id}/resolve`, data),
  listEmailRules: () => get<EmailRule[]>('/crm/api/in/v1/email-rules'),
  createEmailRule: (data: Partial<EmailRule>) => post<EmailRule>('/crm/api/in/v1/email-rules', data),
  updateEmailRule: (id: string, data: Partial<EmailRule>) => put<EmailRule>(`/crm/api/in/v1/email-rules/${id}`, data),
  deleteEmailRule: (id: string) => del<void>(`/crm/api/in/v1/email-rules/${id}`),
  listEmailCampaigns: () => get<EmailCampaign[]>('/crm/api/in/v1/email-campaigns'),
  createEmailCampaign: (data: Partial<EmailCampaign>) => post<EmailCampaign>('/crm/api/in/v1/email-campaigns', data),
  sendEmailCampaign: (id: string) => post<EmailCampaign>(`/crm/api/in/v1/email-campaigns/${id}/send`),
  listQualityIssues: (productId?: string) => get<QualityIssue[]>('/crm/api/in/v1/quality-issues', productId ? { productId } : undefined),
  createQualityIssue: (data: Partial<QualityIssue>) => post<QualityIssue>('/crm/api/in/v1/quality-issues', data),
  resolveQualityIssue: (id: string) => patch<QualityIssue>(`/crm/api/in/v1/quality-issues/${id}/resolve`),
};

export const tmsApi = {
  listCarriers: () => get<Carrier[]>('/tms/api/in/v1/carriers'),
  createCarrier: (data: Partial<Carrier>) => post<Carrier>('/tms/api/in/v1/carriers', data),
};

export const fbaApi = {
  listShipmentPlans: () => get<ShipmentPlan[]>('/fba/api/in/v1/shipments'),
  createShipmentPlan: (data: Partial<ShipmentPlan>) => post<ShipmentPlan>('/fba/api/in/v1/shipments', data),
};

export const biApi = {
  getCockpitData: () => get<Record<string, unknown>>('/bi/api/in/v1/cockpit'),
  getDashboardSummary: () => get<unknown>('/bi/api/in/v1/dashboard/summary'),
  listKpis: () => get<KpiMetric[]>('/bi/api/in/v1/kpis'),
  recordKpi: (data: Partial<KpiMetric>) => post<KpiMetric>('/bi/api/in/v1/kpis', data),
  listAlertRules: (domain?: string) => get<AlertRule[]>('/bi/api/in/v1/alert-rules', domain ? { domain } : undefined),
  createAlertRule: (data: Partial<AlertRule>) => post<AlertRule>('/bi/api/in/v1/alert-rules', data),
  listReports: (reportType?: string) => get<ReportDefinition[]>('/bi/api/in/v1/reports', reportType ? { reportType } : undefined),
  listTrends: (metricCode?: string) => get<CockpitTrend[]>('/bi/api/in/v1/trends', metricCode ? { metricCode } : undefined),
  generateTrend: (data: { metricCode: string; metricName: string; period: string; dataPoints: number }) => post<CockpitTrend>('/bi/api/in/v1/trends', data),
  listRankings: (rankingType?: string) => get<RankingData[]>('/bi/api/in/v1/rankings', rankingType ? { rankingType } : undefined),
  generateRanking: (data: { rankingType: string; dimension: string; items: { rankKey: string; label: string; value: number; rank: number }[] }) => post<RankingData>('/bi/api/in/v1/rankings', data),
};

export const sysApi = {
  listWebhookEndpoints: () => get<WebhookEndpoint[]>('/sys/api/in/v1/webhooks'),
  createWebhookEndpoint: (data: Partial<WebhookEndpoint>) => post<WebhookEndpoint>('/sys/api/in/v1/webhooks', data),
};
