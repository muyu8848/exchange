<script setup lang="ts">
import type { WithdrawOrder } from '@/api/types'
import { withdrawApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { copyText } from '@/utils/app'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'withdrawDetail',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const toast = useGlobalToast()

const { t } = useI18n()
const dictItemText = useDictItemText()
const { currentLang } = useI18nSync()



const goldIcon = '#C9B48A'
const mutedIcon = '#9A9690'

const orderId = ref('')
const order = ref<WithdrawOrder | null>(null)
const loading = ref(true)
const cancelling = ref(false)

const isPending = computed(() => order.value?.status === '1')
const canCancel = computed(() => isPending.value && !cancelling.value)

onLoad((query) => {
  orderId.value = String(query?.id || '')
  if (!orderId.value) {
    toast.error({ msg: t('common.paramsError') })
    setTimeout(() => router.back(), 1000)
    return
  }
  loadOrder()
})

onShow(() => {
  if (orderId.value) loadOrder()
})

async function loadOrder() {
  loading.value = true
  try {
    order.value = await apiData(withdrawApi.findOrderById(orderId.value))
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  if (!canCancel.value) return
  cancelling.value = true
  try {
    await apiData(withdrawApi.cancelOrder(orderId.value))
    toast.success({ msg: t('withdrawDetail.cancelSuccess') })
    loadOrder()
  } finally {
    cancelling.value = false
  }
}

function statusClass(status?: string) {
  if (status === '3') return 'wd-status--done'
  if (status === '1') return 'wd-status--pending'
  if (status === '2') return 'wd-status--approved'
  if (status === '4') return 'wd-status--rejected'
  if (status === '5') return 'wd-status--closed'
  return 'wd-status--closed'
}

function statusIcon(status?: string) {
  if (status === '1') return 'clock-circle'
  if (status === '2' || status === '3') return 'check-circle'
  if (status === '4') return 'close-circle-fill'
  return 'info-circle'
}

function statusDesc(status?: string) {
  return dictItemText('withdrawDesc', status, '')
}

function openExplorer(url: string) {
  // #ifdef H5
  window.open(url, '_blank')
  // #endif
  // #ifndef H5
  uni.setClipboardData({
    data: url,
    success: () => {
      toast.success({ msg: t('withdrawDetail.linkCopied') })
    },
  })
  // #endif
}
</script>

<template>
  <view class="wd-page">
    <AppNavBar :title="t('withdrawDetail.title')" />
    <view v-if="loading" class="wd-loading">
      <view class="wd-loading__pulse" />
      <text>{{ t('common.loading') }}</text>
    </view>

    <template v-else-if="order">
      <!-- 头部：到账金额在上，状态行(icon+文字)在下 -->
      <view class="wd-header" :class="statusClass(order.status)">
        <view v-if="order.actualAmount != null" class="wd-header__amount">
          <text class="wd-header__amount-value">{{ order.actualAmount }} {{ order.assetCode }}</text>
        </view>

        <view class="wd-header__status">
          <view class="wd-header__icon">
            <wd-icon :name="statusIcon(order.status)" size="48rpx" :color="goldIcon" />
          </view>
          <view class="wd-header__status-text">
            <text class="wd-header__name">{{ dictItemText('withdraw', order.status, order.statusName) }}</text>
            <text class="wd-header__desc">{{ statusDesc(order.status) }}</text>
            <text v-if="order.auditRemark && (order.status === '4' || order.status === '2')" class="wd-header__remark">
              {{ order.auditRemark }}
            </text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="wd-card">
        <view class="wd-card__section">
          <text class="wd-card__title">{{ t('withdrawDetail.infoTitle') }}</text>
          <view class="wd-row">
            <text class="wd-row__label">{{ t('common.time') }}</text>
            <text class="wd-row__value">{{ order.createTime }}</text>
          </view>
          <view class="wd-row">
            <text class="wd-row__label">{{ t('withdrawDetail.network') }}</text>
            <text class="wd-row__value">{{ order.chainType }}</text>
          </view>
          <view class="wd-row">
            <text class="wd-row__label">{{ t('common.fee') }}</text>
            <text class="wd-row__value">{{ order.fee ?? 0 }} {{ order.assetCode }}</text>
          </view>

        </view>

        <view class="wd-card__divider" />

        <view class="wd-card__section">
          <text class="wd-card__title">{{ t('withdrawDetail.address') }}</text>
          <view class="wd-addr">
            <text v-if="order.addressExplorerUrl" class="wd-addr__text wd-addr__text--link"
              @click="openExplorer(order.addressExplorerUrl)">{{ order.withdrawalAddress }}</text>
            <text v-else class="wd-addr__text">{{ order.withdrawalAddress }}</text>
            <text class="wd-addr__copy" @click="copyText(order.withdrawalAddress || '')">{{ t('common.copy') }}</text>
          </view>
        </view>
      </view>

      <!-- 提示 -->
      <view class="wd-tips">
        <text class="wd-tips__title">{{ t('withdrawDetail.tipsTitle') }}</text>
        <view class="wd-tips__item">
          <text class="wd-tips__dot">·</text>
          <text>{{ t('withdrawDetail.tip1') }}</text>
        </view>
        <view class="wd-tips__item">
          <text class="wd-tips__dot">·</text>
          <text>{{ t('withdrawDetail.tip2') }}</text>
        </view>
        <view class="wd-tips__item">
          <text class="wd-tips__dot">·</text>
          <text>{{ t('withdrawDetail.tip3') }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view v-if="canCancel" class="wd-actions">
        <view class="wd-cancel-btn" @click="handleCancel">
          {{ cancelling ? t('withdrawDetail.cancelling') : t('withdrawDetail.cancel') }}
        </view>
      </view>
    </template>

    <view v-else class="wd-loading">
      <text>{{ t('withdrawDetail.notFound') }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.wd-page {
  min-height: 100vh;
  padding: 0 0 calc(48rpx + env(safe-area-inset-bottom));
  background: $page-bg;
  color: $text-primary;
}

.wd-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  padding: 120rpx 28rpx;
  font-size: 24rpx;
  color: $text-muted;

  &__pulse {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(var(--gold-rgb), 0.2);
    border-top-color: $gold;
    animation: wd-spin 0.8s linear infinite;
  }
}

@keyframes wd-spin {
  to {
    transform: rotate(360deg);
  }
}

.wd-header {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 24rpx 28rpx 0;
  padding: 48rpx 36rpx 40rpx;
  border-radius: 24rpx;
  background: $surface-3;
  border: 1rpx solid rgba(var(--gold-rgb), 0.18);
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.35);
  text-align: center;
  overflow: hidden;

  /* 顶部柔和金色光晕 */
  &::before {
    content: '';
    position: absolute;
    top: -120rpx;
    left: 50%;
    width: 360rpx;
    height: 240rpx;
    transform: translateX(-50%);
    background: radial-gradient(circle, rgba(var(--gold-rgb), 0.16) 0%, rgba(var(--gold-rgb), 0) 70%);
    pointer-events: none;
  }

  &__icon {
    position: relative;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(var(--gold-rgb), 0.14) 0%, rgba(var(--gold-rgb), 0.04) 70%);
    border: 1rpx solid rgba(var(--gold-rgb), 0.28);
  }

  &__status {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    gap: 20rpx;
    width: 100%;
  }

  &__status-text {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }

  &__name {
    font-size: 32rpx;
    font-weight: 600;
    color: $text-primary;
    letter-spacing: 1rpx;
  }

  &__desc {
    margin-top: 6rpx;
    font-size: 22rpx;
    color: $text-secondary;
    line-height: 1.6;
  }

  &__remark {
    margin-top: 10rpx;
    padding: 10rpx 18rpx;
    border-radius: 10rpx;
    font-size: 22rpx;
    color: $danger;
    background: rgba(var(--danger-rgb), 0.08);
    align-self: flex-start;
  }

  &__amount {
    margin-bottom: 32rpx;
  }

  &__amount-value {
    font-size: 52rpx;
    font-weight: 700;
    color: $gold-light;
    letter-spacing: 0.5rpx;
  }

  &--pending .wd-header__name {
    color: $gold-light;
  }

  &--approved .wd-header__name {
    color: $silver;
  }

  &--done .wd-header__name {
    color: $success;
  }

  &--rejected .wd-header__name {
    color: $danger;
  }
}

.wd-card {
  margin: 24rpx 28rpx 0;
  border-radius: 20rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  overflow: hidden;

  &__section {
    padding: 24rpx;
  }

  &__title {
    display: block;
    font-size: 24rpx;
    font-weight: 500;
    color: $text-muted;
    letter-spacing: 0.06em;
    margin-bottom: 16rpx;
  }

  &__divider {
    height: 1rpx;
    background: $border-subtle;
  }
}

.wd-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 10rpx 0;
  font-size: 26rpx;

  &__label {
    color: $text-muted;
    flex-shrink: 0;
  }

  &__value {
    color: $text-primary;
    text-align: right;

    &--gold {
      color: $gold;
      font-weight: 600;
    }

    &--mono {
      font-family: monospace;
      font-size: 22rpx;
    }
  }

  &__value-wrap {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  &__copy {
    font-size: 22rpx;
    color: $gold;
  }
}

.wd-addr {
  display: flex;
  align-items: center;
  gap: 12rpx;

  &__text {
    flex: 1;
    min-width: 0;
    font-size: 24rpx;
    color: $text-primary;
    word-break: break-all;
    line-height: 1.5;

    &--link {
      color: $silver;
      text-decoration: underline;
    }
  }

  &__copy {
    flex-shrink: 0;
    font-size: 22rpx;
    color: $gold;
  }
}

.wd-tips {
  margin: 24rpx 28rpx 0;
  padding: 24rpx 28rpx;
  border-radius: 16rpx;
  background: $surface-3;
  border: 1rpx solid $border-subtle;

  &__title {
    display: block;
    font-size: 22rpx;
    letter-spacing: 0.12em;
    color: $text-muted;
    margin-bottom: 14rpx;
  }

  &__item {
    display: flex;
    align-items: flex-start;
    gap: 10rpx;
    padding: 4rpx 0;
    font-size: 22rpx;
    line-height: 1.6;
    color: $text-muted;
  }

  &__dot {
    flex-shrink: 0;
    color: $gold;
  }
}

.wd-actions {
  margin: 32rpx 28rpx 0;
}

.wd-cancel-btn {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: $danger;
  background: rgba(var(--danger-rgb), 0.08);
  border: 1rpx solid rgba(var(--danger-rgb), 0.16);
}
</style>
