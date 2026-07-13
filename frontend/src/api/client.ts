import type { AuthUser } from '../types';

const TOKEN_KEY = 'jits_token';
const USER_KEY = 'jits_user';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

/** OAuth callback üçün: user məlumatı hələ bilinmir, əvvəlcə tokeni saxlayıb /api/auth/me çağırılır. */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function getStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function storeSession(token: string, user: AuthUser): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...((options.headers as Record<string, string>) ?? {})
  };
  const res = await fetch(path, { ...options, headers });

  if (res.status === 401) {
    clearSession();
    window.location.assign('/login');
    throw new ApiError(401, 'Unauthorized');
  }

  if (!res.ok) {
    let message = `Xəta (${res.status})`;
    try {
      const body = await res.json();
      message = body.message || body.error || message;
    } catch {
      // body JSON deyil
    }
    throw new ApiError(res.status, message);
  }

  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
