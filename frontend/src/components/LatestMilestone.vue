<template>
  <section class="card latest-milestone" v-if="baby">
    <div class="latest-head">
      <h2>最近一次里程碑</h2>
      <span class="latest-hint">按实际日期</span>
    </div>
    <van-skeleton v-if="loading" title :row="2" loading />
    <div v-else-if="latest" class="latest-body">
      <van-image
        v-if="latest.photoUrl"
        :src="latest.photoUrl"
        width="64"
        height="64"
        radius="8"
        fit="cover"
      />
      <div class="latest-info">
        <div class="latest-title">
          <van-tag type="primary">{{ latest.type }}</van-tag>
          <strong>{{ formatDate(latest.milestoneDate) }}</strong>
        </div>
        <p class="latest-age">{{ ageText(baby.birthday, latest.milestoneDate) }}</p>
        <p v-if="latest.description" class="latest-desc">{{ latest.description }}</p>
      </div>
    </div>
    <van-empty v-else image="search" description="还没有里程碑记录，去时间线记一条吧" image-size="72" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { fetchLatest } from '../api';
import { ageText, formatDate } from '../date-utils';
import type { Baby, Milestone } from '../types';

const props = defineProps<{ baby: Baby | null }>();

const latest = ref<Milestone | null>(null);
const loading = ref(false);

async function load() {
  if (!props.baby) {
    latest.value = null;
    return;
  }
  loading.value = true;
  try {
    latest.value = await fetchLatest(props.baby.id);
  } catch {
    latest.value = null;
  } finally {
    loading.value = false;
  }
}

function onChanged(e: Event) {
  const id = (e as CustomEvent<{ babyId: number }>).detail?.babyId;
  if (!id || id === props.baby?.id) load();
}

watch(() => props.baby?.id, load);
onMounted(() => {
  load();
  window.addEventListener('milestones-changed', onChanged);
});
onUnmounted(() => window.removeEventListener('milestones-changed', onChanged));
</script>

<style scoped>
.latest-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.latest-head h2 {
  margin: 0;
  font-size: 16px;
}
.latest-hint {
  font-size: 12px;
  color: #a58a7e;
}
.latest-body {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}
.latest-info {
  flex: 1;
  min-width: 0;
}
.latest-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}
.latest-age {
  margin: 6px 0 0;
  font-size: 13px;
  color: #8d6e63;
}
.latest-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: #6d554b;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
