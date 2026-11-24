<script setup lang="ts">
import { currencyLogo } from '@/utils/currency'
import { useSymbolStore } from '@/composables/useSymbolStore'
import type { MarketCategory, MarketPair } from '@/composables/useMarkets'

const props = withDefaults(
  defineProps<{
    item: MarketPair
    category: MarketCategory
    /** true=点击跳转（行情/首页）；false=仅 emit select（弹窗内由父级处理） */
    navigate?: boolean
  }>(),
  { navigate: true },
)

const emit = defineEmits<{
  (e: 'select', item: MarketPair, category: MarketCategory): void
}>()

const router = useRouter()

const tradeRoute: Record<MarketCategory, string> = {
  spot: 'spotTrade',
  perp: 'perpTrade',
  second: 'secondContractTrade',
}

function logoOf(item: MarketPair) {
  return currencyLogo(item.baseCurrencyCode || item.base)
}

function formatPrice(val?: string) {
  if (val === undefined || val === null || val === '') return '—'
  if (Number.isNaN(Number(val))) return val
  const [int, dec] = val.split('.')
  const grouped = (int || '0').replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return dec ? `${grouped}.${dec}` : grouped
}

function formatChg(val?: string) {
  if (val === undefined || val === null || val === '') return '—'
  const n = parseFloat(val)
  if (Number.isNaN(n)) return '—'
  return `${n > 0 ? '+' : ''}${n.toFixed(2)}%`
}

function chgClass(val?: string) {
  const n = parseFloat(val)
  if (Number.isNaN(n) || n === 0) return 'market-row__chg--flat'
  return n > 0 ? 'market-row__chg--up' : 'market-row__chg--down'
}

function goTrade(item: MarketPair) {
  // 弹窗内（navigate=false）不跳转，仅把选中的交易对抛给父级处理
  if (!props.navigate) {
    emit('select', item, props.category)
    return
  }
  const store = useSymbolStore()
  const name = tradeRoute[props.category]
  // 现货/合约为 tabbar 页，必须用 pushTab；且 tabbar 页无法用 query 传参，改用 store 暂存目标交易对
  if (props.category === 'spot' || props.category === 'perp') {
    if (props.category === 'spot') store.requestSpot(item.symbolCode)
    else store.requestPerp(item.symbolCode)
    router.pushTab({ name })
  }
  else {
    router.push({ name, query: { symbolCode: item.symbolCode } })
  }
}
</script>

<template>
  <view class="market-row" @click="goTrade(item)">
    <view class="market-row__pair">
      <image
        v-if="logoOf(item)"
        :src="logoOf(item)"
        class="market-row__logo"
        mode="aspectFit"
      />
      <text v-else class="market-row__logo-fallback">{{ item.base.charAt(0) }}</text>
      <view class="market-row__names">
        <text class="market-row__base">{{ item.base }}<text class="market-row__quote">/{{ item.quote }}</text></text>
      </view>
    </view>
    <view class="market-row__price">
      <text>{{ formatPrice(item.closePrice) }}</text>
    </view>
    <view class="market-row__chg-col">
      <view class="market-row__chg" :class="chgClass(item.chg)">
        <text>{{ formatChg(item.chg) }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.market-row {
  display: flex;
  align-items: center;
  padding: 24rpx 4rpx;
  border-top: 1rpx solid $border-subtle;

  &:first-child {
    border-top: none;
  }
}

.market-row__pair {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.market-row__logo {
  flex-shrink: 0;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
}

.market-row__logo-fallback {
  flex-shrink: 0;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $surface-3;
  color: $text-secondary;
  font-size: 26rpx;
}

.market-row__names {
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.market-row__base {
  font-size: 30rpx;
  font-weight: 500;
  color: $text-primary;
}

.market-row__quote {
  font-size: 24rpx;
  color: $text-muted;
  font-weight: 400;
}

.market-row__price {
  width: 200rpx;
  text-align: right;
  font-size: 30rpx;
  color: $text-primary;
  font-weight: 500;
}

.market-row__chg-col {
  width: 170rpx;
  display: flex;
  justify-content: flex-end;
}

.market-row__chg {
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
  text-align: center;
  line-height: 1.3;

  &--up {
    background: #2EBD85;
    color: #ffffff;
  }

  &--down {
    background: #F6465D;
    color: #ffffff;
  }

  &--flat {
    background: rgba(255, 255, 255, 0.06);
    color: #9A9690;
  }
}
</style>
