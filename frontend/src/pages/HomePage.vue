<template>
  <main>
    <header class="hero">
      <div class="hero-row">
        <div>
          <h1>宝宝成长记录</h1>
          <p v-if="store.activeBaby.value">
            {{ store.activeBaby.value.name }} · 出生 {{ store.activeBaby.value.birthday }} ·
            {{ describeAge(store.activeBaby.value.birthday) }}
          </p>
          <p v-else>还没有宝宝档案，先去「宝宝」页创建吧</p>
        </div>
        <BabySwitcher v-if="store.babies.value.length" />
      </div>
    </header>

    <section class="card latest-card" @click="goTimeline">
      <div class="card-title-row">
        <h2>最近一次里程碑</h2>
        <van-button v-if="store.activeBaby.value" size="small" type="primary" round @click.stop="onAdd">
          <van-icon name="plus" /> 记录一笔
        </van-button>
      </div>

      <van-skeleton v-if="store.loading.milestones" title :row="2" loading />

      <div v-else-if="store.latest.value" class="latest-body">
        <van-image
          v-if="store.latest.value.photoUrl"
          :src="store.latest.value.photoUrl"
          width="64"
          height="64"
          radius="10"
          fit="cover"
        >
          <template #error><div class="thumb-fallback">无图</div></template>
        </van-image>
        <div class="latest-info">
          <p class="latest-type">{{ store.latest.value.typeLabel }}</p>
          <p class="latest-date">
            {{ store.latest.value.milestoneDate }} · {{ daysFromToday(store.latest.value.milestoneDate) }} 天前
          </p>
          <p v-if="store.latest.value.note" class="latest-note">{{ store.latest.value.note }}</p>
        </div>
      </div>
      <van-empty v-else image-size="72" description="暂无里程碑记录" />

      <p class="card-foot" @click="goTimeline">查看完整时间线 →</p>
    </section>

    <section class="card quick-card" v-if="store.activeBaby.value">
      <van-grid :column-num="1" :border="false">
        <van-grid-item icon="cluster-o" text="成长里程碑时间线" @click="goTimeline" />
      </van-grid>
    </section>

    <MilestoneForm v-model:show="formShow" @saved="store.loadMilestones()" />
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useStore } from '../store';
import { daysFromToday, describeAge } from '../utils/date';
import BabySwitcher from '../components/BabySwitcher.vue';
import MilestoneForm from '../components/MilestoneForm.vue';

const props = defineProps<{ onNavigate: (tab: string) => void }>();
const store = useStore();
const formShow = ref(false);

function onAdd() {
  formShow.value = true;
}

function goTimeline() {
  props.onNavigate('milestones');
}
</script>
