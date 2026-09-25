<template>
  <main>
    <header class="hero">
      <div class="hero-row">
        <div>
          <h1>成长里程碑</h1>
          <p v-if="store.activeBaby.value">
            {{ store.activeBaby.value.name }} · 出生 {{ store.activeBaby.value.birthday }}
          </p>
          <p v-else>请先在「宝宝」页创建档案</p>
        </div>
        <BabySwitcher v-if="store.babies.value.length" />
      </div>
    </header>

    <section class="card" v-if="store.activeBaby.value">
      <div class="card-title-row">
        <h2>时间线</h2>
        <van-button size="small" type="primary" round @click="onAdd">
          <van-icon name="plus" /> 记录一笔
        </van-button>
      </div>
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <MilestoneTimeline :birthday="store.activeBaby.value.birthday" @edit="onEdit" />
      </van-pull-refresh>
    </section>

    <section class="card" v-else>
      <van-empty description="先创建宝宝档案，再来记录第一次翻身、第一颗牙">
        <van-button type="primary" round size="small" @click="props.onNavigate('babies')">
          去创建档案
        </van-button>
      </van-empty>
    </section>

    <MilestoneForm
      v-model:show="formShow"
      :editing="editing"
      @saved="store.loadMilestones()"
    />

    <van-action-sheet
      v-model:show="actionShow"
      :actions="actions"
      cancel-text="取消"
      close-on-click-action
      @select="onAction"
    />
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { showConfirmDialog, showSuccessToast } from 'vant';
import { useStore } from '../store';
import { deleteMilestone } from '../api';
import type { Milestone } from '../types';
import BabySwitcher from '../components/BabySwitcher.vue';
import MilestoneForm from '../components/MilestoneForm.vue';
import MilestoneTimeline from '../components/MilestoneTimeline.vue';

const props = defineProps<{ onNavigate: (tab: string) => void }>();
const store = useStore();
const formShow = ref(false);
const editing = ref<Milestone | null>(null);
const refreshing = ref(false);
const actionShow = ref(false);
const selected = ref<Milestone | null>(null);

const actions = [
  { name: '修改（改日期后时间线自动重排）', value: 'edit' },
  { name: '删除', value: 'delete', color: '#ee0a24' },
];

function onAdd() {
  editing.value = null;
  formShow.value = true;
}

function onEdit(item: Milestone) {
  selected.value = item;
  actionShow.value = true;
}

function onAction(action: { value: string }) {
  const item = selected.value;
  selected.value = null;
  if (!item) return;
  if (action.value === 'edit') {
    editing.value = item;
    formShow.value = true;
  } else if (action.value === 'delete') {
    void onDelete(item);
  }
}

async function onDelete(item: Milestone) {
  try {
    await showConfirmDialog({
      title: '删除这条里程碑？',
      message: `${item.typeLabel} · ${item.milestoneDate}，删除后无法恢复。`,
      confirmButtonText: '删除',
      confirmButtonColor: '#ee0a24',
    });
    await deleteMilestone(item.id);
    showSuccessToast('已删除');
    await store.loadMilestones();
  } catch {
    /* 取消删除 */
  }
}

async function onRefresh() {
  await store.loadMilestones();
  refreshing.value = false;
}
</script>
