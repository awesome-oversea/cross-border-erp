import { get, post } from './client';
import type { User } from '@/types';

export async function login(username: string, password: string) {
  return post<{ token: string; user: User }>('/iam/api/in/v1/auth/login', { username, password });
}

export async function getCurrentUser() {
  return get<User>('/iam/api/in/v1/auth/me');
}

export async function logout() {
  return post<void>('/iam/api/in/v1/auth/logout');
}
