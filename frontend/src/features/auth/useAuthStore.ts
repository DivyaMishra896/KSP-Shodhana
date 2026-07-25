import { create } from 'zustand';

export type OfficerRole = 'ROLE_OFFICER' | 'ROLE_INSPECTOR' | 'ROLE_SUPERINTENDENT';

interface AuthState {
  currentRole: OfficerRole;
  badgeNumber: string;
  officerName: string;
  jwtToken: string;
  setRole: (role: OfficerRole) => Promise<void>;
  hasPermission: (requiredRole: OfficerRole) => boolean;
  initToken: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  currentRole: 'ROLE_SUPERINTENDENT',
  badgeNumber: 'KSP-SP-9912',
  officerName: 'SP Rajesh Gowda',
  jwtToken: '',

  initToken: async () => {
    const { currentRole, badgeNumber } = get();
    try {
      const res = await fetch('/api/proxy/api/v1/auth/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ officerBadge: badgeNumber, role: currentRole }),
      });
      if (res.ok) {
        const body = await res.json();
        const token = body.data?.token || '';
        if (typeof window !== 'undefined') {
          localStorage.setItem('ksp_jwt_token', token);
        }
        set({ jwtToken: token });
      }
    } catch (e) {
      console.error('Failed to initialize cryptographic JWT token:', e);
    }
  },

  setRole: async (role: OfficerRole) => {
    let name = 'Constable Officer';
    let badge = 'KSP-OFF-1002';
    if (role === 'ROLE_INSPECTOR') {
      name = 'Inspector Vikram Patil';
      badge = 'KSP-INS-4481';
    } else if (role === 'ROLE_SUPERINTENDENT') {
      name = 'SP Rajesh Gowda';
      badge = 'KSP-SP-9912';
    }

    set({ currentRole: role, officerName: name, badgeNumber: badge });

    // Fetch real signed JJWT token from backend API
    try {
      const res = await fetch('/api/proxy/api/v1/auth/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ officerBadge: badge, role }),
      });
      if (res.ok) {
        const body = await res.json();
        const token = body.data?.token || '';
        if (typeof window !== 'undefined') {
          localStorage.setItem('ksp_jwt_token', token);
        }
        set({ jwtToken: token });
      }
    } catch (e) {
      console.error('Failed to issue real cryptographic JWT token:', e);
    }
  },

  hasPermission: (requiredRole: OfficerRole) => {
    const { currentRole } = get();
    if (currentRole === 'ROLE_SUPERINTENDENT') return true;
    if (requiredRole === 'ROLE_INSPECTOR' && currentRole === 'ROLE_INSPECTOR') return true;
    if (requiredRole === 'ROLE_OFFICER') return true;
    return false;
  },
}));
