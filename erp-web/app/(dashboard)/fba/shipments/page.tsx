'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { fbaApi } from '@/lib/api';
import type { ShipmentPlan } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' },
  SUBMITTED: { color: 'blue', text: '已提交' },
  IN_PRODUCTION: { color: 'orange', text: '生产中' },
  SHIPPED: { color: 'cyan', text: '已发货' },
  RECEIVED: { color: 'green', text: '已接收' },
  CANCELLED: { color: 'red', text: '已取消' },
};

export default function ShipmentsPage() {
  const [plans, setPlans] = useState<ShipmentPlan[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fbaApi.listShipmentPlans();
      setPlans(data || []);
    } catch {
      setPlans([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await fbaApi.createShipmentPlan(values);
      message.success('备货计划创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: '目的仓库', dataIndex: 'destinationFc', key: 'destinationFc', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '建议数量', dataIndex: 'suggestedQuantity', key: 'suggestedQuantity', render: (v: number) => <span style={{ fontWeight: 600 }}>{v}</span> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>备货计划</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建计划</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="planId" columns={columns} dataSource={plans} loading={loading} size="middle" />
      </Card>
      <Modal title="新建备货计划" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="sellerSku" label="Seller SKU" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="destinationFc" label="目的仓库" rules={[{ required: true }]}>
            <Select options={[{ value: 'US-SEA1', label: '美国西雅图仓' }, { value: 'US-LAX1', label: '美国洛杉矶仓' }, { value: 'EU-FRA1', label: '德国法兰克福仓' }, { value: 'JP-TYO1', label: '日本东京仓' }]} />
          </Form.Item>
          <Form.Item name="suggestedQuantity" label="建议数量" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
