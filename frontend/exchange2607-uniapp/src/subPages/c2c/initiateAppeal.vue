<script setup lang="ts">
import type { C2cOrderDetail, C2cSetting } from '@/api/types'
import { c2cApi, dictApi, settingApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import { useAuthStore } from '@/store/auth'
import { copyText } from '@/utils/app'

definePage({
  name: 'initiateAppeal',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const toast = useGlobalToast()
const authStore = useAuthStore()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()



const orderId = ref('')
type AppealInitiator = 'buyer' | 'seller'
const initiator = ref<AppealInitiator>('buyer')
const orderDetail = ref<C2cOrderDetail | null>(null)
const appealType = ref('')
const pageLoading = ref(true)
const loadError = ref(false)
const loading = ref(false)

const appealTypeDict = ref<{ dictItemCode: string; dictItemName: string }[]>([])

const allowedAppealTypeCodes = ref<string[]>([])

const c2cSetting = ref<C2cSetting>({})

const appealTypes = computed(() =>
  appealTypeDict.value
    .filter((item) => allowedAppealTypeCodes.value.includes(item.dictItemCode))
    .map((item) => ({
      value: item.dictItemCode,
      label: dictItemText('c2cAppealType', item.dictItemCode, item.dictItemName),
    })))

/** 申诉理由补充说明 */
const APPEAL_HINT_KEYS: Record<string, string> = {
  OVER_PAID: 'c2cAppeal.hintOverPaid',
  BUYER_UNPAID: 'c2cAppeal.hintBuyerUnpaid',
  NON_OWNER_ACCOUNT: 'c2cAppeal.hintNonOwnerAccount',
}

function appealHint(code: string) {
  const key = APPEAL_HINT_KEYS[code]
  return key ? t(key) : ''
}

const roleLabel = computed(() =>
  initiator.value === 'buyer' ? t('c2cAppeal.roleBuyer') : t('c2cAppeal.roleSeller'))

const counterpartyLabel = computed(() => {
  const d = orderDetail.value
  if (!d) {
    return '—'
  }
  if (initiator.value === 'buyer') {
    return d.sellerNickName || t('c2cCommon.seller')
  }
  return d.buyerNickName || d.buyerRealName || t('c2cCommon.buyer')
})

const canSubmit = computed(() => {
  return !!appealType.value && !!orderId.value && !!orderDetail.value && !loading.value
})

const submitLabel = computed(() => {
  if (loading.value) {
    return t('common.submitting')
  }
  if (!appealType.value) {
    return t('c2cAppeal.selectReason')
  }
  return t('c2cAppeal.submit')
})

onLoad((query) => {
  authStore.loadFromStorage()
  orderId.value = (query?.orderId as string) || ''
  loadAppealTypeDict()
  loadOrder()
  loadAllowedAppealTypes()
  loadC2cSetting()
})

async function loadOrder() {
  if (!orderId.value) {
    pageLoading.value = false
    loadError.value = true
    return
  }
  pageLoading.value = true
  loadError.value = false
  try {
    orderDetail.value = await apiData(c2cApi.getOrderDetail(orderId.value))
    if (orderDetail.value) {
      initiator.value = orderDetail.value.buyerId === authStore.accountId ? 'buyer' : 'seller'
    }
  }
  catch {
    orderDetail.value = null
    loadError.value = true
  }
  finally {
    pageLoading.value = false
  }
}

async function loadAppealTypeDict() {
  try {
    appealTypeDict.value = (await apiData(dictApi.findDictItemInCache('c2cAppealType'))) || []
  }
  catch {
    appealTypeDict.value = []
  }
}

async function loadAllowedAppealTypes() {
  try {
    allowedAppealTypeCodes.value = (await apiData(c2cApi.getAllowedAppealTypes(orderId.value))) || []
  }
  catch {
    allowedAppealTypeCodes.value = []
  }
}

async function loadC2cSetting() {
  try {
    c2cSetting.value = (await apiData(settingApi.getC2cSetting())) || {}
  }
  catch {
    c2cSetting.value = {}
  }
}

function selectType(value: string) {
  appealType.value = value
}

async function submit() {
  if (!canSubmit.value) {
    if (!appealType.value) {
      toast.error({ msg: t('c2cAppeal.selectReason') })
    }
    return
  }
  loading.value = true
  try {
    await apiData(c2cApi.initiateAppeal({
      orderId: orderId.value,
      appealType: appealType.value,
    }))
    toast.success({ msg: t('c2cAppeal.submitted') })
    setTimeout(() => {
      router.replace({
        name: 'orderDetail',
        query: { orderId: orderId.value },
      })
    }, 1200)
  }
  catch {
    // apiData 已弹出后端错误提示（如 5 分钟限制/未支持该申诉），此处仅避免未处理的 Promise rejection
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="ia-page">
    <AppNavBar :title="t('c2cAppeal.title')" />
    <view class="ia-page__glow" />

    <view v-if="pageLoading" class="ia-order-card ia-order-card--skeleton">
      <view class="ia-skeleton__line ia-skeleton__line--short" />
      <view class="ia-skeleton__line" />
      <view class="ia-skeleton__line ia-skeleton__line--mid" />
    </view>

    <view v-else-if="loadError" class="ia-state-card">
      <text class="ia-state-card__title">{{ t('c2cAppeal.loadFail') }}</text>
      <text class="ia-state-card__desc">{{ t('c2cAppeal.loadFailDesc') }}</text>
      <view class="ia-state-card__btn" @click="loadOrder()">
        {{ t('common.reload') }}
      </view>
    </view>

    <view v-else-if="orderDetail" class="ia-order-card">
      <view class="ia-order-card__head">
        <view class="ia-order-card__badge">{{ roleLabel }}</view>
        <text class="ia-order-card__status">{{ dictItemText('c2cOrderStatus', orderDetail.status, orderDetail.statusName) }}</text>
      </view>
      <view class="ia-order-card__amount">
        <text class="ia-order-card__value">{{ orderDetail.fiatAmount }}</text>
        <text class="ia-order-card__unit">{{ orderDetail.fiatCode }}</text>
      </view>
      <view class="ia-order-card__sub">
        <text class="ia-order-card__sub-label">{{ t('c2cAppeal.tradeAmount') }}</text>
        <text class="ia-order-card__sub-value">{{ orderDetail.amount }} {{ orderDetail.assetCode }}</text>
      </view>
      <view class="ia-order-card__row">
        <text class="ia-order-card__label">{{ t('c2cCommon.orderNo') }}</text>
        <text class="ia-order-card__mono">{{ orderDetail.orderNo }}</text>
        <text class="ia-order-card__copy" @click="copyText(orderDetail.orderNo)">{{ t('common.copy') }}</text>
      </view>
      <view class="ia-order-card__row">
        <text class="ia-order-card__label">{{ initiator === 'buyer' ? t('c2cCommon.seller') : t('c2cCommon.buyer') }}</text>
        <text class="ia-order-card__text">{{ counterpartyLabel }}</text>
      </view>
    </view>

    <view class="ia-notice">
      <view class="ia-notice__row">
        <text class="ia-notice__index">1.</text>
        <text>{{ t('c2cAppeal.notice1') }}</text>
      </view>
      <view class="ia-notice__row">
        <text class="ia-notice__index">2.</text>
        <text>{{ t('c2cAppeal.notice2') }}</text>
      </view>
      <view class="ia-notice__row">
        <text class="ia-notice__index">3.</text>
        <text>{{ t('c2cAppeal.notice3', { minutes: c2cSetting.appealIntervalMinutes ?? 5 }) }}</text>
      </view>
    </view>

    <view class="ia-form-card" :class="{ 'ia-form-card--disabled': loadError || pageLoading }">
      <text class="ia-form-card__label">{{ t('c2cAppeal.reasonLabel') }}</text>
      <view class="ia-type-list">
        <view
          v-for="item in appealTypes"
          :key="item.value"
          class="ia-type-item"
          :class="{ 'ia-type-item--active': appealType === item.value }"
          @click="selectType(item.value)"
        >
          <view class="ia-type-item__radio" :class="{ 'ia-type-item__radio--checked': appealType === item.value }" />
          <view class="ia-type-item__body">
            <text class="ia-type-item__label">{{ item.label }}</text>
            <text v-if="appealHint(item.value)" class="ia-type-item__hint">{{ appealHint(item.value) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="form-submit-bar">
      <view
        class="form-submit-btn form-submit-btn--warning"
        :class="{ 'form-submit-btn--disabled': !canSubmit }"
        @click="submit"
      >
        {{ submitLabel }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.ia-page {
  @include form-page;
}

.ia-page__glow {
  @include page-top-glow;
}

.ia-order-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  margin: 24rpx 28rpx 0;
  padding: 24rpx 28rpx;
  border-left: 4rpx solid #d0a868;

  &--skeleton {
    min-height: 180rpx;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16rpx;
    margin-bottom: 16rpx;
  }

  &__badge {
    padding: 6rpx 16rpx;
    border-radius: 999rpx;
    font-size: 20rpx;
    color: #d0a868;
    background: rgba(208, 168, 104, 0.12);
    border: 1rpx solid rgba(208, 168, 104, 0.28);
    letter-spacing: 0.04em;
  }

  &__status {
    font-size: 22rpx;
    color: $text-muted;
  }

  &__amount {
    display: flex;
    align-items: baseline;
    gap: 8rpx;
    margin-bottom: 12rpx;
  }

  &__sub {
    display: flex;
    align-items: baseline;
    gap: 12rpx;
    margin-bottom: 20rpx;
  }

  &__sub-label {
    flex-shrink: 0;
    font-size: 22rpx;
    color: $text-muted;
  }

  &__sub-value {
    font-size: 26rpx;
    color: $text-secondary;
  }

  &__value {
    font-size: 44rpx;
    font-weight: 600;
    color: $text-highlight;
    line-height: 1.1;
  }

  &__unit {
    font-size: 24rpx;
    color: $text-secondary;
  }

  &__row {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 10rpx 0;

    & + & {
      border-top: 1rpx solid $border-subtle;
    }
  }

  &__label {
    flex-shrink: 0;
    width: 120rpx;
    font-size: 24rpx;
    color: $text-muted;
  }

  &__mono {
    flex: 1;
    min-width: 0;
    font-family: ui-monospace, monospace;
    font-size: 24rpx;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__text {
    flex: 1;
    min-width: 0;
    font-size: 26rpx;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__copy {
    flex-shrink: 0;
    font-size: 22rpx;
    color: $gold;
  }
}

.ia-skeleton__line {
  height: 28rpx;
  margin-bottom: 16rpx;
  border-radius: 8rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: ia-shimmer 1.4s ease-in-out infinite;

  &--short {
    width: 40%;
  }

  &--mid {
    width: 72%;
    margin-bottom: 0;
  }
}

@keyframes ia-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.ia-state-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 24rpx 28rpx 0;
  padding: 56rpx 32rpx;
  border-radius: 20rpx;
  text-align: center;

  &__title {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-primary;
  }

  &__desc {
    margin-top: 12rpx;
    font-size: 22rpx;
    line-height: 1.6;
    color: $text-muted;
  }

  &__btn {
    margin-top: 28rpx;
    padding: 16rpx 40rpx;
    border-radius: 999rpx;
    font-size: 24rpx;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.22);
  }
}

.ia-notice {
  @include notice-banner;
}

.ia-notice__row {
  display: flex;
  gap: 8rpx;
  font-size: 24rpx;
  line-height: 1.55;
  color: $text-secondary;

  & + & {
    margin-top: 8rpx;
  }
}

.ia-notice__index {
  flex-shrink: 0;
  color: $gold;
}

.ia-form-card {
  @include form-card;
  padding-top: 20rpx;

  &--textarea {
    @include form-textarea-deep;
  }

  &--disabled {
    opacity: 0.5;
    pointer-events: none;
  }

  &__label {
    @include form-field-label;
    margin-bottom: 12rpx;
  }
}

.ia-type-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.ia-type-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 22rpx 20rpx;
  border-radius: 12rpx;
  border: 1rpx solid $border-subtle;
  background: $surface-3;
  transition: border-color 0.15s, background 0.15s;

  &:active {
    background: rgba(255, 255, 255, 0.03);
  }

  &--active {
    border-color: rgba(208, 168, 104, 0.42);
    background: rgba(208, 168, 104, 0.08);
  }
}

.ia-type-item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.ia-type-item__radio {
  flex-shrink: 0;
  width: 32rpx;
  height: 32rpx;
  margin-top: 2rpx;
  border-radius: 50%;
  border: 2rpx solid $text-muted;
  box-sizing: border-box;
  transition: border-color 0.15s;

  &--checked {
    border-color: #d0a868;
    background: radial-gradient(circle, #d0a868 0%, #d0a868 38%, transparent 40%);
  }
}

.ia-type-item__label {
  font-size: 28rpx;
  line-height: 1.45;
  color: $text-primary;
}

.ia-type-item__hint {
  font-size: 22rpx;
  line-height: 1.4;
  color: $text-muted;
}

.form-submit-bar {
  @include form-submit-bar;
}

.form-submit-btn {
  flex: none;
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #1a1a1a;
  background: linear-gradient(180deg, #f0d9a8 0%, #d0a868 100%);
  box-shadow: 0 4rpx 16rpx rgba(208, 168, 104, 0.24);

  &--warning {
    height: 88rpx;
  }

  &--disabled {
    opacity: 0.42;
    pointer-events: none;
  }
}
</style>
