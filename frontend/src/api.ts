import type {
  Baby,
  Milestone,
  MilestonePayload,
  MilestoneSaveResult,
  NewBaby,
} from './types';

const BASE = '/api';

async function request(method: string, path: string, body?: unknown): Promise<any> {
  const res = await fetch(BASE + path, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const data = await res.json().catch(() => null);
  if (data && data.success === false) {
    const err = new Error(data.message || '请求失败，请稍后重试');
    (err as Error & { code?: string }).code = data.code;
    throw err;
  }
  return data;
}

export const fetchBabies = () => request('GET', '/babies') as Promise<Baby[]>;

export const createBaby = (baby: NewBaby) =>
  request('POST', '/babies', baby) as Promise<Baby>;

export const fetchMilestoneTypes = () =>
  request('GET', '/milestones/types') as Promise<{ types: string[] }>;

export const fetchTimeline = (babyId: number) =>
  request('GET', `/milestones?babyId=${babyId}`) as Promise<Milestone[]>;

export const fetchLatest = (babyId: number) =>
  request('GET', `/milestones/latest?babyId=${babyId}`) as Promise<Milestone | null>;

export const createMilestone = (payload: MilestonePayload) =>
  request('POST', '/milestones', payload) as Promise<MilestoneSaveResult>;

export const updateMilestone = (id: number, payload: MilestonePayload) =>
  request('PUT', `/milestones/${id}`, payload) as Promise<Milestone>;

/** 里程碑数据变化后通知首页/档案页刷新“最近一次记录”。 */
export function emitMilestonesChanged(babyId: number) {
  window.dispatchEvent(new CustomEvent('milestones-changed', { detail: { babyId } }));
}
