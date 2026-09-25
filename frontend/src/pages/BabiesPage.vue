<template>
  <main>
    <header class="hero">
      <div class="hero-row">
        <div>
          <h1>宝宝档案</h1>
          <p>记录出生日期、身高、体重和血型</p>
        </div>
        <van-button
          v-if="store.babies.value.length"
          size="small"
          plain
          color="#fff"
          round
          @click="babyFormShow = true"
        >
          <van-icon name="plus" /> 新增档案
        </van-button>
      </div>
    </header>

    <van-skeleton v-if="store.loading.babies" title :row="3" loading />

    <section v-else-if="store.babies.value.length" class="baby-list">
      <div
        v-for="baby in store.babies.value"
        :key="baby.id"
        class="card baby-card"
        :class="{ active: baby.id === store.activeBabyId.value }"
        @click="store.setActiveBaby(baby.id)"
      >
        <div class="baby-card-head">
          <div>
            <h2>{{ baby.name }}</h2>
            <p>
              {{ baby.birthday }} 出生 · {{ describeAge(baby.birthday) }}
            </p>
          </div>
          <van-tag v-if="baby.id === store.activeBabyId.value" type="primary" round>当前</van-tag>
        </div>

        <div class="baby-meta">
          <span>血型 {{ baby.bloodType || '未填' }}</span>
          <span>身高 {{ baby.initialHeight ? baby.initialHeight + ' cm' : '未填' }}</span>
          <span>体重 {{ baby.initialWeight ? baby.initialWeight + ' kg' : '未填' }}</span>
        </div>

        <div class="baby-latest" @click.stop="goTimeline(baby.id)">
          <template v-if="latestMap[baby.id]">
            <van-icon name="underway-o" />
            <div>
              <p class="bl-title">最近一次：{{ latestMap[baby.id]!.typeLabel }}</p>
              <p class="bl-meta">
                {{ latestMap[baby.id]!.milestoneDate }} ·
                {{ daysFromToday(latestMap[baby.id]!.milestoneDate) }} 天前
              </p>
            </div>
          </template>
          <template v-else>
            <van-icon name="records" />
            <p class="bl-title">暂无里程碑记录</p>
          </template>
        </div>
      </div>
    </section>

    <section v-else class="card">
      <van-empty description="还没有宝宝档案">
        <van-button type="primary" round size="small" @click="babyFormShow = true">
          创建第一个档案
        </van-button>
      </van-empty>
    </section>

    <BabyForm v-model:show="babyFormShow" />
  </main>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useStore } from '../store';
import { latestMilestone } from '../api';
import type { Milestone } from '../types';
import { daysFromToday, describeAge } from '../utils/date';
import BabyForm from '../components/BabyForm.vue';

const props = defineProps<{ onNavigate: (tab: string) => void }>();
const store = useStore();
const babyFormShow = ref(false);

// 每个宝宝档案各自展示最近一次里程碑
const latestMap = reactive<Record<number, Milestone | null>>({});

async function refreshLatest() {
  await Promise.all(
    store.babies.value.map(async (baby) => {
      latestMap[baby.id] = await latestMilestone(baby.id);
    })
  );
}

function goTimeline(babyId: number) {
  if (babyId !== store.activeBabyId.value) {
    store.setActiveBaby(babyId);
  }
  void store.loadMilestones();
  props.onNavigate('milestones');
}

watch(() => store.babies.value, refreshLatest);
watch(() => store.milestonesVersion.value, refreshLatest);
onMounted(refreshLatest);
</script>
