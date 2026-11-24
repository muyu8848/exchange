import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import zhTW from './zh-TW'
import enUS from './en-US'
import Locale from '@wot-ui/ui/locale'
import WotEnUS from '@wot-ui/ui/locale/lang/en-US'
import WotZhTW from '@wot-ui/ui/locale/lang/zh-TW'

Locale.add({ 'en-US': WotEnUS, 'zh-TW': WotZhTW })

const messages = {
  'zh-CN': zhCN,
  'zh-TW': zhTW,
  'en-US': enUS,
}

const i18n = createI18n({
  locale: uni.getStorageSync('currentLang') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages,
  legacy: false,
})

const originalT = i18n.global.t.bind(i18n.global) as unknown as (key: string, ...args: unknown[]) => string

/**
 * 展开 {'字面量'}，并替换 {name} / {0} 占位符。
 * - {'|'}：vue-i18n 的字面量插值，用于避免 | 被当成复数分隔符
 * - {name}：命名参数（项目主要用法）
 * - {0}：数字索引参数
 */
function interpolateTemplate(template: string, values?: unknown[] | Record<string, unknown>): string {
  const normalized = template.replace(/\{'([^']*)'\}/g, '$1')

  if (Array.isArray(values)) {
    return normalized.replace(/\{(\d+)\}/g, (_, index) => {
      const v = values[Number(index)]
      return v == null ? `{${index}}` : String(v)
    })
  }

  if (values && typeof values === 'object') {
    return normalized.replace(/\{(\w+)\}/g, (_, key) => {
      const v = (values as Record<string, unknown>)[key]
      return v == null ? `{${key}}` : String(v)
    })
  }

  return normalized
}

/**
 * 包装 t：App / 小程序端的 vue-i18n 运行时编译器被 uni-app 裁成 stub，
 * 带参数的 t(key, params) 会原样返回 {name} 占位符，这里在运行时补一次字符串替换。
 * 官方推荐做法见 https://starter.wot-ui.cn/guide/i18n.html（文档用 {0} 索引，这里扩展到命名参数）。
 * 注：项目内未使用 $t()，故只需包装 composer.t。
 */
i18n.global.t = ((key: string, ...args: unknown[]): string => {
  const result = String(originalT(key, ...args))

  // t(key, { symbol: 'BTC' })
  if (args.length === 1 && args[0] && typeof args[0] === 'object' && !Array.isArray(args[0])) {
    return interpolateTemplate(result, args[0] as Record<string, unknown>)
  }
  // t(key, [a, b])
  if (args.length === 1 && Array.isArray(args[0])) {
    return interpolateTemplate(result, args[0])
  }
  // t(key, a, b)
  if (args.length > 1) {
    return interpolateTemplate(result, args)
  }
  // t(key)：仍需展开 {'|'}
  return interpolateTemplate(result)
}) as typeof i18n.global.t

Locale.use(i18n.global.locale.value)
// App 端 uni.setLocale 会触发应用重启，跳过（详见 hooks/useI18nSync.ts 说明）
// #ifndef APP-PLUS
uni.setLocale(i18n.global.locale.value)
// #endif

export default i18n
