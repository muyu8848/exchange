<script setup lang="ts">
import type { PerpetualOrderDetailItem } from '@/api/types'
import { perpetualApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { copyText } from '@/utils/app'
import { useSymbolStore } from '@/composables/useSymbolStore'
import { formatSymbol, formatBaseSymbol } from '@/utils/symbol'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'perpOrderDetail',
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
const order = ref<PerpetualOrderDetailItem | null>(null)
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
    const data = await apiData(perpetualApi.findMyOrderById(orderId.value))
    order.value = data || null
  }
  catch {
    order.value = null
  }
  finally {
    loading.value = false
  }
}

/** 点击"去交易" → 跳转交易页并切换到该交易对 */
function goTrade(symbolCode?: string) {
  if (!symbolCode) {
    return
  }
  useSymbolStore().requestPerp(symbolCode)
  router.pushTab({ name: 'perpTrade' })
}

function baseCode(symbolCode?: string) {
  return formatBaseSymbol(symbolCode) || ''
}

function formatNum(val?: number | null) {
  if (val == null) {
    return '—'
  }
  return String(val)
}

function pnlClass(pnl?: number) {
  if (pnl == null) {
    return ''
  }
  if (pnl > 0) {
    return 'pod-pnl--win'
  }
  if (pnl < 0) {
    return 'pod-pnl--loss'
  }
  return ''
}
</script>

<template>
  <view class="pod-page">
    <AppNavBar :title="t('perpOrderDetail.title')" />
    <view class="pod-page__glow" />

    <view v-if="loading" class="pod-loading">
      <text>{{ t('common.loading') }}</text>
    </view>

    <template v-else-if="order">
      <view class="pod-body">
        <view class="pod-card">
          <!-- 头部：交易对 + 去交易（参考历史委托列表卡片头部风格） -->
          <view class="pod-head">
            <view class="pod-head__symbol">
              <text>{{ formatSymbol(order.symbolCode) }}</text>
            </view>
            <view class="pod-head__goto" @click="goTrade(order.symbolCode)">
              <text class="pod-head__goto-text">{{ t('perpCommon.goTrade') }}</text>
              <wd-icon name="right" size="24rpx" color="#9a9690" />
            </view>
          </view>

          <!-- 标签行：方向 逐仓 委托类型 杠杆（参考历史委托列表卡片） -->
          <view class="pod-tags">
            <wd-tag size="small" variant="light" round
              :type="order.direction === 'long' ? 'success' : 'danger'">
              {{ dictItemText('perpetualOrderDirection', `${order.type}-${order.direction}`, order.orderDirectionName) }}
            </wd-tag>
            <wd-tag size="small" variant="plain" round color="#C9B48A" bgColor="rgba(201, 180, 138, 0.12)">
              {{ t('perpCommon.isolated') }}
            </wd-tag>
            <wd-tag size="small" variant="plain" round>{{ dictItemText('perpetualEntrustType', order.entrustType, order.entrustTypeName) }}</wd-tag>
            <wd-tag size="small" variant="plain" round>{{ order.leverage }}x</wd-tag>
          </view>

          <!-- 正文信息行 -->
          <view class="pod-row">
            <text class="pod-row__label">{{ t('perpCommon.status') }}</text>
            <text class="pod-row__value">{{ dictItemText('perpetualOrderStatus', order.status, order.statusName) }}</text>
          </view>
          <view class="pod-row">
            <text class="pod-row__label">{{ order.entrustType === 'limit' ? t('perpCommon.entrustPrice') : t('perpOrderDetail.fieldAvgPrice') }}</text>
            <text class="pod-row__value">{{ formatNum(order.avgPrice) }}</text>
          </view>
          <view class="pod-row">
            <text class="pod-row__label">{{ t('perpCommon.entrustSizeWithBase', { base: baseCode(order.symbolCode) }) }}</text>
            <text class="pod-row__value">{{ formatNum(order.size) }}</text>
          </view>
          <view v-if="order.status === '1'" class="pod-row">
            <text class="pod-row__label">{{ t('perpCommon.filledSizeWithBase', { base: baseCode(order.symbolCode) }) }}</text>
            <text class="pod-row__value">{{ formatNum(order.filledSize) }}</text>
          </view>
          <view v-if="order.status === '1' && order.fee != null" class="pod-row">
            <text class="pod-row__label">{{ t('perpOrderDetail.fieldFee') }}</text>
            <text class="pod-row__value">{{ formatNum(order.fee) }}</text>
          </view>
          <view class="pod-row">
            <text class="pod-row__label">{{ t('perpOrderDetail.fieldPnl') }}</text>
            <text class="pod-row__value" :class="pnlClass(order.pnl)">
              {{ order.pnl == null ? '—' : (order.pnl > 0 ? '+' : '') + order.pnl }}
            </text>
          </view>
          <view v-if="order.status === '1' && order.createTime" class="pod-row">
            <text class="pod-row__label">{{ t('common.time') }}</text>
            <text class="pod-row__value">{{ order.createTime }}</text>
          </view>
          <view v-if="order.status === '1' && order.fillTime" class="pod-row">
            <text class="pod-row__label">{{ t('perpOrderDetail.fieldFillTime') }}</text>
            <text class="pod-row__value">{{ order.fillTime }}</text>
          </view>
          <view class="pod-row">
            <text class="pod-row__label">{{ t('perpCommon.orderNo') }}</text>
            <view class="pod-row__value pod-row__value--copy" @click="copyText(order.orderNo || '')">
              <text>{{ order.orderNo }}</text>
              <text class="pod-row__copy">{{ t('common.copy') }}</text>
            </view>
          </view>
        </view>
      </view>
    </template>

    <view v-else class="pod-empty">
      <wd-icon name="exclamation-circle" size="56rpx" color="#6A6660" />
      <text>{{ t('perpOrderDetail.empty') }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.pod-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding: 0 0 48rpx;

  :deep(.wd-tag) {
    --wot-tag-color: #{$text-secondary};
    --wot-tag-info-color: #{$text-secondary};
    --wot-tag-info-light-bg: #{$surface-3};
  }
}

.pod-page__glow {
  @include page-top-glow;
  height: 280rpx;
}

.pod-loading,
.pod-empty {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 160rpx 28rpx;
  font-size: 26rpx;
  color: $text-muted;
}

.pod-body {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx 0;
}

/* 卡片：参考历史委托列表卡片风格 */
.pod-card {
  position: relative;
  padding: 20rpx;
  border-radius: 12rpx;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
}

/* 头部：交易对 + 去交易（对齐历史委托列表卡片头部） */
.pod-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14rpx;

  &__symbol {
    font-size: 28rpx;
    font-weight: 600;
    color: $text-highlight;
  }

  &__goto {
    display: inline-flex;
    align-items: center;
    gap: 6rpx;

    &:active {
      opacity: 0.6;
    }
  }

  &__goto-text {
    font-size: 22rpx;
    color: $text-muted;
  }
}

/* 标签行：方向 逐仓 委托类型 杠杆（参考历史委托列表卡片） */
.pod-tags {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
  margin-bottom: 14rpx;
}

/* 信息行 */
.pod-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  padding: 12rpx 0;

  &__label {
    font-size: 24rpx;
    color: $text-muted;
  }

  &__value {
    flex: 1;
    min-width: 0;
    text-align: right;
    font-size: 24rpx;
    font-weight: 500;
    color: $text-primary;
    word-break: break-all;

    &--copy {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 12rpx;
    }
  }

  &__copy {
    flex-shrink: 0;
    font-size: 22rpx;
    color: $gold-light;
  }
}

.pod-pnl {
  &--win {
    color: $success;
  }

  &--loss {
    color: #c97a7a;
  }
}
</style>
