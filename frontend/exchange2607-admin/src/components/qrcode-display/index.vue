<template>
  <div ref="rootRef" class="qrcode-display">
    <t-qrcode :value="text" :size="size || 200" type="canvas" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = withDefaults(
  defineProps<{
    text: string;
    size?: number;
  }>(),
  {
    size: 200,
  },
);

const rootRef = ref<HTMLDivElement | null>(null);

const download = (fileName = 'qrcode.png') => {
  const canvas = rootRef.value?.querySelector('canvas');
  if (!canvas) {
    return;
  }
  const link = document.createElement('a');
  link.download = fileName;
  link.href = canvas.toDataURL('image/png');
  link.click();
};

defineExpose({
  download,
});
</script>

<style lang="less" scoped>
.qrcode-display {
  display: flex;
  justify-content: center;
}
</style>
