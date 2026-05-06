'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, CheckOutlined } from '@ant-design/icons';
import { crmApi } from '@/lib/api';
import type { ServiceTicket } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  OPEN: { color: 'orange', text: '待处理' },
  IN_PROGRESS: { color: 'blue', text: '处理中' },
  RESOLVED: { color: 'green', text: '已解决' },
  CLOSED: { color: 'default', text: '已关闭' },
};

export default function TicketsPage() {
  const [tickets, setTickets] = useState<ServiceTicket[]>([]);
  const [loading, setLoading] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [resolveModalOpen, setResolveModalOpen] = useState(false);
  const [currentTicket, setCurrentTicket] = useState<ServiceTicket | null>(null);
  const [form] = Form.useForm();
  const [resolveForm] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await crmApi.listTickets();
      setTickets(data || []);
    } catch {
      setTickets([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.createTicket(values);
      message.success('工单创建成功');
      setCreateModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const handleResolve = async () => {
    try {
      const values = await resolveForm.validateFields();
      if (!currentTicket) return;
      await crmApi.resolveTicket(currentTicket.ticketId, { resolution: values.resolution });
      message.success('工单已解决');
      setResolveModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '主题', dataIndex: 'subject', key: 'subject', ellipsis: true },
    { title: '客户ID', dataIndex: 'customerId', key: 'customerId' },
    { title: '负责人', dataIndex: 'assignee', key: 'assignee', render: (v: string) => v || <Tag>未分配</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: ServiceTicket) => (
        <Space>
          {record.status !== 'RESOLVED' && record.status !== 'CLOSED' && (
            <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => { setCurrentTicket(record); resolveForm.resetFields(); setResolveModalOpen(true); }}>解决</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>工单管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setCreateModalOpen(true); }}>新建工单</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="ticketId" columns={columns} dataSource={tickets} loading={loading} size="middle" />
      </Card>
      <Modal title="新建工单" open={createModalOpen} onOk={handleCreate} onCancel={() => setCreateModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="customerId" label="客户ID" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="subject" label="主题" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="解决工单" open={resolveModalOpen} onOk={handleResolve} onCancel={() => setResolveModalOpen(false)} width={480}>
        <Form form={resolveForm} layout="vertical">
          <Form.Item name="resolution" label="解决方案" rules={[{ required: true, message: '请输入解决方案' }]}>
            <Input.TextArea rows={4} placeholder="请输入解决方案" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
