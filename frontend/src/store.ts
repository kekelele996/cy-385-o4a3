import { defineStore } from './tiny-store';
import { fetchBabies } from './api';
import type { Baby } from './types';

interface AppState {
  babies: Baby[];
  currentBabyId: number | null;
  milestoneTypes: string[];
  loaded: boolean;
}

const SELECTED_KEY = 'babytracker.selectedBabyId';

export const useAppStore = defineStore({
  state: {
    babies: [],
    currentBabyId: null,
    milestoneTypes: [],
    loaded: false,
  } as AppState,
  getters: {
    currentBaby(state): Baby | null {
      return state.babies.find((b) => b.id === state.currentBabyId) ?? null;
    },
  },
  actions: {
    async loadBabies(force = false) {
      if (this.state.loaded && !force) return;
      const babies = await fetchBabies();
      this.state.babies = babies;
      this.state.loaded = true;
      const savedId = Number(localStorage.getItem(SELECTED_KEY));
      if (savedId && babies.some((b: Baby) => b.id === savedId)) {
        this.state.currentBabyId = savedId;
      } else {
        this.state.currentBabyId = babies[0]?.id ?? null;
      }
    },
    selectBaby(id: number) {
      this.state.currentBabyId = id;
      localStorage.setItem(SELECTED_KEY, String(id));
    },
    addBaby(baby: Baby) {
      this.state.babies = [...this.state.babies, baby];
      this.selectBaby(baby.id);
    },
    setMilestoneTypes(types: string[]) {
      this.state.milestoneTypes = types;
    },
  },
});
