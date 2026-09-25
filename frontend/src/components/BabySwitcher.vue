<template>
  <van-dropdown-menu class="baby-switcher">
    <van-dropdown-item v-model="value" :options="options" @change="onChange" />
  </van-dropdown-menu>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useStore } from '../store';

const store = useStore();

const value = ref<number>(0);
watch(
  () => store.activeBabyId.value,
  (id) => (value.value = id)
);

const options = computed(() =>
  store.babies.value.map((b) => ({ text: b.name, value: b.id }))
);

function onChange(id: number) {
  store.setActiveBaby(id);
  // App.vue 监听 activeBabyId 统一触发 loadMilestones，这里不重复请求
}
</script>

<style scoped>
.baby-switcher {
  --van-dropdown-menu-background: transparent;
  --van-dropdown-menu-title-color: #fff;
  --van-dropdown-menu-title-font-size: 15px;
  --van-dropdown-menu-height: 32px;
}
</style>
