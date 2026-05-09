'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, InputNumber } from 'antd';
import { PlusOutlined, SyncOutlined, EditOutlined, SearchOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { somApi } from '@/lib/api';
import type { Listing, PageParams } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  ACTIVE: { color: 'green', text: '在售' },
  INACTIVE: { color: 'default', text: '停售' },
  DRAFT: { color: 'orange', text: '草稿' },
  SUSPENDED: { color: 'red', text: '下架' },
};

export default function ListingsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingListing, setEditingListing] = useState<Listing | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<Listing>('/som/api/in/v1/listings', params);

  const handleSync = async () => {
    try {
      const storeId = params.storeId as string;
      if (!storeId) { message.warning('请先选择店铺'); return; }
      await somApi.syncListings(storeId);
      message.success('同步任务已发起');
      mutate();
    } catch { message.error('同步失败'); }
  };

  const handleEdit = (record: Listing) => {
    setEditingListing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingListing) {
        await somApi.updateListing(editingListing.listingId, values);
        message.success('Listing更新成功');
      }
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const columns = [
    { title: 'SKU', dataIndex: 'sku', key: 'sku', width: 140 },
    { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true, width: 280, render: (v: string, r: Listing) => (
      <a href={`/pdm/products?keyword=${r.sellerSku}`}>{v}</a>
    )},
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: 'ASIN', dataIndex: 'asin', key: 'asin', width: 120 },
    { title: '价格', dataIndex: 'price', key: 'price', render: (v: number) => v != null ? `$${v.toFixed(2)}` : '-' },
    { title: '库存', dataIndex: 'quantity', key: 'quantity', render: (v: number) => v ?? '-' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: '操作', key: 'action', width: 120, render: (_: unknown, record: Listing) => (
      <Space>
        <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/som/monitors?listingId=${record.listingId}`;
        }}>监控</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>Listing管理</Title>
          <Space>
            <Input placeholder="搜索SKU/ASIN" prefix={<SearchOutlined />} allowClear
              style={{ width: 200 }} onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
            <Select placeholder="店铺" allowClear style={{ width: 160 }}
              onChange={(v) => setParams({ ...params, storeId: v })} />
            <Select placeholder="状态" allowClear style={{ width: 120 }}
              options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))}
              onChange={(v) => setParams({ ...params, status: v })} />
            <Button icon={<SyncOutlined />} onClick={handleSync}>同步Listing</Button>
          </Space>
        </div>
        <Table
          rowKey="listingId"
          dataSource={data?.list || []}
          columns={columns}
          scroll={{ x: 1200 }}
          pagination={{
            current: params.page,
            pageSize: params.size,
            total: data?.total || 0,
            onChange: (page, size) => setParams({ ...params, page, size }),
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>
      <Modal
        title="编辑Listing"
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="标题">
            <Input />
          </Form.Item>
          <Form.Item name="price" label="价格">
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="quantity" label="库存数量">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
