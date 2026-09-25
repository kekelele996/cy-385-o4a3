import { request } from './request';
import type { Baby, Milestone, MilestoneFormData, MilestoneTypeOption } from '../types';

export function listBabies(): Promise<Baby[]> {
  return request('/babies');
}

export function createBaby(data: Omit<Baby, 'id'>): Promise<Baby> {
  return request('/babies', { method: 'POST', body: JSON.stringify(data) });
}

export function listMilestoneTypes(): Promise<MilestoneTypeOption[]> {
  return request('/milestones/types');
}

export function listMilestones(babyId: number): Promise<Milestone[]> {
  return request(`/milestones?babyId=${babyId}`);
}

export function latestMilestone(babyId: number): Promise<Milestone | null> {
  return request(`/milestones/latest?babyId=${babyId}`);
}

export function createMilestone(data: MilestoneFormData): Promise<Milestone> {
  return request('/milestones', { method: 'POST', body: JSON.stringify(data) });
}

export function updateMilestone(id: number, data: MilestoneFormData): Promise<Milestone> {
  return request(`/milestones/${id}`, { method: 'PUT', body: JSON.stringify(data) });
}

export function deleteMilestone(id: number): Promise<void> {
  return request(`/milestones/${id}`, { method: 'DELETE' });
}
