import { get, getPage, post, put, patch, del } from './client';
import type {
  User, Role, Position, ObjectPermission, Permission,
  Product, Listing, Store, PriceRule, ListingMonitor,
  Order, OrderSyncLog, OrderRefund,
  Customer, ServiceTicket, Ticket, EmailRule, EmailCampaign, QualityIssue, QualityInspection, Campaign,
  Warehouse, WarehouseLocation, InventoryBalance, InventoryTransaction, StockCheck,
  Supplier, PurchaseOrder, PurchasePlan,
  CostEvent, Payment, Bill, Settlement, ProfitSummary, Reconciliation,
  Carrier, ShipmentPlan, ShipmentTracking, ShippingBatch,
  FbaShipment, FbaInventory, FbaRemoval,
  AdCampaign, AdGroup, AdKeyword, AdStrategy,
  AlertRule, ReportDefinition, KpiMetric, CockpitTrend, TrendData, RankingData, DashboardMetrics,
  WebhookEndpoint, SysConfig, BusinessRule, ApprovalFlow, Connector, ComplianceRule,
  PageParams,
} from '@/types';

export const iamApi = {
  listUsers: (params?: PageParams & Record<string, unknown>) => getPage<User>('/iam/api/in/v1/users', params),
  getUser: (id: string) => get<User>(`/iam/api/in/v1/users/${id}`),
  createUser: (data: Partial<User>) => post<User>('/iam/api/in/v1/users', data),
  updateUser: (id: string, data: Partial<User>) => put<User>(`/iam/api/in/v1/users/${id}`, data),
  deleteUser: (id: string) => del<void>(`/iam/api/in/v1/users/${id}`),
  listRoles: (params?: PageParams) => getPage<Role>('/iam/api/in/v1/roles', params),
  createRole: (data: Partial<Role>) => post<Role>('/iam/api/in/v1/roles', data),
  updateRole: (id: string, data: Partial<Role>) => put<Role>(`/iam/api/in/v1/roles/${id}`, data),
  deleteRole: (id: string) => del<void>(`/iam/api/in/v1/roles/${id}`),
  listPositions: (params?: PageParams) => getPage<Position>('/iam/api/in/v1/positions', params),
  createPosition: (data: Partial<Position>) => post<Position>('/iam/api/in/v1/positions', data),
  updatePosition: (id: string, data: Partial<Position>) => put<Position>(`/iam/api/in/v1/positions/${id}`, data),
  listObjectPermissions: (userId: string) => get<ObjectPermission[]>(`/iam/api/in/v1/object-permissions?userId=${userId}`),
  grantObjectPermission: (data: Partial<ObjectPermission>) => post<ObjectPermission>('/iam/api/in/v1/object-permissions', data),
  revokeObjectPermission: (id: string) => del<void>(`/iam/api/in/v1/object-permissions/${id}`),
};

export const pdmApi = {
  listProducts: (params?: PageParams & Record<string, unknown>) => getPage<Product>('/pdm/api/in/v1/products', params),
  getProduct: (id: string) => get<Product>(`/pdm/api/in/v1/products/${id}`),
  createProduct: (data: Partial<Product>) => post<Product>('/pdm/api/in/v1/products', data),
  updateProduct: (id: string, data: Partial<Product>) => put<Product>(`/pdm/api/in/v1/products/${id}`, data),
  deleteProduct: (id: string) => del<void>(`/pdm/api/in/v1/products/${id}`),
};

export const somApi = {
  listStores: (params?: PageParams) => getPage<Store>('/som/api/in/v1/stores', params),
  getStore: (id: string) => get<Store>(`/som/api/in/v1/stores/${id}`),
  createStore: (data: Partial<Store>) => post<Store>('/som/api/in/v1/stores', data),
  updateStore: (id: string, data: Partial<Store>) => put<Store>(`/som/api/in/v1/stores/${id}`, data),
  listListings: (params?: PageParams & Record<string, unknown>) => getPage<Listing>('/som/api/in/v1/listings', params),
  getListing: (id: string) => get<Listing>(`/som/api/in/v1/listings/${id}`),
  updateListing: (id: string, data: Partial<Listing>) => put<Listing>(`/som/api/in/v1/listings/${id}`, data),
  syncListings: (storeId: string) => post<void>(`/som/api/in/v1/listings/sync?storeId=${storeId}`),
  listPriceRules: (params?: PageParams & Record<string, unknown>) => getPage<PriceRule>('/som/api/in/v1/price-rules', params),
  createPriceRule: (data: Partial<PriceRule>) => post<PriceRule>('/som/api/in/v1/price-rules', data),
  updatePriceRule: (id: string, data: Partial<PriceRule>) => put<PriceRule>(`/som/api/in/v1/price-rules/${id}`, data),
  deletePriceRule: (id: string) => del<void>(`/som/api/in/v1/price-rules/${id}`),
  listListingMonitors: (params?: PageParams & Record<string, unknown>) => getPage<ListingMonitor>('/som/api/in/v1/listing-monitors', params),
  refreshListingMonitor: (listingId: string) => post<ListingMonitor>(`/som/api/in/v1/listing-monitors/${listingId}/refresh`),
};

export const adsApi = {
  listCampaigns: (params?: PageParams & Record<string, unknown>) => getPage<AdCampaign>('/ads/api/in/v1/campaigns', params),
  getCampaign: (id: string) => get<AdCampaign>(`/ads/api/in/v1/campaigns/${id}`),
  createCampaign: (data: Partial<AdCampaign>) => post<AdCampaign>('/ads/api/in/v1/campaigns', data),
  updateCampaign: (id: string, data: Partial<AdCampaign>) => put<AdCampaign>(`/ads/api/in/v1/campaigns/${id}`, data),
  pauseCampaign: (id: string) => patch<AdCampaign>(`/ads/api/in/v1/campaigns/${id}/pause`),
  resumeCampaign: (id: string) => patch<AdCampaign>(`/ads/api/in/v1/campaigns/${id}/resume`),
  listAdGroups: (campaignId: string) => get<AdGroup[]>(`/ads/api/in/v1/campaigns/${campaignId}/groups`),
  createAdGroup: (campaignId: string, data: Partial<AdGroup>) => post<AdGroup>(`/ads/api/in/v1/campaigns/${campaignId}/groups`, data),
  listKeywords: (groupId: string) => get<AdKeyword[]>(`/ads/api/in/v1/groups/${groupId}/keywords`),
  createKeyword: (groupId: string, data: Partial<AdKeyword>) => post<AdKeyword>(`/ads/api/in/v1/groups/${groupId}/keywords`, data),
  listStrategies: (params?: PageParams) => getPage<AdStrategy>('/ads/api/in/v1/strategies', params),
  createStrategy: (data: Partial<AdStrategy>) => post<AdStrategy>('/ads/api/in/v1/strategies', data),
  syncCampaignData: (storeId: string) => post<void>(`/ads/api/in/v1/campaigns/sync?storeId=${storeId}`),
};

export const omsApi = {
  listOrders: (params?: PageParams & Record<string, unknown>) => getPage<Order>('/oms/api/in/v1/orders', params),
  getOrder: (id: string) => get<Order>(`/oms/api/in/v1/orders/${id}`),
  importOrder: (data: Record<string, unknown>) => post<Order>('/oms/api/in/v1/orders/import', data),
  syncOrders: (data: { platform: string; storeId: string; syncType: string }) => post<OrderSyncLog>('/oms/api/in/v1/orders/sync', data),
  markPaid: (id: string) => patch<Order>(`/oms/api/in/v1/orders/${id}/paid`),
  cancelOrder: (id: string, data?: { reason?: string }) => patch<Order>(`/oms/api/in/v1/orders/${id}/cancel`, data),
  auditOrder: (id: string, data: { action: string; reason?: string }) => patch<Order>(`/oms/api/in/v1/orders/${id}/audit`, data),
  requestRefund: (orderId: string, data: { refundType: string; amount: number; currency: string; reason: string }) => post<OrderRefund>(`/oms/api/in/v1/orders/${orderId}/refunds`, data),
  listSyncLogs: (params?: PageParams & Record<string, unknown>) => getPage<OrderSyncLog>('/oms/api/in/v1/orders/sync-logs', params),
};

export const scmApi = {
  listSuppliers: (params?: PageParams & Record<string, unknown>) => getPage<Supplier>('/scm/api/in/v1/suppliers', params),
  getSupplier: (id: string) => get<Supplier>(`/scm/api/in/v1/suppliers/${id}`),
  createSupplier: (data: Partial<Supplier>) => post<Supplier>('/scm/api/in/v1/suppliers', data),
  updateSupplier: (id: string, data: Partial<Supplier>) => put<Supplier>(`/scm/api/in/v1/suppliers/${id}`, data),
  listPurchaseOrders: (params?: PageParams & Record<string, unknown>) => getPage<PurchaseOrder>('/scm/api/in/v1/purchase-orders', params),
  getPurchaseOrder: (id: string) => get<PurchaseOrder>(`/scm/api/in/v1/purchase-orders/${id}`),
  createPurchaseOrder: (data: Partial<PurchaseOrder>) => post<PurchaseOrder>('/scm/api/in/v1/purchase-orders', data),
  approvePurchaseOrder: (id: string) => patch<PurchaseOrder>(`/scm/api/in/v1/purchase-orders/${id}/approve`),
  receivePurchaseOrder: (id: string, data: { items: { itemId: string; receivedQty: number }[] }) => patch<PurchaseOrder>(`/scm/api/in/v1/purchase-orders/${id}/receive`, data),
  listPurchasePlans: (params?: PageParams & Record<string, unknown>) => getPage<PurchasePlan>('/scm/api/in/v1/purchase-plans', params),
  generatePurchasePlans: () => post<void>('/scm/api/in/v1/purchase-plans/generate'),
  approvePurchasePlan: (id: string) => patch<PurchasePlan>(`/scm/api/in/v1/purchase-plans/${id}/approve`),
  convertPlanToOrder: (id: string) => post<PurchaseOrder>(`/scm/api/in/v1/purchase-plans/${id}/convert`),
};

export const wmsApi = {
  listWarehouses: (params?: PageParams) => getPage<Warehouse>('/wms/api/in/v1/warehouses', params),
  getWarehouse: (id: string) => get<Warehouse>(`/wms/api/in/v1/warehouses/${id}`),
  createWarehouse: (data: Partial<Warehouse>) => post<Warehouse>('/wms/api/in/v1/warehouses', data),
  updateWarehouse: (id: string, data: Partial<Warehouse>) => put<Warehouse>(`/wms/api/in/v1/warehouses/${id}`, data),
  listLocations: (warehouseId: string) => get<WarehouseLocation[]>(`/wms/api/in/v1/warehouses/${warehouseId}/locations`),
  listInventory: (params?: PageParams & Record<string, unknown>) => getPage<InventoryBalance>('/wms/api/in/v1/inventory', params),
  listTransactions: (params?: PageParams & Record<string, unknown>) => getPage<InventoryTransaction>('/wms/api/in/v1/inventory/transactions', params),
  adjustInventory: (data: { skuId: string; warehouseId: string; quantity: number; reason: string }) => post<InventoryTransaction>('/wms/api/in/v1/inventory/adjust', data),
  listStockChecks: (params?: PageParams) => getPage<StockCheck>('/wms/api/in/v1/stock-checks', params),
  createStockCheck: (data: { warehouseId: string }) => post<StockCheck>('/wms/api/in/v1/stock-checks', data),
};

export const fbaApi = {
  listShipmentPlans: (params?: PageParams & Record<string, unknown>) => getPage<ShipmentPlan>('/fba/api/in/v1/shipments', params),
  createShipmentPlan: (data: Partial<ShipmentPlan>) => post<ShipmentPlan>('/fba/api/in/v1/shipments', data),
  listFbaShipments: (params?: PageParams & Record<string, unknown>) => getPage<FbaShipment>('/fba/api/in/v1/fba-shipments', params),
  getFbaShipment: (id: string) => get<FbaShipment>(`/fba/api/in/v1/fba-shipments/${id}`),
  listFbaInventory: (params?: PageParams & Record<string, unknown>) => getPage<FbaInventory>('/fba/api/in/v1/fba-inventory', params),
  listRemovals: (params?: PageParams & Record<string, unknown>) => getPage<FbaRemoval>('/fba/api/in/v1/removals', params),
  createRemoval: (data: Partial<FbaRemoval>) => post<FbaRemoval>('/fba/api/in/v1/removals', data),
  generateRestockSuggestions: () => post<void>('/fba/api/in/v1/restock/generate'),
};

export const tmsApi = {
  listCarriers: (params?: PageParams) => getPage<Carrier>('/tms/api/in/v1/carriers', params),
  createCarrier: (data: Partial<Carrier>) => post<Carrier>('/tms/api/in/v1/carriers', data),
  updateCarrier: (id: string, data: Partial<Carrier>) => put<Carrier>(`/tms/api/in/v1/carriers/${id}`, data),
  listShipmentTrackings: (params?: PageParams & Record<string, unknown>) => getPage<ShipmentTracking>('/tms/api/in/v1/trackings', params),
  getShipmentTracking: (trackingNo: string) => get<ShipmentTracking>(`/tms/api/in/v1/trackings/${trackingNo}`),
  listShippingBatches: (params?: PageParams & Record<string, unknown>) => getPage<ShippingBatch>('/tms/api/in/v1/shipping-batches', params),
  createShippingBatch: (data: Partial<ShippingBatch>) => post<ShippingBatch>('/tms/api/in/v1/shipping-batches', data),
  confirmShippingBatch: (id: string) => patch<ShippingBatch>(`/tms/api/in/v1/shipping-batches/${id}/confirm`),
};

export const crmApi = {
  listCustomers: (params?: PageParams & Record<string, unknown>) => getPage<Customer>('/crm/api/in/v1/customers', params),
  getCustomer: (id: string) => get<Customer>(`/crm/api/in/v1/customers/${id}`),
  createCustomer: (data: Partial<Customer>) => post<Customer>('/crm/api/in/v1/customers', data),
  updateCustomer: (id: string, data: Partial<Customer>) => put<Customer>(`/crm/api/in/v1/customers/${id}`, data),
  listTickets: (params?: PageParams & Record<string, unknown>) => getPage<Ticket>('/crm/api/in/v1/tickets', params),
  createTicket: (data: Partial<Ticket>) => post<Ticket>('/crm/api/in/v1/tickets', data),
  resolveTicket: (id: string, data: { resolution: string }) => patch<Ticket>(`/crm/api/in/v1/tickets/${id}/resolve`, data),
  listEmailRules: (params?: PageParams & Record<string, unknown>) => getPage<EmailRule>('/crm/api/in/v1/email-rules', params),
  createEmailRule: (data: Partial<EmailRule>) => post<EmailRule>('/crm/api/in/v1/email-rules', data),
  updateEmailRule: (id: string, data: Partial<EmailRule>) => put<EmailRule>(`/crm/api/in/v1/email-rules/${id}`, data),
  deleteEmailRule: (id: string) => del<void>(`/crm/api/in/v1/email-rules/${id}`),
  listCampaigns: (params?: PageParams & Record<string, unknown>) => getPage<Campaign>('/crm/api/in/v1/campaigns', params),
  createCampaign: (data: Partial<Campaign>) => post<Campaign>('/crm/api/in/v1/campaigns', data),
  listQualityInspections: (params?: PageParams & Record<string, unknown>) => getPage<QualityInspection>('/crm/api/in/v1/quality-inspections', params),
  listQualityIssues: (params?: PageParams & Record<string, unknown>) => getPage<QualityIssue>('/crm/api/in/v1/quality-issues', params),
  createQualityIssue: (data: Partial<QualityIssue>) => post<QualityIssue>('/crm/api/in/v1/quality-issues', data),
  resolveQualityIssue: (id: string) => patch<QualityIssue>(`/crm/api/in/v1/quality-issues/${id}/resolve`),
};

export const fmsApi = {
  listCostEvents: (params?: PageParams & Record<string, unknown>) => getPage<CostEvent>('/fms/api/in/v1/cost-events', params),
  listPayments: (params?: PageParams & Record<string, unknown>) => getPage<Payment>('/fms/api/in/v1/payments', params),
  listBills: (params?: PageParams & Record<string, unknown>) => getPage<Bill>('/fms/api/in/v1/bills', params),
  payBill: (id: string, data: { paidAmount: number }) => patch<Bill>(`/fms/api/in/v1/bills/${id}/pay`, data),
  listSettlements: (params?: PageParams & Record<string, unknown>) => getPage<Settlement>('/fms/api/in/v1/settlements', params),
  confirmSettlement: (id: string) => patch<Settlement>(`/fms/api/in/v1/settlements/${id}/confirm`),
  getProfitSummary: (params?: Record<string, unknown>) => get<ProfitSummary[]>('/fms/api/in/v1/profit/summary', params),
  listReconciliations: (params?: PageParams & Record<string, unknown>) => getPage<Reconciliation>('/fms/api/in/v1/reconciliations', params),
  resolveReconciliation: (id: string, data: { resolution: string }) => patch<Reconciliation>(`/fms/api/in/v1/reconciliations/${id}/resolve`, data),
  listVatStatuses: () => get<unknown[]>('/fms/api/in/v1/vat-statuses'),
};

export const biApi = {
  getCockpitData: () => get<Record<string, unknown>>('/bi/api/in/v1/cockpit'),
  getDashboardSummary: () => get<DashboardMetrics>('/bi/api/in/v1/dashboard/summary'),
  listKpis: (params?: PageParams & Record<string, unknown>) => getPage<KpiMetric>('/bi/api/in/v1/kpis', params),
  recordKpi: (data: Partial<KpiMetric>) => post<KpiMetric>('/bi/api/in/v1/kpis', data),
  listAlertRules: (params?: PageParams & Record<string, unknown>) => getPage<AlertRule>('/bi/api/in/v1/alert-rules', params),
  createAlertRule: (data: Partial<AlertRule>) => post<AlertRule>('/bi/api/in/v1/alert-rules', data),
  listReports: (params?: PageParams & Record<string, unknown>) => getPage<ReportDefinition>('/bi/api/in/v1/reports', params),
  listTrends: (params?: Record<string, unknown>) => get<CockpitTrend[]>('/bi/api/in/v1/trends', params),
  generateTrend: (data: { metricCode: string; metricName: string; period: string; dataPoints: number }) => post<CockpitTrend>('/bi/api/in/v1/trends', data),
  listRankings: (params?: Record<string, unknown>) => get<RankingData[]>('/bi/api/in/v1/rankings', params),
  generateRanking: (data: { rankingType: string; dimension: string; items: { rankKey: string; label: string; value: number; rank: number }[] }) => post<RankingData>('/bi/api/in/v1/rankings', data),
};

export const dashboardApi = {
  getMetrics: () => get<DashboardMetrics>('/dashboard/api/in/v1/metrics'),
  getRecentOrders: (limit?: number) => get<Order[]>('/dashboard/api/in/v1/recent-orders', limit ? { limit } : undefined),
  getSalesTrend: (days?: number) => get<Record<string, unknown>>('/dashboard/api/in/v1/sales-trend', days ? { days } : undefined),
  getPlatformDistribution: () => get<Record<string, unknown>>('/dashboard/api/in/v1/platform-distribution'),
  getTopProducts: (limit?: number) => get<Record<string, unknown>>('/dashboard/api/in/v1/top-products', limit ? { limit } : undefined),
};

export const sysApi = {
  listConfigs: (params?: PageParams & Record<string, unknown>) => getPage<SysConfig>('/sys/api/in/v1/configs', params),
  updateConfig: (id: string, data: { configValue: string }) => put<SysConfig>(`/sys/api/in/v1/configs/${id}`, data),
  listWebhookEndpoints: (params?: PageParams) => getPage<WebhookEndpoint>('/sys/api/in/v1/webhooks', params),
  createWebhookEndpoint: (data: Partial<WebhookEndpoint>) => post<WebhookEndpoint>('/sys/api/in/v1/webhooks', data),
  updateWebhookEndpoint: (id: string, data: Partial<WebhookEndpoint>) => put<WebhookEndpoint>(`/sys/api/in/v1/webhooks/${id}`, data),
  deleteWebhookEndpoint: (id: string) => del<void>(`/sys/api/in/v1/webhooks/${id}`),
  testWebhookEndpoint: (id: string) => post<Record<string, unknown>>(`/sys/api/in/v1/webhooks/${id}/test`),
  listBusinessRules: (params?: PageParams & Record<string, unknown>) => getPage<BusinessRule>('/sys/api/in/v1/business-rules', params),
  createBusinessRule: (data: Partial<BusinessRule>) => post<BusinessRule>('/sys/api/in/v1/business-rules', data),
  createRuleVersion: (id: string, data: { contentJson: string; changeDescription: string }) => post<BusinessRule>(`/sys/api/in/v1/business-rules/${id}/versions`, data),
  simulateRule: (id: string, data: { inputContext: string }) => post<Record<string, unknown>>(`/sys/api/in/v1/business-rules/${id}/simulate`, data),
  listApprovalFlows: (params?: PageParams) => getPage<ApprovalFlow>('/sys/api/in/v1/approval-flows', params),
  createApprovalFlow: (data: Partial<ApprovalFlow>) => post<ApprovalFlow>('/sys/api/in/v1/approval-flows', data),
  listConnectors: (params?: PageParams) => getPage<Connector>('/sys/api/in/v1/connectors', params),
  createConnector: (data: Partial<Connector>) => post<Connector>('/sys/api/in/v1/connectors', data),
  testConnector: (id: string) => post<Record<string, unknown>>(`/sys/api/in/v1/connectors/${id}/test`),
  syncConnector: (id: string) => post<void>(`/sys/api/in/v1/connectors/${id}/sync`),
  listComplianceRules: (params?: PageParams) => getPage<ComplianceRule>('/sys/api/in/v1/compliance-rules', params),
  createComplianceRule: (data: Partial<ComplianceRule>) => post<ComplianceRule>('/sys/api/in/v1/compliance-rules', data),
};
