export type AppLocale = 'zh-CN' | 'zh-TW' | 'en-US'

export interface LanguageOption {
  /** 语言码，需与 i18n / uni / Wot UI 组件库保持一致（如 zh-CN、en-US） */
  value: AppLocale
  /** 母语名称（如 简体中文 / English），按各自母语显示，不随界面语言翻译 */
  label: string
}

/** 全局唯一语言清单：新增语言只需在此追加一项，并配套 src/locale/<code>.ts 语言包 */
export const LANGUAGES: LanguageOption[] = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'zh-TW', label: '繁體中文' },
  { value: 'en-US', label: 'English' },
]
