import { computed } from 'vue'
import Locale from '@wot-ui/ui/locale'
import i18n from '../locale'
import { LANGUAGES } from '@/locale/languages'
import type { AppLocale } from '@/locale/languages'

const SUPPORTED_LOCALES = LANGUAGES.map(l => l.value)

export function setLocale(locale: string, syncComponentLib = true) {
  const next: AppLocale = (SUPPORTED_LOCALES as string[]).includes(locale)
    ? (locale as AppLocale)
    : 'zh-CN'
  // App 端 uni.setLocale 会触发应用重启（原生层刷新导航栏/tabbar 等资源），
  // 项目使用自定义导航栏 + wot-ui 组件，不依赖原生文案，跳过以免切语言时重启
  // #ifndef APP-PLUS
  uni.setLocale(next)
  // #endif
  i18n.global.locale.value = next
  uni.setStorageSync('currentLang', next)
  if (syncComponentLib) {
    Locale.use(next)
  }
  return next
}

export function useI18nSync() {
  const currentLang = computed(() => i18n.global.locale.value as AppLocale)
  return { currentLang, setLocale }
}
