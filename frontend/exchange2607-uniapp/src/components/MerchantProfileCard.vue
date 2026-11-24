<script setup lang="ts">
import type { MerchantProfileVO } from '@/api/types'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  profile: MerchantProfileVO | null
}>()

const { t } = useI18n()

/** 模板内便捷引用（computed 在模板中自动解包） */
const profile = computed(() => props.profile)

/** 只展示已认证的资质：未认证项不渲染，避免灰色 tag 稀释可信感 */
const verifiedBadges = computed(() => {
  const p = props.profile
  if (!p) {
    return []
  }
  return [
    { key: 'email', label: t('c2cMerchantCard.emailVerified'), ok: p.verifiedEmail },
    { key: 'phone', label: t('c2cMerchantCard.phoneVerified'), ok: p.verifiedPhone },
    { key: 'identity', label: t('c2cMerchantCard.identityVerified'), ok: p.verifiedIdentity },
    { key: 'advanced', label: t('c2cMerchantCard.advancedVerified'), ok: p.verifiedAdvanced },
  ].filter(b => b.ok)
})

/** 格式化数字 */
function formatNum(v?: number): string {
  if (!v && v !== 0) {
    return '—'
  }
  return String(v)
}

/** 格式化百分比 */
function formatRate(v?: number): string {
  if (!v && v !== 0) {
    return '—'
  }
  return `${v}%`
}

/** 取首字符作为头像 */
function firstChar(name?: string): string {
  return name?.charAt(0) || '?'
}

/** 完成率颜色：绿/黄/红三档 */
function rateColorClass(v?: number): string {
  if (!v && v !== 0) {
    return ''
  }
  if (v >= 90) {
    return 'mpc__c-good'
  }
  if (v >= 70) {
    return 'mpc__c-warn'
  }
  return 'mpc__c-bad'
}
</script>

<template>
  <view v-if="profile" class="mpc">
    <!-- 首行：头像 + 昵称 + 注册时间 -->
    <view class="mpc__header">
      <view class="mpc__avatar-ring">
        <view class="mpc__avatar">
          <text class="mpc__avatar-text">{{ firstChar(profile.nickname) }}</text>
        </view>
      </view>
      <view class="mpc__user">
        <text class="mpc__nickname">{{ profile.nickname }}</text>
        <view class="mpc__regtime">
          <text class="mpc__regtime-label">{{ t('c2cMerchantCard.registerTime') }}</text>
          <text class="mpc__regtime-value">{{ profile.registerTime }}</text>
        </view>
      </view>
    </view>

    <!-- 认证徽章：仅展示已认证项 -->
    <view v-if="verifiedBadges.length" class="mpc__badges">
      <view v-for="badge in verifiedBadges" :key="badge.key" class="mpc__badge">
        <wd-icon name="check" size="22rpx" />
        <text>{{ badge.label }}</text>
      </view>
    </view>

    <!-- 交易数据统计 -->
    <view v-if="profile.stats" class="mpc__stats">
      <!-- Hero：累计完成率 -->
      <view class="mpc__hero">
        <text class="mpc__hero-label">{{ t('c2cMerchantCard.completionRateTotal') }}</text>
        <text class="mpc__hero-value" :class="rateColorClass(profile.stats.completionRateTotal)">{{ formatRate(profile.stats.completionRateTotal) }}</text>
        <text class="mpc__hero-sub">{{ t('c2cMerchantCard.completionRateTotalDesc') }}</text>
      </view>

      <view class="mpc__divider" />

      <!-- 30日数据 -->
      <view class="mpc__group">
        <text class="mpc__group-title">{{ t('c2cMerchantCard.stats30d') }}</text>
        <view class="mpc__tiles">
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.orderCount') }}</text>
            <text class="mpc__tile-value">{{ formatNum(profile.stats.orderCount30d) }}<text class="mpc__tile-unit">{{ t('c2cMerchantCard.unitTimes') }}</text></text>
          </view>
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.completionRate') }}</text>
            <text class="mpc__tile-value" :class="rateColorClass(profile.stats.completionRate30d)">{{ formatRate(profile.stats.completionRate30d) }}</text>
          </view>
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.buyCompletionRate') }}</text>
            <text class="mpc__tile-value" :class="rateColorClass(profile.stats.completionRateBuy)">{{ formatRate(profile.stats.completionRateBuy) }}</text>
          </view>
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.sellCompletionRate') }}</text>
            <text class="mpc__tile-value" :class="rateColorClass(profile.stats.completionRateSell)">{{ formatRate(profile.stats.completionRateSell) }}</text>
          </view>
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.avgReleaseTime') }}</text>
            <text class="mpc__tile-value">{{ profile.stats.avgReleaseTime30d ? profile.stats.avgReleaseTime30d + t('c2cMerchantCard.unitMinutes') : '—' }}</text>
          </view>
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.avgPaymentTime') }}</text>
            <text class="mpc__tile-value">{{ profile.stats.avgPaymentTime30d ? profile.stats.avgPaymentTime30d + t('c2cMerchantCard.unitMinutes') : '—' }}</text>
          </view>
        </view>
      </view>

      <view class="mpc__divider" />

      <!-- 累计数据 -->
      <view class="mpc__group">
        <text class="mpc__group-title">{{ t('c2cMerchantCard.statsTotal') }}</text>
        <view class="mpc__tiles">
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.orderCount') }}</text>
            <text class="mpc__tile-value">{{ formatNum(profile.stats.totalOrderCount) }}<text class="mpc__tile-unit">{{ t('c2cMerchantCard.unitTimes') }}</text></text>
          </view>
          <view class="mpc__tile">
            <text class="mpc__tile-label">{{ t('c2cMerchantCard.tradeUsers') }}</text>
            <text class="mpc__tile-value">{{ formatNum(profile.stats.totalTradeUsers) }}<text class="mpc__tile-unit">{{ t('c2cMerchantCard.unitPeople') }}</text></text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.mpc {
  position: relative;
  padding: 32rpx 28rpx 28rpx;
  border-radius: 20rpx;
  background: linear-gradient(160deg, rgba(var(--surface-3-rgb), 0.94) 0%, rgba(var(--surface-2-rgb), 0.94) 100%);
  border: 1rpx solid rgba(var(--gold-rgb), 0.16);
  box-shadow: 0 10rpx 34rpx rgba(0, 0, 0, 0.12), inset 0 1rpx 0 $highlight-edge;
  backdrop-filter: blur(10rpx);
  overflow: hidden;

  /* 顶部金色渐变饰线 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3rpx;
    background: linear-gradient(90deg, transparent, $gold-light, transparent);
    opacity: 0.85;
  }

  &__header {
    position: relative;
    display: flex;
    align-items: center;
    gap: 20rpx;

    /* 头像后方金色光晕 */
    &::after {
      content: '';
      position: absolute;
      top: -20rpx;
      left: -10rpx;
      width: 200rpx;
      height: 120rpx;
      background: radial-gradient(circle, rgba(201, 180, 138, 0.18), transparent 70%);
      filter: blur(10rpx);
      pointer-events: none;
    }
  }

  &__avatar-ring {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 108rpx;
    height: 108rpx;
    border-radius: 50%;
    background: radial-gradient(circle at 32% 28%, rgba(201, 180, 138, 0.3) 0%, rgba(201, 180, 138, 0.06) 60%, transparent 75%);
    box-shadow: 0 0 30rpx rgba(201, 180, 138, 0.22), inset 0 0 20rpx rgba(201, 180, 138, 0.1);
    flex-shrink: 0;
  }

  &__avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #4a4131 0%, #6e5d3e 100%);
    border: 2rpx solid rgba(201, 180, 138, 0.3);
  }

  &__avatar-text {
    font-size: 40rpx;
    font-weight: 500;
    color: $gold-light;
  }

  &__user {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
    min-width: 0;
  }

  &__nickname {
    font-size: 32rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__regtime {
    display: flex;
    align-items: center;
    gap: 6rpx;
    margin-top: 2rpx;
  }

  &__regtime-label {
    font-size: 20rpx;
    color: $text-muted;
  }

  &__regtime-value {
    font-size: 20rpx;
    color: $text-secondary;
  }

  /* ===== Badges（仅已认证项，flex 自适应宽度） ===== */
  &__badges {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-top: 22rpx;
  }

  &__badge {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 10rpx 16rpx;
    border-radius: 10rpx;
    font-size: 20rpx;
    color: $gold-light;
    background: linear-gradient(135deg, rgba(201, 180, 138, 0.2), rgba(201, 180, 138, 0.06));
    border: 1rpx solid rgba(201, 180, 138, 0.45);
    box-shadow: 0 0 16rpx rgba(201, 180, 138, 0.18);

    :deep(.wd-icon) {
      /* 跟随主题的对比色：日间 page-bg 浅（配深金底），夜间 page-bg 深（配浅金底） */
      color: var(--page-bg) !important;
      background: $gold-light;
      border-radius: 50%;
      padding: 2rpx;
    }
  }

  /* ===== Stats ===== */
  &__stats {
    margin-top: 28rpx;
  }

  &__hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    padding: 26rpx;
    border-radius: 16rpx;
    background: linear-gradient(135deg, rgba(201, 180, 138, 0.12), rgba(255, 255, 255, 0.02));
    border: 1rpx solid rgba(201, 180, 138, 0.14);
  }

  &__hero-label {
    font-size: 22rpx;
    color: $text-muted;
    letter-spacing: 3rpx;
  }

  &__hero-value {
    font-size: 60rpx;
    font-weight: 700;
    line-height: 1.1;
  }

  &__hero-sub {
    font-size: 20rpx;
    color: $text-secondary;
  }

  &__divider {
    height: 1rpx;
    margin: 26rpx 0;
    background: linear-gradient(90deg, transparent, $border-subtle, transparent);
  }

  &__group-title {
    display: block;
    font-size: 22rpx;
    font-weight: 500;
    color: $gold-light;
    opacity: 0.85;
    margin-bottom: 14rpx;
    padding-left: 4rpx;
  }

  &__tiles {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 14rpx;
  }

  &__tile {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    padding: 16rpx 18rpx;
    border-radius: 12rpx;
    background: $surface-3;
    border: 1rpx solid $border-subtle;
  }

  &__tile-label {
    font-size: 22rpx;
    color: $text-muted;
  }

  &__tile-value {
    font-size: 30rpx;
    font-weight: 600;
    color: $text-highlight;
  }

  &__tile-unit {
    font-size: 20rpx;
    font-weight: 400;
    color: $text-muted;
    margin-left: 4rpx;
  }

  /* 完成率三档配色（hero / tile 通用） */
  &__c-good {
    color: #9cc9a8;
  }

  &__c-warn {
    color: #d4c97a;
  }

  &__c-bad {
    color: #d89090;
  }
}
</style>
