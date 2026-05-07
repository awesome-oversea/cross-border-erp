'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Tag, message, Card, Typography, Switch } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { tmsApi } from '@/lib/api';
import type { Carrier, PageParams } from '@/types';

const { Title } = Typography;

export default function CarriersPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Carrier | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<Carrier>('/tms/api/in/v1/carriers', params);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editing) { await tmsApi.updateCarrier(editing.carrierId, values); }
      else { await tmsApi.createCarrier(values); }
      message.success(editing ? '物流商更新成功' : '物流商创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const columns = [
    { title: '编码', dataIndex: 'code', key: 'code' },
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '状态', dataIndex: 'enabled', key: 'enabled', render: (v: boolean) => <Tag color={v ? 'green' : 'red'}>{v ? '启用' : '禁用'}</Tag> },
    { title: '操作', key: 'action', render: (_: unknown, record: Carrier) => (
      <Button type="link" icon={<EditOutlined />} onClick={() => { setEditing(record); form.setFieldsValue(record); setModalOpen(true); }}>编辑</Button>
    )},
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>物流商管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditing(null); form.resetFields(); setModalOpen(true); }}>新建物流商</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="carrierId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title={editing ? '编辑物流商' : '新建物流商'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="编码" rules={[{ required: true }]}><Input placeholder="如: DHL, FedEx, UPS" /></Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}><Switch /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
