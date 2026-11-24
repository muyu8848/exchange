import { ref } from 'vue'

export type AppTheme = 'dark' | 'light'

const STORAGE_KEY = 'app_theme'

const theme = ref<AppTheme>('light')

function applyPageBackground(mode: AppTheme) {
  const color = mode === 'dark' ? '#030304' : '#f5f6f8'
  // 注意：uni.setBackgroundColor 是微信小程序专属 API，App 端不存在（调用会抛 TypeError）。
  // App 端页面背景由 layouts 的 app-layout 通过 CSS 变量跟随主题，无需运行时干预。
  // #ifdef H5
  document.documentElement.style.backgroundColor = color
  document.body.style.backgroundColor = color
  document.documentElement.classList.toggle('theme-dark', mode === 'dark')
  document.documentElement.classList.toggle('theme-light', mode === 'light')
  // #endif
}

export function applyNavigationBar(mode: AppTheme) {
  const backgroundColor = mode === 'dark' ? '#030304' : '#ffffff'
  const frontColor = mode === 'dark' ? '#ffffff' : '#000000'
  uni.setNavigationBarColor({
    frontColor,
    backgroundColor,
    fail: () => {},
  })
  // #ifdef H5
  const head = document.querySelector('.uni-page-head') as HTMLElement | null
  if (head) {
    head.style.backgroundColor = backgroundColor
    head.style.color = frontColor
  }
  // #endif
  // #ifdef APP-PLUS
  // @ts-ignore
  plus.navigator.setStatusBarStyle(mode === 'dark' ? 'light' : 'dark')
  // #endif
}

function applyTabBar(mode: AppTheme) {
  uni.setTabBarStyle({
    color: mode === 'dark' ? '#9a9690' : '#8a8a90',
    selectedColor: mode === 'dark' ? '#c9b48a' : '#a8894f',
    backgroundColor: mode === 'dark' ? '#0a0a0c' : '#ffffff',
    borderStyle: mode === 'dark' ? 'black' : 'white',
    fail: () => {},
  })
}

export function setAppTheme(mode: AppTheme) {
  theme.value = mode
  uni.setStorageSync(STORAGE_KEY, mode)
  applyPageBackground(mode)
  applyNavigationBar(mode)
  applyTabBar(mode)
}

export function initAppTheme() {
  const saved = (uni.getStorageSync(STORAGE_KEY) as AppTheme) || 'light'
  setAppTheme(saved)
}

export function useAppTheme() {
  return {
    theme,
    setTheme: setAppTheme,
    initTheme: initAppTheme,
  }
}
