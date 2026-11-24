<script setup lang="ts">
import type { PaymentAccount } from '@/api/types'
import { apiData } from '@/api/http'
import { paymentAccountApi } from '@/api/modules'
import { useMemberStore } from '@/store/member'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import { paymentAccountListRows } from '@/utils/paymentAccount'

definePage({
  name: 'paymentAccountList',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const toast = useGlobalToast()
const message = useMessage()
const memberStore = useMemberStore()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()



const DEFAULT_LOGO = '/static/img/payment/default.svg'
const list = ref<PaymentAccount[]>([])
const authState = ref('')
const pageLoading = ref(true)
const loadError = ref(false)

const noData = computed(() => !pageLoading.value && !loadError.value && list.value.length === 0)

onShow(() => {
  loadMember()
  loadList()
})

onPullDownRefresh(() => {
  Promise.all([loadMember(), loadList(true)]).finally(() => {
    uni.stopPullDownRefresh()
  })
})

async function loadMember() {
  try {
    const info = await memberStore.ensureMemberInfo()
    authState.value = info.authenticationStatus || ''
  }
  catch {
    authState.value = ''
  }
}

async function loadList(fromPull = false) {
  const silent = !fromPull && list.value.length > 0
  if (!silent) {
    pageLoading.value = true
  }
  loadError.value = false
  try {
    list.value = await apiData(paymentAccountApi.findAll()) || []
  }
  catch {
    list.value = []
    loadError.value = true
  }
  finally {
    pageLoading.value = false
  }
}

function previewQrcode(url?: string) {
  if (url) {
    uni.previewImage({ urls: [url] })
  }
}

function accountTypeName(item: PaymentAccount) {
  return item.paymentAccountTypeName
    || item.paymentAccountTypeCode
    || '—'
}

function logoSrc(item: PaymentAccount) {
  return item.paymentAccountTypeLogo || DEFAULT_LOGO
}

function listRows(item: PaymentAccount) {
  return paymentAccountListRows(item, t('paymentAccount.realName'))
}

function todayReceivedText(item: PaymentAccount) {
  const amount = item.todayReceivedAmount ?? 0
  const count = item.todayReceivedCount ?? 0
  return t('paymentAccount.todayReceivedValue', { amount, count })
}

const togglingId = ref('')

/** Switch 状态切换：true→启用、false→停用；失败由后端错误提示并刷新恢复真实状态 */
async function onSwitchChange(item: PaymentAccount, checked: boolean) {
  if (togglingId.value) {
    return
  }
  togglingId.value = item.id
  try {
    if (checked) {
      await apiData(paymentAccountApi.enable(item.id))
    }
    else {
      await apiData(paymentAccountApi.disable(item.id))
    }
    toast.success({ msg: checked ? t('paymentAccount.enabled') : t('paymentAccount.disabled') })
    await loadList()
  }
  finally {
    togglingId.value = ''
  }
}

/** 列表删除入口（二次确认；被进行中挂单绑定等失败由后端提示） */
function confirmDel(item: PaymentAccount) {
  message.confirm({ title: t('paymentAccount.deleteConfirmTitle'), msg: t('paymentAccount.deleteConfirmMsg') }).then(async (ok) => {
    if (!ok)
      return
    await apiData(paymentAccountApi.del(item.id))
    toast.success({ msg: t('paymentAccount.deleted') })
    await loadList()
  }).catch(() => {
    // 用户点击取消，不做任何处理
  })
}

function goDetail(item: PaymentAccount) {
  router.push({ name: 'paymentAccountDetail', query: { id: item.id } })
}

function goAdd() {
  if (authState.value === '1') {
    message.confirm({ title: t('paymentAccount.deleteConfirmTitle'), msg: t('paymentAccount.realNameFirst'), confirmButtonText: t('paymentAccount.goVerify') }).then((ok) => {
      if (ok)
        router.push({ name: 'realNameAuth' })
    })
    return
  }
  if (authState.value === '2') {
    toast.info({ msg: t('paymentAccount.verifying') })
    return
  }
  router.push({ name: 'selectPaymentAccountType' })
}
</script>

<template>
  <view class="rpl-page">
    <AppNavBar :title="t('paymentAccount.title')" />
    <view class="rpl-page__glow" />
    <view class="rpl-page__mesh" />

    <view class="rpl-hero rpl-hero--compact">
      <view class="rpl-hero__glow" />
      <view class="rpl-hero__head">
        <view class="rpl-hero__icon-ring">
          <view class="rpl-wallet-icon" />
        </view>
        <view class="rpl-hero__head-text">
          <text class="rpl-hero__title">
            {{ t('paymentAccount.title') }}
          </text>
          <text class="rpl-hero__desc">
            {{ t('paymentAccount.heroDesc1') }}
          </text>
          <text class="rpl-hero__desc">
            {{ t('paymentAccount.heroDesc2') }}
          </text>
        </view>
      </view>
    </view>

    <view class="rpl-body">
      <view v-if="pageLoading" class="rpl-skeleton">
        <view v-for="i in 3" :key="i" class="rpl-skeleton__item" />
      </view>

      <view v-else-if="loadError" class="rpl-state-card">
        <text class="rpl-state-card__title">
          {{ t('common.loadFail') }}
        </text>
        <text class="rpl-state-card__desc">
          {{ t('common.networkError') }}
        </text>
        <view class="rpl-state-card__btn" @click="loadList()">
          {{ t('common.reload') }}
        </view>
      </view>

      <view v-else-if="noData" class="rpl-state-card rpl-state-card--empty">
        <view class="rpl-empty-icon" />
        <text class="rpl-state-card__title">
          {{ t('paymentAccount.empty') }}
        </text>
        <text class="rpl-state-card__desc">
          {{ t('paymentAccount.emptyDesc') }}
        </text>
      </view>

      <view v-else class="rpl-list">
        <view
          v-for="item in list"
          :key="item.id"
          class="rpl-item"
          :class="{ 'rpl-item--disabled': item.status === '2' }"
          @click="goDetail(item)"
        >
          <view class="rpl-item__hairline" />
          <view class="rpl-item__top">
            <view class="rpl-item__icon-wrap">
              <wd-img :src="logoSrc(item)" width="44rpx" height="44rpx" />
            </view>
            <text class="rpl-item__type">
              {{ accountTypeName(item) }}
            </text>
            <view class="rpl-item__top-actions" @click.stop>
              <view class="rpl-item__del" @click.stop="confirmDel(item)">
                <wd-icon name="delete" custom-class="rpl-item__del-icon" />
              </view>
              <wd-switch
                :model-value="item.status === '1'"
                :loading="togglingId === item.id"
                size="40rpx"
                active-color="#c9b48a"
                inactive-color="#46464c"
                @change="(e) => onSwitchChange(item, Boolean(e.value))"
              />
            </view>
          </view>
          <view v-for="row in listRows(item)" :key="row.key" class="rpl-item__kv">
            <text class="rpl-item__kv-label">
              {{ row.label }}
            </text>
            <text v-if="row.value !== undefined" class="rpl-item__kv-value">
              {{ row.value }}
            </text>
            <wd-img
              v-else-if="row.image"
              class="rpl-item__kv-thumb"
              :src="row.image"
              width="64rpx"
              height="64rpx"
              mode="aspectFit"
              radius="8rpx"
              @click.stop="previewQrcode(row.image)"
            />
          </view>
          <view class="rpl-item__kv">
            <text class="rpl-item__kv-label">
              {{ t('paymentAccount.todayReceived') }}
            </text>
            <text class="rpl-item__kv-value rpl-item__kv-value--gold">
              {{ todayReceivedText(item) }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <view class="rpl-submit-bar rpl-submit-bar--fixed">
      <view class="rpl-submit-btn" @click="goAdd">
        {{ t('paymentAccount.add') }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.rpl-page {
  @include page-shell;
  position: relative;
  overflow-x: hidden;
  min-height: 100vh;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.rpl-page__glow {
  @include page-top-glow;
}

.rpl-page__mesh {
  position: absolute;
  top: 80rpx;
  left: 50%;
  width: 560rpx;
  height: 560rpx;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(201, 180, 138, 0.08) 0%, transparent 68%);
  pointer-events: none;
}

.rpl-hero {
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(165deg, rgba(var(--surface-3-rgb), 0.96) 0%, rgba(var(--surface-1-rgb), 0.98) 100%);
  border: 1rpx solid $border-subtle;
  box-shadow:
    inset 0 1rpx 0 $highlight-edge,
    0 16rpx 48rpx rgba(0, 0, 0, 0.28);
  overflow: hidden;

  &__glow {
    position: absolute;
    top: -40rpx;
    right: -20rpx;
    width: 240rpx;
    height: 240rpx;
    background: radial-gradient(circle, rgba(201, 180, 138, 0.14) 0%, transparent 70%);
    pointer-events: none;
  }

  &__head {
    display: flex;
    align-items: flex-start;
    gap: 20rpx;
  }

  &__icon-ring {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: linear-gradient(145deg, rgba(201, 180, 138, 0.16) 0%, rgba(201, 180, 138, 0.04) 100%);
    border: 1rpx solid rgba(201, 180, 138, 0.22);
    box-shadow: 0 0 24rpx rgba(201, 180, 138, 0.12);
  }

  &__head-text {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
    letter-spacing: 0.04em;
    line-height: 1.35;
  }

  &__desc {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    line-height: 1.55;
    color: $text-secondary;
  }
}

.rpl-wallet-icon {
  position: relative;
  width: 32rpx;
  height: 24rpx;
  border: 3rpx solid $gold-light;
  border-radius: 6rpx;
  box-sizing: border-box;

  &::before {
    content: '';
    position: absolute;
    top: 6rpx;
    left: -3rpx;
    right: -3rpx;
    height: 3rpx;
    background: $gold-light;
  }

  &::after {
    content: '';
    position: absolute;
    right: 6rpx;
    top: 12rpx;
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: $gold-light;
  }
}

.rpl-body {
  position: relative;
  z-index: 1;
  padding: 16rpx 28rpx 0;
}

.rpl-skeleton__item {
  height: 180rpx;
  margin-bottom: 16rpx;
  border-radius: 20rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: rpl-shimmer 1.4s ease-in-out infinite;
}

@keyframes rpl-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.rpl-state-card {
  @include dark-card;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 56rpx 32rpx;
  border-radius: 20rpx;
  text-align: center;

  &--empty {
    padding-top: 72rpx;
    padding-bottom: 72rpx;
  }

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

.rpl-empty-icon {
  width: 72rpx;
  height: 72rpx;
  margin-bottom: 20rpx;
  border-radius: 50%;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 24rpx;
    width: 30rpx;
    height: 20rpx;
    border: 3rpx solid $text-muted;
    border-radius: 6rpx;
    box-sizing: border-box;
    transform: translateX(-50%);
  }

  &::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: 18rpx;
    width: 10rpx;
    height: 10rpx;
    border-radius: 50%;
    background: $text-muted;
    transform: translateX(-50%);
  }
}

.rpl-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.rpl-item {
  @include dark-card;
  position: relative;
  padding: 22rpx 24rpx;
  border-radius: 20rpx;

  &__hairline {
    position: absolute;
    top: 0;
    left: 24rpx;
    right: 24rpx;
    height: 1rpx;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(201, 180, 138, 0.35) 50%,
      transparent 100%
    );
  }

  &__head {
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
  }

  &__icon-wrap {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64rpx;
    height: 64rpx;
    border-radius: 14rpx;
    background: $surface-3;
    border: 1rpx solid $border-subtle;
  }

  &__title-wrap {
    flex: 1;
    min-width: 0;
  }

  &__title-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10rpx;
  }

  &__type {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-primary;
  }

  &__name {
    display: block;
    margin-top: 6rpx;
    font-size: 22rpx;
    color: $text-muted;
  }

  &__tag {
    padding: 4rpx 12rpx;
    border-radius: 6rpx;
    font-size: 18rpx;
    letter-spacing: 0.04em;

    &--success {
      color: $success;
      background: $success-soft;
      border: 1rpx solid rgba(130, 184, 153, 0.22);
    }

    &--pending {
      color: $gold-light;
      background: $gold-soft;
      border: 1rpx solid rgba(201, 180, 138, 0.22);
    }

    &--muted {
      color: $text-muted;
      background: $surface-3;
      border: 1rpx solid $border-subtle;
    }
  }

  &__detail {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16rpx;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid $border-subtle;
  }

  &__detail-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  &__stat {
    font-size: 22rpx;
    color: $gold-light;
    @include amount-text;
  }

  &__account {
    flex: 1;
    min-width: 0;
    font-size: 24rpx;
    color: $text-secondary;
    word-break: break-all;
    line-height: 1.5;
  }

  &__qr {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 10rpx 16rpx;
    border-radius: 999rpx;
    font-size: 20rpx;
    color: $gold;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.18);
  }

  &__qr-icon {
    width: 20rpx;
    height: 20rpx;
    border: 2rpx solid $gold;
    border-radius: 2rpx;
    position: relative;

    &::before,
    &::after {
      content: '';
      position: absolute;
      background: $gold;
    }

    &::before {
      top: 3rpx;
      left: 3rpx;
      width: 5rpx;
      height: 5rpx;
      border-radius: 1rpx;
    }

    &::after {
      right: 3rpx;
      bottom: 3rpx;
      width: 4rpx;
      height: 4rpx;
      border-radius: 1rpx;
    }
  }
}

.rpl-submit-bar {
  position: relative;
  z-index: 1;
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));

  &--fixed {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 20;
    padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
    background: linear-gradient(
      180deg,
      rgba(var(--page-bg-rgb), 0) 0%,
      rgba(var(--page-bg-rgb), 0.94) 28%,
      $page-bg 100%
    );
    border-top: 1rpx solid $border-subtle;
    backdrop-filter: blur(12px);
  }
}

.rpl-submit-btn {
  @include btn-gold-block;
}
.rpl-item--disabled {
  opacity: 0.6;
}

.rpl-item__side {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
}

.rpl-item__switch {
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  color: $gold;
  background: $gold-soft;
  border: 1rpx solid rgba(201, 180, 138, 0.22);

  &--enable {
    color: $success;
    background: $success-soft;
    border: 1rpx solid rgba(130, 184, 153, 0.22);
  }
}
.rpl-item__top {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.rpl-item__top-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-left: auto;
  flex-shrink: 0;
}

.rpl-item__del {
  display: flex;
  align-items: center;
  padding: 8rpx;
}

.rpl-item__del-icon {
  font-size: 34rpx;
  color: $text-muted;
}

.rpl-item__kv {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 14rpx;
}

.rpl-item__kv-label {
  flex-shrink: 0;
  font-size: 24rpx;
  color: $text-muted;
}

.rpl-item__kv-value {
  flex: 1;
  min-width: 0;
  text-align: right;
  font-size: 24rpx;
  color: $text-primary;
  word-break: break-all;
}

.rpl-item__kv-value--gold {
  color: $gold-light;
}

.rpl-item__kv-thumb {
  flex-shrink: 0;
}
</style>
