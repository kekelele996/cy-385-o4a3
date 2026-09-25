import { computed, reactive, ref } from 'vue';
import type { Baby, Milestone, MilestoneTypeOption } from './types';
import {
  createBaby as apiCreateBaby,
  listBabies,
  listMilestones,
  listMilestoneTypes,
  latestMilestone,
} from './api';

const ACTIVE_BABY_KEY = 'babytracker.activeBabyId';

const babies = ref<Baby[]>([]);
const milestones = ref<Milestone[]>([]);
const latest = ref<Milestone | null>(null);
const types = ref<MilestoneTypeOption[]>([]);
const loading = reactive({ babies: false, milestones: false });

const activeBabyId = ref<number>(Number(localStorage.getItem(ACTIVE_BABY_KEY)) || 0);
const activeBaby = computed<Baby | null>(
  () => babies.value.find((b) => b.id === activeBabyId.value) ?? babies.value[0] ?? null
);

/** 里程碑数据版本号，每次重新拉取自增，供页面感知数据变化（v-show 下不会重新 mount） */
const milestonesVersion = ref(0);

async function loadBabies() {
  loading.babies = true;
  try {
    babies.value = await listBabies();
    if (!activeBabyId.value || !babies.value.some((b) => b.id === activeBabyId.value)) {
      setActiveBaby(babies.value[0]?.id ?? 0);
    }
  } finally {
    loading.babies = false;
  }
}

function setActiveBaby(id: number) {
  activeBabyId.value = id;
  if (id) localStorage.setItem(ACTIVE_BABY_KEY, String(id));
}

async function loadTypes() {
  if (types.value.length) return;
  types.value = await listMilestoneTypes();
}

async function loadMilestones() {
  if (!activeBaby.value) {
    milestones.value = [];
    latest.value = null;
    return;
  }
  loading.milestones = true;
  try {
    const [list, latestOne] = await Promise.all([
      listMilestones(activeBaby.value.id),
      latestMilestone(activeBaby.value.id),
    ]);
    milestones.value = list;
    latest.value = latestOne;
    milestonesVersion.value += 1;
  } finally {
    loading.milestones = false;
  }
}

async function addBaby(data: Omit<Baby, 'id'>): Promise<Baby> {
  const baby = await apiCreateBaby(data);
  await loadBabies();
  setActiveBaby(baby.id);
  await loadMilestones();
  return baby;
}

export function useStore() {
  return {
    babies,
    milestones,
    latest,
    types,
    loading,
    activeBabyId,
    activeBaby,
    milestonesVersion,
    loadBabies,
    setActiveBaby,
    loadTypes,
    loadMilestones,
    addBaby,
  };
}
