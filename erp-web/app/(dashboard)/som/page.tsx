'use client';

import { Card, Typography, Row, Col, Statistic, Tag, Table, Badge } from 'antd';
import { ShopOutlined, GlobalOutlined, DollarOutlined, RiseOutlined } from '@ant-design/icons';

const { Title } = Typography;

const storeStats = [
  { key: '1', platform: 'Amazon', storeName: 'US Main Store', region: '北美', status: 'ACTIVE', orders: 856, revenue: 42800, listingCount: 320 },
  { key: '2', platform: 'Amazon', storeName: 'EU Store', region: '欧洲', status: 'ACTIVE', orders: 423, revenue: 21150, listingCount: 180 },
  { key: '3', platform: 'Shopify', storeName: 'Brand Official', region: '全球', status: 'ACTIVE', orders: 312, revenue: 46800, listingCount: 95 },
  { key: '4', platform: 'eBay', storeName: 'Clearance Outlet', region: '北美', status: 'ACTIVE', orders: 189, revenue: 5670, listingCount: 210 },
  { key: '5', platform: 'Shopee', storeName: 'SEA Store', region: '东南亚', status: 'INACTIVE', orders: 0, revenue: 0, listingCount: 45 },
];

export default function SomPage() {
  const columns = [
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag color={v === 'Amazon' ? '#ff9900' : v === 'Shopify' ? '#96bf48' : '#86b817'}>{v}</Tag> },
    { title: '店铺名称', dataIndex: 'storeName', key: 'storeName' },
    { title: '区域', dataIndex: 'region', key: 'region', render: (v: string) => <Tag>{v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={v === 'ACTIVE' ? 'green' : 'default'} text={v === 'ACTIVE' ? '运营中' : '停用'} /> },
    { title: '订单数', dataIndex: 'orders', key: 'orders' },
    { title: '销售额', dataIndex: 'revenue', key: 'revenue', render: (v: number) => `$${v.toLocaleString()}` },
    { title: 'Listing数', dataIndex: 'listingCount', key: 'listingCount' },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>销售运营</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="运营店铺" value={4} suffix="/ 5" prefix={<ShopOutlined />} valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="覆盖区域" value={4} suffix="个" prefix={<GlobalOutlined />} valueStyle={{ color: '#722ed1' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="总销售额" value={116420} prefix={<><DollarOutlined />$</>} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="总订单" value={1780} suffix="单" valueStyle={{ color: '#fa8c16' }} />
          </Card>
        </Col>
      </Row>
      <Card title="店铺概览" style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={storeStats} size="middle" pagination={false} />
      </Card>
    </div>
  );
}
