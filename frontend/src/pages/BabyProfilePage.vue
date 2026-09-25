<template>
  <div>
    <header class="page-head">
      <h1>宝宝档案</h1>
      <p>管理宝宝基础信息，查看最近一次里程碑</p>
    </header>

    <BabySwitcher />

    <section v-if="currentBaby" class="card profile-card">
      <h2>{{ currentBaby.name }}</h2>
      <van-cell-group inset :border="false">
        <van-cell title="出生日期" :value="formatDate(currentBaby.birthday)" />
        <van-cell title="当前月龄" :value="ageText(currentBaby.birthday, today)" />
        <van-cell title="血型" :value="currentBaby.bloodType || '未填写'" />
        <van-cell title="出生身高" :value="currentBaby.initialHeight ? currentBaby.initialHeight + ' cm' : '未填写'" />
        <van-cell title="出生体重" :value="currentBaby.initialWeight ? currentBaby.initialWeight + ' kg' : '未填写'" />
      </van-cell-group>
    </section>

    <LatestMilestone v-if="currentBaby" :baby="currentBaby" />

    <section class="card">
      <h2 v-if="hasBabies" class="form-title">再建一个宝宝档案</h2>
      <van-form v-if="!hasBabies || creating" @submit="submitBaby">
        <van-field
          v-model="babyForm.name"
          label="昵称"
          placeholder="宝宝的小名"
          :rules="[{ required: true, message: '请填写昵称' }]"
        />
        <van-field label="出生日期" required>
          <template #input>
            <input v-model="babyForm.birthday" class="date-input" type="date" :max="today" />
          </template>
        </van-field>
        <van-field
          v-model="babyForm.bloodType"
          label="血型"
          placeholder="如 A / B / O / AB（可选）"
        />
        <van-field
          v-model.number="babyForm.initialHeight"
          label="出生身高"
          type="number"
          placeholder="cm（可选）"
        />
        <van-field
          v-model.number="babyForm.initialWeight"
          label="出生体重"
          type="number"
          placeholder="kg（可选）"
        />
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="saving">保存档案</van-button>
        </div>
      </van-form>
      <van-button v-else plain block round @click="creating = true">新建宝宝档案</van-button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, type ComputedRef } from 'vue';
import { showSuccessToast } from 'vant';
import BabySwitcher from '../components/BabySwitcher.vue';
import LatestMilestone from '../components/LatestMilestone.vue';
import { createBaby } from '../api';
import { formatDate, ageText, todayISO } from '../date-utils';
import { useAppStore } from '../store';
import type { Baby } from '../types';

const store = useAppStore();
const currentBaby = store.currentBaby as ComputedRef<Baby | null>;
const today = todayISO();

const creating = ref(false);
const saving = ref(false);
const formError = ref('');
const hasBabies = computed(() => store.state.babies.length > 0);

const emptyForm = () => ({
  name: '',
  birthday: '',
  bloodType: '',
  initialHeight: '' as number | '',
  initialWeight: '' as number | '',
});
const babyForm = reactive(emptyForm());

async function submitBaby() {
  formError.value = '';
  if (!babyForm.birthday) {
    formError.value = '请选择出生日期';
    return;
  }
  if (babyForm.birthday > today) {
    formError.value = '出生日期不能晚于今天';
    return;
  }
  saving.value = true;
  try {
    const baby = await createBaby({
      name: babyForm.name.trim(),
      birthday: babyForm.birthday,
      bloodType: babyForm.bloodType.trim() || undefined,
      initialHeight: babyForm.initialHeight === '' ? undefined : Number(babyForm.initialHeight),
      initialWeight: babyForm.initialWeight === '' ? undefined : Number(babyForm.initialWeight),
    });
    await store.addBaby(baby);
    showSuccessToast('档案已创建');
    Object.assign(babyForm, emptyForm());
    creating.value = false;
  } catch (e) {
    formError.value = (e as Error).message;
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.page-head {
  padding: 8px 4px 10px;
}
.page-head h1 {
  margin: 0;
  font-size: 20px;
}
.page-head p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9c7e72;
}
.profile-card h2 {
  margin: 4px 0 8px;
  font-size: 17px;
}
.profile-card :deep(.van-cell-group--inset) {
  margin: 0 -8px;
}
.form-title {
  margin: 4px 0 10px;
  font-size: 16px;
}
.date-input {
  width: 100%;
  border: 1px solid #e6c9bb;
  border-radius: 6px;
  padding: 7px 10px;
  font-size: 15px;
  color: #3b2b24;
}
.form-error {
  color: #ee0a24;
  font-size: 13px;
  margin: 6px 16px 0;
}
.form-actions {
  padding: 16px 16px 4px;
}
</style>
