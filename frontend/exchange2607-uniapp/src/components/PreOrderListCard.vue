<script setup lang="ts">
import type { PreOrderItem } from '@/api/types'
import { apiData } from '@/api/http'
import { c2cApi } from '@/api/modules'
import { useI18n } from 'vue-i18n'
import { useDictItemText } from '@/utils/dictItem'
import { DEFAULT_LOGO } from '@/utils/paymentAccountType'

function preOrderStatusBadgeClass(status?: string) {
  if (status === '2') {
    return 'trade-list-badge--success'
  }
  if (status === '3') {
    return 'trade-list-badge--fail'
  }
  return 'trade-list-badge--gold'
}

const props = defineProps<{
  order: PreOrderItem
  currencyCode: string
}>()

const emit = defineEmits<{
  click: []
  updated: []
}>()

const toast = useGlobalToast()
const { t } = useI18n()
const dictItemText = useDictItemText()

const PAYMENT_CHIP_LIMIT = 3

const supportedPayments = computed(() => {
  return props.order.supportedPaymentAccountTypes || []
})

const visiblePayments = computed(() => supportedPayments.value.slice(0, PAYMENT_CHIP_LIMIT))
const hiddenPaymentCount = computed(() => supportedPayments.value.length - visiblePayments.value.length)

const isBuy = computed(() => props.order.orderType === 'buy')
const sideClass = computed(() => (isBuy.value ? 'trade-list-card__side--buy' : 'trade-list-card__side--sell'))

const isFloatPrice = computed(() => props.order.priceType === 'float')
const premiumText = computed(() => {
  const rate = props.order.premiumRate
  if (rate == null) {
    return ''
  }
  return `${rate > 0 ? '+' : ''}${rate}%`
})

const isActive = computed(() => props.order.status === '1')
const isPaused = computed(() => props.order.status === '4')

const displayAmount = computed(() => {
  if (isActive.value && props.order.availableAmount != null) {
    return props.order.availableAmount
  }
  return props.order.amount
})

const amountLabel = computed(() => {
  if (!isActive.value) {
    return t('c2cPreOrderCard.totalAmount')
  }
  return isBuy.value ? t('c2cPreOrderCard.availableToBuy') : t('c2cPreOrderCard.availableToSell')
})

const limitText = computed(() => {
  const { singleMinFiatAmount, singleMaxFiatAmount } = props.order
  if (singleMinFiatAmount == null && singleMaxFiatAmount == null) {
    return ''
  }
  return t('c2cPreOrderCard.limitRange', {
    min: singleMinFiatAmount ?? 0,
    max: singleMaxFiatAmount ?? '∞',
    fiat: props.order.fiatCode || '',
  })
})

const toggling = ref(false)
const canPause = computed(() => isActive.value && !(props.order.tradingAmount != null && props.order.tradingAmount > 0))
const pauseDisabledHint = computed(() => (canPause.value ? '' : t('c2cPreOrderCard.pauseDisabledHint')))

async function togglePauseResume() {
  if (toggling.value) {
    return
  }
  if (isActive.value && !canPause.value) {
    toast.error({ msg: pauseDisabledHint.value })
    return
  }
  toggling.value = true
  try {
    if (isActive.value) {
      await apiData(c2cApi.pausePreOrder(props.order.id))
      toast.success({ msg: t('c2cPreOrderCard.pausedToast') })
    }
    else {
      await apiData(c2cApi.resumePreOrder(props.order.id))
      toast.success({ msg: t('c2cPreOrderCard.resumedToast') })
    }
    emit('updated')
  }
  catch {}
  finally {
    toggling.value = false
  }
}
</script>

<template>
  <view class="trade-list-card" @click="$emit('click')">
    <view class="trade-list-card__hairline" />

    <view class="trade-list-card__head">
      <view class="trade-list-card__tags">
        <view class="trade-list-card__side" :class="sideClass">
          {{ dictItemText('c2cPreOrderType', order.orderType, order.orderTypeName) }}
        </view>
        <text class="trade-list-card__pair">
          {{ order.assetCode }}
        </text>
      </view>
      <view class="trade-list-card__status">
        <view class="trade-list-badge" :class="preOrderStatusBadgeClass(order.status)">
          {{ dictItemText('c2cPreOrderStatus', order.status, order.statusName) }}
        </view>
        <wd-icon name="arrow-right" size="28rpx" color="#9A9690" />
      </view>
    </view>

    <view class="trade-list-card__main trade-list-card__main--pre">
      <view class="trade-list-card__amount-block trade-list-card__price-block">
        <text class="trade-list-card__amount-label">
          {{ t('c2cPreOrderCard.unitPrice') }}
        </text>
        <view class="trade-list-card__amount">
          <text class="trade-list-card__amount-num">
            {{ order.price ?? '—' }}
          </text>
          <text class="trade-list-card__amount-unit">
            {{ order.fiatCode }}
          </text>
          <view
            v-if="isFloatPrice"
            class="trade-list-card__float-badge"
            :class="{ 'trade-list-card__float-badge--neg': order.premiumRate != null && order.premiumRate < 0 }"
          >
            {{ t('c2cPreOrderCard.floatPrefix') }} {{ premiumText }}
          </view>
        </view>
      </view>
      <view class="trade-list-card__amount-block">
        <text class="trade-list-card__amount-label">
          {{ amountLabel }}
        </text>
        <view class="trade-list-card__amount">
          <text class="trade-list-card__amount-num">
            {{ displayAmount }}
          </text>
          <text class="trade-list-card__amount-unit">
            {{ currencyCode }}
          </text>
        </view>
      </view>
    </view>

    <view class="trade-list-card__meta">
      <text v-if="limitText" class="trade-list-card__limit">
        {{ limitText }}
      </text>
      <view class="trade-list-card__pay-chips">
        <view
          v-for="pt in visiblePayments"
          :key="pt.code"
          class="trade-list-card__pay-chip"
        >
          <wd-img :src="pt.logo || DEFAULT_LOGO" width="28rpx" height="28rpx" />
          <text>{{ pt.name }}</text>
        </view>
        <view v-if="hiddenPaymentCount > 0" class="trade-list-card__pay-chip trade-list-card__pay-chip--more">
          <text>+{{ hiddenPaymentCount }}</text>
        </view>
      </view>
    </view>

    <view class="trade-list-card__divider" />

    <view class="trade-list-card__foot" :class="{ 'trade-list-card__foot--right': !isActive && !isPaused }">
      <text class="trade-list-card__foot-text">
        {{ order.createTime }}
      </text>
      <view
        v-if="isActive || isPaused"
        class="trade-list-card__toggle"
        :class="{
          'trade-list-card__toggle--resume': isPaused,
          'trade-list-card__toggle--disabled': isActive && !canPause,
        }"
        @click.stop="togglePauseResume"
      >
        {{ isPaused ? t('c2cPreOrderCard.resume') : t('c2cPreOrderCard.pause') }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;
@use '@/styles/trade-list-card.scss';

.trade-list-card__pair {
  font-size: 24rpx;
  font-weight: 500;
  color: #e6e2da;
  white-space: nowrap;
}

.trade-list-card__price-block {
  align-items: flex-start;
}

.trade-list-card__float-badge {
  align-self: flex-end;
  margin-left: 4rpx;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  font-size: 18rpx;
  line-height: 1.5;
  color: #f0d9a8;
  background: rgba(208, 168, 104, 0.14);
  border: 1rpx solid rgba(208, 168, 104, 0.28);

  &--neg {
    color: #e8b0b0;
    background: rgba(208, 128, 128, 0.14);
    border-color: rgba(208, 128, 128, 0.28);
  }
}

.trade-list-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.trade-list-card__limit {
  font-size: 22rpx;
  color: $text-muted;
  white-space: nowrap;
}

.trade-list-card__pay-chips {
  flex: 1;
  justify-content: flex-end;
}

.trade-list-card__pay-chip--more {
  color: $text-muted;
}

.trade-list-card__toggle {
  padding: 8rpx 28rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  color: $gold-light;
  border: 1rpx solid rgba(201, 180, 138, 0.32);
  background: rgba(201, 180, 138, 0.1);

  &--resume {
    color: #0d0d0f;
    background: linear-gradient(135deg, #d0a868 0%, #e5d9c0 100%);
    border-color: transparent;
    font-weight: 500;
  }

  &--disabled {
    opacity: 0.4;
  }
}
</style>
