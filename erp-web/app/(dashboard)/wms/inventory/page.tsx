'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Card, Typography, Tag, Badge, Select, Space, Button, Input } from 'antd';
import { SearchOutlined, WarningOutlined } from '@ant-design/icons';
import { wmsApi } from '@/lib/api';
import type { InventoryBalance } from '@/types';

const { Title } = Typography;

export default function InventoryPage() {
  const [inventory, setInventory] = useState<InventoryBalance[]>([]);
  const [loading, setLoading] = useState(false);
  const [warehouseId, setWarehouseId] = useState<string>('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await wmsApi.listInventory(warehouseId || undefined);
      setInventory(data || []);
    } catch {
      setInventory([]);
    } finally {
      setLoading(false);
    }
  }, [warehouseId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const columns = [
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: '仓库ID', dataIndex: 'warehouseId', key: 'warehouseId' },
    { title: '可用库存', dataIndex: 'available', key: 'available',
      render: (v: number) => <span style={{ color: v > 20 ? '#52c41a' : v > 0 ? '#faad14' : '#ff4d4f', fontWeight: 600 }}>{v}</span>,
    },
    { title: '预留库存', dataIndex: 'reserved', key: 'reserved', render: (v: number) => <Tag color="orange">{v}</Tag> },
    { title: '在途库存', dataIndex: 'inTransit', key: 'inTransit', render: (v: number) => <Tag color="blue">{v}</Tag> },
    { title: '冻结库存', dataIndex: 'frozen', key: 'frozen', render: (v: number) => <Tag color="red">{v}</Tag> },
    { title: '库存状态', key: 'status',
      render: (_: unknown, r: InventoryBalance) => {
        if (r.available <= 0) return <Badge status="error" text="缺货" />;
        if (r.available <= 20) return <Badge status="warning" text="低库存" />;
        return <Badge status="success" text="正常" />;
      },
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>库存台账</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space wrap>
          <Input placeholder="搜索SKU" prefix={<SearchOutlined />} style={{ width: 240 }} />
          <Select placeholder="选择仓库" allowClear style={{ width: 200 }} value={warehouseId || undefined} onChange={setWarehouseId}
            options={[{ value: 'wh-001', label: '深圳仓' }, { value: 'wh-002', label: '义乌仓' }, { value: 'wh-fba-us', label: 'FBA美国仓' }]} />
          <Button type="primary" onClick={fetchData}>查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey={(r) => `${r.skuId}-${r.warehouseId}`} columns={columns} dataSource={inventory} loading={loading} size="middle"
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 条` }} />
      </Card>
    </div>
  );
}
