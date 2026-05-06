import { create } from 'zustand';
import type { User } from '@/types';

interface AuthState {
  token: string | null;
  user: User | null;
  tenantId: string | null;
  setAuth: (token: string, user: User, tenantId: string) => void;
  clearAuth: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: typeof window !== 'undefined' ? localStorage.getItem('erp_token') : null,
  user: null,
  tenantId: typeof window !== 'undefined' ? localStorage.getItem('erp_tenant_id') : null,
  setAuth: (token, user, tenantId) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('erp_token', token);
      localStorage.setItem('erp_tenant_id', tenantId);
    }
    set({ token, user, tenantId });
  },
  clearAuth: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('erp_token');
      localStorage.removeItem('erp_tenant_id');
    }
    set({ token: null, user: null, tenantId: null });
  },
  isAuthenticated: () => !!get().token,
}));

interface SidebarState {
  collapsed: boolean;
  toggle: () => void;
}

export const useSidebarStore = create<SidebarState>((set) => ({
  collapsed: false,
  toggle: () => set((s) => ({ collapsed: !s.collapsed })),
}));
