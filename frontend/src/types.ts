export interface Baby {
  id: number;
  name: string;
  birthday: string;
  bloodType?: string;
  initialHeight?: number;
  initialWeight?: number;
}

export interface NewBaby {
  name: string;
  birthday: string;
  bloodType?: string;
  initialHeight?: number;
  initialWeight?: number;
}

export interface Milestone {
  id: number;
  babyId: number;
  type: string;
  milestoneDate: string;
  description?: string | null;
  photoUrl?: string | null;
  createdAt: string;
  updatedAt?: string;
}

export interface MilestonePayload {
  babyId: number;
  type: string;
  milestoneDate: string;
  description?: string;
  photoUrl?: string;
}

export interface MilestoneSaveResult {
  saved: boolean;
  message: string;
  record: Milestone | null;
  existing: Milestone | null;
}
