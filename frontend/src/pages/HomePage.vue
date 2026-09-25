<template>
  <div>
    <header class="hero">
      <BabySwitcher />
      <template v-if="currentBaby">
        <h1>
          {{ currentBaby.name }}
          <span class="hero-age">{{ ageText(currentBaby.birthday, today) }}</span>
        </h1>
        <p class="hero-sub">
          出生于 {{ formatDate(currentBaby.birthday) }}
          <template v-if="currentBaby.bloodType"> · 血型 {{ currentBaby.bloodType }}</template>
        </p>
      </template>
      <template v-else>
        <h1>宝宝成长记录</h1>
        <p class="hero-sub">还没有宝宝档案，请先到「宝宝」页创建</p>
      </template>
    </header>

    <LatestMilestone :baby="currentBaby" />

    <section class="card quick-entry" @click="goTimeline">
      <div>
        <strong>成长里程碑时间线</strong>
        <p>第一次翻身、第一颗牙……按时间顺序都在这里</p>
      </div>
      <van-icon name="arrow" size="18" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { type ComputedRef } from 'vue';
import BabySwitcher from '../components/BabySwitcher.vue';
import LatestMilestone from '../components/LatestMilestone.vue';
import { useAppStore } from '../store';
import { ageText, formatDate, todayISO } from '../date-utils';
import type { Baby } from '../types';

const store = useAppStore();
const currentBaby = store.currentBaby as ComputedRef<Baby | null>;
const today = todayISO();

const emit = defineEmits<{ navigate: [tab: string] }>();
function goTimeline() {
  emit('navigate', 'timeline');
}
</script>

<style scoped>
.hero {
  background: #ff8a65;
  color: #fff;
  border-radius: 10px;
  padding: 4px 16px 18px;
}
.hero :deep(.baby-switcher .van-dropdown-menu__bar) {
  background: transparent;
  box-shadow: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.35);
  color: #fff;
}
.hero :deep(.van-dropdown-menu__bar .van-dropdown-menu__item-text) {
  color: #fff;
  font-weight: 600;
}
.hero h1 {
  margin: 14px 0 4px;
  font-size: 22px;
}
.hero-age {
  font-size: 14px;
  font-weight: 400;
  margin-left: 8px;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  padding: 2px 10px;
}
.hero-sub {
  margin: 0;
  font-size: 13px;
  opacity: 0.9;
}
.quick-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.quick-entry p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9c7e72;
}
</style>
