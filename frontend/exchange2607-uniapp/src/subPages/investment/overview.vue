<script setup lang="ts">
import type { InvestmentOverview } from '@/api/types'
import { investmentApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { currencyLogo, currencyName, loadAllCurrencies } from '@/utils/currency'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import AppTabs from '@/components/AppTabs.vue'

definePage({
  name: 'investmentOverview',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()

const overview = ref<InvestmentOverview | null>(null)
const loading = ref(false)
const activeTab = ref<'currency' | 'product'>('currency')
const expandedCurrency = ref<string | null>(null)
const expandedProduct = ref<string | null>(null)

const { t } = useI18n()
const { currentLang } = useI18nSync()



onShow(() => {
  loadAllCurrencies()
  loadData()
})

onPullDownRefresh(() => {
  loadData(true)
})

async function loadData(fromPull = false) {
  loading.value = true
  try {
    overview.value = await apiData(investmentApi.findOverview())
  }
  catch {
    overview.value = null
  }
  finally {
    loading.value = false
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

const tabs = computed<{ key: 'currency' | 'product', label: string }[]>(() => [
  { key: 'currency', label: t('investmentOverview.tabCurrency') },
  { key: 'product', label: t('investmentOverview.tabProduct') },
])

function switchTab(value: string | number) {
  activeTab.value = value as 'currency' | 'product'
}

function toggleCurrency(assetCode: string) {
  expandedCurrency.value = expandedCurrency.value === assetCode ? null : assetCode
}

function toggleProduct(key: string) {
  expandedProduct.value = expandedProduct.value === key ? null : key
}

function productKey(item: { productType?: string }) {
  return `${item.productType}`
}

function goInvestment() {
  router.push({ name: 'investment' })
}

function goOrderRecord() {
  router.push({ name: 'investmentOrderRecord' })
}

function goFlexibleDetail(assetCode: string, flexibleAccountId?: string) {
  router.push({
    name: 'investmentFlexibleDetail',
    query: { assetCode, ...(flexibleAccountId ? { flexibleAccountId } : {}) },
  })
}

function goFixedDetail(positionId: string) {
  router.push({ name: 'investmentFixedDetail', query: { positionId } })
}

function onDetailClick(
  item: { productType?: string, positionId?: string, flexibleAccountId?: string },
  assetCode?: string,
) {
  if (item.productType === 'FLEXIBLE') {
    goFlexibleDetail(assetCode || '', item.flexibleAccountId)
  }
  else if (item.positionId) {
    goFixedDetail(item.positionId)
  }
}

function formatNum(val: string | number | undefined) {
  if (val == null) return '0'
  return String(val)
}

function productIcon(productType?: string) {
  if (productType === 'FLEXIBLE') return 'lightning-circle-fill'
  if (productType === 'NEW_USER') return 'gift'
  return 'lock'
}

/**
 * 产品类型名称本地化：后端 productTypeName 为中文，直接渲染会在英文环境露中文，
 * 因此优先按 productType（+ lockDays）映射，未覆盖的类型才回落到后端名称。
 */
function productTypeLabel(productType?: string, lockDays?: number, fallback?: string): string {
  if (productType === 'FLEXIBLE') return t('investmentCommon.flexible')
  if (productType === 'FIXED') {
    return lockDays != null
      ? t('investmentCommon.fixedTermDays', { n: lockDays })
      : t('dictItem.investmentProductType.FIXED')
  }
  return fallback || productType || '—'
}
</script>

<template>
  <view class="io-page">
    <AppNavBar :title="t('investmentOverview.navTitle')" />
    <view class="io-glow" />

    <!-- 头部资产卡 -->
    <view class="io-header">
      <view class="io-header__deco" />
      <view class="io-header__left">
        <text class="io-header__label">{{ t('investmentCommon.totalAssets') }}</text>
        <view class="io-header__amount-row">
          <text class="io-header__value">{{ formatNum(overview?.totalUsdt) }}</text>
          <text class="io-header__unit">USDT</text>
        </view>
        <view class="io-header__sub-row">
          <text class="io-header__sub-label">{{ t('investmentCommon.accumulatedEarnings') }}</text>
          <text class="io-header__sub-value">+{{ formatNum(overview?.totalAccruedInterestUsdt) }}</text>
          <text class="io-header__sub-unit">USDT</text>
        </view>
      </view>
      <view class="io-header__right">
        <view class="io-header__logo" @click="goOrderRecord">
          <wd-icon name="list" size="40rpx" color="#C9B48A" />
        </view>
      </view>
    </view>

    <!-- 去赚币按钮 -->
    <view class="io-btn" @click="goInvestment">
      <text>{{ t('investmentOverview.goEarn') }}</text>
      <wd-icon name="arrow-right" size="28rpx" color="#1a1814" />
    </view>

    <!-- Tab（AppTabs chip） -->
    <view class="io-tabs-wrap">
      <AppTabs
        variant="chip" size="md" item-padding="12rpx 36rpx"
        :model-value="activeTab" :items="tabs" @update:model-value="switchTab"
      />
    </view>

    <view v-if="loading" class="io-empty">{{ t('common.loading') }}</view>
    <view v-else-if="!overview || (overview.currencySummaries.length === 0 && overview.productSummaries.length === 0)" class="io-empty">
      {{ t('investmentOverview.noAssets') }}
    </view>

    <template v-else>
      <!-- 币种 Tab -->
      <template v-if="activeTab === 'currency'">
        <view
          v-for="item in overview.currencySummaries"
          :key="item.assetCode"
          class="io-card"
        >
          <view class="io-card__hairline" />
          <view class="io-card__head" @click="toggleCurrency(item.assetCode)">
            <view class="io-card__left">
              <image
                v-if="currencyLogo(item.assetCode)"
                :src="currencyLogo(item.assetCode)"
                class="io-card__logo"
                mode="aspectFit"
              />
              <text v-else class="io-card__symbol">{{ item.assetCode.charAt(0) }}</text>
              <view class="io-card__info">
                <text class="io-card__code">{{ item.assetCode }}</text>
                <text class="io-card__count">{{ t('investmentOverview.positionCount', { n: item.positionCount }) }}</text>
              </view>
            </view>
            <view class="io-card__right">
              <view class="io-card__values">
                <text class="io-card__amount">{{ formatNum(item.totalValue) }}</text>
                <text class="io-card__usdt">≈ {{ formatNum(item.usdtValue) }} USDT</text>
              </view>
              <wd-icon
                :name="expandedCurrency === item.assetCode ? 'up' : 'down'"
                size="28rpx"
                color="#9A9690"
              />
            </view>
          </view>
          <view v-if="expandedCurrency === item.assetCode" class="io-card__details">
            <view
              v-for="(detail, idx) in item.details"
              :key="idx"
              class="io-detail"
              :class="{ 'io-detail--clickable': detail.productType === 'FLEXIBLE' || detail.positionId }"
              @click="onDetailClick(detail, item.assetCode)"
            >
              <view class="io-detail__accent" />
              <view class="io-detail__body">
                <view class="io-detail__row">
                  <text class="io-detail__name">{{ productTypeLabel(detail.productType, detail.lockDays, detail.productTypeName) }}<text v-if="detail.productType === 'FIXED' && detail.elapsedDays != null" class="io-detail__lock">{{ t('investmentOverview.lockProgress', { elapsed: detail.elapsedDays, total: detail.lockDays }) }}</text></text>
                </view>
                <!-- 活期 -->
                <template v-if="detail.productType === 'FLEXIBLE'">
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('common.amount') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.totalPrincipal) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.annualRate') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.annualRate) }}%</text>
                    </view>
                  </view>
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.yesterdayEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.yesterdayInterest) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.accumulatedEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.confirmedInterest) }}</text>
                    </view>
                  </view>
                </template>
                <!-- 定期 -->
                <template v-else>
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('common.amount') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.totalPrincipal) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.annualRate') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.annualRate) }}%</text>
                    </view>
                  </view>
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.confirmedEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.confirmedInterest) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.estimatedTotalEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.estimatedTotalInterest) }}</text>
                    </view>
                  </view>
                </template>
              </view>
            </view>
          </view>
        </view>
      </template>

      <!-- 产品 Tab -->
      <template v-else>
        <view
          v-for="item in overview.productSummaries"
          :key="productKey(item)"
          class="io-card"
        >
          <view class="io-card__hairline" />
          <view class="io-card__head" @click="toggleProduct(productKey(item))">
            <view class="io-card__left">
              <view class="io-card__icon">
                <wd-icon :name="productIcon(item.productType)" size="32rpx" color="#C9B48A" />
              </view>
              <view class="io-card__info">
                <text class="io-card__code">{{ productTypeLabel(item.productType, item.lockDays, item.productTypeName) }}</text>
                <text class="io-card__count">{{ t('investmentOverview.positionCount', { n: item.positionCount }) }}</text>
              </view>
            </view>
            <view class="io-card__right">
              <view class="io-card__values io-card__values--inline">
                <text class="io-card__amount">{{ formatNum(item.usdtValue) }}</text>
                <text class="io-card__usdt">USDT</text>
              </view>
              <wd-icon
                :name="expandedProduct === productKey(item) ? 'up' : 'down'"
                size="28rpx"
                color="#9A9690"
              />
            </view>
          </view>
          <view v-if="expandedProduct === productKey(item)" class="io-card__details">
            <view
              v-for="(detail, idx) in item.details"
              :key="idx"
              class="io-detail"
              :class="{ 'io-detail--clickable': item.productType === 'FLEXIBLE' || detail.positionId }"
              @click="onDetailClick({ productType: item.productType, positionId: detail.positionId, flexibleAccountId: detail.flexibleAccountId }, detail.assetCode || '')"
            >
              <view class="io-detail__accent" />
              <view class="io-detail__body">
                <view class="io-detail__row">
                  <view class="io-detail__left">
                    <image
                      v-if="currencyLogo(detail.assetCode || '')"
                      :src="currencyLogo(detail.assetCode || '')"
                      class="io-detail__logo"
                      mode="aspectFit"
                    />
                    <text class="io-detail__name">{{ detail.assetCode }}<text v-if="item.productType === 'FIXED' && detail.elapsedDays != null" class="io-detail__lock">{{ t('investmentOverview.lockProgress', { elapsed: detail.elapsedDays, total: detail.lockDays }) }}</text></text>
                  </view>
                </view>
                <!-- 活期 -->
                <template v-if="item.productType === 'FLEXIBLE'">
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('common.amount') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.totalPrincipal) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.annualRate') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.annualRate) }}%</text>
                    </view>
                  </view>
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.yesterdayEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.yesterdayInterest) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.accumulatedEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.confirmedInterest) }}</text>
                    </view>
                  </view>
                </template>
                <!-- 定期 -->
                <template v-else>
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('common.amount') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.totalPrincipal) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.annualRate') }}</text>
                      <text class="io-detail__amount">{{ formatNum(detail.annualRate) }}%</text>
                    </view>
                  </view>
                  <view class="io-detail__row io-detail__row--stats">
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.confirmedEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.confirmedInterest) }}</text>
                    </view>
                    <view class="io-detail__stat">
                      <text class="io-detail__stat-label">{{ t('investmentCommon.estimatedTotalEarnings') }}</text>
                      <text class="io-detail__interest">{{ formatNum(detail.estimatedTotalInterest) }}</text>
                    </view>
                  </view>
                </template>
              </view>
            </view>
          </view>
        </view>
      </template>
    </template>

    <!-- 底部风险提示 -->
    <view class="io-footer">
      <view class="io-footer__title">
        <wd-icon name="info-circle" size="24rpx" color="#6e6a64" />
        <text>{{ t('investmentCommon.riskWarning') }}</text>
      </view>
      <text class="io-footer__text">{{ t('investmentCommon.riskDisclaimer') }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.io-page {
  @include page-shell;
  padding: 0 0 24rpx;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.io-glow {
  position: absolute;
  top: -120rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 600rpx;
  height: 400rpx;
  background: radial-gradient(ellipse at center, rgba(201, 180, 138, 0.08) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* 头部资产卡 */
.io-header {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 32rpx;
  border-radius: 20rpx;
  background: linear-gradient(145deg, rgba(var(--gold-rgb), 0.08) 0%, $surface-2 100%);
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  margin: 24rpx 24rpx 16rpx;
  overflow: hidden;
}

.io-header__deco {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.4), transparent);
}

.io-header__left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.io-header__label {
  font-size: 22rpx;
  letter-spacing: 0.1em;
  color: $text-muted;
}

.io-header__amount-row {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.io-header__value {
  @include amount-text;
  font-size: 52rpx;
  color: $text-highlight;
  font-weight: 700;
  line-height: 1.1;
}

.io-header__unit {
  font-size: 26rpx;
  color: $text-muted;
}

.io-header__sub-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 8rpx;
}

.io-header__sub-label {
  font-size: 22rpx;
  color: $text-muted;
}

.io-header__sub-value {
  @include amount-text;
  font-size: 28rpx;
  color: $gold;
  font-weight: 600;
}

.io-header__sub-unit {
  font-size: 22rpx;
  color: $text-muted;
}

.io-header__right {
  flex-shrink: 0;
}

.io-header__logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $gold-soft;
  border: 1rpx solid rgba(201, 180, 138, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 去赚币按钮 */
.io-btn {
  @include btn-gold-block;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin: 0 24rpx 28rpx;

  :deep(.wd-icon) {
    color: $page-bg !important;
  }
}

/* Tab 外层：视觉交给 AppTabs，这里只负责页面级定位与间距 */
.io-tabs-wrap {
  position: relative;
  z-index: 1;
  margin: 0 24rpx 20rpx;
}

.io-empty {
  position: relative;
  z-index: 1;
  padding: 100rpx 24rpx;
  text-align: center;
  color: $text-muted;
  font-size: 26rpx;
}

/* 卡片 */
.io-card {
  position: relative;
  z-index: 1;
  margin: 0 24rpx 16rpx;
  border-radius: 16rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  overflow: hidden;
}

.io-card__hairline {
  position: absolute;
  top: 0;
  left: 24rpx;
  right: 24rpx;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.28), transparent);
}

.io-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 26rpx 28rpx;
}

.io-card__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.io-card__logo {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 1rpx solid $border-subtle;
}

.io-card__symbol {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 600;
  color: $gold;
}

.io-card__icon {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: $gold-soft;
  border: 1rpx solid rgba(201, 180, 138, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

.io-card__info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.io-card__code {
  font-size: 28rpx;
  color: $text-highlight;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.io-card__count {
  font-size: 22rpx;
  color: $text-muted;
}

.io-card__right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.io-card__values {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.io-card__values--inline {
  flex-direction: row;
  align-items: baseline;
  gap: 8rpx;
}

.io-card__amount {
  @include amount-text;
  font-size: 30rpx;
  color: $text-highlight;
  font-weight: 600;
}

.io-card__usdt {
  font-size: 22rpx;
  color: $text-muted;
}

.io-card__details {
  border-top: 1rpx solid $border-subtle;
  background: $surface-1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.io-detail {
  display: flex;
  align-items: stretch;
  border-radius: 12rpx;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  overflow: hidden;

  &--clickable {
    cursor: pointer;
    transition: background 0.2s ease;

    &:active {
      background: $surface-2;
    }
  }
}

.io-detail__accent {
  flex-shrink: 0;
  width: 4rpx;
  background: linear-gradient(180deg, rgba(201, 180, 138, 0.5) 0%, rgba(201, 180, 138, 0.1) 100%);
}

.io-detail__body {
  flex: 1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.io-detail__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;

  &--stats {
    gap: 32rpx;
  }
}

.io-detail__left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.io-detail__logo {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  border: 1rpx solid $border-subtle;
}

.io-detail__name {
  font-size: 26rpx;
  color: $text-highlight;
  font-weight: 500;
}

.io-detail__lock {
  margin-left: 8rpx;
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  color: $gold;
  background: $gold-soft;
}

.io-detail__stat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6rpx;

  &:first-child {
    align-items: flex-start;
  }
}

.io-detail__stat-label {
  font-size: 20rpx;
  letter-spacing: 0.04em;
  color: $text-muted;
}

.io-detail__amount {
  @include amount-text;
  font-size: 28rpx;
  color: $text-highlight;
  font-weight: 600;
}

.io-detail__interest {
  @include amount-text;
  font-size: 28rpx;
  color: $gold;
  font-weight: 600;
}

/* 底部风险提示 */
.io-footer {
  position: relative;
  z-index: 1;
  margin: 16rpx 24rpx 0;
  padding: 24rpx 28rpx;
  border-radius: 14rpx;
  background: $surface-1;
  border: 1rpx solid $border-subtle;
}

.io-footer__title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
  font-size: 22rpx;
  color: $text-muted;
  font-weight: 500;
}

.io-footer__text {
  font-size: 22rpx;
  line-height: 1.7;
  color: $text-muted;
}
</style>
