'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography } from 'antd';
import { PlusOutlined, SyncOutlined, EditOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { somApi } from '@/lib/api';
import type { Store, PageParams } from '@/types';

const { Title } = Typography;

const platformOptions = [
  { value: 'AMAZON', label: 'Amazon' },
  { value: 'SHOPIFY', label: 'Shopify' },
  { value: 'EBAY', label: 'eBay' },
  { value: 'LAZADA', label: 'Lazada' },
  { value: 'SHOPEE', label: 'Shopee' },
  { value: 'TEMU', label: 'Temu' },
  { value: 'TIKTOK', label: 'TikTok Shop' },
];

const statusMap: Record<string, { color: string; text: string }> = {
  ACTIVE: { color: 'green', text: '运营中' },
  INACTIVE: { color: 'default', text: '已停用' },
  SUSPENDED: { color: 'red', text: '已冻结' },
};

export default function StoresPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingStore, setEditingStore] = useState<Store | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<Store>('/som/api/in/v1/stores', params);

  const handleCreate = () => {
    setEditingStore(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleEdit = (record: Store) => {
    setEditingStore(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingStore) {
        await somApi.updateStore(editingStore.storeId, values);
        message.success('店铺更新成功');
      } else {
        await somApi.createStore(values);
        message.success('店铺创建成功');
      }
      setModalOpen(false);
      mutate();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '店铺名称', dataIndex: 'storeName', key: 'storeName', render: (v: string, r: Store) => (
      <a href={`/som/listings?storeId=${r.storeId}`}>{v}</a>
    )},
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => (
      <Tag color="blue">{v}</Tag>
    )},
    { title: '店铺ID', dataIndex: 'platformStoreId', key: 'platformStoreId' },
    { title: '站点', dataIndex: 'marketplace', key: 'marketplace' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: '货币', dataIndex: 'currency', key: 'currency' },
    { title: '操作', key: 'action', render: (_: unknown, record: Store) => (
      <Space>
        <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/som/listings?storeId=${record.storeId}`;
        }}>Listing</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>店铺管理</Title>
          <Space>
            <Select placeholder="平台筛选" allowClear style={{ width: 140 }}
              options={platformOptions} onChange={(v) => setParams({ ...params, platform: v })} />
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新增店铺</Button>
          </Space>
        </div>
        <Table
          rowKey="storeId"
          dataSource={data?.list || []}
          columns={columns}
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
        title={editingStore ? '编辑店铺' : '新增店铺'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        width={560}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="storeName" label="店铺名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
            <Select options={platformOptions} />
          </Form.Item>
          <Form.Item name="platformStoreId" label="平台店铺ID" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="marketplace" label="站点">
            <Input placeholder="如: US, UK, DE, JP" />
          </Form.Item>
          <Form.Item name="currency" label="货币">
            <Select options={[
              { value: 'USD', label: 'USD' },
              { value: 'EUR', label: 'EUR' },
              { value: 'GBP', label: 'GBP' },
              { value: 'JPY', label: 'JPY' },
              { value: 'CNY', label: 'CNY' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
