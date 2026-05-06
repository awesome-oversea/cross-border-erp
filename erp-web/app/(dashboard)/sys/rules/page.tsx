'use client';

import { useState } from 'react';
import { Card, Typography, Table, Tag, Button, Modal, Form, Input, Select, InputNumber, Space, message, Badge } from 'antd';
import { PlusOutlined, PlayCircleOutlined, HistoryOutlined } from '@ant-design/icons';

const { Title } = Typography;

const mockRules = [
  { key: '1', ruleId: 'R-001', ruleType: 'ORDER_AUDIT', ruleName: '高风险订单审核', version: 3, status: 'ACTIVE', lastExecuted: '2026-05-03 14:30:00' },
  { key: '2', ruleId: 'R-002', ruleType: 'LOGISTICS_SELECT', ruleName: '物流商选择策略', version: 2, status: 'ACTIVE', lastExecuted: '2026-05-03 15:00:00' },
  { key: '3', ruleId: 'R-003', ruleType: 'BILLING', ruleName: '计费规则-标准件', version: 5, status: 'ACTIVE', lastExecuted: '2026-05-03 12:00:00' },
  { key: '4', ruleId: 'R-004', ruleType: 'COST_ALLOC', ruleName: '成本归集规则', version: 1, status: 'DRAFT', lastExecuted: '-' },
  { key: '5', ruleId: 'R-005', ruleType: 'COMPLIANCE', ruleName: '贸易合规检查', version: 4, status: 'ACTIVE', lastExecuted: '2026-05-03 16:00:00' },
];

export default function RulesPage() {
  const [versionModalOpen, setVersionModalOpen] = useState(false);
  const [simulateModalOpen, setSimulateModalOpen] = useState(false);
  const [versionForm] = Form.useForm();
  const [simulateForm] = Form.useForm();

  const columns = [
    { title: '规则ID', dataIndex: 'ruleId', key: 'ruleId' },
    { title: '规则类型', dataIndex: 'ruleType', key: 'ruleType', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
    { title: '版本', dataIndex: 'version', key: 'version', render: (v: number) => <Badge count={`v${v}`} style={{ backgroundColor: '#1890ff' }} /> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'ACTIVE' ? 'green' : 'default'}>{v}</Tag> },
    { title: '最后执行', dataIndex: 'lastExecuted', key: 'lastExecuted' },
    {
      title: '操作', key: 'action', width: 240,
      render: (_: unknown, record: typeof mockRules[0]) => (
        <Space>
          <Button size="small" icon={<PlusOutlined />} onClick={() => { versionForm.setFieldsValue({ ruleId: record.ruleId, ruleType: record.ruleType, ruleName: record.ruleName }); setVersionModalOpen(true); }}>新版本</Button>
          <Button size="small" icon={<PlayCircleOutlined />} onClick={() => { simulateForm.setFieldsValue({ ruleId: record.ruleId, ruleVersion: record.version }); setSimulateModalOpen(true); }}>模拟</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>业务规则</Title>
      <Card style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={mockRules} size="middle" />
      </Card>
      <Modal title="创建规则版本" open={versionModalOpen} onOk={() => { message.success('版本已创建'); setVersionModalOpen(false); }} onCancel={() => setVersionModalOpen(false)} width={560}>
        <Form form={versionForm} layout="vertical">
          <Form.Item name="ruleId" label="规则ID">
            <Input disabled />
          </Form.Item>
          <Form.Item name="ruleType" label="规则类型">
            <Input disabled />
          </Form.Item>
          <Form.Item name="ruleName" label="规则名称">
            <Input disabled />
          </Form.Item>
          <Form.Item name="contentJson" label="规则内容(JSON)" rules={[{ required: true }]}>
            <Input.TextArea rows={6} placeholder='{"condition": "...", "action": "..."}' />
          </Form.Item>
          <Form.Item name="changeDescription" label="变更说明">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="模拟回放" open={simulateModalOpen} onOk={() => { message.success('模拟完成'); setSimulateModalOpen(false); }} onCancel={() => setSimulateModalOpen(false)} width={560}>
        <Form form={simulateForm} layout="vertical">
          <Form.Item name="ruleId" label="规则ID">
            <Input disabled />
          </Form.Item>
          <Form.Item name="ruleVersion" label="版本">
            <InputNumber disabled style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="inputContext" label="输入上下文(JSON)" rules={[{ required: true }]}>
            <Input.TextArea rows={6} placeholder='{"orderId": "OM-001", "amount": 100}' />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
