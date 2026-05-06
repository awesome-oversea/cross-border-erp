'use client';

import { useState } from 'react';
import { Card, Typography, Table, Tag, Input, Space, Button, Modal, Form, Select, message } from 'antd';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons';

const { Title } = Typography;

const mockConfigs = [
  { key: '1', configKey: 'order.auto_confirm_hours', configValue: '24', category: 'ORDER', description: '订单自动确认时间(小时)', editable: true },
  { key: '2', configKey: 'inventory.low_stock_threshold', configValue: '20', category: 'INVENTORY', description: '低库存预警阈值', editable: true },
  { key: '3', configKey: 'finance.currency_base', configValue: 'CNY', category: 'FINANCE', description: '基准币种', editable: true },
  { key: '4', configKey: 'logistics.default_carrier', configValue: 'DHL', category: 'LOGISTICS', description: '默认物流商', editable: true },
  { key: '5', configKey: 'system.max_retry_count', configValue: '3', category: 'SYSTEM', description: '最大重试次数', editable: false },
  { key: '6', configKey: 'webhook.timeout_seconds', configValue: '30', category: 'SYSTEM', description: 'Webhook超时时间(秒)', editable: true },
];

export default function ConfigsPage() {
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const columns = [
    { title: '参数键', dataIndex: 'configKey', key: 'configKey', render: (v: string) => <code style={{ fontSize: 13 }}>{v}</code> },
    { title: '参数值', dataIndex: 'configValue', key: 'configValue', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '分类', dataIndex: 'category', key: 'category', render: (v: string) => <Tag>{v}</Tag> },
    { title: '描述', dataIndex: 'description', key: 'description' },
    { title: '可编辑', dataIndex: 'editable', key: 'editable', render: (v: boolean) => <Tag color={v ? 'green' : 'default'}>{v ? '是' : '否'}</Tag> },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: typeof mockConfigs[0]) => record.editable ? <Button type="link" onClick={() => { form.setFieldsValue(record); setModalOpen(true); }}>编辑</Button> : null,
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>系统参数</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space>
          <Input placeholder="搜索参数键" prefix={<SearchOutlined />} style={{ width: 280 }} />
          <Button type="primary">查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={mockConfigs} size="middle" pagination={false} />
      </Card>
      <Modal title="编辑参数" open={modalOpen} onOk={() => { message.success('参数已更新'); setModalOpen(false); }} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="configKey" label="参数键">
            <Input disabled />
          </Form.Item>
          <Form.Item name="configValue" label="参数值" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input disabled />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
