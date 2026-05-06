'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Tag, Space, message, Card, Typography, Badge, Progress } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { biApi } from '@/lib/api';
import type { KpiMetric } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  ON_TRACK: { color: 'green', text: '达标' },
  AT_RISK: { color: 'orange', text: '风险' },
  OFF_TRACK: { color: 'red', text: '偏离' },
  NOT_STARTED: { color: 'default', text: '未开始' },
};

export default function KpisPage() {
  const [kpis, setKpis] = useState<KpiMetric[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await biApi.listKpis();
      setKpis(data || []);
    } catch {
      setKpis([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await biApi.recordKpi(values);
      message.success('KPI指标已录入');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '指标编码', dataIndex: 'kpiCode', key: 'kpiCode' },
    { title: '指标名称', dataIndex: 'kpiName', key: 'kpiName' },
    { title: '分类', dataIndex: 'category', key: 'category', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '当前值', dataIndex: 'value', key: 'value', render: (v: number, r: KpiMetric) => `${v} ${r.unit}` },
    { title: '目标值', dataIndex: 'targetValue', key: 'targetValue', render: (v: number, r: KpiMetric) => `${v} ${r.unit}` },
    {
      title: '达成率', key: 'achievement',
      render: (_: unknown, r: KpiMetric) => {
        const pct = r.targetValue > 0 ? Math.round((r.value / r.targetValue) * 100) : 0;
        return <Progress percent={pct} size="small" status={pct >= 100 ? 'success' : pct >= 70 ? 'normal' : 'exception'} />;
      },
    },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '测量时间', dataIndex: 'measuredAt', key: 'measuredAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>KPI指标</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>录入指标</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="kpiId" columns={columns} dataSource={kpis} loading={loading} size="middle" />
      </Card>
      <Modal title="录入KPI指标" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="kpiCode" label="指标编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="kpiName" label="指标名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="category" label="分类" rules={[{ required: true }]}>
            <Select options={[{ value: 'FINANCE', label: '财务' }, { value: 'SALES', label: '销售' }, { value: 'OPERATION', label: '运营' }, { value: 'CUSTOMER', label: '客户' }]} />
          </Form.Item>
          <Form.Item name="value" label="当前值" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="targetValue" label="目标值" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="unit" label="单位" rules={[{ required: true }]}>
            <Input placeholder="如: USD, %, 单" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
