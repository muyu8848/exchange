<script setup lang="ts">
import type { C2cOrderDetail } from '@/api/types'
import { c2cApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import { useAuthStore } from '@/store/auth'

definePage({
  name: 'cancelOrder',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const toast = useGlobalToast()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()


const orderId = ref('')
const orderDetail = ref<C2cOrderDetail | null>(null)
const pageLoading = ref(true)
const loadError = ref(false)
const submitting = ref(false)

/** 一级选择：直接取消 / 卖家原因（协议取消） */
type Level1 = 'direct' | 'protocol' | null
const selectedLevel1 = ref<Level1>(null)
/** 选卖家原因时的子原因 code */
const selectedSubReason = ref('')

const hasSubReasons = computed(() => selectedLevel1.value === 'protocol')
const canSubmit = computed(() => {
  if (selectedLevel1.value === 'direct')
    return !!orderDetail.value
  if (selectedLevel1.value === 'protocol' && selectedSubReason.value)
    return !!orderDetail.value
  return false
})
const isPendingPay = computed(() => orderDetail.value?.status === '1')

/* ── 角色判断 ── */
const authStore = useAuthStore()
const isBuyer = computed(() => orderDetail.value?.buyerId === authStore.accountId)
const isSeller = computed(() => orderDetail.value?.sellerId === authStore.accountId)

/* ── 子原因数据 ── */
/** 买家协议取消原因（卖家过错），文案走 dictItem.c2cCancelReason */
const BUYER_PROTOCOL_CODES = ['SELLER_EXTRA_FEE', 'SELLER_PAY_PROBLEM', 'SELLER_REFUNDED', 'SELLER_UNREACHABLE']
/** 卖家协议取消原因（买家过错） */
const SELLER_PROTOCOL_CODES = ['BUYER_OFF_PLATFORM', 'BUYER_UNREACHABLE']
/** 按当前角色展示对应的协议取消原因 */
const PROTOCOL_REASONS = computed(() =>
  (isSeller.value ? SELLER_PROTOCOL_CODES : BUYER_PROTOCOL_CODES).map(code => ({
    code,
    label: dictItemText('c2cCancelReason', code),
  })))

onLoad((query) => {
  authStore.loadFromStorage()
  orderId.value = (query?.orderId as string) || ''
  loadOrder()
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
  }
  catch {
    orderDetail.value = null
    loadError.value = true
  }
  finally {
    pageLoading.value = false
  }
}

function selectLevel1(level: Level1) {
  if (submitting.value)
    return
  selectedLevel1.value = level
  if (level !== 'protocol')
    selectedSubReason.value = ''
}

function selectSubReason(code: string) {
  if (submitting.value)
    return
  selectedSubReason.value = code
}

function goBack() {
  router.back()
}

/* ── 确认直接取消 ── */
async function confirmDirect() {
  if (!canSubmit.value || submitting.value)
    return
  submitting.value = true
  try {
    await apiData(c2cApi.buyerCancelTrade({
      orderId: orderId.value,
      cancelReason: 'BUYER_NO_PAY',
    }))
    toast.success({ msg: t('c2cCancelOrder.cancelled') })
    setTimeout(() => goBack(), 800)
  }
  catch (e: any) {
    e?.message && toast.error({ msg: e.message })
  }
  finally {
    submitting.value = false
  }
}

/* ── 申请协议取消 ── */
async function confirmProtocol() {
  if (!canSubmit.value || submitting.value)
    return
  submitting.value = true
  try {
    if (isSeller.value) {
      await apiData(c2cApi.submitSellerProtocolCancel({
        orderId: orderId.value,
        cancelReason: selectedSubReason.value,
      }))
      toast.success({ msg: t('c2cCancelOrder.submittedToBuyer') })
    }
    else {
      await apiData(c2cApi.submitBuyerProtocolCancel({
        orderId: orderId.value,
        cancelReason: selectedSubReason.value,
      }))
      toast.success({ msg: t('c2cCancelOrder.submittedToSeller') })
    }
    setTimeout(() => goBack(), 800)
  }
  catch (e: any) {
    e?.message && toast.error({ msg: e.message })
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="co-page">
    <AppNavBar :title="t('c2cCancelOrder.title')" />
    <view class="co-page__glow" />

    <!-- 加载态 -->
    <view v-if="pageLoading" class="co-skeleton">
      <view class="co-skeleton__line co-skeleton__line--half" />
      <view class="co-skeleton__line" />
      <view class="co-skeleton__line" />
      <view class="co-skeleton__line co-skeleton__line--three" />
    </view>

    <!-- 加载失败 -->
    <view v-else-if="loadError" class="co-state">
      <text class="co-state__title">{{ t('c2cCancelOrder.loadFail') }}</text>
      <text class="co-state__desc">{{ t('c2cCancelOrder.loadFailDesc') }}</text>
      <view class="co-state__btn" @click="loadOrder">{{ t('common.reload') }}</view>
    </view>

    <!-- 主内容 -->
    <view v-else class="co-content">
      <!-- 说明卡片 -->
      <view class="co-notice">
        <view class="co-notice__item">
          <text class="co-notice__num">1</text>
          <text class="co-notice__text">{{ t('c2cCancelOrder.notice1') }}</text>
        </view>
        <view v-if="isBuyer" class="co-notice__item">
          <text class="co-notice__num">2</text>
          <text class="co-notice__text">{{ t('c2cCancelOrder.notice2') }}</text>
        </view>
        <view v-if="isSeller" class="co-notice__item">
          <text class="co-notice__num">3</text>
          <text class="co-notice__text">{{ t('c2cCancelOrder.notice3') }}</text>
        </view>
      </view>

      <text class="co-title">{{ t('c2cCancelOrder.selectReasonTitle') }}</text>

      <!-- 一级选择：卡片 -->
      <view class="co-card">
        <!-- 选项1：直接取消（仅买家，卖家不可单方直撤） -->
        <view
          v-if="isBuyer"
          class="co-card__item"
          :class="{ 'co-card__item--active': selectedLevel1 === 'direct' }"
          @click="selectLevel1('direct')"
        >
          <view class="co-card__radio">
            <view v-if="selectedLevel1 === 'direct'" class="co-card__radio-dot" />
          </view>
          <view class="co-card__body">
            <text class="co-card__title">{{ t('c2cCancelOrder.directTitle') }}</text>
            <text class="co-card__desc">{{ t('c2cCancelOrder.directDesc') }}</text>
          </view>
        </view>

        <!-- 选项2：卖家原因无责取消 -->
        <view
          class="co-card__item"
          :class="{ 'co-card__item--active': selectedLevel1 === 'protocol' }"
          @click="selectLevel1('protocol')"
        >
          <view class="co-card__radio">
            <view v-if="selectedLevel1 === 'protocol'" class="co-card__radio-dot" />
          </view>
          <view class="co-card__body">
            <text class="co-card__title">{{ isSeller ? t('c2cCancelOrder.protocolTitleSeller') : t('c2cCancelOrder.protocolTitleBuyer') }}</text>
            <text class="co-card__desc">{{ t('c2cCancelOrder.protocolDesc') }}</text>
          </view>
        </view>
      </view>

      <!-- 子原因：选卖家原因后缩进展开 -->
      <view v-if="hasSubReasons" class="co-sub">
        <text class="co-sub__label">{{ t('c2cCancelOrder.subReasonLabel') }}</text>
        <view
          v-for="reason in PROTOCOL_REASONS"
          :key="reason.code"
          class="co-sub__item"
          :class="{ 'co-sub__item--active': selectedSubReason === reason.code }"
          @click="selectSubReason(reason.code)"
        >
          <view class="co-sub__radio">
            <view v-if="selectedSubReason === reason.code" class="co-sub__radio-dot" />
          </view>
          <text class="co-sub__text">{{ reason.label }}</text>
        </view>
      </view>

      <!-- 底部操作栏（按钮基于选择状态切换，无返回按钮） -->
      <view v-if="canSubmit" class="co-actions">
        <view
          v-if="selectedLevel1 === 'direct'"
          class="co-actions__btn co-actions__btn--primary"
          :class="{ 'co-actions__btn--disabled': submitting }"
          @click="confirmDirect"
        >
          {{ submitting ? t('common.submitting') : t('c2cCancelOrder.confirmCancel') }}
        </view>
        <view
          v-else-if="selectedLevel1 === 'protocol' && selectedSubReason"
          class="co-actions__btn co-actions__btn--primary"
          :class="{ 'co-actions__btn--disabled': submitting }"
          @click="confirmProtocol"
        >
          {{ submitting ? t('common.submitting') : t('c2cCancelOrder.applyProtocol') }}
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.co-page {
  @include form-page;
}

.co-page__glow {
  @include page-top-glow;
}

/* ── 骨架屏 ── */
.co-skeleton {
  padding: 40rpx 28rpx 0;

  &__line {
    height: 72rpx;
    margin-bottom: 16rpx;
    border-radius: 10rpx;
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.04) 25%, rgba(255, 255, 255, 0.07) 50%, rgba(255, 255, 255, 0.04) 75%);
    background-size: 200% 100%;
    animation: co-shimmer 1.5s infinite;

    &--half {
      width: 50%;
      height: 36rpx;
      margin-bottom: 32rpx;
    }

    &--three {
      width: 70%;
    }
  }
}

@keyframes co-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ── 错误状态 ── */
.co-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 120rpx 40rpx;

  &__title {
    font-size: 30rpx;
    font-weight: 500;
    color: $text-primary;
  }

  &__desc {
    font-size: 24rpx;
    color: $text-muted;
  }

  &__btn {
    margin-top: 16rpx;
    padding: 14rpx 40rpx;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: $gold;
    background: rgba(201, 180, 138, 0.1);
    border: 1rpx solid rgba(201, 180, 138, 0.3);
  }
}

/* ── 主内容 ── */
.co-content {
  position: relative;
  z-index: 1;
  padding: 32rpx 28rpx 0;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

/* ── 说明卡片 ── */
.co-notice {
  position: relative;
  z-index: 1;
  padding: 24rpx 24rpx 24rpx 28rpx;
  margin-bottom: 32rpx;
  border-radius: 12rpx;
  background: rgba(230, 168, 23, 0.06);
  border: 1rpx solid rgba(230, 168, 23, 0.18);
  border-left: 4rpx solid #e6a817;

  &__item {
    display: flex;
    align-items: flex-start;
    gap: 14rpx;

    & + & {
      margin-top: 16rpx;
    }
  }

  &__num {
    flex-shrink: 0;
    width: 32rpx;
    height: 32rpx;
    line-height: 32rpx;
    text-align: center;
    border-radius: 50%;
    font-size: 20rpx;
    font-weight: 600;
    color: #1a1a1c;
    background: rgba(230, 168, 23, 0.7);
    margin-top: 2rpx;
  }

  &__text {
    flex: 1;
    font-size: 24rpx;
    color: $text-secondary;
    line-height: 1.6;
  }
}

.co-title {
  display: block;
  font-size: 34rpx;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 20rpx;
}

/* ── 一级选择卡片 ── */
.co-card {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-bottom: 20rpx;

  &__item {
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
    padding: 22rpx 20rpx;
    border-radius: 12rpx;
    background: rgba(255, 255, 255, 0.035);
    border: 1.5rpx solid transparent;
    transition: all 0.15s ease;

    &:active {
      background: rgba(255, 255, 255, 0.06);
    }

    &--active {
      border-color: rgba(201, 180, 138, 0.45);
      background: rgba(201, 180, 138, 0.07);
    }
  }

  &__radio {
    flex-shrink: 0;
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 2rpx solid $text-muted;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 2rpx;
  }

  &__radio-dot {
    width: 18rpx;
    height: 18rpx;
    border-radius: 50%;
    background: $gold;
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5rpx;
  }

  &__title {
    font-size: 28rpx;
    color: $text-primary;
    font-weight: 500;
    line-height: 1.4;
  }

  &__desc {
    font-size: 24rpx;
    color: $text-muted;
    line-height: 1.4;
  }
}

/* ── 子原因区域（缩进） ── */
.co-sub {
  margin-bottom: 24rpx;
  margin-left: 20rpx;
  padding: 18rpx 18rpx 4rpx;
  border-radius: 10rpx;
  background: rgba(255, 255, 255, 0.025);

  &__label {
    display: block;
    font-size: 22rpx;
    color: $text-muted;
    margin-bottom: 12rpx;
    padding-left: 4rpx;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 14rpx;
    padding: 16rpx 14rpx;
    margin-bottom: 8rpx;
    border-radius: 8rpx;
    background: rgba(255, 255, 255, 0.03);
    border: 1rpx solid transparent;
    transition: all 0.15s ease;

    &:active {
      background: rgba(255, 255, 255, 0.055);
    }

    &--active {
      border-color: rgba(201, 180, 138, 0.35);
      background: rgba(201, 180, 138, 0.07);
    }
  }

  &__radio {
    flex-shrink: 0;
    width: 28rpx;
    height: 28rpx;
    border-radius: 50%;
    border: 2rpx solid $text-muted;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__radio-dot {
    width: 14rpx;
    height: 14rpx;
    border-radius: 50%;
    background: $gold;
  }

  &__text {
    font-size: 25rpx;
    color: $text-primary;
    line-height: 1.4;
  }
}

/* ── 底部操作栏（仅操作按钮） ── */
.co-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  display: flex;
  gap: 20rpx;
  padding: 24rpx 28rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(var(--surface-1-rgb), 0.95);
  backdrop-filter: blur(12px);
  border-top: 1rpx solid $border-subtle;

  &__btn {
    flex: 1;
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10rpx;
    font-size: 28rpx;
    font-weight: 500;
    letter-spacing: 0.02em;
    transition: all 0.15s ease;

    &--primary {
      color: #1a1a1c;
      background: linear-gradient(135deg, #d4b97a 0%, #c9b48a 100%);
    }

    &--disabled {
      opacity: 0.42;
      pointer-events: none;
    }

    &:active {
      transform: scale(0.98);
    }
  }
}
</style>
