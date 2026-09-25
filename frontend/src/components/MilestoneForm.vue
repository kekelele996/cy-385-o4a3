<template>
  <van-popup
    :show="show"
    position="bottom"
    round
    teleport="body"
    :style="{ maxHeight: '90vh' }"
    @update:show="(v: boolean) => !v && onClose()"
  >
    <div class="popup-head">
      <span>{{ editing ? '修改里程碑' : '新增里程碑' }}</span>
      <van-icon name="cross" size="20" @click="onClose" />
    </div>

    <div class="popup-body">
      <van-notice-bar
        v-if="errorMessage"
        wrapable
        :scrollable="false"
        type="danger"
        :text="errorMessage"
      />

      <van-cell-group inset class="form-group">
        <van-field
          label="类型"
          :model-value="typeLabel"
          is-link
          readonly
          required
          placeholder="请选择里程碑类型"
          input-align="right"
          @click="openTypePicker"
        />
        <van-field
          label="实际日期"
          :model-value="form.milestoneDate"
          is-link
          readonly
          required
          placeholder="发生在哪一天"
          :class="{ 'field-error': dateInvalid }"
          @click="openDatePicker"
        />
        <van-field
          label="说明"
          type="textarea"
          rows="2"
          autosize
          maxlength="500"
          show-word-limit
          v-model="form.note"
          placeholder="例如：睡着睡着自己翻过去了"
        />
        <van-field
          label="照片地址"
          v-model="form.photoUrl"
          placeholder="填写已有图片链接（http(s)://）"
          clearable
          @update:model-value="photoBroken = false"
        >
          <template #extra>
            <van-image
              v-if="form.photoUrl"
              :src="form.photoUrl"
              width="48"
              height="48"
              radius="6"
              fit="cover"
              @error="photoBroken = true"
            />
          </template>
        </van-field>
      </van-cell-group>
      <p v-if="photoBroken" class="photo-hint">图片地址暂时无法访问，已保留链接。</p>

      <!-- 同一天同类型已有记录 -->
      <div v-if="existing" class="existing-card">
        <van-icon name="info-o" />
        <div>
          <p class="existing-title">
            {{ existing.milestoneDate }} 的「{{ existing.typeLabel }}」已有记录
          </p>
          <p class="existing-meta">提交于 {{ formatDateTime(existing.createdAt) }}，同一天同类型只保留最早提交的一条。</p>
          <p v-if="existing.note" class="existing-note">说明：{{ existing.note }}</p>
        </div>
      </div>
    </div>

    <div class="popup-foot">
      <van-button block plain type="primary" @click="onClose">取消</van-button>
      <van-button block type="primary" :loading="submitting" @click="onSubmit">
        {{ editing ? '保存修改' : '保存' }}
      </van-button>
    </div>

    <!-- 类型选择 -->
    <van-popup v-model:show="typePickerShow" position="bottom" round teleport="body">
      <van-picker
        title="选择里程碑类型"
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="typePickerShow = false"
      />
    </van-popup>

    <!-- 日期选择 -->
    <van-popup v-model:show="datePickerShow" position="bottom" round teleport="body">
      <van-date-picker
        title="实际发生日期"
        v-model="dateValues"
        :min-date="minDate"
        :max-date="maxDate"
        :columns-type="['year', 'month', 'day']"
        @confirm="onDateConfirm"
        @cancel="datePickerShow = false"
      />
    </van-popup>
  </van-popup>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { showSuccessToast } from 'vant';
import type { Milestone, MilestoneFormData } from '../types';
import { useStore } from '../store';
import { createMilestone, updateMilestone } from '../api';
import { formatDateTime, today } from '../utils/date';

const props = defineProps<{
  show: boolean;
  editing?: Milestone | null;
}>();
const emit = defineEmits<{
  (e: 'update:show', value: boolean): void;
  (e: 'saved'): void;
}>();

const store = useStore();

const form = reactive<MilestoneFormData>({
  babyId: 0,
  typeCode: '',
  milestoneDate: '',
  note: '',
  photoUrl: '',
});

const typePickerShow = ref(false);
const datePickerShow = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const dateInvalid = ref(false);
const photoBroken = ref(false);
const existing = ref<Milestone | null>(null);
const dateValues = ref<string[]>([]);

const minDate = computed(() => {
  const birthday = store.activeBaby.value?.birthday;
  return birthday ? new Date(birthday + 'T00:00:00') : new Date(2000, 0, 1);
});
const maxDate = new Date();

const typeColumns = computed(() =>
  store.types.value.map((t) => ({ text: t.label, value: t.code }))
);
const typeLabel = computed(
  () => store.types.value.find((t) => t.code === form.typeCode)?.label ?? ''
);

watch(
  () => props.show,
  (visible) => {
    if (!visible) return;
    photoBroken.value = false;
    errorMessage.value = '';
    dateInvalid.value = false;
    existing.value = null;
    form.babyId = store.activeBaby.value?.id ?? 0;
    if (props.editing) {
      form.typeCode = props.editing.typeCode;
      form.milestoneDate = props.editing.milestoneDate;
      form.note = props.editing.note ?? '';
      form.photoUrl = props.editing.photoUrl ?? '';
    } else {
      form.typeCode = '';
      form.milestoneDate = today();
      form.note = '';
      form.photoUrl = '';
    }
    dateValues.value = form.milestoneDate.split('-');
  }
);

function openTypePicker() {
  errorMessage.value = '';
  typePickerShow.value = true;
}

function onTypeConfirm({ selectedOptions }: { selectedOptions: Array<{ text: string; value: string }> }) {
  form.typeCode = selectedOptions[0]?.value ?? '';
  typePickerShow.value = false;
}

function openDatePicker() {
  errorMessage.value = '';
  dateInvalid.value = false;
  dateValues.value = form.milestoneDate ? form.milestoneDate.split('-') : today().split('-');
  datePickerShow.value = true;
}

function onDateConfirm({ selectedValues }: { selectedValues: string[] }) {
  form.milestoneDate = selectedValues.join('-');
  datePickerShow.value = false;
}

async function onSubmit() {
  errorMessage.value = '';
  dateInvalid.value = false;
  existing.value = null;

  if (!form.babyId) {
    errorMessage.value = '请先在「宝宝」页创建宝宝档案。';
    return;
  }
  if (!form.typeCode) {
    errorMessage.value = '请选择里程碑类型。';
    return;
  }
  if (!form.milestoneDate) {
    errorMessage.value = '请选择实际发生日期。';
    dateInvalid.value = true;
    return;
  }

  const birthday = store.activeBaby.value?.birthday;
  if (birthday && form.milestoneDate < birthday) {
    dateInvalid.value = true;
    errorMessage.value = `日期 ${form.milestoneDate} 早于宝宝出生日 ${birthday}，时间不合适，未保存。`;
    return;
  }
  if (form.milestoneDate > today()) {
    dateInvalid.value = true;
    errorMessage.value = `日期 ${form.milestoneDate} 晚于今天，时间不合适，未保存。`;
    return;
  }

  submitting.value = true;
  try {
    if (props.editing) {
      await updateMilestone(props.editing.id, { ...form });
      showSuccessToast('已保存，时间线已重新排序');
    } else {
      await createMilestone({ ...form });
      showSuccessToast('里程碑已记录');
    }
    emit('saved');
    emit('update:show', false);
  } catch (err) {
    const e = err as { code?: string; message: string; data?: unknown };
    errorMessage.value = e.message || '保存失败，请稍后重试。';
    if (
      e.code === 'MILESTONE_DATE_BEFORE_BIRTH' ||
      e.code === 'MILESTONE_DATE_AFTER_TODAY'
    ) {
      dateInvalid.value = true;
    }
    if (e.code === 'MILESTONE_DUPLICATE' && e.data) {
      existing.value = e.data as Milestone;
    }
  } finally {
    submitting.value = false;
  }
}

function onClose() {
  emit('update:show', false);
}
</script>
