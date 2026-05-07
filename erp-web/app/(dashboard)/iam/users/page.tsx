'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Popconfirm, Card, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { iamApi } from '@/lib/api';
import type { User, PageParams } from '@/types';

const { Title } = Typography;

export default function UsersPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<User>('/iam/api/in/v1/users', params);

  const handleCreate = () => { setEditingUser(null); form.resetFields(); setModalOpen(true); };
  const handleEdit = (record: User) => { setEditingUser(record); form.setFieldsValue(record); setModalOpen(true); };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingUser) { await iamApi.updateUser(editingUser.userId, values); message.success('用户更新成功'); }
      else { await iamApi.createUser(values); message.success('用户创建成功'); }
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const handleDelete = async (id: string) => {
    try { await iamApi.deleteUser(id); message.success('已删除'); mutate(); } catch { message.error('删除失败'); }
  };

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '手机', dataIndex: 'phone', key: 'phone' },
    { title: '部门', dataIndex: 'departmentId', key: 'departmentId' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'ACTIVE' ? 'green' : 'red'}>{v}</Tag> },
    { title: '角色', dataIndex: 'roles', key: 'roles', render: (roles: string[]) => roles?.map((r) => <Tag key={r} color="blue">{r}</Tag>) },
    { title: '操作', key: 'action', render: (_: unknown, record: User) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.userId)}>
          <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>
      </Space>
    )},
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>用户管理</Title>
        <Space>
          <Input placeholder="搜索用户" allowClear style={{ width: 200 }}
            onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建用户</Button>
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="userId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title={editingUser ? '编辑用户' : '新建用户'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="email" label="邮箱" rules={[{ type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="phone" label="手机号"><Input /></Form.Item>
          <Form.Item name="departmentId" label="部门">
            <Select placeholder="选择部门" options={[{ value: 'dept-001', label: '运营部' }, { value: 'dept-002', label: '采购部' }, { value: 'dept-003', label: '仓储部' }]} />
          </Form.Item>
          <Form.Item name="status" label="状态" initialValue="ACTIVE">
            <Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
