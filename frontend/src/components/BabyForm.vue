<template>
  <van-popup
    :show="show"
    position="bottom"
    round
    teleport="body"
    :style="{ maxHeight: '90vh' }"
    @update:show="(v: boolean) => !v && emit('update:show', false)"
  >
    <div class="popup-head">
      <span>新增宝宝档案</span>
      <van-icon name="cross" size="20" @click="emit('update:show', false)" />
    </div>

    <div class="popup-body">
      <van-notice-bar v-if="errorMessage" wrapable :scrollable="false" type="danger" :text="errorMessage" />
      <van-cell-group inset class="form-group">
        <van-field label="姓名" v-model="form.name" placeholder="宝宝的小名" required />
        <van-field
          label="出生日期"
          :model-value="form.birthday"
          is-link
          readonly
          required
          placeholder="选择出生日期"
          @click="birthdayPickerShow = true"
        />
        <van-field
          label="血型"
          :model-value="bloodLabel"
          is-link
          readonly
          placeholder="可选"
          @click="bloodPickerShow = true"
        />
        <van-field
          label="出生身高(cm)"
          v-model="form.initialHeight"
          type="number"
          placeholder="可选"
        />
        <van-field
          label="出生体重(kg)"
          v-model="form.initialWeight"
          type="number"
          placeholder="可选"
        />
      </van-cell-group>
    </div>

    <div class="popup-foot">
      <van-button block plain type="primary" @click="emit('update:show', false)">取消</van-button>
      <van-button block type="primary" :loading="submitting" @click="onSubmit">保存</van-button>
    </div>

    <van-popup v-model:show="birthdayPickerShow" position="bottom" round teleport="body">
      <van-date-picker
        title="出生日期"
        v-model="birthdayValues"
        :min-date="new Date(2000, 0, 1)"
        :max-date="new Date()"
        @confirm="onBirthdayConfirm"
        @cancel="birthdayPickerShow = false"
      />
    </van-popup>

    <van-popup v-model:show="bloodPickerShow" position="bottom" round teleport="body">
      <van-picker
        title="血型"
        :columns="bloodColumns"
        @confirm="onBloodConfirm"
        @cancel="bloodPickerShow = false"
      />
    </van-popup>
  </van-popup>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { showSuccessToast } from 'vant';
import { useStore } from '../store';
import { today } from '../utils/date';

const props = defineProps<{ show: boolean }>();
const emit = defineEmits<{ (e: 'update:show', value: boolean): void }>();

const store = useStore();

const form = reactive({
  name: '',
  birthday: '',
  bloodType: '',
  initialHeight: '',
  initialWeight: '',
});
const birthdayPickerShow = ref(false);
const bloodPickerShow = ref(false);
const birthdayValues = ref<string[]>(today().split('-'));
const submitting = ref(false);
const errorMessage = ref('');

const bloodColumns = [
  { text: 'A型', value: 'A型' },
  { text: 'B型', value: 'B型' },
  { text: 'AB型', value: 'AB型' },
  { text: 'O型', value: 'O型' },
];
const bloodLabel = computed(() => form.bloodType || '');

watch(
  () => props.show,
  (visible) => {
    if (!visible) return;
    form.name = '';
    form.birthday = '';
    form.bloodType = '';
    form.initialHeight = '';
    form.initialWeight = '';
    birthdayValues.value = today().split('-');
    errorMessage.value = '';
  }
);

function onBirthdayConfirm({ selectedValues }: { selectedValues: string[] }) {
  form.birthday = selectedValues.join('-');
  birthdayPickerShow.value = false;
}

function onBloodConfirm({ selectedOptions }: { selectedOptions: Array<{ value: string }> }) {
  form.bloodType = selectedOptions[0]?.value ?? '';
  bloodPickerShow.value = false;
}

async function onSubmit() {
  errorMessage.value = '';
  if (!form.name.trim()) {
    errorMessage.value = '请填写宝宝姓名。';
    return;
  }
  if (!form.birthday) {
    errorMessage.value = '请选择出生日期。';
    return;
  }
  if (form.birthday > today()) {
    errorMessage.value = '出生日期不能晚于今天。';
    return;
  }

  submitting.value = true;
  try {
    await store.addBaby({
      name: form.name.trim(),
      birthday: form.birthday,
      bloodType: form.bloodType || null,
      initialHeight: form.initialHeight ? Number(form.initialHeight) : null,
      initialWeight: form.initialWeight ? Number(form.initialWeight) : null,
    });
    showSuccessToast('档案已创建');
    emit('update:show', false);
  } catch (err) {
    errorMessage.value = (err as Error).message || '保存失败，请稍后重试。';
  } finally {
    submitting.value = false;
  }
}
</script>
