<template>
  <div class="app-shell">
    <HomePage v-show="active === 'home'" :on-navigate="setActive" />
    <MilestonesPage v-show="active === 'milestones'" :on-navigate="setActive" />
    <BabiesPage v-show="active === 'babies'" :on-navigate="setActive" />

    <van-tabbar v-model="active" fixed placeholder safe-area-inset-bottom>
      <van-tabbar-item name="home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item name="milestones" icon="cluster-o">里程碑</van-tabbar-item>
      <van-tabbar-item name="babies" icon="contact-o">宝宝</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import HomePage from './pages/HomePage.vue';
import MilestonesPage from './pages/MilestonesPage.vue';
import BabiesPage from './pages/BabiesPage.vue';
import { useStore } from './store';

const active = ref('home');
const store = useStore();

function setActive(tab: string) {
  active.value = tab;
}

watch(
  () => store.activeBabyId.value,
  () => store.loadMilestones()
);

onMounted(async () => {
  await store.loadTypes();
  await store.loadBabies();
  await store.loadMilestones();
});
</script>
