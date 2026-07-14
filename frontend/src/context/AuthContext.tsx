import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import { api, clearSession, getStoredUser, getToken, setToken, storeSession } from '../api/client';
import { getLang } from '../i18n/strings';
import type { AuthResponse, AuthUser, RegisterPayload } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<AuthUser>;
  register: (payload: RegisterPayload) => Promise<AuthUser>;
  forgotPassword: (email: string) => Promise<void>;
  resetPassword: (token: string, newPassword: string) => Promise<AuthUser>;
  loginWithToken: (token: string) => Promise<AuthUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => (getToken() ? getStoredUser() : null));

  const applyAuth = useCallback((response: AuthResponse): AuthUser => {
    const authUser: AuthUser = {
      email: response.email,
      displayName: response.displayName,
      role: response.role
    };
    storeSession(response.token, authUser);
    setUser(authUser);
    return authUser;
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await api<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });
      return applyAuth(response);
    },
    [applyAuth]
  );

  const register = useCallback(
    async (payload: RegisterPayload) => {
      const response = await api<AuthResponse>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      return applyAuth(response);
    },
    [applyAuth]
  );

  const forgotPassword = useCallback(async (email: string) => {
    await api<void>('/api/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email, locale: getLang() })
    });
  }, []);

  const resetPassword = useCallback(
    async (token: string, newPassword: string) => {
      const response = await api<AuthResponse>('/api/auth/reset-password', {
        method: 'POST',
        body: JSON.stringify({ token, newPassword })
      });
      return applyAuth(response);
    },
    [applyAuth]
  );

  const loginWithToken = useCallback(async (token: string) => {
    setToken(token);
    try {
      const me = await api<AuthUser>('/api/auth/me');
      storeSession(token, me);
      setUser(me);
      return me;
    } catch (err) {
      clearSession();
      throw err;
    }
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        login,
        register,
        forgotPassword,
        resetPassword,
        loginWithToken,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth AuthProvider daxilində istifadə olunmalıdır');
  }
  return ctx;
}
