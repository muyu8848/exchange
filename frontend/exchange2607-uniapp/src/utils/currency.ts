import type { CurrencyOption } from '@/api/types'
import { commonApi } from '@/api/modules'
import { apiData } from '@/api/http'

let cachedCurrencies: CurrencyOption[] | null = null

export async function loadAllCurrencies(force = false) {
  if (!cachedCurrencies || force) {
    try {
      cachedCurrencies = await apiData(commonApi.findAllCurrency()) || []
    }
    catch {
      cachedCurrencies = []
    }
  }
  return cachedCurrencies
}

export function getAllCurrencies() {
  return cachedCurrencies || []
}

export function currencyLogo(code: string, currencies?: CurrencyOption[]) {
  const matched = (currencies || cachedCurrencies || []).find(item => item.currencyCode === code)
  return matched?.currencyLogo || ''
}

export function currencyName(code: string, fallback = '') {
  const matched = cachedCurrencies?.find(item => item.currencyCode === code)
  return matched?.currencyName || fallback || code
}

export function findCurrency(code: string, currencies?: CurrencyOption[]) {
  const list = currencies || cachedCurrencies || []
  return list.find(item => item.currencyCode === code)
}

export function currencyScale(code: string, fallback = 2, currencies?: CurrencyOption[]) {
  const matched = findCurrency(code, currencies)
  return matched?.scale ?? fallback
}
