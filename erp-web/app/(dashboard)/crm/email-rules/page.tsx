'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge, Switch } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { crmApi } from '@/lib/api';
import type { EmailRule, PageParams } from '@/types';

const { Title } = Typography;

const triggerMap: Record<string, string> = {
  ORDER_CREATED: '订单创建', ORDER_SHIPPED: '订单发货', ORDER_DELIVERED: '订单签收',
  TICKET_CREATED: '工单创建', TICKET_RESOLVED: '工单解决', PAYMENT_RECEIVED: '付款到账',
};

export default function EmailRulesPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<EmailRule | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<EmailRule>('/crm/api/in/v1/email-rules', params);

  const handleCreate = () => { setEditing(null); form.resetFields(); setModalOpen(true); };
  const handleEdit = (record: EmailRule) => { setEditing(record); form.setFieldsValue(record); setModalOpen(true); };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editing) { await crmApi.updateEmailRule(editing.ruleId, values); }
      else { await crmApi.createEmailRule(values); }
      message.success(editing ? '规则更新成功' : '规则创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const columns = [
    { title: '规则名称', dataIndex: 'name', key: 'name' },
    { title: '触发条件', dataIndex: 'trigger', key: 'trigger', render: (v: string) => <Tag color="blue">{triggerMap[v] || v}</Tag> },
    { title: '模板', dataIndex: 'templateId', key: 'templateId' },
    { title: '启用', dataIndex: 'enabled', key: 'enabled', render: (v: boolean) => <Badge color={v ? 'green' : 'default'} text={v ? '是' : '否'} /> },
    { title: '操作', key: 'action', render: (_: unknown, record: EmailRule) => <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button> },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>邮件规则</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建规则</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="ruleId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title={editing ? '编辑规则' : '新建规则'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="规则名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="trigger" label="触发条件" rules={[{ required: true }]}>
            <Select options={Object.entries(triggerMap).map(([k, v]) => ({ value: k, label: v }))} />
          </Form.Item>
          <Form.Item name="templateId" label="邮件模板" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}><Switch /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
