import { ref, computed } from 'vue'
import { spotSymbolApi, perpetualApi, contractSymbolApi } from '@/api/modules'
import { apiData } from '@/api/http'

export type MarketCategory = 'spot' | 'perp' | 'second'

export interface MarketPair {
  symbolCode: string
  base: string
  quote: string
  baseCurrencyCode?: string
  closePrice?: string
  chg?: string
}

function splitSymbol(code?: string) {
  if (!code) return { base: '', quote: '' }
  const parts = code.split(/[-/]/)
  return { base: parts[0] || code, quote: parts[1] || '' }
}

interface RawSymbol {
  symbolCode?: string
  baseCurrencyCode?: string
  closePrice?: string
  chg?: string
}

export function useMarkets() {
  const activeTab = ref<MarketCategory>('spot')
  const keyword = ref('')
  const loading = ref(false)
  const items = ref<MarketPair[]>([])
  const cache = new Map<MarketCategory, MarketPair[]>()

  const activeSubTab = ref<'hot' | 'gain' | 'loss'>('hot')

  async function fetchRaw(category: MarketCategory): Promise<RawSymbol[]> {
    if (category === 'spot') return apiData(spotSymbolApi.findEnableSymbol())
    if (category === 'perp') return apiData(perpetualApi.findEnableSymbols())
    return apiData(contractSymbolApi.findEnableSymbol())
  }

  function mapRaw(raw: RawSymbol[]): MarketPair[] {
    return (raw || []).map((it) => {
      const { base, quote } = splitSymbol(it.symbolCode)
      return {
        symbolCode: it.symbolCode || '',
        base,
        quote,
        baseCurrencyCode: it.baseCurrencyCode,
        closePrice: it.closePrice,
        chg: it.chg,
      } as MarketPair
    })
  }

  async function load(category: MarketCategory, force = false) {
    const cached = cache.get(category)
    if (cached && !force) {
      items.value = cached
      return
    }
    loading.value = true
    try {
      const raw = await fetchRaw(category)
      const list = mapRaw(raw)
      cache.set(category, list)
      items.value = list
    }
    catch {
      items.value = cache.get(category) || []
    }
    finally {
      loading.value = false
    }
  }

  // 轮询：就地合并最新价/涨跌，不替换数组，避免整列重渲、跳滚动、丢排序
  async function refreshPrices(category: MarketCategory) {
    try {
      const raw = await fetchRaw(category)
      if (category !== activeTab.value) return // 已切换 tab，丢弃过期数据
      const updates = new Map<string, { closePrice?: string; chg?: string }>()
      for (const it of (raw || [])) {
        if (it.symbolCode) updates.set(it.symbolCode, { closePrice: it.closePrice, chg: it.chg })
      }
      for (const item of items.value) {
        const u = updates.get(item.symbolCode)
        if (!u) continue
        if (u.closePrice !== undefined && u.closePrice !== item.closePrice) item.closePrice = u.closePrice
        if (u.chg !== undefined && u.chg !== item.chg) item.chg = u.chg
      }
    }
    catch {
      // 轮询失败静默忽略，下一个周期继续
    }
  }

  let pollTimer: ReturnType<typeof setInterval> | null = null
  function startPolling(intervalMs = 3000) {
    stopPolling()
    pollTimer = setInterval(() => {
      refreshPrices(activeTab.value)
    }, intervalMs)
  }
  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function switchTab(category: MarketCategory) {
    activeTab.value = category
    load(category)
  }

  function switchSubTab(tab: 'hot' | 'gain' | 'loss') {
    activeSubTab.value = tab
  }

  const displayList = computed(() => {
    const kw = keyword.value.trim().toLowerCase()
    let list = items.value
    if (kw) {
      list = list.filter(
        it =>
          it.symbolCode.toLowerCase().includes(kw)
          || it.base.toLowerCase().includes(kw)
          || it.quote.toLowerCase().includes(kw),
      )
    }
    if (activeSubTab.value === 'gain') {
      return [...list].sort(
        (a, b) => (parseFloat(b.chg || '') || 0) - (parseFloat(a.chg || '') || 0),
      )
    }
    if (activeSubTab.value === 'loss') {
      return [...list].sort(
        (a, b) => (parseFloat(a.chg || '') || 0) - (parseFloat(b.chg || '') || 0),
      )
    }
    return list
  })

  return {
    activeTab,
    keyword,
    loading,
    displayList,
    activeSubTab,
    load,
    switchTab,
    switchSubTab,
    startPolling,
    stopPolling,
  }
}
