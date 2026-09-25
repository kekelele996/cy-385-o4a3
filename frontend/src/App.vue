<template>
  <main>
    <HomePage v-show="active === 'home'" @navigate="onNavigate" />
    <TimelinePage v-show="active === 'timeline'" />
    <BabyProfilePage v-show="active === 'baby'" />

    <van-tabbar v-model="active" fixed safe-area-inset-bottom>
      <van-tabbar-item name="home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item name="timeline" icon="records">里程碑</van-tabbar-item>
      <van-tabbar-item name="baby" icon="contact-o">宝宝</van-tabbar-item>
    </van-tabbar>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { showFailToast } from 'vant';
import HomePage from './pages/HomePage.vue';
import TimelinePage from './pages/TimelinePage.vue';
import BabyProfilePage from './pages/BabyProfilePage.vue';
import { fetchMilestoneTypes } from './api';
import { useAppStore } from './store';

const active = ref<'home' | 'timeline' | 'baby'>('home');
const store = useAppStore();

function onNavigate(tab: string) {
  if (tab === 'home' || tab === 'timeline' || tab === 'baby') {
    active.value = tab;
  }
}

onMounted(async () => {
  try {
    await store.loadBabies();
    const { types } = await fetchMilestoneTypes();
    store.setMilestoneTypes(types);
  } catch (e) {
    showFailToast((e as Error).message || '数据加载失败');
  }
});
</script>
