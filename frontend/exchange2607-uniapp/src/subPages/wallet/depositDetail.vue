<script setup lang="ts">
import type { DepositOrder } from '@/api/types'
import { depositApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { copyText } from '@/utils/app'
import { qrCodeImageUrl } from '@/utils/qrcode'
import { useAppStore } from '@/store/app'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import QrCodePopup from '@/components/QrCodePopup.vue'

definePage({
  name: 'depositDetail',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const toast = useGlobalToast()
const appStore = useAppStore()

const { t } = useI18n()
const dictItemText = useDictItemText()
const { currentLang } = useI18nSync()

/** 收款二维码放大弹窗 */
const showQrPopup = ref(false)



const goldIcon = '#C9B48A'
const silverIcon = '#C4C6CA'
const successIcon = '#82B899'
const warnIcon = '#c97a7a'
const mutedIcon = '#9A9690'

const orderId = ref('')
const order = ref<DepositOrder | null>(null)
const txHash = ref('')
const loading = ref(true)
const submitting = ref(false)
const countdown = ref(0)
const orderDetailExpanded = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const isPayFlow = computed(() => ['1', '2'].includes(order.value?.status || ''))
const isTerminal = computed(() => ['3', '4', '5'].includes(order.value?.status || ''))

const showActualAmount = computed(() => {
  const o = order.value
  if (!o || o.actualAmount == null) {
    return false
  }
  if (o.amount == null) {
    return true
  }
  return Number(o.actualAmount) !== Number(o.amount)
})


const canSubmitTx = computed(() =>
  !!order.value && order.value.status === '1' && txHash.value.trim().length >= 10 && !submitting.value,
)

const depositQrImage = computed(() => {
  const addr = order.value?.depositAddress
  return addr ? qrCodeImageUrl(addr, 220) : ''
})

const statusStripDesc = computed(() => {
  const o = order.value
  if (!o) {
    return ''
  }
  if (o.status === '5' && o.closeReason) {
    return o.closeReason
  }
  return dictItemText('depositDesc', o.status, '')
})

const statusStripIcon = computed(() => {
  const status = order.value?.status
  if (status === '1' || status === '4') {
    return 'clock-circle'
  }
  if (status === '2' || status === '3') {
    return 'check-circle'
  }
  if (status === '5') {
    return 'close-circle-fill'
  }
  return 'close-circle-fill'
})

const statusStripIconColor = computed(() => {
  const status = order.value?.status
  if (status === '1') {
    return goldIcon
  }
  if (status === '2') {
    return silverIcon
  }
  if (status === '3') {
    return successIcon
  }
  if (status === '4') {
    return warnIcon
  }
  if (status === '5') {
    return silverIcon
  }
  return mutedIcon
})

function statusTone(status?: string) {
  if (status === '3') {
    return 'done'
  }
  if (status === '2') {
    return 'confirm'
  }
  if (status === '1') {
    return 'pending'
  }
  if (status === '4') {
    return 'expired'
  }
  return 'closed'
}

onLoad((query) => {
  orderId.value = (query?.id as string) || ''
  loadOrder()
})

onShow(() => {
  if (orderId.value && order.value) {
    loadOrder(false)
  }
})

onUnload(() => {
  stopCountdown()
})

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function startCountdown(seconds: number) {
  stopCountdown()
  countdown.value = Math.max(0, seconds)
  if (countdown.value <= 0) {
    return
  }
  countdownTimer = setInterval(() => {
    if (countdown.value <= 1) {
      countdown.value = 0
      stopCountdown()
      if (orderId.value) {
        loadOrder(false)
      }
      return
    }
    countdown.value -= 1
  }, 1000)
}

function formatCountdown(sec: number) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

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
    const data = await apiData(depositApi.findOrderById(orderId.value))
    order.value = data || null
    if (data?.status === '1') {
      orderDetailExpanded.value = false
      if (data.expireRemainingSeconds != null) {
        startCountdown(data.expireRemainingSeconds)
      }
    }
    else {
      stopCountdown()
      orderDetailExpanded.value = false
    }
  }
  catch {
    order.value = null
    stopCountdown()
  }
  finally {
    loading.value = false
  }
}

async function handleSubmitTx() {
  if (!canSubmitTx.value || !order.value?.id) {
    return
  }
  submitting.value = true
  try {
    await apiData(depositApi.submitTxHash(order.value.id, txHash.value.trim()))
    toast.success({ msg: t('depositDetail.submitSuccess') })
    await loadOrder(false)
  }
  finally {
    submitting.value = false
  }
}

function goNewDeposit() {
  router.replace({ name: 'deposit' })
}
</script>

<template>
  <view class="udd-page">
    <AppNavBar :title="t('depositDetail.title')" />
    <view class="udd-page__glow" />
    <view class="udd-page__glow udd-page__glow--gold" />

    <view v-if="loading" class="udd-loading">
      <text>{{ t('common.loading') }}</text>
    </view>

    <template v-else-if="order">
      <view class="udd-body">
        <view class="udd-panel udd-panel--order">
          <view class="udd-status-strip" :class="`udd-status-strip--${statusTone(order.status)}`">
            <view class="udd-status-strip__icon">
              <wd-icon :name="statusStripIcon" size="36rpx" :color="statusStripIconColor" />
              <view v-if="order.status === '2'" class="udd-status-strip__pulse" />
            </view>
            <view class="udd-status-strip__main">
              <text class="udd-status-strip__title">{{ dictItemText('deposit', order.status, order.statusName) }}</text>
              <text class="udd-status-strip__desc">{{ statusStripDesc }}</text>
            </view>
            <view v-if="order.status === '1' && countdown > 0" class="udd-status-strip__extra">
              <text class="udd-status-strip__extra-label">{{ t('depositDetail.payTimeLeft') }}</text>
              <text class="udd-status-strip__extra-value">{{ formatCountdown(countdown) }}</text>
            </view>

          </view>

          <template v-if="isPayFlow">
            <view class="udd-pay-card">
              <view class="udd-pay-card__head">
                <text class="udd-pay-card__chain">{{ order.chainType }}</text>
              </view>

              <view v-if="order.depositAddress && depositQrImage" class="udd-pay-card__qr">
                <view class="udd-pay-card__qr-frame" @click="showQrPopup = true">
                  <view class="udd-pay-card__qr-img-wrap">
                    <wd-img :src="depositQrImage" width="220rpx" height="220rpx" />
                  </view>
                </view>
                <text class="udd-pay-card__qr-tip">{{ t('depositDetail.scanTip') }}</text>
                <text class="udd-pay-card__qr-warn">
                  {{ t('depositDetail.networkWarn', { chain: order.chainType }) }}
                </text>
              </view>

              <view v-if="order.depositAddress" class="udd-pay-card__addr">
                <view class="udd-pay-card__addr-body" @click="copyText(order.depositAddress || '')">
                  <text class="udd-pay-card__addr-text">{{ order.depositAddress }}</text>
                  <view class="udd-pay-card__copy">
                    <wd-icon name="copy" size="28rpx" :color="goldIcon" />
                    <text>{{ t('depositDetail.copyAddress') }}</text>
                  </view>
                </view>
              </view>

              <view class="udd-pay-card__divider" />

              <view class="udd-pay-card__amount-row">
                <text class="udd-pay-card__amount-label">{{ t('depositDetail.payableLabel') }}</text>
                <view class="udd-pay-card__amount-value">
                  <text class="udd-pay-card__usdt">{{ order.amount }}</text>
                  <text class="udd-pay-card__unit">{{ order.assetCode }}</text>
                </view>
              </view>

            </view>

            <view class="udd-info-card">
              <view class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.orderNoLabel') }}</text>
                <view class="udd-info-row__value" @click="copyText(order.orderNo || '')">
                  <text>{{ order.orderNo }}</text>
                  <text class="udd-info-row__copy">{{ t('common.copy') }}</text>
                </view>
              </view>
              <view v-if="order.createTime" class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.createTimeLabel') }}</text>
                <text class="udd-info-row__value-text">{{ order.createTime }}</text>
              </view>
              <view v-if="order.status === '2' && order.txHash" class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.txHashLabel') }}</text>
                <text class="udd-info-row__mono">{{ order.txHash }}</text>
              </view>
            </view>

            <view v-if="order.status === '1'" class="udd-section udd-section--proof">
              <text class="udd-section__title">{{ t('depositDetail.proofTitle') }}</text>
              <text class="udd-section__hint">{{ t('depositDetail.proofHint') }}</text>
              <view class="udd-input-shell udd-input-shell--mono">
                <wd-input v-model="txHash" :placeholder="t('depositDetail.txPlaceholder')" clearable />
              </view>
            </view>
          </template>

          <template v-else-if="isTerminal">
            <view class="udd-result-card" :class="`udd-result-card--${statusTone(order.status)}`">
              <text class="udd-result-card__chain">{{ order.chainType }}</text>
              <view class="udd-pay-card__amount-row">
                <text class="udd-pay-card__amount-label">{{ t('depositDetail.amountLabel') }}</text>
                <view class="udd-pay-card__amount-value">
                  <text class="udd-pay-card__usdt">{{ order.amount }}</text>
                  <text class="udd-pay-card__unit">{{ order.assetCode }}</text>
                </view>
              </view>
              <view v-if="order.status === '3' && showActualAmount" class="udd-result-card__sub">
                {{ t('depositDetail.actualArrival', { amount: order.actualAmount }) }}
              </view>
              <text v-if="order.status === '4' && order.expireTime" class="udd-result-card__meta">
                {{ t('depositDetail.expireTimeLabel') }} {{ order.expireTime }}
              </text>
            </view>

            <view class="udd-detail-block">
              <text class="udd-detail-block__title">{{ t('depositDetail.orderInfoTitle') }}</text>
              <view class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.orderNoLabel') }}</text>
                <view class="udd-info-row__value" @click="copyText(order.orderNo || '')">
                  <text>{{ order.orderNo }}</text>
                  <text class="udd-info-row__copy">{{ t('common.copy') }}</text>
                </view>
              </view>
              <view class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.chainLabel') }}</text>
                <text class="udd-info-row__value-text">{{ order.chainType }}</text>
              </view>
              <view v-if="order.createTime" class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.createTimeLabel') }}</text>
                <text class="udd-info-row__value-text">{{ order.createTime }}</text>
              </view>
              <view v-if="order.status === '3' && order.creditTime" class="udd-info-row">
                <text class="udd-info-row__label">{{ t('depositDetail.confirmTimeLabel') }}</text>
                <text class="udd-info-row__value-text">{{ order.creditTime }}</text>
              </view>
            </view>

            <view v-if="order.txHash || order.fromAddress" class="udd-detail-block">
              <text class="udd-detail-block__title">{{ t('depositDetail.onchainTitle') }}</text>
              <view v-if="order.txHash" class="udd-info-row udd-info-row--stack">
                <text class="udd-info-row__label">{{ t('depositDetail.txHashLabel') }}</text>
                <view class="udd-info-row__value" @click="copyText(order.txHash || '')">
                  <text class="udd-info-row__mono">{{ order.txHash }}</text>
                  <text class="udd-info-row__copy">{{ t('common.copy') }}</text>
                </view>
              </view>
              <view v-if="order.fromAddress" class="udd-info-row udd-info-row--stack">
                <text class="udd-info-row__label">{{ t('depositDetail.fromAddrLabel') }}</text>
                <view class="udd-info-row__value" @click="copyText(order.fromAddress || '')">
                  <text class="udd-info-row__mono">{{ order.fromAddress }}</text>
                  <text class="udd-info-row__copy">{{ t('common.copy') }}</text>
                </view>
              </view>
            </view>
          </template>
        </view>
      </view>

      <view v-if="order.status === '1'" class="udd-submit-bar">
        <view class="udd-submit-bar__inner">
          <view class="udd-submit-btn" :class="{ 'udd-submit-btn--disabled': !canSubmitTx }" @click="handleSubmitTx">
            {{ submitting ? t('common.submitting') : t('depositDetail.submitProof') }}
          </view>
        </view>
      </view>

      <view v-else-if="isTerminal" class="udd-submit-bar">
        <view class="udd-submit-bar__inner">
          <view class="udd-submit-btn" @click="goNewDeposit">
            {{ t('depositDetail.againDeposit') }}
          </view>
        </view>
      </view>
    </template>

    <view v-else class="udd-empty">
      <wd-icon name="exclamation-circle" size="56rpx" color="#6A6660" />
      <text>{{ t('depositDetail.notFound') }}</text>
      <view class="udd-empty__btn" @click="goNewDeposit">{{ t('depositDetail.backDeposit') }}</view>
    </view>

    <!-- 收款二维码放大 -->
    <QrCodePopup
      v-model="showQrPopup"
      :content="order?.depositAddress || ''"
      :title="t('depositDetail.scanTip')"
      :root-portal="false"
    />
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.udd-page {
  @include page-shell;
  position: relative;
  min-height: 100vh;
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
  overflow: hidden;
}

.udd-page__glow {
  @include page-top-glow;
  height: 320rpx;

  &--gold {
    height: 420rpx;
    background: radial-gradient(ellipse 90% 60% at 50% -10%, rgba(201, 180, 138, 0.1) 0%, transparent 72%);
  }
}

.udd-loading,
.udd-empty {
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

.udd-empty__btn {
  margin-top: 12rpx;
  padding: 0 40rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: $gold-light;
  background: rgba(201, 180, 138, 0.12);
  border: 1rpx solid rgba(201, 180, 138, 0.22);
}

.udd-body {
  position: relative;
  z-index: 1;
  padding: 8rpx 28rpx 0;
}

.udd-panel {
  margin-top: 8rpx;
  padding: 0 28rpx 28rpx;
  border-radius: 20rpx;
  background: rgba(var(--surface-2-rgb), 0.72);
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  overflow: hidden;

  &--order {
    padding-top: 0;
  }
}

.udd-section {
  padding: 24rpx 0 0;
  border-top: 1rpx solid rgba(255, 255, 255, 0.05);

  &__title {
    @include label-caps;
    display: block;
    margin-bottom: 18rpx;
    color: $text-secondary;
  }

  &__hint {
    display: block;
    margin: -8rpx 0 16rpx;
    font-size: 22rpx;
    color: $text-muted;
    line-height: 1.6;
  }

  &--proof {
    border-bottom: none;
  }
}

.udd-input-shell {
  @include form-input-deep;

  &--mono {
    :deep(.wd-input) {
      background: $surface-3 !important;
      border: 1rpx solid $border-subtle !important;
      border-radius: 12rpx !important;
      padding: 22rpx 24rpx !important;
      min-height: 88rpx;
    }

    :deep(.wd-input__inner),
    :deep(.wd-input__value),
    :deep(input),
    :deep(.uni-input-input) {
      height: 44rpx !important;
      min-height: 44rpx !important;
      font-size: 24rpx !important;
      line-height: 44rpx !important;
      font-weight: 400 !important;
      font-family: ui-monospace, monospace;
      color: $text-primary !important;
    }

    :deep(.wd-input__placeholder) {
      font-size: 24rpx !important;
    }
  }
}

.udd-status-strip {
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin: 0 -28rpx 20rpx;
  padding: 22rpx 24rpx;
  background:
    linear-gradient(155deg, rgba(201, 180, 138, 0.1) 0%, transparent 46%),
    $surface-1;
  border-bottom: 1rpx solid $border-subtle;

  &--pending {
    background:
      linear-gradient(155deg, rgba(201, 180, 138, 0.12) 0%, transparent 50%),
      $surface-1;
  }

  &--confirm {
    background:
      linear-gradient(155deg, rgba(196, 198, 202, 0.08) 0%, transparent 50%),
      $surface-1;
  }

  &--done {
    background:
      linear-gradient(155deg, rgba(130, 184, 153, 0.1) 0%, transparent 50%),
      $surface-1;
  }

  &--expired {
    background:
      linear-gradient(155deg, rgba(201, 122, 122, 0.08) 0%, transparent 50%),
      $surface-1;
  }

  &--closed {
    background:
      linear-gradient(155deg, rgba(255, 255, 255, 0.04) 0%, transparent 50%),
      $surface-1;
  }

  &__icon {
    position: relative;
    flex-shrink: 0;
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.04);
    border: 1rpx solid $border-subtle;
  }

  &--pending &__icon {
    background: rgba(201, 180, 138, 0.1);
    border-color: rgba(201, 180, 138, 0.2);
  }

  &--confirm &__icon {
    background: rgba(196, 198, 202, 0.08);
    border-color: rgba(196, 198, 202, 0.16);
  }

  &--done &__icon {
    background: rgba(130, 184, 153, 0.12);
    border-color: rgba(130, 184, 153, 0.22);
  }

  &--expired &__icon {
    background: rgba(201, 122, 122, 0.1);
    border-color: rgba(201, 122, 122, 0.18);
  }

  &--closed &__icon {
    background: rgba(196, 198, 202, 0.08);
    border-color: rgba(196, 198, 202, 0.16);
  }

  &__pulse {
    position: absolute;
    inset: -4rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(196, 198, 202, 0.35);
    animation: udd-pulse 1.6s ease-out infinite;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: 30rpx;
    font-weight: 500;
    color: $text-highlight;
    letter-spacing: 0.04em;
    margin-bottom: 6rpx;
  }

  &--pending &__title {
    color: $gold-light;
  }

  &--confirm &__title {
    color: $silver;
  }

  &--done &__title {
    color: $success;
  }

  &--expired &__title {
    color: #c97a7a;
  }

  &--closed &__title {
    color: $text-muted;
  }

  &__desc {
    display: block;
    font-size: 22rpx;
    line-height: 1.5;
    color: $text-secondary;
  }

  &__extra {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4rpx;
  }

  &__extra-label {
    font-size: 20rpx;
    color: $text-muted;
    letter-spacing: 0.04em;
  }

  &__extra-value {
    font-size: 32rpx;
    font-weight: 500;
    color: $gold-light;
    @include amount-text;

    &--success {
      font-size: 26rpx;
      color: $success;
    }
  }
}

@keyframes udd-pulse {
  0% {
    transform: scale(1);
    opacity: 0.72;
  }

  70% {
    transform: scale(1.14);
    opacity: 0;
  }

  100% {
    transform: scale(1.14);
    opacity: 0;
  }
}

.udd-pay-card {
  padding: 28rpx;
  margin-bottom: 20rpx;
  border-radius: 18rpx;
  background:
    linear-gradient(160deg, rgba(201, 180, 138, 0.06) 0%, transparent 42%),
    rgba(var(--surface-3-rgb), 0.6);
  border: 1rpx solid rgba(201, 180, 138, 0.18);
  box-shadow: inset 0 1rpx 0 rgba(201, 180, 138, 0.1);

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16rpx;
    margin-bottom: 20rpx;
  }

  &__chain {
    padding: 6rpx 14rpx;
    border-radius: 8rpx;
    font-size: 24rpx;
    font-weight: 500;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.16);
  }

  &__countdown {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: $gold-light;
    @include amount-text;
  }

  &__qr {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 20rpx;
  }

  &__qr-frame {
    padding: 8rpx;
    border-radius: 20rpx;
    background: linear-gradient(145deg, rgba(201, 180, 138, 0.16), rgba(255, 255, 255, 0.04));
    border: 1rpx solid rgba(201, 180, 138, 0.2);
  }

  &__qr-img-wrap {
    display: inline-flex;
    padding: 16rpx;
    border-radius: 14rpx;
    background: #fff;
  }

  &__qr-tip {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: $text-secondary;
    letter-spacing: 0.04em;
  }

  &__qr-warn {
    margin-top: 10rpx;
    padding: 0 12rpx;
    font-size: 22rpx;
    line-height: 1.5;
    text-align: center;
    color: #c97a7a;
  }

  &__addr {
    padding: 16rpx 18rpx;
    border-radius: 12rpx;
    background: rgba(255, 255, 255, 0.03);
    border: 1rpx solid $border-subtle;
  }

  &__addr-body {
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
  }

  &__addr-text {
    flex: 1;
    min-width: 0;
    font-size: 22rpx;
    line-height: 1.55;
    font-family: ui-monospace, monospace;
    color: $text-primary;
    word-break: break-all;
  }

  &__copy {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 6rpx;
    font-size: 22rpx;
    color: $gold;
  }

  &__divider {
    height: 1rpx;
    margin: 24rpx 0;
    background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.2), transparent);
  }

  &__amount-row {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16rpx;
  }

  &__amount-label {
    font-size: 24rpx;
    color: $text-secondary;
    padding-bottom: 8rpx;
  }

  &__amount-value {
    display: flex;
    align-items: baseline;
    gap: 8rpx;
  }

  &__usdt {
    font-size: 48rpx;
    font-weight: 500;
    color: $text-highlight;
    @include amount-text;
  }

  &__unit {
    font-size: 22rpx;
    color: $text-muted;
  }

  &__credit {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: $text-muted;
    text-align: right;

    text {
      color: $gold-light;
      font-weight: 500;
    }
  }
}

.udd-info-card {
  padding: 8rpx 0 4rpx;
  margin-bottom: 8rpx;

  &__toggle {
    @include gold-link;
    display: block;
    margin-top: 8rpx;
    text-align: center;
    font-size: 22rpx;
  }
}

.udd-info-row {
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

    &--highlight {
      font-size: 28rpx;
      font-weight: 500;
      color: $gold-light;
      @include amount-text;
    }
  }

  &--stack {
    align-items: flex-start;

    .udd-info-row__value {
      flex-direction: column;
      align-items: flex-end;
    }
  }

  &__copy {
    @include info-row-copy;
  }

  &__mono {
    flex: 1;
    min-width: 0;
    font-size: 22rpx;
    font-family: ui-monospace, monospace;
    color: $silver;
    text-align: right;
    word-break: break-all;
  }
}

.udd-result-card {
  padding: 28rpx;
  margin-bottom: 20rpx;
  border-radius: 18rpx;
  background:
    linear-gradient(160deg, rgba(201, 180, 138, 0.06) 0%, transparent 42%),
    rgba(var(--surface-3-rgb), 0.6);
  border: 1rpx solid rgba(201, 180, 138, 0.18);
  box-shadow: inset 0 1rpx 0 rgba(201, 180, 138, 0.1);

  &--done {
    border-color: rgba(130, 184, 153, 0.22);
    background:
      linear-gradient(160deg, rgba(130, 184, 153, 0.08) 0%, transparent 42%),
      rgba(var(--surface-3-rgb), 0.6);
  }

  &--closed {
    border-color: $border-subtle;
    background:
      linear-gradient(160deg, rgba(255, 255, 255, 0.03) 0%, transparent 42%),
      rgba(var(--surface-3-rgb), 0.6);
  }

  &__chain {
    display: inline-flex;
    padding: 6rpx 14rpx;
    margin-bottom: 20rpx;
    border-radius: 8rpx;
    font-size: 24rpx;
    font-weight: 500;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.16);
  }

  &__sub {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: $text-secondary;
    text-align: right;
  }

  &__credit {
    margin-top: 16rpx;
    font-size: 28rpx;
    color: $text-muted;
    text-align: right;

    text {
      color: $gold-light;
      font-weight: 500;
      @include amount-text;
    }
  }

  &__meta {
    display: block;
    margin-top: 14rpx;
    font-size: 22rpx;
    color: $text-muted;
    text-align: right;
    line-height: 1.5;
  }

  &__note {
    display: block;
    margin-top: 16rpx;
    padding: 16rpx 18rpx;
    border-radius: 12rpx;
    font-size: 24rpx;
    line-height: 1.6;
    color: $text-secondary;
    text-align: left;
    background: rgba(255, 255, 255, 0.03);
    border: 1rpx solid $border-subtle;
  }
}

.udd-detail-block {
  padding: 8rpx 0 16rpx;
  margin-bottom: 8rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

  &:last-child {
    margin-bottom: 0;
    border-bottom: none;
  }

  &__title {
    @include label-caps;
    display: block;
    margin-bottom: 4rpx;
    color: $text-secondary;
  }
}

.udd-submit-bar {
  @include form-submit-bar;

  &__inner {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }
}

.udd-submit-btn {
  @include btn-gold-block;
  flex: 1;
}
</style>
