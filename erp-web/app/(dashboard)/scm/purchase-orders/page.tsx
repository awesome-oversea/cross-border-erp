'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, InputNumber, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, CheckOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { scmApi } from '@/lib/api';
import type { PurchaseOrder, PageParams } from '@/types';

const { Title } = Typography;

const poStatusMap: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' }, PENDING: { color: 'orange', text: '待审批' },
  APPROVED: { color: 'blue', text: '已审批' }, ORDERED: { color: 'cyan', text: '已下单' },
  RECEIVED: { color: 'green', text: '已收货' }, CANCELLED: { color: 'red', text: '已取消' },
};

export default function PurchaseOrdersPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<PurchaseOrder>('/scm/api/in/v1/purchase-orders', params);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await scmApi.createPurchaseOrder(values);
      message.success('采购单创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const handleApprove = async (id: string) => {
    try { await scmApi.approvePurchaseOrder(id); message.success('已审批'); mutate(); } catch { message.error('审批失败'); }
  };

  const columns = [
    { title: '采购单号', dataIndex: 'poNo', key: 'poNo' },
    { title: '供应商ID', dataIndex: 'supplierId', key: 'supplierId', render: (v: string) => <a href={`/scm/suppliers?supplierId=${v}`}>{v}</a> },
    { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (v: number, r: PurchaseOrder) => `${r.currency} ${v?.toFixed(2)}` },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={poStatusMap[v]?.color || 'default'} text={poStatusMap[v]?.text || v} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    { title: '操作', key: 'action', render: (_: unknown, r: PurchaseOrder) => r.status === 'PENDING' ? (
      <Button size="small" type="primary" icon={<CheckOutlined />} onClick={() => handleApprove(r.poId)}>审批</Button>
    ) : null },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>采购单管理</Title>
        <Space>
          <Select placeholder="状态" allowClear style={{ width: 120 }}
            options={Object.entries(poStatusMap).map(([k, v]) => ({ value: k, label: v.text }))}
            onChange={(v) => setParams({ ...params, status: v })} />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建采购单</Button>
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="poId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title="新建采购单" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="supplierId" label="供应商" rules={[{ required: true }]}>
            <Select options={[{ value: 'sup-001', label: '深圳电子供应商' }, { value: 'sup-002', label: '义乌百货供应商' }]} />
          </Form.Item>
          <Form.Item name="totalAmount" label="总金额" rules={[{ required: true }]}><InputNumber min={0} precision={2} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="currency" label="币种" initialValue="CNY">
            <Select options={[{ value: 'CNY', label: 'CNY' }, { value: 'USD', label: 'USD' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
