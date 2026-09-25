<template>
  <van-popup
    :show="show"
    position="bottom"
    round
    closeable
    :style="{ maxHeight: '90%' }"
    @update:show="(v: boolean) => emit('update:show', v)"
    @close="onClose"
  >
    <div class="form-wrap">
      <h3>{{ editing ? '修改里程碑' : '新增里程碑' }}</h3>

      <van-form @submit="onSubmit">
        <div class="field-label">类型</div>
        <div class="type-grid">
          <button
            v-for="t in types"
            :key="t"
            type="button"
            class="type-chip"
            :class="{ active: form.type === t }"
            @click="form.type = t"
          >
            {{ t }}
          </button>
        </div>
        <p v-if="errors.type" class="field-error">{{ errors.type }}</p>

        <div class="field-label">实际日期</div>
        <input
          v-model="form.milestoneDate"
          class="date-input"
          type="date"
          :min="currentBaby?.birthday"
          :max="today"
        />
        <p class="date-range">
          可选范围：{{ currentBaby ? formatDate(currentBaby.birthday) : '出生日期' }} 至 今天（{{ formatDate(today) }}）
        </p>
        <p v-if="errors.milestoneDate" class="field-error">{{ errors.milestoneDate }}</p>
        <p v-if="serverError" class="field-error server-error">{{ serverError }}</p>

        <van-field
          v-model="form.description"
          label="说明"
          type="textarea"
          rows="2"
          autosize
          maxlength="500"
          show-word-limit
          placeholder="比如：今天突然能从仰卧翻成俯卧啦"
        />

        <van-field
          v-model="form.photoUrl"
          label="照片地址"
          placeholder="粘贴已有图片地址（可选）"
        >
          <template #button>
            <van-image
              v-if="form.photoUrl"
              :src="form.photoUrl"
              width="36"
              height="36"
              radius="6"
              fit="cover"
            />
          </template>
        </van-field>

        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            {{ editing ? '保存修改' : '提交记录' }}
          </van-button>
        </div>
      </van-form>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { showSuccessToast } from 'vant';
import { createMilestone, emitMilestonesChanged, updateMilestone } from '../api';
import { formatDate, todayISO } from '../date-utils';
import { useAppStore } from '../store';
import type { Baby, Milestone } from '../types';

const props = defineProps<{
  show: boolean;
  babyId: number | null;
  babyBirthday?: string;
  editing?: Milestone | null;
  types: string[];
}>();

const emit = defineEmits<{
  'update:show': [value: boolean];
  saved: [];
}>();

const store = useAppStore();
const today = todayISO();
const currentBaby = computed<Baby | null>(
  () => store.state.babies.find((b) => b.id === props.babyId) ?? null,
);

const form = reactive({
  type: '',
  milestoneDate: todayISO(),
  description: '',
  photoUrl: '',
});

const errors = reactive<{ type?: string; milestoneDate?: string }>({});
const serverError = ref('');
const submitting = ref(false);

const editing = ref<Milestone | null>(null);
watch(
  () => props.show,
  (show) => {
    if (!show) return;
    serverError.value = '';
    errors.type = '';
    errors.milestoneDate = '';
    editing.value = props.editing ?? null;
    if (editing.value) {
      form.type = editing.value.type;
      form.milestoneDate = editing.value.milestoneDate;
      form.description = editing.value.description ?? '';
      form.photoUrl = editing.value.photoUrl ?? '';
    } else {
      form.type = '';
      form.milestoneDate = todayISO();
      form.description = '';
      form.photoUrl = '';
    }
  },
);

function localValidate(): boolean {
  errors.type = '';
  errors.milestoneDate = '';
  serverError.value = '';
  let ok = true;
  if (!form.type) {
    errors.type = '请选择里程碑类型';
    ok = false;
  }
  if (!form.milestoneDate) {
    errors.milestoneDate = '请选择实际日期';
    ok = false;
  } else {
    const b = currentBaby.value;
    if (b && form.milestoneDate < b.birthday) {
      errors.milestoneDate = `实际日期不能早于宝宝出生日期（${formatDate(b.birthday)}），未保存`;
      ok = false;
    } else if (form.milestoneDate > today) {
      errors.milestoneDate = '实际日期不能晚于今天，未保存';
      ok = false;
    }
  }
  return ok;
}

async function onSubmit() {
  if (!props.babyId || !localValidate()) return;
  submitting.value = true;
  const payload = {
    babyId: props.babyId,
    type: form.type,
    milestoneDate: form.milestoneDate,
    description: form.description.trim(),
    photoUrl: form.photoUrl.trim(),
  };
  try {
    if (editing.value) {
      await updateMilestone(editing.value.id, payload);
      showSuccessToast('已保存，时间线已重排');
      emitMilestonesChanged(props.babyId);
      emit('saved');
      emit('update:show', false);
    } else {
      const result = await createMilestone(payload);
      emitMilestonesChanged(props.babyId);
      if (result.saved) {
        showSuccessToast('记录成功');
        emit('saved');
        emit('update:show', false);
      } else {
        // 同一天同类型只留最早提交的：后提交的人在这里看到已有记录
        serverError.value =
          `${result.message}（已有记录提交时间：${result.existing?.createdAt?.replace('T', ' ').slice(0, 16)}），本次未保存`;
      }
    }
  } catch (e) {
    serverError.value = (e as Error).message;
  } finally {
    submitting.value = false;
  }
}

function onClose() {
  emit('update:show', false);
}
</script>

<style scoped>
.form-wrap {
  padding: 20px 16px 28px;
}
.form-wrap h3 {
  margin: 0 0 12px;
  text-align: center;
}
.field-label {
  font-size: 14px;
  color: #5d4037;
  padding: 10px 16px 6px;
}
.type-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 12px;
}
.type-chip {
  border: 1px solid #e6c9bb;
  background: #fffaf6;
  color: #7a5548;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
}
.type-chip.active {
  background: #ff8a65;
  border-color: #ff8a65;
  color: #fff;
}
.date-input {
  display: block;
  width: calc(100% - 32px);
  margin: 0 16px;
  padding: 10px 12px;
  border: 1px solid #e6c9bb;
  border-radius: 8px;
  font-size: 15px;
  color: #3b2b24;
  background: #fff;
  box-sizing: border-box;
}
.date-range {
  margin: 6px 16px 0;
  font-size: 12px;
  color: #a58a7e;
}
.field-error {
  margin: 6px 16px 0;
  font-size: 13px;
  color: #ee0a24;
}
.server-error {
  background: #fef0f0;
  border-radius: 6px;
  padding: 8px 10px;
  line-height: 1.5;
}
.form-actions {
  padding: 18px 16px 0;
}
</style>
