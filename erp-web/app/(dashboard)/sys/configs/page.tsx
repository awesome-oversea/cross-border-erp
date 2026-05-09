'use client';

import { useState } from 'react';
import { Card, Typography, Table, Tag, Input, Space, Button, Modal, Form, Select, message, Tabs } from 'antd';
import { SearchOutlined, EditOutlined, SendOutlined, ApiOutlined, ThunderboltOutlined, SafetyOutlined, PlusOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { sysApi } from '@/lib/api';
import type { SysConfig, WebhookEndpoint, BusinessRule, Connector, ComplianceRule, PageParams } from '@/types';

const { Title } = Typography;

export default function ConfigsPage() {
  const [configParams, setConfigParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [webhookParams, setWebhookParams] = useState<PageParams>({ page: 1, size: 20 });
  const [ruleParams, setRuleParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [connectorParams, setConnectorParams] = useState<PageParams>({ page: 1, size: 20 });
  const [complianceParams, setComplianceParams] = useState<PageParams>({ page: 1, size: 20 });

  const [configModalOpen, setConfigModalOpen] = useState(false);
  const [webhookModalOpen, setWebhookModalOpen] = useState(false);
  const [connectorModalOpen, setConnectorModalOpen] = useState(false);
  const [configForm] = Form.useForm();
  const [webhookForm] = Form.useForm();
  const [connectorForm] = Form.useForm();

  const { data: configs, mutate: mutateConfigs } = usePageApi<SysConfig>('/sys/api/in/v1/configs', configParams);
  const { data: webhooks, mutate: mutateWebhooks } = usePageApi<WebhookEndpoint>('/sys/api/in/v1/webhooks', webhookParams);
  const { data: rules } = usePageApi<BusinessRule>('/sys/api/in/v1/business-rules', ruleParams);
  const { data: connectors, mutate: mutateConnectors } = usePageApi<Connector>('/sys/api/in/v1/connectors', connectorParams);
  const { data: complianceRules } = usePageApi<ComplianceRule>('/sys/api/in/v1/compliance-rules', complianceParams);

  const handleUpdateConfig = async () => {
    try {
      const values = await configForm.validateFields();
      await sysApi.updateConfig(values.configId, { configValue: values.configValue });
      message.success('参数已更新');
      setConfigModalOpen(false);
      mutateConfigs();
    } catch { message.error('更新失败'); }
  };

  const handleCreateWebhook = async () => {
    try {
      const values = await webhookForm.validateFields();
      await sysApi.createWebhookEndpoint(values);
      message.success('Webhook创建成功');
      setWebhookModalOpen(false);
      mutateWebhooks();
    } catch { message.error('创建失败'); }
  };

  const handleTestWebhook = async (id: string) => {
    try {
      await sysApi.testWebhookEndpoint(id);
      message.success('测试请求已发送');
    } catch { message.error('测试失败'); }
  };

  const handleCreateConnector = async () => {
    try {
      const values = await connectorForm.validateFields();
      await sysApi.createConnector(values);
      message.success('连接器创建成功');
      setConnectorModalOpen(false);
      mutateConnectors();
    } catch { message.error('创建失败'); }
  };

  const handleTestConnector = async (id: string) => {
    try {
      await sysApi.testConnector(id);
      message.success('连接测试成功');
    } catch { message.error('连接测试失败'); }
  };

  const configColumns = [
    { title: '参数键', dataIndex: 'configKey', key: 'configKey', render: (v: string) => <code style={{ fontSize: 13 }}>{v}</code> },
    { title: '参数值', dataIndex: 'configValue', key: 'configValue', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '分类', dataIndex: 'category', key: 'category', render: (v: string) => <Tag>{v}</Tag> },
    { title: '描述', dataIndex: 'description', key: 'description' },
    { title: '操作', key: 'action', render: (_: unknown, record: SysConfig) => (
      <Button type="link" size="small" icon={<EditOutlined />} onClick={() => {
        configForm.setFieldsValue(record);
        setConfigModalOpen(true);
      }}>编辑</Button>
    )},
  ];

  const webhookColumns = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: 'URL', dataIndex: 'url', key: 'url', ellipsis: true },
    { title: '事件', dataIndex: 'eventType', key: 'eventType', render: (v: string) => <Tag>{v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'ACTIVE' ? 'green' : 'default'}>{v}</Tag> },
    { title: '操作', key: 'action', render: (_: unknown, r: WebhookEndpoint) => (
      <Space>
        <Button type="link" size="small" icon={<SendOutlined />} onClick={() => handleTestWebhook(r.endpointId)}>测试</Button>
        <Button type="link" size="small" danger onClick={async () => {
          await sysApi.deleteWebhookEndpoint(r.endpointId);
          mutateWebhooks();
        }}>删除</Button>
      </Space>
    )},
  ];

  const ruleColumns = [
    { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
    { title: '规则类型', dataIndex: 'ruleType', key: 'ruleType', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '版本', dataIndex: 'currentVersion', key: 'currentVersion', render: (v: number) => `v${v ?? 1}` },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'ACTIVE' ? 'green' : 'default'}>{v === 'ACTIVE' ? '启用' : v}</Tag> },
    { title: '操作', key: 'action', render: (_: unknown, r: BusinessRule) => (
      <Space>
        <Button type="link" size="small" onClick={async () => {
          try { await sysApi.simulateRule(r.ruleId, { inputContext: '{}' }); message.info('模拟执行完成'); } catch {}
        }}>模拟</Button>
      </Space>
    )},
  ];

  const connectorColumns = [
    { title: '名称', dataIndex: 'connectorName', key: 'connectorName' },
    { title: '类型', dataIndex: 'connectorType', key: 'connectorType', render: (v: string) => <Tag>{v}</Tag> },
    { title: '平台', dataIndex: 'platform', key: 'platform' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'CONNECTED' ? 'green' : v === 'ERROR' ? 'red' : 'orange'}>{v}</Tag> },
    { title: '最后同步', dataIndex: 'lastSyncAt', key: 'lastSyncAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    { title: '操作', key: 'action', render: (_: unknown, r: Connector) => (
      <Space>
        <Button type="link" size="small" icon={<ApiOutlined />} onClick={() => handleTestConnector(r.connectorId)}>测试</Button>
        <Button type="link" size="small" icon={<ThunderboltOutlined />} onClick={async () => {
          await sysApi.syncConnector(r.connectorId);
          message.success('同步任务已发起');
        }}>同步</Button>
      </Space>
    )},
  ];

  const complianceColumns = [
    { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
    { title: '合规类型', dataIndex: 'complianceType', key: 'complianceType', render: (v: string) => <Tag color="purple">{v}</Tag> },
    { title: '地区', dataIndex: 'region', key: 'region' },
    { title: '状态', dataIndex: 'active', key: 'active', render: (v: boolean) => <Tag color={v ? 'green' : 'default'}>{v ? '启用' : '停用'}</Tag> },
  ];

  return (
    <div>
      <Card>
        <Tabs items={[
          {
            key: 'configs',
            label: '系统参数',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>系统参数</Title>
                  <Space>
                    <Input placeholder="搜索参数键" prefix={<SearchOutlined />} allowClear style={{ width: 240 }}
                      onChange={(e) => setConfigParams({ ...configParams, keyword: e.target.value || undefined })} />
                    <Select placeholder="分类" allowClear style={{ width: 140 }}
                      options={[{ value: 'ORDER', label: '订单' }, { value: 'INVENTORY', label: '库存' }, { value: 'FINANCE', label: '财务' }, { value: 'SYSTEM', label: '系统' }]}
                      onChange={(v) => setConfigParams({ ...configParams, category: v })} />
                  </Space>
                </div>
                <Table rowKey="configId" columns={configColumns} dataSource={configs?.list || []}
                  pagination={{ current: configParams.page, pageSize: configParams.size, total: configs?.total || 0, onChange: (page, size) => setConfigParams({ ...configParams, page, size }) }} />
              </>
            ),
          },
          {
            key: 'webhooks',
            label: 'Webhook',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>Webhook管理</Title>
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => { webhookForm.resetFields(); setWebhookModalOpen(true); }}>创建Webhook</Button>
                </div>
                <Table rowKey="endpointId" columns={webhookColumns} dataSource={webhooks?.list || []}
                  pagination={{ current: webhookParams.page, pageSize: webhookParams.size, total: webhooks?.total || 0, onChange: (page, size) => setWebhookParams({ ...webhookParams, page, size }) }} />
              </>
            ),
          },
          {
            key: 'rules',
            label: '业务规则',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>业务规则</Title>
                  <Select placeholder="规则类型" allowClear style={{ width: 140 }}
                    onChange={(v) => setRuleParams({ ...ruleParams, ruleType: v })} />
                </div>
                <Table rowKey="ruleId" columns={ruleColumns} dataSource={rules?.list || []}
                  pagination={{ current: ruleParams.page, pageSize: ruleParams.size, total: rules?.total || 0, onChange: (page, size) => setRuleParams({ ...ruleParams, page, size }) }} />
              </>
            ),
          },
          {
            key: 'connectors',
            label: '连接器',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>平台连接器</Title>
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => { connectorForm.resetFields(); setConnectorModalOpen(true); }}>创建连接器</Button>
                </div>
                <Table rowKey="connectorId" columns={connectorColumns} dataSource={connectors?.list || []}
                  pagination={{ current: connectorParams.page, pageSize: connectorParams.size, total: connectors?.total || 0, onChange: (page, size) => setConnectorParams({ ...connectorParams, page, size }) }} />
              </>
            ),
          },
          {
            key: 'compliance',
            label: '合规规则',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>合规规则</Title>
                </div>
                <Table rowKey="ruleId" columns={complianceColumns} dataSource={complianceRules?.list || []}
                  pagination={{ current: complianceParams.page, pageSize: complianceParams.size, total: complianceRules?.total || 0, onChange: (page, size) => setComplianceParams({ ...complianceParams, page, size }) }} />
              </>
            ),
          },
        ]} />
      </Card>

      <Modal title="编辑参数" open={configModalOpen} onOk={handleUpdateConfig} onCancel={() => setConfigModalOpen(false)} width={480}>
        <Form form={configForm} layout="vertical">
          <Form.Item name="configId" hidden><Input /></Form.Item>
          <Form.Item name="configKey" label="参数键"><Input disabled /></Form.Item>
          <Form.Item name="configValue" label="参数值" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>

      <Modal title="创建Webhook" open={webhookModalOpen} onOk={handleCreateWebhook} onCancel={() => setWebhookModalOpen(false)} width={560}>
        <Form form={webhookForm} layout="vertical">
          <Form.Item name="endpointName" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="url" label="URL" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="events" label="订阅事件" rules={[{ required: true }]}>
            <Select mode="multiple" options={[
              { value: 'order.created', label: '订单创建' },
              { value: 'order.shipped', label: '订单发货' },
              { value: 'inventory.low_stock', label: '库存预警' },
              { value: 'shipment.delivered', label: '货物签收' },
              { value: 'payment.received', label: '收款确认' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="创建连接器" open={connectorModalOpen} onOk={handleCreateConnector} onCancel={() => setConnectorModalOpen(false)} width={560}>
        <Form form={connectorForm} layout="vertical">
          <Form.Item name="connectorName" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="connectorType" label="类型" rules={[{ required: true }]}>
            <Select options={[
              { value: 'MARKETPLACE', label: '电商平台' },
              { value: 'LOGISTICS', label: '物流商' },
              { value: 'PAYMENT', label: '支付渠道' },
              { value: 'AD_PLATFORM', label: '广告平台' },
            ]} />
          </Form.Item>
          <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
            <Select options={[
              { value: 'AMAZON', label: 'Amazon' },
              { value: 'SHOPIFY', label: 'Shopify' },
              { value: 'EBAY', label: 'eBay' },
              { value: 'DHL', label: 'DHL' },
              { value: 'FEDEX', label: 'FedEx' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
