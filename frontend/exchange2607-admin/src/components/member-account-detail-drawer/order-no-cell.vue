<script setup lang="ts">
import CopyIconButton from '@/components/copy-icon-button/index.vue';

defineProps<{
  value?: string | null;
}>();
</script>

<template>
  <div class="order-no">
    <span class="order-no__text" :title="value || ''">{{ value || '—' }}</span>
    <CopyIconButton v-if="value" :text="value" />
  </div>
</template>

<style lang="less" scoped>
.order-no {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.order-no__text {
  overflow: hidden;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
