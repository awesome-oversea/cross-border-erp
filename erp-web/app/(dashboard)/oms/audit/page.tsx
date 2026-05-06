'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Card, Typography, Tag, Space, Button, Badge, Modal, Form, Input, Radio, message } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { omsApi } from '@/lib/api';
import type { Order } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待审核' },
  CONFIRMED: { color: 'blue', text: '已确认' },
  REJECTED: { color: 'red', text: '已驳回' },
};

export default function OrderAuditPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(false);
  const [auditModalOpen, setAuditModalOpen] = useState(false);
  const [currentOrder, setCurrentOrder] = useState<Order | null>(null);
  const [form] = Form.useForm();

  const fetchPendingOrders = useCallback(async () => {
    setLoading(true);
    try {
      const data = await omsApi.listOrders({ status: 'PENDING' });
      setOrders(data || []);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchPendingOrders(); }, [fetchPendingOrders]);

  const handleAudit = (order: Order) => {
    setCurrentOrder(order);
    form.resetFields();
    setAuditModalOpen(true);
  };

  const submitAudit = async () => {
    try {
      const values = await form.validateFields();
      if (!currentOrder) return;
      await omsApi.auditOrder(currentOrder.orderId, { action: values.action, reason: values.reason });
      message.success(values.action === 'APPROVE' ? '订单已审核通过' : '订单已驳回');
      setAuditModalOpen(false);
      fetchPendingOrders();
    } catch {
      message.error('审核操作失败');
    }
  };

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag>{v}</Tag> },
    { title: '客户', dataIndex: 'customerName', key: 'customerName' },
    { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (v: number, r: Order) => `${r.currency} ${v?.toFixed(2)}` },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '下单时间', dataIndex: 'orderDate', key: 'orderDate', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    {
      title: '操作', key: 'action', width: 200,
      render: (_: unknown, record: Order) => (
        <Space>
          <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => handleAudit(record)}>审核</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>订单审核</Title>
        <Badge count={orders.length} overflowCount={99}>
          <Tag color="orange">待审核订单</Tag>
        </Badge>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="orderId" columns={columns} dataSource={orders} loading={loading} size="middle" />
      </Card>
      <Modal title={`审核订单 - ${currentOrder?.orderNo || ''}`} open={auditModalOpen} onOk={submitAudit} onCancel={() => setAuditModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="action" label="审核结果" rules={[{ required: true, message: '请选择审核结果' }]}>
            <Radio.Group>
              <Radio.Button value="APPROVE"><CheckOutlined /> 通过</Radio.Button>
              <Radio.Button value="REJECT"><CloseOutlined /> 驳回</Radio.Button>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="reason" label="审核意见">
            <Input.TextArea rows={3} placeholder="请输入审核意见" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
