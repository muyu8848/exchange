<template>
  <t-icon class="copy-icon" name="file-copy" @click="handleCopy" />
</template>

<script setup lang="ts">
import { copyToClipboard } from '@/utils/clipboard';

const props = defineProps<{
  text?: string;
}>();

const handleCopy = () => {
  copyToClipboard(props.text || '');
};
</script>

<style lang="less" scoped>
.copy-icon {
  margin-left: 4px;
  cursor: pointer;
  color: var(--td-brand-color);
}
</style>
