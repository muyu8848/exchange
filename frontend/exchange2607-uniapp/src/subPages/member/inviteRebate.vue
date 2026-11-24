<template>
  <view class="page-root">
    <AppNavBar :title="t('rebateCommon.title')" />
    <view class="page-glow" />

    <!-- Hero：返佣比例主视觉 -->
    <view class="hero">
      <view class="hero__inner">
        <view class="hero__deco" />
        <view class="hero__content">
          <view class="hero__label-row">
            <view class="hero__label-dot" />
            <text class="hero__label">{{ t('rebateCommon.title') }}</text>
          </view>
          <view class="hero__rate">
            <text class="hero__rate-num">{{ rebateRateText }}</text>
            <text class="hero__rate-unit">{{ t('rebateCommon.rebateRate') }}</text>
          </view>
          <text class="hero__sub-text">{{ t('inviteRebate.heroSub') }}</text>
          <view class="hero__actions">
            <view class="hero__btn hero__btn--gold" @click="goRebateRecord">
              <wd-icon name="tag" size="26rpx" />
              <text>{{ t('inviteRebate.rebateRecord') }}</text>
            </view>
            <view class="hero__btn hero__btn--ghost" @click="goInviteFriends">
              <wd-icon name="friends" size="26rpx" />
              <text>{{ t('rebateCommon.myFriends') }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 邀请卡片：二维码 + 邀请码 + 链接 -->
    <view class="invite">
      <view class="invite__qr" @click="showQr = true">
        <image v-if="qrSrc" class="invite__qr-img" :src="qrSrc" mode="aspectFit" />
        <view v-else class="invite__qr-ph" />
      </view>
      <view class="invite__main">
        <text class="invite__label">{{ t('inviteRebate.myInviteCode') }}</text>
        <view class="invite__row">
          <text class="invite__code">{{ memberInviteCode || '—' }}</text>
          <view class="invite__copy" @click="copyInviteCode">
            <wd-icon name="copy" size="22rpx" />
            <text>{{ t('common.copy') }}</text>
          </view>
        </view>
        <text class="invite__label invite__label--mt">{{ t('inviteRebate.inviteLink') }}</text>
        <view class="invite__row">
          <text class="invite__link">{{ inviteLink || '—' }}</text>
          <view class="invite__copy" @click="copyInviteLink">
            <wd-icon name="link" size="22rpx" />
            <text>{{ t('common.copy') }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 返佣规则 -->
    <view class="rule">
      <view class="rule__head">
        <text class="rule__title">{{ t('inviteRebate.ruleTitle') }}</text>
      </view>
      <view class="rule__row">
        <text class="rule__key">{{ t('rebateCommon.rebateRate') }}</text>
        <text class="rule__val">{{ rebateRateText }}</text>
      </view>
      <text class="rule__desc">{{ t('inviteRebate.ruleDesc', { rate: rebateRateText }) }}</text>
    </view>

    <!-- 活动说明 -->
    <view class="intro">
      <text class="intro__title">{{ t('inviteRebate.introTitle') }}</text>
      <view class="intro__item">
        <text class="intro__dot" />
        <text class="intro__text">{{ t('inviteRebate.intro1') }}</text>
      </view>
      <view class="intro__item">
        <text class="intro__dot" />
        <text class="intro__text">{{ t('inviteRebate.intro2', { rate: rebateRateText }) }}</text>
      </view>
      <view class="intro__item">
        <text class="intro__dot" />
        <text class="intro__text">{{ t('inviteRebate.intro3') }}</text>
      </view>
    </view>

    <QrCodePopup v-model="showQr" :content="inviteLink" :title="t('inviteRebate.qrTitle')" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { rebateRecordApi, settingApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useMemberStore } from '@/store/member'
import { copyText } from '@/utils/app'
import { qrCodeImageUrl } from '@/utils/qrcode'
import { useI18n } from 'vue-i18n'
import QrCodePopup from '@/components/QrCodePopup.vue'

definePage({
  name: 'inviteRebate',
  style: {
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const { t } = useI18n()
const router = useRouter()
const memberStore = useMemberStore()
const { memberInfo } = storeToRefs(memberStore)

const memberInviteCode = computed(() => memberInfo.value?.inviteCode || '')

const h5Url = ref('')
const rebateRate = ref<number | null>(null)
const showQr = ref(false)

const rebateRateText = computed(() => {
  if (rebateRate.value == null) return '—'
  const pct = Number(rebateRate.value) * 100
  const text = Number.isInteger(pct) ? String(pct) : pct.toFixed(2).replace(/\.?0+$/, '')
  return `${text}%`
})

const inviteLink = computed(() => {
  const code = memberInviteCode.value
  const base = (h5Url.value || '').replace(/\/+$/, '')
  if (!code || !base) return ''
  return `${base}/subPages/auth/register?inviteCode=${code}`
})

const qrSrc = computed(() => (inviteLink.value ? qrCodeImageUrl(inviteLink.value, 200) : ''))

onShow(() => {
  memberStore.ensureMemberInfo()
  loadInviteMeta()
})

onPullDownRefresh(() => {
  Promise.all([memberStore.ensureMemberInfo(), loadInviteMeta()])
    .catch(() => {})
    .finally(() => uni.stopPullDownRefresh())
})

function loadInviteMeta() {
  return Promise.all([
    apiData(settingApi.getH5Url()).then(d => { h5Url.value = d || '' }),
    apiData(rebateRecordApi.getRebateRate()).then(d => { rebateRate.value = (d ?? null) as number | null }),
  ]).catch(() => {})
}

function onBack() {
  uni.navigateBack()
}

function goRebateRecord() {
  router.push({ name: 'rebateRecord' })
}

function goInviteFriends() {
  router.push({ name: 'inviteFriends' })
}

function copyInviteCode() {
  if (!memberInviteCode.value) return
  copyText(memberInviteCode.value)
}

function copyInviteLink() {
  if (!inviteLink.value) return
  copyText(inviteLink.value)
}
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.page-root {
  @include page-shell;
  position: relative;
  overflow: hidden;
}

.page-glow {
  @include page-top-glow;
}

/* ===== Hero ===== */
.hero {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx 0;
}

.hero__inner {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 88% 10%, rgba(201, 180, 138, 0.14) 0%, transparent 48%),
    linear-gradient(155deg, rgba(201, 180, 138, 0.07) 0%, transparent 50%),
    $surface-1;
  border: 1rpx solid rgba(201, 180, 138, 0.14);
  border-radius: 24rpx;
  padding: 40rpx 36rpx 36rpx;
  box-shadow:
    inset 0 1rpx 0 $highlight-edge,
    0 12rpx 32rpx rgba(0, 0, 0, 0.28);
}

.hero__deco {
  position: absolute;
  top: -80rpx;
  right: -60rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(201, 180, 138, 0.06) 0%, transparent 65%);
  pointer-events: none;
}

.hero__content {
  position: relative;
  z-index: 1;
}

.hero__label-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.hero__label-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: $gold;
  box-shadow: 0 0 8rpx rgba(201, 180, 138, 0.5);
}

.hero__label {
  @include label-caps;
  font-size: 22rpx;
}

.hero__rate {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.hero__rate-num {
  color: $gold-light;
  font-size: 72rpx;
  font-weight: 700;
  line-height: 1.1;
  @include amount-text;
}

.hero__rate-unit {
  font-size: 26rpx;
  color: $text-secondary;
  letter-spacing: 0.04em;
}

.hero__sub-text {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: $text-muted;
  letter-spacing: 0.04em;
}

.hero__actions {
  display: flex;
  gap: 16rpx;
  margin-top: 32rpx;
}

.hero__btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  border-radius: 14rpx;
  font-size: 26rpx;
  letter-spacing: 0.04em;
}

.hero__btn--gold {
  color: $page-bg;
  background: linear-gradient(135deg, #d8c8a8, #b8a078);
  border: 1rpx solid rgba(209, 196, 168, 0.3);
  box-shadow: 0 4rpx 16rpx rgba(201, 180, 138, 0.15);
}

.hero__btn--ghost {
  color: $gold;
  background: $gold-soft;
  border: 1rpx solid rgba(201, 180, 138, 0.28);
}

/* ===== 邀请卡片 ===== */
.invite {
  position: relative;
  z-index: 1;
  margin: 20rpx 28rpx 0;
  display: flex;
  align-items: stretch;
  gap: 24rpx;
  padding: 28rpx 32rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  border-radius: 20rpx;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
}

.invite__qr {
  flex-shrink: 0;
  width: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx;
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid $border-subtle;
}

.invite__qr-img {
  width: 172rpx;
  height: 172rpx;
}

.invite__qr-ph {
  width: 172rpx;
  height: 172rpx;
  border-radius: 8rpx;
  background: $surface-3;
}

.invite__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10rpx;
}

.invite__label {
  @include label-caps;
}

.invite__label--mt {
  margin-top: 8rpx;
}

.invite__row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.invite__code {
  color: $text-highlight;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 0.1em;
  @include amount-text;
}

.invite__link {
  flex: 1;
  min-width: 0;
  font-size: 22rpx;
  color: $text-secondary;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.invite__copy {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: $gold;
  font-size: 22rpx;
  padding: 8rpx 18rpx;
  border: 1rpx solid rgba(201, 180, 138, 0.28);
  border-radius: 999rpx;
  background: $gold-soft;
}

/* ===== 返佣规则 ===== */
.rule {
  position: relative;
  z-index: 1;
  margin: 20rpx 28rpx 0;
  padding: 28rpx 32rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  border-radius: 20rpx;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
}

.rule__head {
  margin-bottom: 16rpx;
}

.rule__title {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
}

.rule__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid $border-subtle;
}

.rule__key {
  font-size: 24rpx;
  color: $text-secondary;
}

.rule__val {
  font-size: 32rpx;
  font-weight: 600;
  color: $gold-light;
  @include amount-text;
}

.rule__desc {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: $text-muted;
}

/* ===== 活动说明 ===== */
.intro {
  position: relative;
  z-index: 1;
  margin: 20rpx 28rpx 48rpx;
  padding: 28rpx 32rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  border-radius: 20rpx;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
}

.intro__title {
  display: block;
  margin-bottom: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
}

.intro__item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 10rpx 0;
}

.intro__dot {
  flex-shrink: 0;
  width: 8rpx;
  height: 8rpx;
  margin-top: 12rpx;
  border-radius: 50%;
  background: $gold;
}

.intro__text {
  flex: 1;
  font-size: 24rpx;
  line-height: 1.7;
  color: $text-secondary;
}
</style>
