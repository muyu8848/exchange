<script lang="ts" setup>
import { watch } from 'vue'
import { applyNavigationBar, useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()

onMounted(() => {
  applyNavigationBar(theme.value)
})

watch(theme, value => applyNavigationBar(value))
</script>

<script lang="ts">
export default {
  options: {
    addGlobalClass: true,
    virtualHost: true,
    styleIsolation: 'shared',
  },
}
</script>

<template>
  <view class="app-layout" :class="`theme-${theme}`">
    <slot />
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.app-layout {
  min-height: 100vh;
  background: $page-bg;
  color: $text-primary;
}
</style>
