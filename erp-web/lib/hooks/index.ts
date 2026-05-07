'use client';

import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/lib/store';

export function usePermission() {
  const user = useAuthStore((s) => s.user);
  const hasRole = (role: string) => user?.roles?.includes(role) ?? false;
  const hasAnyRole = (roles: string[]) => roles.some((r) => hasRole(r));
  return { hasRole, hasAnyRole, user };
}

export function useBreadcrumbs() {
  const pathname = usePathname();
  const segments = pathname.split('/').filter(Boolean);
  return segments.map((seg, i) => ({
    title: seg.charAt(0).toUpperCase() + seg.slice(1),
    href: '/' + segments.slice(0, i + 1).join('/'),
  }));
}

export { useApi, usePageApi } from './useApi';
