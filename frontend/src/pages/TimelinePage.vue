<template>
  <div>
    <header class="page-head">
      <h1>成长里程碑时间线</h1>
      <p v-if="currentBaby">{{ currentBaby.name }} · 按实际日期从近到远排列</p>
    </header>

    <div v-if="!currentBaby" class="card">
      <van-empty image="error" description="请先在「宝宝」页创建宝宝档案" />
    </div>

    <template v-else>
      <div class="add-bar">
        <van-button type="primary" round icon="plus" @click="openCreate">新增记录</van-button>
      </div>

      <div v-if="loading" class="card">
        <van-skeleton title :row="3" v-for="n in 2" :key="n" />
      </div>

      <div v-else-if="records.length === 0" class="card">
        <van-empty description="还没有里程碑，记下宝宝的第一次吧" />
      </div>

      <div v-else class="timeline">
        <div v-for="(item, idx) in records" :key="item.id" class="t-item">
          <div class="t-rail">
            <span class="t-dot" :class="{ first: idx === 0 }" />
            <span v-if="idx < records.length - 1" class="t-line" />
          </div>
          <div class="card t-card">
            <div class="t-card-head">
              <van-tag type="primary">{{ item.type }}</van-tag>
              <strong>{{ formatDate(item.milestoneDate) }}</strong>
              <van-button size="mini" plain type="primary" @click="openEdit(item)">改日期</van-button>
            </div>
            <p class="t-age">{{ ageText(currentBaby.birthday, item.milestoneDate) }}</p>
            <p v-if="item.description" class="t-desc">{{ item.description }}</p>
            <van-image
              v-if="item.photoUrl"
              :src="item.photoUrl"
              width="120"
              height="120"
              radius="8"
              fit="cover"
              class="t-photo"
            />
            <p class="t-meta">提交于 {{ createdAtText(item.createdAt) }}</p>
          </div>
        </div>
      </div>

      <MilestoneForm
        v-model:show="formShow"
        :baby-id="currentBaby.id"
        :baby-birthday="currentBaby.birthday"
        :editing="editing"
        :types="store.state.milestoneTypes"
        @saved="load"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, type ComputedRef } from 'vue';
import MilestoneForm from '../components/MilestoneForm.vue';
import { fetchTimeline } from '../api';
import { ageText, createdAtText, formatDate } from '../date-utils';
import { useAppStore } from '../store';
import type { Baby, Milestone } from '../types';

const store = useAppStore();
const currentBaby = store.currentBaby as ComputedRef<Baby | null>;

const records = ref<Milestone[]>([]);
const loading = ref(false);
const formShow = ref(false);
const editing = ref<Milestone | null>(null);

async function load() {
  if (!currentBaby.value) return;
  loading.value = true;
  try {
    records.value = await fetchTimeline(currentBaby.value.id);
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editing.value = null;
  formShow.value = true;
}

function openEdit(item: Milestone) {
  editing.value = item;
  formShow.value = true;
}

watch(() => currentBaby.value?.id, load);
onMounted(load);
</script>

<style scoped>
.page-head {
  padding: 8px 4px 4px;
}
.page-head h1 {
  margin: 0;
  font-size: 20px;
  color: #3b2b24;
}
.page-head p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9c7e72;
}
.add-bar {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0;
}
.timeline {
  margin-top: 4px;
}
.t-item {
  display: flex;
  align-items: stretch;
}
.t-rail {
  width: 22px;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}
.t-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ffccbc;
  margin-top: 18px;
  flex-shrink: 0;
}
.t-dot.first {
  background: #ff8a65;
  box-shadow: 0 0 0 4px rgba(255, 138, 101, 0.18);
}
.t-line {
  flex: 1;
  width: 2px;
  background: #ffe0d2;
  margin: 2px 0;
}
.t-card {
  flex: 1;
  margin: 0 0 14px;
}
.t-card-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.t-card-head strong {
  flex: 1;
  font-size: 15px;
}
.t-age {
  margin: 8px 0 0;
  font-size: 13px;
  color: #8d6e63;
}
.t-desc {
  margin: 6px 0 0;
  font-size: 14px;
  color: #4e342e;
  line-height: 1.6;
}
.t-photo {
  margin-top: 10px;
}
.t-meta {
  margin: 8px 0 0;
  font-size: 12px;
  color: #b39a8f;
}
</style>
