import { useI18n } from 'vue-i18n'

/**
 * 字典项文案统一解析：优先用 i18n（dictItem.<dictCode>.<code>），未命中时回落到后端字典项名称，
 * 再不行显示原始 code。命名对应后端 DictHolder.getDictItemName(dictCode, code)。
 * 新增字典项只需在语言包加一条，后端无需改动。
 */
export function useDictItemText() {
  const { t } = useI18n()
  return (
    dictCode: string,
    code: string | number | undefined,
    fallbackName?: string,
  ): string => {
    if (code === undefined || code === null || code === '') {
      return fallbackName ?? ''
    }
    const key = `dictItem.${dictCode}.${code}`
    const text = t(key)
    return text && text !== key ? text : (fallbackName ?? String(code))
  }
}
