'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Descriptions, Badge } from 'antd';
import { PlusOutlined, EditOutlined, EyeOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { pdmApi } from '@/lib/api';
import type { Product, Sku, PageParams } from '@/types';

const { Title } = Typography;

export default function ProductsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [editing, setEditing] = useState<Product | null>(null);
  const [currentProduct, setCurrentProduct] = useState<Product | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<Product>('/pdm/api/in/v1/products', params);

  const handleCreate = () => { setEditing(null); form.resetFields(); setModalOpen(true); };
  const handleEdit = (record: Product) => { setEditing(record); form.setFieldsValue(record); setModalOpen(true); };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editing) { await pdmApi.updateProduct(editing.productId, values); message.success('产品更新成功'); }
      else { await pdmApi.createProduct(values); message.success('产品创建成功'); }
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const showDetail = async (productId: string) => {
    try { const d = await pdmApi.getProduct(productId); setCurrentProduct(d); setDetailOpen(true); } catch { message.error('获取详情失败'); }
  };

  const productColumns = [
    { title: 'SPU编码', dataIndex: 'spuCode', key: 'spuCode' },
    { title: '产品名称', dataIndex: 'productName', key: 'productName' },
    { title: '分类', dataIndex: 'category', key: 'category', render: (v: string) => <Tag>{v}</Tag> },
    { title: '品牌', dataIndex: 'brand', key: 'brand' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={v === 'ACTIVE' ? 'green' : 'default'} text={v} /> },
    { title: 'SKU数', key: 'skuCount', render: (_: unknown, r: Product) => r.skus?.length || 0 },
    { title: '库存', key: 'inventory', render: (_: unknown, r: Product) => <a href={`/wms/inventory?keyword=${r.spuCode}`}>查看</a> },
    { title: '操作', key: 'action', render: (_: unknown, record: Product) => (
      <Space>
        <Button type="link" icon={<EyeOutlined />} onClick={() => showDetail(record.productId)}>详情</Button>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
      </Space>
    )},
  ];

  const skuColumns = [
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: '条码', dataIndex: 'barcode', key: 'barcode' },
    { title: '重量(kg)', dataIndex: 'weight', key: 'weight' },
    { title: '成本价', dataIndex: 'costPrice', key: 'costPrice', render: (v: number) => v?.toFixed(2) },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={v === 'ACTIVE' ? 'green' : 'default'} text={v} /> },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>产品管理</Title>
        <Space>
          <Input placeholder="搜索SPU" allowClear style={{ width: 200 }}
            onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>新建产品</Button>
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="productId" columns={productColumns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title={editing ? '编辑产品' : '新建产品'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="spuCode" label="SPU编码" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="productName" label="产品名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="category" label="分类">
            <Select options={[{ value: 'Electronics', label: '电子' }, { value: 'Clothing', label: '服装' }, { value: 'Home', label: '家居' }, { value: 'Sports', label: '运动' }]} />
          </Form.Item>
          <Form.Item name="brand" label="品牌"><Input /></Form.Item>
          <Form.Item name="status" label="状态" initialValue="ACTIVE">
            <Select options={[{ value: 'ACTIVE', label: '启用' }, { value: 'DRAFT', label: '草稿' }, { value: 'DISCONTINUED', label: '停售' }]} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="产品详情" open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={800}>
        {currentProduct && (
          <div>
            <Descriptions bordered column={2} size="small" style={{ marginBottom: 16 }}>
              <Descriptions.Item label="SPU编码">{currentProduct.spuCode}</Descriptions.Item>
              <Descriptions.Item label="产品名称">{currentProduct.productName}</Descriptions.Item>
              <Descriptions.Item label="分类"><Tag>{currentProduct.category}</Tag></Descriptions.Item>
              <Descriptions.Item label="品牌">{currentProduct.brand}</Descriptions.Item>
              <Descriptions.Item label="状态"><Badge color={currentProduct.status === 'ACTIVE' ? 'green' : 'default'} text={currentProduct.status} /></Descriptions.Item>
            </Descriptions>
            <Title level={5}>SKU列表</Title>
            <Table rowKey="skuId" columns={skuColumns} dataSource={currentProduct.skus || []} pagination={false} size="small" />
          </div>
        )}
      </Modal>
    </div>
  );
}
