<template>
  <div v-if="account" class="payment-account-cell">
    <t-image-viewer v-if="imageLines.length > 0" v-model:visible="viewerVisible" :images="previewImages" />
    <t-tag v-if="showType && typeLabel" size="small" variant="light" theme="primary" class="payment-account-cell__tag">{{ typeLabel }}</t-tag>
    <div v-if="props.showRealName && account.realName" class="payment-account-cell__line">
      <span class="payment-account-cell__label">姓名</span>
      <span class="payment-account-cell__value">{{ account.realName }}</span>
      <copy-icon-button v-if="copyName" :text="account.realName" />
    </div>
    <div v-for="(line, index) in textLines" :key="index" class="payment-account-cell__line">
      <span class="payment-account-cell__label">{{ line.label }}</span>
      <span class="payment-account-cell__value">{{ line.value }}</span>
      <copy-icon-button v-if="line.keyField" :text="line.value" />
    </div>
    <div v-for="(img, index) in imageLines" :key="`img-${index}`" class="payment-account-cell__line">
      <span class="payment-account-cell__label">{{ img.label }}</span>
      <img :src="img.value" class="payment-account-cell__thumb" alt="" @click="openPreview(img.value)" />
    </div>
  </div>
  <span v-else class="payment-account-cell__empty">-</span>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import { resolvePaymentAccount, type PaymentAccountVO } from '@/utils/paymentAccount';

const props = withDefaults(
  defineProps<{
    info?: unknown;
    typeName?: string;
    showType?: boolean;
    /** 是否显示姓名行，默认 true */
    showRealName?: boolean;
    /** 姓名行是否显示复制按钮 */
    copyName?: boolean;
  }>(),
  {
    showType: false,
    showRealName: true,
    copyName: false,
  },
);

const account = computed<PaymentAccountVO | null>(() => resolvePaymentAccount(props.info));

const typeLabel = computed(
  () =>
    props.typeName ||
    account.value?.paymentAccountTypeName ||
    account.value?.paymentAccountType?.name ||
    account.value?.paymentAccountTypeCode ||
    '',
);

const textLines = computed(() => {
  const lines = account.value?.fieldLines;
  if (!Array.isArray(lines)) {
    return [];
  }
  return lines.filter((line) => line.type !== 'image');
});

const imageLines = computed(() => {
  const lines = account.value?.fieldLines;
  if (!Array.isArray(lines)) {
    return [];
  }
  return lines.filter((line) => line.type === 'image');
});

/** 图片预览：预览图数组 = 当前账户所有图片字段，点击哪张就从哪张开始 */
const previewImages = computed(() => imageLines.value.map((img) => img.value));

const viewerVisible = ref(false);

const openPreview = (_url: string) => {
  viewerVisible.value = true;
};
</script>

<style lang="less" scoped>
.payment-account-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
  font-size: 13px;
}

.payment-account-cell__tag {
  align-self: flex-start;
}

.payment-account-cell__line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.payment-account-cell__label {
  flex-shrink: 0;
  min-width: 70px;
  color: var(--td-text-color-secondary);
}

.payment-account-cell__value {
  flex: 1;
  min-width: 0;
  font-weight: 500;
  word-break: break-all;
}

.payment-account-cell__thumb {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 4px;
  border: 1px solid var(--td-component-border);
  object-fit: cover;
  cursor: pointer;
}

.payment-account-cell__empty {
  color: var(--td-text-color-placeholder);
}
</style>
