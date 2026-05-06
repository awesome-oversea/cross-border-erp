'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { crmApi } from '@/lib/api';
import type { Customer } from '@/types';

const { Title } = Typography;

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Customer | null>(null);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await crmApi.listCustomers();
      setCustomers(data || []);
    } catch {
      setCustomers([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.createCustomer(values);
      message.success('客户创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '客户名称', dataIndex: 'name', key: 'name' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '电话', dataIndex: 'phone', key: 'phone' },
    { title: '国家', dataIndex: 'countryCode', key: 'countryCode', render: (v: string) => <Tag>{v}</Tag> },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '订单数', dataIndex: 'totalOrders', key: 'totalOrders' },
    { title: '消费总额', dataIndex: 'totalSpent', key: 'totalSpent', render: (v: number) => `$${v?.toFixed(2)}` },
    { title: '标签', dataIndex: 'tags', key: 'tags', render: (tags: Customer['tags']) => tags?.map((t) => <Tag key={t.tagId} color="purple">{t.tagName}:{t.tagValue}</Tag>) },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>客户管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建客户</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="customerId" columns={columns} dataSource={customers} loading={loading} size="middle"
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 条` }} />
      </Card>
      <Modal title="新建客户" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="客户名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="电话">
            <Input />
          </Form.Item>
          <Form.Item name="countryCode" label="国家">
            <Select options={[{ value: 'US', label: '美国' }, { value: 'UK', label: '英国' }, { value: 'DE', label: '德国' }, { value: 'JP', label: '日本' }]} />
          </Form.Item>
          <Form.Item name="platform" label="来源平台">
            <Select options={[{ value: 'Amazon', label: 'Amazon' }, { value: 'Shopify', label: 'Shopify' }, { value: 'eBay', label: 'eBay' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
