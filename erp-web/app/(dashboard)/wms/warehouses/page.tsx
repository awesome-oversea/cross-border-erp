'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Switch } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { wmsApi } from '@/lib/api';
import type { Warehouse } from '@/types';

const { Title } = Typography;

export default function WarehousesPage() {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Warehouse | null>(null);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await wmsApi.listWarehouses();
      setWarehouses(data || []);
    } catch {
      setWarehouses([]);
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

  const handleEdit = (record: Warehouse) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await wmsApi.createWarehouse(values);
      message.success(editing ? '仓库更新成功' : '仓库创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '仓库名称', dataIndex: 'name', key: 'name' },
    { title: '仓库编码', dataIndex: 'code', key: 'code' },
    { title: '类型', dataIndex: 'type', key: 'type', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
    { title: '状态', dataIndex: 'enabled', key: 'enabled', render: (v: boolean) => <Tag color={v ? 'green' : 'red'}>{v ? '启用' : '禁用'}</Tag> },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: Warehouse) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>仓库管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建仓库</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="warehouseId" columns={columns} dataSource={warehouses} loading={loading} size="middle" />
      </Card>
      <Modal title={editing ? '编辑仓库' : '新建仓库'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="仓库名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="code" label="仓库编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="仓库类型" rules={[{ required: true }]}>
            <Select options={[{ value: 'SELF', label: '自建仓' }, { value: 'THIRD_PARTY', label: '第三方仓' }, { value: 'FBA', label: 'FBA仓' }, { value: 'OVERSEAS', label: '海外仓' }]} />
          </Form.Item>
          <Form.Item name="address" label="地址">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
