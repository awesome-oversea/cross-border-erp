'use client';

import { Card, Typography, Table, Tag, Badge, Row, Col, Statistic, Progress } from 'antd';
import { WarningOutlined, CheckCircleOutlined } from '@ant-design/icons';

const { Title } = Typography;

const restockSuggestions = [
  { key: '1', sellerSku: 'SKU-001', productName: 'Wireless Earbuds Pro', fbaStock: 12, dailySales: 8, daysOfSupply: 1.5, suggestedQty: 200, urgency: 'CRITICAL', fc: 'US-SEA1' },
  { key: '2', sellerSku: 'SKU-004', productName: 'Portable Charger', fbaStock: 45, dailySales: 6, daysOfSupply: 7.5, suggestedQty: 150, urgency: 'HIGH', fc: 'US-LAX1' },
  { key: '3', sellerSku: 'SKU-002', productName: 'LED Desk Lamp', fbaStock: 80, dailySales: 4, daysOfSupply: 20, suggestedQty: 100, urgency: 'MEDIUM', fc: 'US-SEA1' },
  { key: '4', sellerSku: 'SKU-005', productName: 'Water Bottle', fbaStock: 120, dailySales: 3, daysOfSupply: 40, suggestedQty: 0, urgency: 'LOW', fc: 'EU-FRA1' },
  { key: '5', sellerSku: 'SKU-003', productName: 'Yoga Mat', fbaStock: 0, dailySales: 2, daysOfSupply: 0, suggestedQty: 80, urgency: 'CRITICAL', fc: 'JP-TYO1' },
];

const urgencyMap: Record<string, { color: string; text: string }> = {
  CRITICAL: { color: 'red', text: '紧急' },
  HIGH: { color: 'orange', text: '高' },
  MEDIUM: { color: 'blue', text: '中' },
  LOW: { color: 'green', text: '低' },
};

export default function RestockPage() {
  const columns = [
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: '产品名称', dataIndex: 'productName', key: 'productName', ellipsis: true },
    { title: 'FBA库存', dataIndex: 'fbaStock', key: 'fbaStock', render: (v: number) => <span style={{ color: v <= 20 ? '#ff4d4f' : '#52c41a', fontWeight: 600 }}>{v}</span> },
    { title: '日均销量', dataIndex: 'dailySales', key: 'dailySales' },
    { title: '库存天数', dataIndex: 'daysOfSupply', key: 'daysOfSupply', render: (v: number) => (
      <Progress percent={Math.min(v / 30 * 100, 100)} size="small" format={() => `${v}天`} status={v <= 7 ? 'exception' : v <= 14 ? 'active' : 'success'} />
    )},
    { title: '建议补货', dataIndex: 'suggestedQty', key: 'suggestedQty', render: (v: number) => v > 0 ? <Tag color="blue">{v}件</Tag> : <Tag>无需补货</Tag> },
    { title: '紧急程度', dataIndex: 'urgency', key: 'urgency', render: (v: string) => <Badge color={urgencyMap[v]?.color || 'default'} text={urgencyMap[v]?.text || v} /> },
    { title: '仓库', dataIndex: 'fc', key: 'fc', render: (v: string) => <Tag>{v}</Tag> },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>补货建议</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={8}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="紧急补货" value={restockSuggestions.filter((r) => r.urgency === 'CRITICAL').length} suffix="SKU"
              valueStyle={{ color: '#ff4d4f' }} prefix={<WarningOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="建议补货总量" value={restockSuggestions.reduce((s, r) => s + r.suggestedQty, 0)} suffix="件"
              valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="库存充足" value={restockSuggestions.filter((r) => r.urgency === 'LOW').length} suffix="SKU"
              valueStyle={{ color: '#52c41a' }} prefix={<CheckCircleOutlined />} />
          </Card>
        </Col>
      </Row>
      <Card style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={restockSuggestions} size="middle" pagination={false} />
      </Card>
    </div>
  );
}
