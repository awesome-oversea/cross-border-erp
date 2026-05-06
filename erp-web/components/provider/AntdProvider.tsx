import { AntdRegistry } from '@ant-design/nextjs-registry';

export function AntdProvider({ children }: { children: React.ReactNode }) {
  return <AntdRegistry>{children}</AntdRegistry>;
}
