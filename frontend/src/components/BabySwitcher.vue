<template>
  <van-dropdown-menu class="baby-switcher">
    <van-dropdown-item v-model="selected" :options="options" @change="onChange" />
  </van-dropdown-menu>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { showToast } from 'vant';
import { useAppStore } from '../store';

const store = useAppStore();

const options = computed(() =>
  store.state.babies.map((b) => ({ text: b.name, value: b.id })),
);

const selected = ref<number | undefined>(store.state.currentBabyId ?? undefined);
watch(
  () => store.state.currentBabyId,
  (id) => {
    if (id) selected.value = id;
  },
);

function onChange(value: number) {
  store.selectBaby(value);
  showToast(`已切换到 ${store.state.babies.find((b) => b.id === value)?.name}`);
}
</script>
