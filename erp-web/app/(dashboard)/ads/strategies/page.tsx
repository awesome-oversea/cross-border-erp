'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, InputNumber, Switch } from 'antd';
import { PlusOutlined, EditOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { adsApi } from '@/lib/api';
import type { AdStrategy, PageParams } from '@/types';

const { Title } = Typography;

const strategyTypeMap: Record<string, string> = {
  AUTO_BID: '自动出价',
  DAY_PARTING: '分时段投放',
  PLACEMENT: '投放位置优化',
  KEYWORD_HARVEST: '关键词收割',
  BUDGET_PACER: '预算平滑',
};

const statusMap: Record<string, { color: string; text: string }> = {
  ACTIVE: { color: 'green', text: '启用' },
  PAUSED: { color: 'orange', text: '暂停' },
  DRAFT: { color: 'blue', text: '草稿' },
};

export default function StrategiesPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<AdStrategy>('/ads/api/in/v1/strategies', params);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await adsApi.createStrategy(values);
      message.success('策略创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('创建失败'); }
  };

  const columns = [
    { title: '策略名称', dataIndex: 'strategyName', key: 'strategyName' },
    { title: '类型', dataIndex: 'strategyType', key: 'strategyType', render: (v: string) => (
      <Tag color="blue">{strategyTypeMap[v] || v}</Tag>
    )},
    { title: '目标指标', dataIndex: 'targetMetric', key: 'targetMetric', render: (v: string) => v || '-' },
    { title: '目标值', dataIndex: 'targetValue', key: 'targetValue', render: (v: number) => v != null ? v : '-' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: '关联活动数', dataIndex: 'campaignCount', key: 'campaignCount', render: (v: number) => v ?? 0 },
    { title: '操作', key: 'action', width: 100, render: (_: unknown, record: AdStrategy) => (
      <Space>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/ads/campaigns?strategyId=${record.strategyId}`;
        }}>关联活动</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>广告策略</Title>
          <Space>
            <Select placeholder="策略类型" allowClear style={{ width: 160 }}
              options={Object.entries(strategyTypeMap).map(([k, v]) => ({ value: k, label: v }))}
              onChange={(v) => setParams({ ...params, strategyType: v })} />
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>创建策略</Button>
          </Space>
        </div>
        <Table
          rowKey="strategyId"
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
      <Modal title="创建广告策略" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="strategyName" label="策略名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="strategyType" label="策略类型" rules={[{ required: true }]}>
            <Select options={Object.entries(strategyTypeMap).map(([k, v]) => ({ value: k, label: v }))} />
          </Form.Item>
          <Form.Item name="targetMetric" label="目标指标">
            <Select options={[
              { value: 'ACOS', label: 'ACoS' },
              { value: 'ROAS', label: 'ROAS' },
              { value: 'CTR', label: 'CTR' },
              { value: 'CPC', label: 'CPC' },
              { value: 'IMPRESSIONS', label: '曝光量' },
            ]} />
          </Form.Item>
          <Form.Item name="targetValue" label="目标值">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
