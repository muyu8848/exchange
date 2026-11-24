<script setup lang="ts">
import type { ConfigProviderProps } from '@wot-ui/ui/components/wd-config-provider/types'
import { useAppTheme } from '@/composables/useAppTheme'
import { useManualThemeStore } from '@/store/manualThemeStore'

// theme 统一由 useAppTheme 控制（设置页切换的那套），
// 避免 wot-ui 全局组件（wd-dialog / wd-toast 等）走 manualThemeStore 的默认 dark，
// 导致日间模式下退出登录等弹窗仍显示夜间样式。
const { theme } = useAppTheme()
// 仅取主题色阶（primary1~primary10）注入 wot-ui
const manualThemeStore = useManualThemeStore()
const themeVars = computed(() => manualThemeStore.themeVars)

const buttonConfig: ConfigProviderProps['button'] = {
  size: 'large',
}
</script>

<template>
  <wd-config-provider :theme-vars="themeVars" :theme="theme" :button="buttonConfig" :custom-class="`page-wraper theme-${theme}`">
    <ku-root-view />
    <wd-notify />
    <wd-dialog />
    <wd-toast :z-index="10001" />
    <global-loading />
    <global-toast />
    <global-dialog />
  </wd-config-provider>
</template>
