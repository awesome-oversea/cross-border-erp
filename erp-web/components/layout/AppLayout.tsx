'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Layout, Menu, Typography, Avatar, Dropdown, Space } from 'antd';
import {
  DashboardOutlined,
  TeamOutlined,
  ShoppingOutlined,
  AppstoreOutlined,
  HomeOutlined,
  DollarOutlined,
  CarOutlined,
  CustomerServiceOutlined,
  BarChartOutlined,
  SettingOutlined,
  ContainerOutlined,
  ShopOutlined,
  ThunderboltOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import { useAuthStore, useSidebarStore } from '@/lib/store';

const { Sider, Header, Content } = Layout;
const { Text } = Typography;

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '工作台', domain: 'dashboard' },
  { key: '/iam', icon: <TeamOutlined />, label: '组织权限', domain: 'iam',
    children: [
      { key: '/iam/users', label: '用户管理' },
      { key: '/iam/roles', label: '角色管理' },
      { key: '/iam/positions', label: '岗位管理' },
      { key: '/iam/permissions', label: '对象权限' },
    ],
  },
  { key: '/pdm', icon: <AppstoreOutlined />, label: '产品开发', domain: 'pdm',
    children: [
      { key: '/pdm/products', label: '产品管理' },
      { key: '/pdm/listings', label: 'Listing管理' },
    ],
  },
  { key: '/som', icon: <ShopOutlined />, label: '销售运营', domain: 'som',
    children: [
      { key: '/som/stores', label: '店铺管理' },
      { key: '/som/listings', label: 'Listing管理' },
      { key: '/som/price-rules', label: '价格规则' },
      { key: '/som/monitors', label: 'Listing监控' },
    ],
  },
  { key: '/oms', icon: <ShoppingOutlined />, label: '订单管理', domain: 'oms',
    children: [
      { key: '/oms/orders', label: '订单列表' },
      { key: '/oms/audit', label: '订单审核' },
    ],
  },
  { key: '/scm', icon: <ContainerOutlined />, label: '供应链', domain: 'scm',
    children: [
      { key: '/scm/suppliers', label: '供应商' },
      { key: '/scm/purchase-orders', label: '采购单' },
    ],
  },
  { key: '/wms', icon: <HomeOutlined />, label: '仓储管理', domain: 'wms',
    children: [
      { key: '/wms/warehouses', label: '仓库管理' },
      { key: '/wms/inventory', label: '库存台账' },
    ],
  },
  { key: '/fba', icon: <ThunderboltOutlined />, label: 'FBA管理', domain: 'fba',
    children: [
      { key: '/fba/shipments', label: '备货计划' },
      { key: '/fba/restock', label: '补货建议' },
    ],
  },
  { key: '/tms', icon: <CarOutlined />, label: '物流管理', domain: 'tms',
    children: [
      { key: '/tms/carriers', label: '物流商' },
      { key: '/tms/tracking', label: '轨迹查询' },
    ],
  },
  { key: '/crm', icon: <CustomerServiceOutlined />, label: '客服售后', domain: 'crm',
    children: [
      { key: '/crm/customers', label: '客户管理' },
      { key: '/crm/tickets', label: '工单管理' },
      { key: '/crm/email-rules', label: '邮件规则' },
      { key: '/crm/campaigns', label: '邮件营销' },
      { key: '/crm/quality', label: '质量问题' },
    ],
  },
  { key: '/fms', icon: <DollarOutlined />, label: '财务管理', domain: 'fms',
    children: [
      { key: '/fms/cost-events', label: '成本事件' },
      { key: '/fms/profit', label: '利润报表' },
    ],
  },
  { key: '/ads', icon: <ThunderboltOutlined />, label: '广告管理', domain: 'ads',
    children: [
      { key: '/ads/campaigns', label: '广告活动' },
      { key: '/ads/strategies', label: '广告策略' },
    ],
  },
  { key: '/bi', icon: <BarChartOutlined />, label: '商业智能', domain: 'bi',
    children: [
      { key: '/bi/cockpit', label: '经营驾驶舱' },
      { key: '/bi/kpis', label: 'KPI指标' },
      { key: '/bi/reports', label: '报表中心' },
      { key: '/bi/trends', label: '趋势分析' },
    ],
  },
  { key: '/sys', icon: <SettingOutlined />, label: '系统设置', domain: 'sys',
    children: [
      { key: '/sys/configs', label: '系统参数' },
      { key: '/sys/webhooks', label: 'Webhook' },
      { key: '/sys/rules', label: '业务规则' },
    ],
  },
];

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const collapsed = useSidebarStore((s) => s.collapsed);
  const toggle = useSidebarStore((s) => s.toggle);
  const user = useAuthStore((s) => s.user);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const selectedKeys = [pathname];
  const openKeys = menuItems
    .filter((m) => m.children && pathname.startsWith(m.key))
    .map((m) => m.key);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={220}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          background: '#001529',
        }}
      >
        <div style={{ height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '12px 0' }}>
          <Text style={{ color: '#fff', fontSize: collapsed ? 14 : 18, fontWeight: 700, whiteSpace: 'nowrap' }}>
            {collapsed ? 'ERP' : '跨境电商ERP'}
          </Text>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={selectedKeys}
          defaultOpenKeys={openKeys}
          items={menuItems.map((item) => ({
            key: item.key,
            icon: item.icon,
            label: item.children ? item.label : <Link href={item.key}>{item.label}</Link>,
            children: item.children?.map((child) => ({
              key: child.key,
              label: <Link href={child.key}>{child.label}</Link>,
            })),
          }))}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 220, transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
            position: 'sticky',
            top: 0,
            zIndex: 10,
          }}
        >
          <Space>
            {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
              style: { fontSize: 18, cursor: 'pointer' },
              onClick: toggle,
            })}
          </Space>
          <Dropdown
            menu={{
              items: [
                { key: 'profile', icon: <UserOutlined />, label: '个人信息' },
                { type: 'divider' },
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: () => {
                    clearAuth();
                    window.location.href = '/login';
                  },
                },
              ],
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <Text>{user?.username || '管理员'}</Text>
            </Space>
          </Dropdown>
        </Header>
        <Content style={{ margin: 16, padding: 24, background: '#f5f5f5', minHeight: 'calc(100vh - 64px - 48px)' }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
