'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, EyeOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { crmApi } from '@/lib/api';
import type { Ticket, PageParams } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  OPEN: { color: 'orange', text: '待处理' }, IN_PROGRESS: { color: 'blue', text: '处理中' },
  RESOLVED: { color: 'green', text: '已解决' }, CLOSED: { color: 'default', text: '已关闭' },
};
const priorityMap: Record<string, { color: string; text: string }> = {
  URGENT: { color: 'red', text: '紧急' }, HIGH: { color: 'orange', text: '高' },
  MEDIUM: { color: 'blue', text: '中' }, LOW: { color: 'default', text: '低' },
};

export default function TicketsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<Ticket>('/crm/api/in/v1/tickets', params);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.createTicket(values);
      message.success('工单创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const columns = [
    { title: '工单号', dataIndex: 'ticketNo', key: 'ticketNo' },
    { title: '客户', dataIndex: 'customerId', key: 'customerId', render: (v: string) => <a href={`/crm/customers?customerId=${v}`}>{v}</a> },
    { title: '主题', dataIndex: 'subject', key: 'subject', ellipsis: true },
    { title: '优先级', dataIndex: 'priority', key: 'priority', render: (v: string) => <Badge color={priorityMap[v]?.color || 'default'} text={priorityMap[v]?.text || v} /> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>工单管理</Title>
        <Space>
          <Select placeholder="状态" allowClear style={{ width: 120 }}
            options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))}
            onChange={(v) => setParams({ ...params, status: v })} />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建工单</Button>
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="ticketId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title="新建工单" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="customerId" label="客户ID" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="subject" label="主题" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="description" label="描述"><Input.TextArea rows={4} /></Form.Item>
          <Form.Item name="priority" label="优先级" initialValue="MEDIUM">
            <Select options={Object.entries(priorityMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
