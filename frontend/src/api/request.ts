import type { ApiError } from '../types';

const BASE = '/api';

/**
 * 统一请求封装：后端业务异常以 HTTP 400 + { code, message, data } 返回，
 * 这里把它抛出为带 code/data 的错误，供页面提示“哪项时间不合适”等信息。
 */
export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options,
  });

  if (res.status === 204) {
    return undefined as T;
  }

  const body = await res.json().catch(() => null);
  if (!res.ok || (body && body.success === false)) {
    const error = new Error(body?.message || `请求失败（${res.status}）`) as Error & {
      code?: string;
      data?: unknown;
    };
    error.code = (body as ApiError | null)?.code;
    error.data = (body as ApiError | null)?.data;
    throw error;
  }
  return body as T;
}
