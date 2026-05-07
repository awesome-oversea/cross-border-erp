import axios from 'axios';
import type { Result, PageResult, PageParams } from '@/types';

const apiClient = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('erp_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    const tenantId = localStorage.getItem('erp_tenant_id');
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('erp_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const res = await apiClient.get<Result<T>>(url, { params });
  return res.data.data;
}

export async function getPage<T>(url: string, params?: PageParams & Record<string, unknown>): Promise<PageResult<T>> {
  const res = await apiClient.get<Result<PageResult<T>>>(url, { params });
  return res.data.data;
}

export async function post<T>(url: string, data?: unknown): Promise<T> {
  const res = await apiClient.post<Result<T>>(url, data);
  return res.data.data;
}

export async function put<T>(url: string, data?: unknown): Promise<T> {
  const res = await apiClient.put<Result<T>>(url, data);
  return res.data.data;
}

export async function patch<T>(url: string, data?: unknown): Promise<T> {
  const res = await apiClient.patch<Result<T>>(url, data);
  return res.data.data;
}

export async function del<T>(url: string): Promise<T> {
  const res = await apiClient.delete<Result<T>>(url);
  return res.data.data;
}

export default apiClient;
