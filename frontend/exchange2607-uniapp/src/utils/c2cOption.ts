import type { C2cFiatOption, C2cSymbolOption } from '@/api/types'
import { apiData } from '@/api/http'
import { commonApi, c2cApi } from '@/api/modules'

/**
 * C2C 元数据加载（仿 utils/currency.ts 的 loadAllCurrencies 范式）。
 *
 * 法币列表是全局低频配置，App 启动时预取并常驻内存缓存；
 * 交易对列表随运营上下架变化，页面打开时实时获取（不缓存），保证新鲜度。
 */

let cachedFiats: C2cFiatOption[] | null = null
let fiatPromise: Promise<C2cFiatOption[]> | null = null

/** 启用的 C2C 法币：已缓存则直接返回；force 用于页面下拉刷新等场景强刷 */
export async function loadC2cOptionFiats(force = false): Promise<C2cFiatOption[]> {
  if (cachedFiats && !force) {
    return cachedFiats
  }
  if (!fiatPromise) {
    fiatPromise = apiData(commonApi.findAllC2cFiat())
      .then((list) => {
        cachedFiats = list || []
        return cachedFiats
      })
      .catch((err) => {
        cachedFiats = null
        throw err
      })
      .finally(() => {
        fiatPromise = null
      })
  }
  return fiatPromise
}

/** 同步读取已缓存的法币列表（未加载时为空数组） */
export function getC2cOptionFiats(): C2cFiatOption[] {
  return cachedFiats || []
}

/** 法币符号（¥/$/€），元数据缺失时回退法币代码；适用于金额符号展示 */
export function c2cFiatSymbolOf(fiatCode?: string): string {
  if (!fiatCode) {
    return ''
  }
  return getC2cOptionFiats().find(f => f.fiatCode === fiatCode)?.fiatSymbol || fiatCode
}

/** 法币金额精度（小数位数），未配置默认 2；适用于金额输入/归一 */
export function c2cFiatScaleOf(fiatCode?: string, fallback = 2): number {
  if (!fiatCode) {
    return fallback
  }
  return getC2cOptionFiats().find(f => f.fiatCode === fiatCode)?.fiatScale ?? fallback
}

/** 启用的 C2C 交易对：页面打开时实时获取，不做全局缓存（保证运营上下架后的新鲜度） */
export function loadC2cOptionSymbols(): Promise<C2cSymbolOption[]> {
  return apiData(c2cApi.findAllEnabledSymbol()).then(list => list || [])
}