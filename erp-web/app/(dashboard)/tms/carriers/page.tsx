'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Switch } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { tmsApi } from '@/lib/api';
import type { Carrier } from '@/types';

const { Title } = Typography;

export default function CarriersPage() {
  const [carriers, setCarriers] = useState<Carrier[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Carrier | null>(null);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await tmsApi.listCarriers();
      setCarriers(data || []);
    } catch {
      setCarriers([]);
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
      await tmsApi.createCarrier(values);
      message.success(editing ? '物流商更新成功' : '物流商创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '编码', dataIndex: 'code', key: 'code' },
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '状态', dataIndex: 'enabled', key: 'enabled', render: (v: boolean) => <Tag color={v ? 'green' : 'red'}>{v ? '启用' : '禁用'}</Tag> },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: Carrier) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalOpen(true); }}>编辑</Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>物流商管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建物流商</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="carrierId" columns={columns} dataSource={carriers} loading={loading} size="middle" />
      </Card>
      <Modal title={editing ? '编辑物流商' : '新建物流商'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="编码" rules={[{ required: true }]}>
            <Input placeholder="如: DHL, FedEx, UPS" />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
