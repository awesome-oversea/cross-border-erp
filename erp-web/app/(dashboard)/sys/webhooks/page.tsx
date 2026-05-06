'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, EditOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { sysApi } from '@/lib/api';
import type { WebhookEndpoint } from '@/types';

const { Title } = Typography;

export default function WebhooksPage() {
  const [webhooks, setWebhooks] = useState<WebhookEndpoint[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await sysApi.listWebhookEndpoints();
      setWebhooks(data || []);
    } catch {
      setWebhooks([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await sysApi.createWebhookEndpoint(values);
      message.success('Webhook创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: 'URL', dataIndex: 'url', key: 'url', ellipsis: true, render: (v: string) => <code style={{ fontSize: 12 }}>{v}</code> },
    { title: '事件类型', dataIndex: 'eventType', key: 'eventType', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={v === 'ACTIVE' ? 'green' : 'default'} text={v} /> },
    { title: '重试次数', dataIndex: 'retryCount', key: 'retryCount' },
    { title: '超时(秒)', dataIndex: 'timeoutSeconds', key: 'timeoutSeconds' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>Webhook管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建Webhook</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="endpointId" columns={columns} dataSource={webhooks} loading={loading} size="middle" />
      </Card>
      <Modal title="新建Webhook" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="url" label="回调URL" rules={[{ required: true }, { type: 'url', message: '请输入有效URL' }]}>
            <Input placeholder="https://your-server.com/webhook" />
          </Form.Item>
          <Form.Item name="eventType" label="事件类型" rules={[{ required: true }]}>
            <Select mode="multiple" options={[
              { value: 'ORDER_CREATED', label: '订单创建' },
              { value: 'ORDER_SHIPPED', label: '订单发货' },
              { value: 'INVENTORY_LOW', label: '库存预警' },
              { value: 'SHIPMENT_RECEIVED', label: '货件接收' },
              { value: 'PAYMENT_RECEIVED', label: '收款通知' },
            ]} />
          </Form.Item>
          <Form.Item name="retryCount" label="重试次数" initialValue={3}>
            <Select options={[{ value: 0, label: '不重试' }, { value: 3, label: '3次' }, { value: 5, label: '5次' }]} />
          </Form.Item>
          <Form.Item name="timeoutSeconds" label="超时时间(秒)" initialValue={30}>
            <Select options={[{ value: 10, label: '10秒' }, { value: 30, label: '30秒' }, { value: 60, label: '60秒' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
