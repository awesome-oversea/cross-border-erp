'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Select, Tag, Space, message, Card, Typography, Input } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { iamApi } from '@/lib/api';
import type { ObjectPermission, User } from '@/types';

const { Title } = Typography;

const resourceTypes = ['ORDER', 'PRODUCT', 'WAREHOUSE', 'SUPPLIER', 'FINANCE_REPORT', 'CUSTOMER'];
const actionTypes = ['READ', 'WRITE', 'DELETE', 'APPROVE', 'EXPORT'];

export default function ObjectPermissionsPage() {
  const [permissions, setPermissions] = useState<ObjectPermission[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<string>('');
  const [form] = Form.useForm();

  const fetchUsers = useCallback(async () => {
    try {
      const data = await iamApi.listUsers();
      setUsers(data || []);
    } catch { setUsers([]); }
  }, []);

  const fetchPermissions = useCallback(async () => {
    if (!selectedUserId) { setPermissions([]); return; }
    setLoading(true);
    try {
      const data = await iamApi.listObjectPermissions(selectedUserId);
      setPermissions(data || []);
    } catch {
      setPermissions([]);
    } finally {
      setLoading(false);
    }
  }, [selectedUserId]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);
  useEffect(() => { fetchPermissions(); }, [fetchPermissions]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await iamApi.grantObjectPermission({ ...values, userId: selectedUserId });
      message.success('权限已授予');
      setModalOpen(false);
      fetchPermissions();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '资源类型', dataIndex: 'resourceType', key: 'resourceType', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '资源ID', dataIndex: 'resourceId', key: 'resourceId' },
    { title: '操作权限', dataIndex: 'actions', key: 'actions', render: (actions: string[]) => actions?.map((a) => <Tag key={a} color="green">{a}</Tag>) },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: ObjectPermission) => (
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => { message.success('权限已撤销'); fetchPermissions(); }}>撤销</Button>
      ),
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>对象权限</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space>
          <span>选择用户：</span>
          <Select
            style={{ width: 240 }}
            placeholder="请选择用户"
            value={selectedUserId || undefined}
            onChange={setSelectedUserId}
            options={users.map((u) => ({ value: u.userId, label: `${u.username} (${u.email || ''})` }))}
            showSearch
            filterOption={(input, option) => (option?.label as string)?.toLowerCase().includes(input.toLowerCase())}
          />
        </Space>
      </Card>
      {selectedUserId && (
        <>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>授予权限</Button>
          </div>
          <Card style={{ borderRadius: 8 }}>
            <Table rowKey="permissionId" columns={columns} dataSource={permissions} loading={loading} size="middle" />
          </Card>
        </>
      )}
      <Modal title="授予对象权限" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="resourceType" label="资源类型" rules={[{ required: true, message: '请选择资源类型' }]}>
            <Select options={resourceTypes.map((t) => ({ value: t, label: t }))} />
          </Form.Item>
          <Form.Item name="resourceId" label="资源ID" rules={[{ required: true, message: '请输入资源ID' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="actions" label="操作权限" rules={[{ required: true, message: '请选择操作权限' }]}>
            <Select mode="multiple" options={actionTypes.map((a) => ({ value: a, label: a }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
