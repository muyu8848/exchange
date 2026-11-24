<script setup lang="ts">
import type { SpotSymbolItem } from '@/api/types'
import { spotSymbolApi, klineApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { API_BASE_URL } from '@/config'
import { formatSymbol } from '@/utils/symbol'
import KLineChart from '@/components/KLineChart/index.vue'
import { useAppTheme } from '@/composables/useAppTheme'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'spotChart',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    disableScroll: false,
  },
})

const router = useRouter()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const { theme } = useAppTheme()

const symbolCode = ref('')
const symbolInfo = ref<SpotSymbolItem | null>(null)
const initialized = ref(false)
const priceScale = ref(2)
const amountScale = ref(4)
const chartPeriod = ref('1D')
const apiBaseUrl = API_BASE_URL
const tokenName = uni.getStorageSync('tokenName') || ''
const tokenValue = uni.getStorageSync('tokenValue') || ''

let priceTimer: ReturnType<typeof setInterval> | null = null
let orderBookTimer: ReturnType<typeof setInterval> | null = null

// 盘口数据（与交易页口径一致：各档挂单量占比深度条）
interface OrderBookLevel { price: string, size: string }
interface OrderBookData { asks: OrderBookLevel[], bids: OrderBookLevel[], ts: string }
const orderBook = ref<OrderBookData | null>(null)

const chartPeriods = computed(() => [
  { label: t('spot.period1m'), value: '1m' },
  { label: t('spot.period15m'), value: '15m' },
  { label: t('spot.period1H'), value: '1H' },
  { label: t('spot.period4H'), value: '4H' },
  { label: t('spot.period1D'), value: '1D' },
])

const priceColor = computed(() => {
  if (!symbolInfo.value?.chg) return ''
  const val = parseFloat(symbolInfo.value.chg)
  if (val > 0) return 'sc-price--up'
  if (val < 0) return 'sc-price--down'
  return ''
})

const chgText = computed(() => {
  if (!symbolInfo.value?.chg) return '0.00%'
  const val = parseFloat(symbolInfo.value.chg)
  const sign = val > 0 ? '+' : ''
  return `${sign}${val.toFixed(2)}%`
})

const displaySymbol = computed(() => formatSymbol(symbolCode.value))

function formatPrice(val?: string | number) {
  if (val === undefined || val === null || val === '') return '—'
  const num = typeof val === 'string' ? parseFloat(val) : val
  if (isNaN(num)) return '—'
  const fixed = num.toFixed(priceScale.value)
  const parts = fixed.split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return parts.join('.')
}

function formatSize(val: string) {
  const num = parseFloat(val)
  if (isNaN(num)) return '—'
  if (num >= 1e6) return (num / 1e6).toFixed(2) + 'M'
  if (num >= 1e3) return (num / 1e3).toFixed(2) + 'K'
  return num.toFixed(amountScale.value)
}

// 盘口显示：asks 倒序（最高价在顶部），bids 正序（最高价在顶部）
const displayAsks = computed(() => {
  if (!orderBook.value?.asks) return []
  return [...orderBook.value.asks].reverse().slice(0, 5)
})

const displayBids = computed(() => {
  if (!orderBook.value?.bids) return []
  return orderBook.value.bids.slice(0, 5)
})

/** 盘口最大档位数量（深度条基准） */
const bookMaxSize = computed(() => {
  const sizes = [...displayAsks.value, ...displayBids.value]
    .map(i => Number.parseFloat(i.size) || 0)
  return Math.max(...sizes, 1)
})

/** 单档深度条百分比 */
function bookDepthPercent(size?: string) {
  const s = Number.parseFloat(size || '') || 0
  return Math.min(100, (s / bookMaxSize.value) * 100)
}

/** 盘口 Buy/Sell 占比（按可见档位成交量加权） */
const bookBuyQty = computed(() => displayBids.value.reduce((s, i) => s + Number.parseFloat(i.size), 0))
const bookSellQty = computed(() => displayAsks.value.reduce((s, i) => s + Number.parseFloat(i.size), 0))
const bookBuyPct = computed(() => {
  const total = bookBuyQty.value + bookSellQty.value
  return total > 0 ? Math.round((bookBuyQty.value / total) * 100) : 50
})
const bookSellPct = computed(() => 100 - bookBuyPct.value)

onLoad((query) => {
  symbolCode.value = (query?.symbolCode as string) || ''
  if (!symbolCode.value) {
    router.back()
    return
  }
  initLoad()
})


onShow(() => {
  if (symbolCode.value && initialized.value) {
    startPolling()
  }
})


onHide(() => {
  stopPolling()
})

onUnmounted(() => {
  stopPolling()
})

async function initLoad() {
  await loadSymbolInfo()
  priceScale.value = symbolInfo.value?.priceScale ?? 2
  amountScale.value = symbolInfo.value?.amountScale ?? 4
  initialized.value = true
  await loadOrderBook()
  startPolling()
}

async function loadSymbolInfo() {
  try {
    const data = await apiData(spotSymbolApi.findSymbolInfo(symbolCode.value))
    symbolInfo.value = data
  } catch {
    // ignore
  }
}

async function loadOrderBook() {
  try {
    const raw = await apiData(klineApi.findOrderBook(symbolCode.value, 'spot'))
    if (raw) {
      // OKX books5 格式: asks/bids 是数组的数组 [["price","size","0","count"], ...]
      const parsed = JSON.parse(raw)
      orderBook.value = {
        asks: (parsed.asks || []).map((a: string[]) => ({ price: a[0], size: a[1] })),
        bids: (parsed.bids || []).map((b: string[]) => ({ price: b[0], size: b[1] })),
        ts: parsed.ts,
      }
    }
  } catch {
    // ignore
  }
}

function startPolling() {
  stopPolling()
  priceTimer = setInterval(async () => {
    await loadSymbolInfo()
  }, 3000)
  orderBookTimer = setInterval(async () => {
    await loadOrderBook()
  }, 2000)
}

function stopPolling() {
  if (priceTimer) { clearInterval(priceTimer); priceTimer = null }
  if (orderBookTimer) { clearInterval(orderBookTimer); orderBookTimer = null }
}

function onChartPeriodChange(val: string) {
  if (chartPeriod.value === val) return
  chartPeriod.value = val
}

function goOrderRecord() {
  router.push({ name: 'spotOrderRecord' })
}

function goBack() {
  router.back()
}
</script>

<template>
  <view class="sc-page">
    <AppNavBar :title="t('spot.chartTitle')" />
    <!-- 顶部交易对信息 -->
    <view class="sc-symbol-bar">
      <view class="sc-symbol-bar__left" @click="goBack">
        <text class="sc-symbol-bar__code">{{ displaySymbol }}</text>
      </view>
    </view>

    <!-- 价格信息 -->
    <view class="sc-price-bar">
      <view class="sc-price-bar__left">
        <text class="sc-price-bar__value" :class="priceColor">
          {{ formatPrice(symbolInfo?.closePrice) }}
        </text>
        <text class="sc-price-bar__chg" :class="priceColor">{{ chgText }}</text>
      </view>
      <view class="sc-price-bar__right">
        <view class="sc-price-bar__stat">
          <text class="sc-price-bar__stat-label">{{ t('common.high') }}</text>
          <text class="sc-price-bar__stat-value">{{ formatPrice(symbolInfo?.highestPrice) }}</text>
        </view>
        <view class="sc-price-bar__stat">
          <text class="sc-price-bar__stat-label">{{ t('common.low') }}</text>
          <text class="sc-price-bar__stat-value">{{ formatPrice(symbolInfo?.lowestPrice) }}</text>
        </view>
      </view>
    </view>

    <!-- K线周期切换 -->
    <view class="sc-chart-periods">
      <view
        v-for="p in chartPeriods"
        :key="p.value"
        class="sc-chart-periods__item"
        :class="{ 'sc-chart-periods__item--active': chartPeriod === p.value }"
        @click="onChartPeriodChange(p.value)"
      >
        {{ p.label }}
      </view>
    </view>

    <!-- K线图 -->
    <view class="sc-chart-wrap">
      <KLineChart
        :period="chartPeriod"
        :symbol="symbolCode"
        :price-scale="priceScale"
        :api-base-url="apiBaseUrl"
        :token-name="tokenName"
        :token-value="tokenValue"
        biz-type="spot"
        :theme="theme"
      />
    </view>

    <!-- 底部盘口深度 -->
    <view class="sc-book">
      <!-- 卖盘（从高到低） -->
      <view class="sc-book__side sc-book__side--ask">
        <view class="sc-book__header">
          <text class="sc-book__header-label">{{ t('spot.price') }}</text>
          <text class="sc-book__header-label">{{ t('common.amount') }}</text>
        </view>
        <view class="sc-book__list">
          <view
            v-for="(item, idx) in displayAsks"
            :key="'ask' + idx"
            class="sc-book__row sc-book__row--ask"
          >
            <view
              class="sc-book__depth sc-book__depth--ask"
              :style="{ width: `${bookDepthPercent(item.size)}%` }"
            />
            <text class="sc-book__price sc-book__price--ask">{{ formatPrice(item.price) }}</text>
            <text class="sc-book__size">{{ formatSize(item.size) }}</text>
          </view>
        </view>
      </view>

      <!-- 买盘（从高到低） -->
      <view class="sc-book__side sc-book__side--bid">
        <view class="sc-book__header">
          <text class="sc-book__header-label">{{ t('spot.price') }}</text>
          <text class="sc-book__header-label">{{ t('common.amount') }}</text>
        </view>
        <view class="sc-book__list">
          <view
            v-for="(item, idx) in displayBids"
            :key="'bid' + idx"
            class="sc-book__row sc-book__row--bid"
          >
            <view
              class="sc-book__depth sc-book__depth--bid"
              :style="{ width: `${bookDepthPercent(item.size)}%` }"
            />
            <text class="sc-book__price sc-book__price--bid">{{ formatPrice(item.price) }}</text>
            <text class="sc-book__size">{{ formatSize(item.size) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Buy/Sell 占比 -->
    <view class="sc-book__ratio">
      <view class="sc-book__ratio-head">
        <text class="sc-book__ratio-text sc-book__ratio-text--buy">Buy {{ bookBuyPct }}%</text>
        <text class="sc-book__ratio-text sc-book__ratio-text--sell">Sell {{ bookSellPct }}%</text>
      </view>
      <view class="sc-book__ratio-bar">
        <view class="sc-book__ratio-fill sc-book__ratio-fill--buy" :style="{ width: `${bookBuyPct}%` }" />
        <view class="sc-book__ratio-fill sc-book__ratio-fill--sell" :style="{ width: `${bookSellPct}%` }" />
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.sc-page {
  @include page-shell;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sc-symbol-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 28rpx;

  &__left {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__code {
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__order-text {
    font-size: 24rpx;
    color: $text-muted;
  }
}

.sc-price-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 28rpx 16rpx;

  &__left {
    display: flex;
    align-items: baseline;
    gap: 16rpx;
  }

  &__value {
    font-size: 40rpx;
    font-weight: 600;
    color: $text-highlight;
    @include amount-text;

    &.sc-price--up { color: $success; }
    &.sc-price--down { color: #d08080; }
  }

  &__chg {
    font-size: 26rpx;
    @include amount-text;

    &.sc-price--up { color: $success; }
    &.sc-price--down { color: #d08080; }
  }

  &__right {
    display: flex;
    gap: 24rpx;
  }

  &__stat {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4rpx;
  }

  &__stat-label {
    @include label-caps;
    font-size: 18rpx;
  }

  &__stat-value {
    font-size: 24rpx;
    color: $text-secondary;
    @include amount-text;
  }
}

.sc-chart-periods {
  display: flex;
  gap: 8rpx;
  padding: 0 28rpx 16rpx;

  &__item {
    padding: 8rpx 20rpx;
    border-radius: 8rpx;
    font-size: 24rpx;
    color: $text-muted;
    background: $surface-2;
    border: 1rpx solid $border-subtle;

    &--active {
      color: $gold-light;
      background: $gold-soft;
      border-color: rgba(201, 180, 138, 0.28);
    }
  }
}

.sc-chart-wrap {
  height: 50vh;
  position: relative;
  margin: 0 28rpx 16rpx;
  background: $surface-1;
  border: 1rpx solid $border-subtle;
  border-radius: 16rpx;
  overflow: hidden;
}

/* 底部盘口深度 */
.sc-book {
  display: flex;
  align-items: stretch;
  gap: 12rpx;
  margin: 0 28rpx;
  padding: 12rpx;
  background: $surface-1;
  border: 1rpx solid $border-subtle;
  border-radius: 16rpx;

  &__side {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 3rpx;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 8rpx 6rpx;
    border-bottom: 1rpx solid $border-subtle;
    margin-bottom: 4rpx;
  }

  &__header-label {
    font-size: 20rpx;
    color: $text-muted;
    letter-spacing: 0.02em;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 3rpx;
  }

  &__row {
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx;
    border-radius: 4rpx;

    &--ask { background: rgba(208, 128, 128, 0.04); }
    &--bid { background: rgba(130, 184, 153, 0.04); }
  }

  &__depth {
    position: absolute;
    top: 0;
    bottom: 0;
    z-index: 0;
    transition: width 0.25s ease;
    pointer-events: none;

    &--ask {
      right: 0;
      background: linear-gradient(90deg, transparent 0%, rgba(var(--danger-rgb), 0.2) 100%);
    }

    &--bid {
      left: 0;
      background: linear-gradient(90deg, rgba(var(--success-rgb), 0.2) 0%, transparent 100%);
    }
  }

  &__price {
    position: relative;
    z-index: 1;
    font-size: 22rpx;
    @include amount-text;

    &--ask { color: #d08080; }
    &--bid { color: $success; }
  }

  &__size {
    position: relative;
    z-index: 1;
    font-size: 22rpx;
    color: $text-muted;
    text-align: right;
  }

  &__ratio {
    margin: 12rpx 28rpx 16rpx;
    padding: 10rpx 12rpx;
    background: $surface-1;
    border: 1rpx solid $border-subtle;
    border-radius: 12rpx;
  }

  &__ratio-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6rpx;
  }

  &__ratio-text {
    font-size: 20rpx;
    font-weight: 500;

    &--buy { color: $success; }
    &--sell { color: #d08080; }
  }

  &__ratio-bar {
    display: flex;
    height: 6rpx;
    border-radius: 3rpx;
    overflow: hidden;
    background: $surface-2;
  }

  &__ratio-fill {
    height: 100%;
    transition: width 0.3s ease;

    &--buy { background: $success; }
    &--sell { background: #d08080; }
  }
}
</style>
