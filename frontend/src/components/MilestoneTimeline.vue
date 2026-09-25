<template>
  <div class="timeline">
    <van-empty v-if="!store.milestones.value.length" description="还没有里程碑，点「+ 记录一笔」记下第一次吧" />

    <div v-for="(item, index) in store.milestones.value" :key="item.id" class="tl-item">
      <div class="tl-axis">
        <span class="tl-dot" />
        <span v-if="index < store.milestones.value.length - 1" class="tl-line" />
      </div>
      <div class="tl-card" @click="onEdit(item)">
        <div class="tl-card-head">
          <van-tag plain type="primary">{{ item.typeLabel }}</van-tag>
          <span class="tl-date">{{ item.milestoneDate }}</span>
          <span class="tl-age">（宝宝 {{ describeAge(birthday, item.milestoneDate) }}）</span>
        </div>
        <p v-if="item.note" class="tl-note">{{ item.note }}</p>
        <van-image
          v-if="item.photoUrl"
          :src="item.photoUrl"
          class="tl-photo"
          fit="cover"
          radius="8"
        >
          <template #error>
            <div class="tl-photo-fallback">图片无法访问</div>
          </template>
        </van-image>
        <p class="tl-created">提交于 {{ formatDateTime(item.createdAt) }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Milestone } from '../types';
import { useStore } from '../store';
import { describeAge, formatDateTime } from '../utils/date';

const props = defineProps<{ birthday: string }>();
const emit = defineEmits<{ (e: 'edit', item: Milestone): void }>();

const store = useStore();
const birthday = props.birthday;

function onEdit(item: Milestone) {
  emit('edit', item);
}
</script>
