'use client';

import { Card, Typography, Table, Tag, Badge, Input, Space, Button, Select } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

const mockListings = [
  { key: '1', listingId: 'L-001', platform: 'Amazon', sellerSku: 'SKU-001', title: 'Wireless Bluetooth Earbuds', asin: 'B0XXXXX1', price: 29.99, status: 'ACTIVE', stock: 150 },
  { key: '2', listingId: 'L-002', platform: 'Shopify', sellerSku: 'SKU-002', title: 'LED Desk Lamp', asin: '-', price: 45.00, status: 'ACTIVE', stock: 80 },
  { key: '3', listingId: 'L-003', platform: 'eBay', sellerSku: 'SKU-003', title: 'Yoga Mat Premium', asin: '-', price: 22.50, status: 'INACTIVE', stock: 0 },
  { key: '4', listingId: 'L-004', platform: 'Amazon', sellerSku: 'SKU-004', title: 'Portable Charger 20000mAh', asin: 'B0XXXXX2', price: 35.99, status: 'ACTIVE', stock: 200 },
  { key: '5', listingId: 'L-005', platform: 'Shopee', sellerSku: 'SKU-005', title: 'Stainless Steel Water Bottle', asin: '-', price: 18.00, status: 'ACTIVE', stock: 50 },
];

export default function ListingsPage() {
  const columns = [
    { title: 'Listing ID', dataIndex: 'listingId', key: 'listingId' },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag color={v === 'Amazon' ? '#ff9900' : v === 'Shopify' ? '#96bf48' : '#86b817'}>{v}</Tag> },
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
    { title: 'ASIN', dataIndex: 'asin', key: 'asin' },
    { title: '价格', dataIndex: 'price', key: 'price', render: (v: number) => `$${v.toFixed(2)}` },
    { title: '库存', dataIndex: 'stock', key: 'stock', render: (v: number) => <Badge count={v} overflowCount={9999} style={{ backgroundColor: v > 0 ? '#52c41a' : '#ff4d4f' }} /> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={v === 'ACTIVE' ? 'green' : 'default'} text={v} /> },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>Listing管理</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space wrap>
          <Input placeholder="搜索SKU/标题" prefix={<SearchOutlined />} style={{ width: 240 }} />
          <Select placeholder="平台" allowClear style={{ width: 140 }}
            options={[{ value: 'Amazon', label: 'Amazon' }, { value: 'Shopify', label: 'Shopify' }, { value: 'eBay', label: 'eBay' }, { value: 'Shopee', label: 'Shopee' }]} />
          <Button type="primary">查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={mockListings} size="middle" pagination={{ pageSize: 20 }} />
      </Card>
    </div>
  );
}
