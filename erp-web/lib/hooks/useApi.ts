'use client';

import useSWR from 'swr';
import type { PageParams, PageResult } from '@/types';

async function fetcher<T>(url: string): Promise<T> {
  const res = await fetch(url, {
    headers: (() => {
      const h: Record<string, string> = { 'Content-Type': 'application/json' };
      if (typeof window !== 'undefined') {
        const token = localStorage.getItem('erp_token');
        if (token) h['Authorization'] = `Bearer ${token}`;
        const tenantId = localStorage.getItem('erp_tenant_id');
        if (tenantId) h['X-Tenant-Id'] = tenantId;
      }
      return h;
    })(),
  });
  if (!res.ok) {
    if (res.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('erp_token');
      window.location.href = '/login';
    }
    throw new Error(`API error: ${res.status}`);
  }
  const json = await res.json();
  return json.data;
}

export function useApi<T>(url: string | null, options?: { revalidateOnFocus?: boolean }) {
  return useSWR<T>(url, fetcher, {
    revalidateOnFocus: false,
    dedupingInterval: 5000,
    ...options,
  });
}

export function usePageApi<T>(
  baseUrl: string | null,
  params?: PageParams & Record<string, unknown>,
  options?: { revalidateOnFocus?: boolean }
) {
  const searchParams = new URLSearchParams();
  if (params?.page !== undefined) searchParams.set('page', String(params.page));
  if (params?.size !== undefined) searchParams.set('size', String(params.size));
  if (params?.sort) searchParams.set('sort', params.sort);
  Object.entries(params || {}).forEach(([k, v]) => {
    if (k !== 'page' && k !== 'size' && k !== 'sort' && v !== undefined && v !== null && v !== '') {
      searchParams.set(k, String(v));
    }
  });
  const url = baseUrl ? `${baseUrl}?${searchParams.toString()}` : null;
  return useSWR<PageResult<T>>(url, fetcher, {
    revalidateOnFocus: false,
    dedupingInterval: 5000,
    ...options,
  });
}

export { fetcher };
