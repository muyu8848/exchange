<script setup lang="ts">
import type { SpotOrderItem } from '@/api/types'
import { spotApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { copyText } from '@/utils/app'
import { useSymbolStore } from '@/composables/useSymbolStore'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'spotOrderDetail',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()
const orderId = ref('')
const order = ref<SpotOrderItem | null>(null)
const loading = ref(true)

onLoad((query) => {
  orderId.value = (query?.id as string) || ''
  loadOrder()
})


onShow(() => {
  if (orderId.value && order.value) {
    loadOrder(false)
  }
})


async function loadOrder(showLoading = true) {
  if (!orderId.value) {
    order.value = null
    loading.value = false
    return
  }
  if (showLoading) {
    loading.value = true
  }
  try {
    const data = await apiData(spotApi.findMyOrderById(orderId.value))
    order.value = data || null
  }
  catch {
    order.value = null
  }
  finally {
    loading.value = false
  }
}

/** 点击交易对 → 跳转现货交易页并切换到该交易对 */
function goTrade() {
  const symbolCode = order.value?.symbolCode
  if (!symbolCode) return
  useSymbolStore().requestSpot(symbolCode)
  router.pushTab({ name: 'spotTrade' })
}

function sideClass(side?: string) {
  if (side === 'buy') {
    return 'sod-side--buy'
  }
  return 'sod-side--sell'
}

</script>

<template>
  <view class="sod-page">
    <AppNavBar :title="t('spot.orderDetailTitle')" />
    <view class="sod-page__glow" />

    <view v-if="loading" class="sod-loading">
      <text>{{ t('common.loading') }}</text>
    </view>

    <template v-else-if="order">
      <view class="sod-body">
        <view class="sod-panel">
          <!-- 头部：方向 交易对 类型  |  状态 -->
          <view class="sod-head">
            <view class="sod-head__left">
              <view class="sod-side" :class="sideClass(order.side)">
                {{ dictItemText('spotOrderSide', order.side, order.sideName) }}
              </view>
              <view class="sod-symbol sod-symbol-link" @click="goTrade">
                <text>{{ order.symbolCode }}</text>
                <wd-icon name="right" size="24rpx" color="#9a9690" />
              </view>
              <view class="sod-tag">
                <text>{{ dictItemText('spotEntrustType', order.entrustType, order.entrustTypeName) }}</text>
              </view>
            </view>
            <text class="sod-status">{{ dictItemText('spotOrderStatus', order.status, order.statusName) }}</text>
          </view>

          <!-- 委托与成交信息 -->
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.entrustPrice') }}</text>
            <text class="sod-info-row__value-text">{{ order.entrustType === 'market' ? t('spot.market') : `${order.price} ${order.quoteCurrencyCode || ''}` }}</text>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.dealPrice') }}</text>
            <text class="sod-info-row__value-text">{{ order.dealPrice || 0 }} {{ order.quoteCurrencyCode }}</text>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.entrustAmount') }}</text>
            <text class="sod-info-row__value-text">{{ order.amount }} {{ order.baseCurrencyCode }}</text>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.dealAmount') }}</text>
            <text class="sod-info-row__value-text">{{ order.dealAmount || 0 }} {{ order.baseCurrencyCode }}</text>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.dealTotal') }}</text>
            <text class="sod-info-row__value-text">{{ order.dealTotal || 0 }} {{ order.quoteCurrencyCode }}</text>
          </view>
        </view>

        <!-- 下方详情信息 -->
        <view class="sod-info-panel">
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.orderNo') }}</text>
            <view class="sod-info-row__value" @click="copyText(order.orderNo || '')">
              <text class="sod-info-row__text">{{ order.orderNo }}</text>
              <text class="sod-info-row__copy">{{ t('common.copy') }}</text>
            </view>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.entrustTime') }}</text>
            <text class="sod-info-row__value-text">{{ order.createTime }}</text>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('spot.dealTime') }}</text>
            <text class="sod-info-row__value-text">{{ order.dealTime }}</text>
          </view>
          <view class="sod-info-row">
            <text class="sod-info-row__label">{{ t('common.fee') }}</text>
            <text class="sod-info-row__value-text">{{ order.fee || 0 }} {{ order.feeAssetCode }}</text>
          </view>
        </view>
      </view>
    </template>

    <view v-else class="sod-empty">
      <wd-icon name="exclamation-circle" size="56rpx" color="#6A6660" />
      <text>{{ t('spot.orderNotFound') }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.sod-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
}

.sod-page__glow {
  @include page-top-glow;
  height: 320rpx;
}

.sod-loading,
.sod-empty {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 160rpx 40rpx;
  font-size: 26rpx;
  color: $text-muted;
}

.sod-body {
  position: relative;
  z-index: 1;
  padding: 8rpx 28rpx 0;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.sod-panel {
  padding: 28rpx 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(160deg, $surface-2 0%, $surface-1 100%);
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4rpx;
    background: $gold;
    opacity: 0.5;
  }
}

.sod-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;

  &__left {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
}

.sod-side {
  padding: 4rpx 16rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
  font-weight: 500;

  &--buy {
    color: $success;
    background: $success-soft;
  }

  &--sell {
    color: #c97a7a;
    background: rgba(201, 122, 122, 0.1);
  }
}

.sod-symbol {
  font-size: 28rpx;
  font-weight: 500;
  color: $text-highlight;
}

.sod-symbol-link {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;

  &:active {
    opacity: 0.6;
  }
}

.sod-tag {
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  font-size: 18rpx;
  color: $gold-light;
  background: $gold-soft;
}

.sod-status {
  font-size: 24rpx;
  color: $text-secondary;
}

.sod-info-panel {
  padding: 24rpx;
  border-radius: 20rpx;
  background: rgba(var(--surface-2-rgb), 0.72);
  border: 1rpx solid $border-subtle;
}

.sod-info-row {
  @include info-row;

  &__label {
    @include info-row-label;
    width: 140rpx;
  }

  &__value {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12rpx;
    text-align: right;
    color: $text-primary;
    word-break: break-all;
  }

  &__value-text {
    flex: 1;
    min-width: 0;
    text-align: right;
    color: $text-primary;
    word-break: break-all;
  }

  &__text {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 24rpx;
  }

  &__copy {
    @include info-row-copy;
  }
}
</style>
