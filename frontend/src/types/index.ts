export interface Baby {
  id: number;
  name: string;
  birthday: string;
  bloodType?: string | null;
  initialHeight?: number | null;
  initialWeight?: number | null;
}

export interface MilestoneTypeOption {
  code: string;
  label: string;
}

export interface Milestone {
  id: number;
  babyId: number;
  typeCode: string;
  typeLabel: string;
  milestoneDate: string;
  note?: string | null;
  photoUrl?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

/** 后端统一错误返回 */
export interface ApiError {
  success: false;
  code: string;
  message: string;
  data?: unknown;
}

export interface MilestoneFormData {
  babyId: number;
  typeCode: string;
  milestoneDate: string;
  note: string;
  photoUrl: string;
}
