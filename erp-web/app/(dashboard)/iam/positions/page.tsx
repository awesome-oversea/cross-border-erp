'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Tag, Space, message, Card, Typography } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { iamApi } from '@/lib/api';
import type { Position } from '@/types';

const { Title } = Typography;

export default function PositionsPage() {
  const [positions, setPositions] = useState<Position[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Position | null>(null);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await iamApi.listPositions();
      setPositions(data || []);
    } catch {
      setPositions([]);
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

  const handleEdit = (record: Position) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await iamApi.createPosition(values);
      message.success(editing ? '岗位更新成功' : '岗位创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '岗位名称', dataIndex: 'name', key: 'name' },
    { title: '部门', dataIndex: 'departmentId', key: 'departmentId', render: (v: string) => <Tag>{v}</Tag> },
    { title: '级别', dataIndex: 'level', key: 'level', render: (v: number) => <Tag color="geekblue">L{v}</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleDateString() : '-' },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: Position) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>岗位管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建岗位</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="positionId" columns={columns} dataSource={positions} loading={loading} size="middle" />
      </Card>
      <Modal title={editing ? '编辑岗位' : '新建岗位'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="岗位名称" rules={[{ required: true, message: '请输入岗位名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="departmentId" label="所属部门" rules={[{ required: true, message: '请选择部门' }]}>
            <Select options={[{ value: 'dept-001', label: '运营部' }, { value: 'dept-002', label: '采购部' }, { value: 'dept-003', label: '仓储部' }, { value: 'dept-004', label: '财务部' }]} />
          </Form.Item>
          <Form.Item name="level" label="岗位级别" rules={[{ required: true, message: '请输入级别' }]}>
            <InputNumber min={1} max={10} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
