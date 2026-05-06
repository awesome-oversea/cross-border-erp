import type { Metadata } from 'next';
import { AntdProvider } from '@/components/provider/AntdProvider';

export const metadata: Metadata = {
  title: '跨境电商ERP',
  description: '跨境电商ERP管理系统',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <AntdProvider>{children}</AntdProvider>
      </body>
    </html>
  );
}
