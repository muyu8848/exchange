import { ref } from 'vue'
import { spotSymbolApi, perpetualApi } from '@/api/modules'
import { apiData } from '@/api/http'
import type { SpotSymbolItem, PerpetualSymbolItem } from '@/api/types'

// 全局单例：现货/永续启用交易对列表 + 上次交易对持久化
// 1) 避免行情页、选对弹层、交易 tab 各自重复拉取
// 2) 为「无 symbol 直接打开」的交易 tab 提供默认交易对
const spotSymbols = ref<SpotSymbolItem[]>([])
const perpSymbols = ref<PerpetualSymbolItem[]>([])

const lastSpotSymbol = ref<string>(uni.getStorageSync('lastSpotSymbol') || '')
const lastPerpSymbol = ref<string>(uni.getStorageSync('lastPerpSymbol') || '')

// 从行情/选对/订单详情等跳转到交易 tab 时，tabbar 页无法用 query 传参，
// 故用「待切换 symbol」暂存，由交易页 onShow 消费（消费后清空，避免重复触发）
const pendingSpotSymbol = ref<string>('')
const pendingPerpSymbol = ref<string>('')

function requestSpot(code: string) { pendingSpotSymbol.value = code }
function requestPerp(code: string) { pendingPerpSymbol.value = code }
function consumeSpot() { const c = pendingSpotSymbol.value; pendingSpotSymbol.value = ''; return c }
function consumePerp() { const c = pendingPerpSymbol.value; pendingPerpSymbol.value = ''; return c }

let spotLoading = false
let perpLoading = false

function persistSpot(code: string) {
  lastSpotSymbol.value = code
  uni.setStorageSync('lastSpotSymbol', code)
}

function persistPerp(code: string) {
  lastPerpSymbol.value = code
  uni.setStorageSync('lastPerpSymbol', code)
}

async function loadSpotSymbols(force = false) {
  if (spotLoading) return spotSymbols.value
  if (!force && spotSymbols.value.length) return spotSymbols.value
  spotLoading = true
  try {
    const data = await apiData(spotSymbolApi.findEnableSymbol())
    spotSymbols.value = data || []
  }
  catch {
    // ignore
  }
  finally {
    spotLoading = false
  }
  return spotSymbols.value
}

async function loadPerpSymbols(force = false) {
  if (perpLoading) return perpSymbols.value
  if (!force && perpSymbols.value.length) return perpSymbols.value
  perpLoading = true
  try {
    const data = await apiData(perpetualApi.findEnableSymbols())
    perpSymbols.value = data || []
  }
  catch {
    // ignore
  }
  finally {
    perpLoading = false
  }
  return perpSymbols.value
}

function resolveDefault(list: { symbolCode: string }[], last: string, fallback: string) {
  if (last && list.some(item => item.symbolCode === last)) return last
  return list[0]?.symbolCode || fallback
}

export function useSymbolStore() {
  return {
    spotSymbols,
    perpSymbols,
    lastSpotSymbol,
    lastPerpSymbol,
    requestSpot,
    requestPerp,
    consumeSpot,
    consumePerp,
    loadSpotSymbols,
    loadPerpSymbols,
    getDefaultSpot: () => resolveDefault(spotSymbols.value, lastSpotSymbol.value, 'BTC-USDT'),
    getDefaultPerp: () => resolveDefault(perpSymbols.value, lastPerpSymbol.value, 'BTC-USDT'),
    setLastSpot: persistSpot,
    setLastPerp: persistPerp,
  }
}
