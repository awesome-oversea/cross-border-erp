'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, InputNumber, Switch } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { somApi } from '@/lib/api';
import type { PriceRule, PageParams } from '@/types';

const { Title } = Typography;

const ruleTypeMap: Record<string, { color: string; text: string }> = {
  FIXED_PRICE: { color: 'blue', text: '固定定价' },
  PERCENTAGE: { color: 'green', text: '百分比调价' },
  COMPETITOR_FOLLOW: { color: 'orange', text: '跟价' },
  MIN_MAX: { color: 'purple', text: '区间定价' },
};

export default function PriceRulesPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<PriceRule | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<PriceRule>('/som/api/in/v1/price-rules', params);

  const handleCreate = () => {
    setEditingRule(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleEdit = (record: PriceRule) => {
    setEditingRule(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await somApi.deletePriceRule(id);
      message.success('规则已删除');
      mutate();
    } catch { message.error('删除失败'); }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRule) {
        await somApi.updatePriceRule(editingRule.ruleId, values);
        message.success('规则更新成功');
      } else {
        await somApi.createPriceRule(values);
        message.success('规则创建成功');
      }
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const columns = [
    { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
    { title: '类型', dataIndex: 'ruleType', key: 'ruleType', render: (v: string) => {
      const s = ruleTypeMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: '目标平台', dataIndex: 'targetPlatform', key: 'targetPlatform', render: (v: string) => v ? <Tag>{v}</Tag> : '-' },
    { title: '调价幅度', key: 'adjustment', render: (_: unknown, r: PriceRule) => {
      if (r.ruleType === 'PERCENTAGE') return `${r.adjustmentValue ?? 0}%`;
      if (r.ruleType === 'FIXED_PRICE') return `$${r.adjustmentValue ?? 0}`;
      return r.adjustmentValue ?? '-';
    }},
    { title: '最低价', dataIndex: 'minPrice', key: 'minPrice', render: (v: number) => v != null ? `$${v}` : '-' },
    { title: '最高价', dataIndex: 'maxPrice', key: 'maxPrice', render: (v: number) => v != null ? `$${v}` : '-' },
    { title: '启用', dataIndex: 'enabled', key: 'enabled', render: (v: boolean) => <Tag color={v ? 'green' : 'default'}>{v ? '是' : '否'}</Tag> },
    { title: '操作', key: 'action', width: 150, render: (_: unknown, record: PriceRule) => (
      <Space>
        <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.ruleId)}>删除</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>价格规则</Title>
          <Space>
            <Select placeholder="规则类型" allowClear style={{ width: 140 }}
              options={Object.entries(ruleTypeMap).map(([k, v]) => ({ value: k, label: v.text }))}
              onChange={(v) => setParams({ ...params, ruleType: v })} />
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新增规则</Button>
          </Space>
        </div>
        <Table
          rowKey="ruleId"
          dataSource={data?.list || []}
          columns={columns}
          pagination={{
            current: params.page,
            pageSize: params.size,
            total: data?.total || 0,
            onChange: (page, size) => setParams({ ...params, page, size }),
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>
      <Modal
        title={editingRule ? '编辑规则' : '新增规则'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        width={560}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="ruleName" label="规则名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="ruleType" label="规则类型" rules={[{ required: true }]}>
            <Select options={Object.entries(ruleTypeMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
          <Form.Item name="targetPlatform" label="目标平台">
            <Select allowClear options={[
              { value: 'AMAZON', label: 'Amazon' },
              { value: 'SHOPIFY', label: 'Shopify' },
              { value: 'EBAY', label: 'eBay' },
            ]} />
          </Form.Item>
          <Form.Item name="adjustmentValue" label="调价幅度">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="minPrice" label="最低价">
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="maxPrice" label="最高价">
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
