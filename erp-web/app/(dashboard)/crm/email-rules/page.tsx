'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { crmApi } from '@/lib/api';
import type { EmailRule } from '@/types';

const { Title } = Typography;

export default function EmailRulesPage() {
  const [rules, setRules] = useState<EmailRule[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<EmailRule | null>(null);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await crmApi.listEmailRules();
      setRules(data || []);
    } catch {
      setRules([]);
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

  const handleEdit = (record: EmailRule) => {
    setEditing(record);
    form.setFieldsValue({ conditions: JSON.stringify(record.conditions), assignTo: record.assignTo, priority: record.priority, status: record.status });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const ruleData = { ...values, conditions: JSON.parse(values.conditions) };
      if (editing) {
        await crmApi.updateEmailRule(editing.ruleId, ruleData);
        message.success('规则更新成功');
      } else {
        await crmApi.createEmailRule(ruleData);
        message.success('规则创建成功');
      }
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await crmApi.deleteEmailRule(id);
      message.success('规则已删除');
      fetchData();
    } catch {
      message.error('删除失败');
    }
  };

  const columns = [
    { title: '分配给', dataIndex: 'assignTo', key: 'assignTo' },
    { title: '优先级', dataIndex: 'priority', key: 'priority', render: (v: number) => <Tag color={v <= 3 ? 'red' : v <= 6 ? 'orange' : 'blue'}>P{v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'ACTIVE' ? 'green' : 'default'}>{v}</Tag> },
    { title: '条件', dataIndex: 'conditions', key: 'conditions', render: (v: Record<string, unknown>) => <Tag>{JSON.stringify(v).substring(0, 50)}...</Tag> },
    { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: EmailRule) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Popconfirm title="确认删除?" onConfirm={() => handleDelete(record.ruleId)}>
            <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>邮件规则</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建规则</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="ruleId" columns={columns} dataSource={rules} loading={loading} size="middle" />
      </Card>
      <Modal title={editing ? '编辑邮件规则' : '新建邮件规则'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="conditions" label="匹配条件(JSON)" rules={[{ required: true }]}
            extra='例如: {"subject.contains":"refund","from.domain":"gmail.com"}'>
            <Input.TextArea rows={4} placeholder='{"subject.contains":"refund"}' />
          </Form.Item>
          <Form.Item name="assignTo" label="分配给" rules={[{ required: true }]}>
            <Input placeholder="客服人员ID或邮箱" />
          </Form.Item>
          <Form.Item name="priority" label="优先级" rules={[{ required: true }]} initialValue={5}>
            <Select options={Array.from({ length: 10 }, (_, i) => ({ value: i + 1, label: `P${i + 1}` }))} />
          </Form.Item>
          <Form.Item name="status" label="状态" initialValue="ACTIVE">
            <Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
