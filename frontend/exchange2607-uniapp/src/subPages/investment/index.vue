<script setup lang="ts">
import type { InvestmentProduct, InvestmentOverview } from '@/api/types'
import { investmentApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useMemberStore } from '@/store/member'
import { currencyLogo, loadAllCurrencies } from '@/utils/currency'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'investment',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const memberStore = useMemberStore()

const products = ref<InvestmentProduct[]>([])
const overview = ref<InvestmentOverview | null>(null)
const sellBalance = ref(0)
const loading = ref(false)
const expandedAsset = ref<string | null>(null)

const { t } = useI18n()
const { currentLang } = useI18nSync()



onShow(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData(true)
})

async function loadData(fromPull = false) {
  loading.value = true
  try {
    const [productRes, overviewRes, _currRes] = await Promise.allSettled([
      apiData(investmentApi.findEnabledProducts()),
      apiData(investmentApi.findOverview()),
      loadAllCurrencies(),
    ])
    products.value = productRes.status === 'fulfilled' ? (productRes.value || []) : []
    overview.value = overviewRes.status === 'fulfilled' ? (overviewRes.value || null) : null
    // 尝试获取 sellBalance
    if (!memberStore.memberInfo) {
      await memberStore.ensureMemberInfo().catch(() => {})
    }
    sellBalance.value = memberStore.memberInfo?.sellBalance ?? 0
  }
  catch {
    products.value = products.value || []
  }
  finally {
    loading.value = false
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

// 格式化数值：最多保留8位小数，去除末尾多余零
const fmtNum = (n: number | undefined | null) => parseFloat((n ?? 0).toFixed(8)).toString()

// 新人专享产品
const newUserProducts = computed(() =>
  products.value.filter(p => p.newUserExclusive),
)

// 非新人专享产品，按币种分组
const groupedProducts = computed(() => {
  const map = new Map<string, InvestmentProduct[]>()
  for (const p of products.value) {
    if (p.newUserExclusive) continue
    const code = p.assetCode || 'UNKNOWN'
    if (!map.has(code)) map.set(code, [])
    map.get(code)!.push(p)
  }
  // 每组内排序：活期在前，定期按 lockDays 升序
  for (const [, list] of map) {
    list.sort((a, b) => {
      if (a.productType === 'FLEXIBLE' && b.productType !== 'FLEXIBLE') return -1
      if (a.productType !== 'FLEXIBLE' && b.productType === 'FLEXIBLE') return 1
      return (a.lockDays ?? 0) - (b.lockDays ?? 0)
    })
  }
  return Array.from(map.entries()).map(([assetCode, list]) => ({ assetCode, list }))
})

// 计算某币种组的收益率范围
function rateRange(list: InvestmentProduct[]): string {
  const rates: number[] = []
  for (const p of list) {
    if (p.productType === 'FLEXIBLE') {
      // 活期：取补贴后年化（高）和基础年化（低）
      rates.push(p.annualRate ?? 0)
      if (p.subsidizedRate && p.subsidizedRate > 0) {
        rates.push(p.subsidizedRate)
      }
    }
    else {
      rates.push(p.annualRate ?? 0)
    }
  }
  const min = Math.min(...rates)
  const max = Math.max(...rates)
  if (min === max) return `${fmtNum(min)}%`
  return `${fmtNum(min)}%~${fmtNum(max)}%`
}

// 产品类型名称
function productLabel(p: InvestmentProduct): string {
  if (p.productType === 'FLEXIBLE') return t('investmentCommon.flexible')
  return t('investmentCommon.fixedTermDays', { n: p.lockDays ?? '' })
}

// 产品利率文案
function productRateText(p: InvestmentProduct): string {
  if (p.productType === 'FLEXIBLE') {
    const hasSubsidy = (p.subsidizedRate ?? 0) > 0 && (p.rewardLimit ?? 0) > 0
    if (hasSubsidy) {
      return `${fmtNum(p.subsidizedRate)}%`
    }
    return `${fmtNum(p.annualRate)}%`
  }
  return `${fmtNum(p.annualRate)}%`
}

// 活期是否有补贴
function hasSubsidy(p: InvestmentProduct): boolean {
  return p.productType === 'FLEXIBLE' && (p.subsidizedRate ?? 0) > 0 && (p.rewardLimit ?? 0) > 0
}

function toggleAsset(code: string) {
  expandedAsset.value = expandedAsset.value === code ? null : code
}

// ---- FAQ ----
const faqList = computed(() => [
  { q: t('investmentIndex.faqWhatIsEarnQ'), a: t('investmentIndex.faqWhatIsEarnA') },
  { q: t('investmentIndex.faqFlexibleRuleQ'), a: t('investmentIndex.faqFlexibleRuleA') },
  { q: t('investmentIndex.faqFixedRuleQ'), a: t('investmentIndex.faqFixedRuleA') },
  { q: t('investmentIndex.faqCalcQ'), a: t('investmentIndex.faqCalcA') },
  { q: t('investmentIndex.faqRedeemQ'), a: t('investmentIndex.faqRedeemA') },
])
const expandedFaq = ref<number | null>(null)

function toggleFaq(idx: number) {
  expandedFaq.value = expandedFaq.value === idx ? null : idx
}

function goOrderRecord() {
  router.push({ name: 'investmentOrderRecord' })
}

function goProductDetail(product: InvestmentProduct) {
  router.push({ name: 'investmentProductDetail', query: { id: product.id, assetCode: product.assetCode || '' } })
}

function formatNum(val: string | number | undefined | null) {
  if (val == null) return '0'
  return String(val)
}
</script>

<template>
  <view class="inv-page">
    <AppNavBar :title="t('investmentCommon.earn')" />
    <view class="inv-glow" />

    <!-- 头部资产卡 -->
    <view class="inv-header">
      <view class="inv-header__deco" />
      <view class="inv-header__body">
        <view class="inv-header__left">
          <text class="inv-header__label">{{ t('investmentCommon.totalAssets') }}</text>
          <view class="inv-header__amount-row">
            <text class="inv-header__value">{{ formatNum(overview?.totalUsdt) }}</text>
            <text class="inv-header__unit">USDT</text>
          </view>
          <view class="inv-header__sub-row">
            <text class="inv-header__sub-label">{{ t('investmentCommon.accumulatedEarnings') }}</text>
            <text class="inv-header__sub-value">+{{ formatNum(overview?.totalAccruedInterestUsdt) }}</text>
            <text class="inv-header__sub-unit">USDT</text>
          </view>
          <view class="inv-header__sub-row">
            <text class="inv-header__sub-label">{{ t('investmentCommon.yesterdayEarnings') }}</text>
            <text class="inv-header__sub-value">+{{ formatNum(overview?.yesterdayInterestUsdt) }}</text>
            <text class="inv-header__sub-unit">USDT</text>
          </view>
        </view>
        <view class="inv-header__right">
          <view class="inv-header__logo" @click="goOrderRecord">
            <wd-icon name="list" size="40rpx" color="#C9B48A" />
          </view>
        </view>
      </view>
    </view>

    <view v-if="loading && products.length === 0" class="inv-empty">
      <text>{{ t('common.loading') }}</text>
    </view>
    <view v-else-if="products.length === 0" class="inv-empty">
      <text>{{ t('investmentIndex.noProducts') }}</text>
    </view>

    <!-- 新人专享 -->
    <view
      v-for="item in newUserProducts"
      :key="item.id"
      class="inv-card inv-card--newuser"
      @click="goProductDetail(item)"
    >
      <view class="inv-card__head">
        <view class="inv-card__badge">🎁 {{ t('investmentIndex.newUserExclusive') }}</view>
        <text class="inv-card__rate">{{ fmtNum(item.annualRate) }}%<text class="inv-card__rate-unit">APY</text></text>
      </view>
      <text class="inv-card__name">{{ item.assetCode }} · {{ t('investmentIndex.newUserExclusive') }}</text>
      <view class="inv-card__meta">
        <text>{{ t('investmentIndex.termDays', { n: item.lockDays ?? 0 }) }}</text>
        <text>{{ t('investmentIndex.personalQuota', { amount: item.maxHoldPerUser ?? t('investmentIndex.unlimited') }) }}</text>
      </view>
      <view class="inv-card__foot">
        <text>{{ t('investmentIndex.firstTimeOnly') }}</text>
        <wd-icon name="arrow-right" size="28rpx" color="#C9B48A" />
      </view>
    </view>

    <!-- 按币种分组 -->
    <view
      v-for="group in groupedProducts"
      :key="group.assetCode"
      class="inv-group"
    >
      <view class="inv-group__hairline" />
      <view class="inv-group__head" @click="toggleAsset(group.assetCode)">
        <view class="inv-group__left">
          <image
            v-if="currencyLogo(group.assetCode)"
            :src="currencyLogo(group.assetCode)"
            class="inv-group__logo"
            mode="aspectFit"
          />
          <text v-else class="inv-group__symbol">{{ group.assetCode.charAt(0) }}</text>
          <view class="inv-group__info">
            <text class="inv-group__code">{{ group.assetCode }}</text>
            <text class="inv-group__count">{{ t('investmentIndex.productCount', { n: group.list.length }) }}</text>
          </view>
        </view>
        <view class="inv-group__right">
          <text class="inv-group__rate">{{ rateRange(group.list) }}</text>
          <wd-icon
            :name="expandedAsset === group.assetCode ? 'up' : 'down'"
            size="28rpx"
            color="#9A9690"
          />
        </view>
      </view>

      <!-- 展开后的产品列表 -->
      <view v-if="expandedAsset === group.assetCode" class="inv-group__details">
        <view
          v-for="item in group.list"
          :key="item.id"
          class="inv-product"
          @click="goProductDetail(item)"
        >
          <view class="inv-product__accent" />
          <view class="inv-product__body">
            <view class="inv-product__row">
              <text class="inv-product__name">{{ productLabel(item) }}</text>
            </view>
<view class="inv-product__row inv-product__row--meta">
<text v-if="item.productType === 'FLEXIBLE'">{{ t('investmentIndex.flexibleMeta') }}</text>
<text v-else>{{ t('investmentIndex.fixedMeta') }}</text>
</view>
          </view>
          <view class="inv-product__rate-wrap">
            <text v-if="hasSubsidy(item)" class="inv-product__rate-tag">{{ t('investmentIndex.limitedBoost') }}</text>
            <text class="inv-product__rate">{{ productRateText(item) }}</text>
            <wd-icon name="right" size="24rpx" color="#6e6a64" />
          </view>
        </view>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="inv-faq">
      <view class="inv-faq__title">
        <wd-icon name="question-circle" size="24rpx" color="#9A9690" />
        <text>{{ t('investmentIndex.faqTitle') }}</text>
      </view>
      <view class="inv-faq__list">
        <view
          v-for="(item, idx) in faqList"
          :key="idx"
          class="inv-faq__item"
        >
          <view class="inv-faq__q" @click="toggleFaq(idx)">
            <text class="inv-faq__q-text">{{ item.q }}</text>
            <wd-icon
              :name="expandedFaq === idx ? 'up' : 'down'"
              size="24rpx"
              color="#9A9690"
            />
          </view>
          <view v-if="expandedFaq === idx" class="inv-faq__a">
            <text>{{ item.a }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部风险提示 -->
    <view class="inv-footer">
      <view class="inv-footer__title">
        <wd-icon name="info-circle" size="24rpx" color="#6e6a64" />
        <text>{{ t('investmentCommon.riskWarning') }}</text>
      </view>
      <text class="inv-footer__text">{{ t('investmentCommon.riskDisclaimer') }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.inv-page {
  @include page-shell;
  padding: 0 0 24rpx;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.inv-glow {
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
.inv-header {
  position: relative;
  z-index: 1;
  margin: 24rpx 24rpx 16rpx;
  border-radius: 20rpx;
  background: linear-gradient(145deg, rgba(var(--gold-rgb), 0.08) 0%, $surface-2 100%);
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  overflow: hidden;
}

.inv-header__deco {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.4), transparent);
}

.inv-header__body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 32rpx;
}

.inv-header__left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.inv-header__label {
  font-size: 22rpx;
  letter-spacing: 0.1em;
  color: $text-muted;
}

.inv-header__amount-row {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.inv-header__value {
  @include amount-text;
  font-size: 52rpx;
  color: $text-highlight;
  font-weight: 700;
  line-height: 1.1;
}

.inv-header__unit {
  font-size: 26rpx;
  color: $text-muted;
}

.inv-header__sub-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 8rpx;
}

.inv-header__sub-label {
  font-size: 22rpx;
  color: $text-muted;
}

.inv-header__sub-value {
  @include amount-text;
  font-size: 28rpx;
  color: $gold;
  font-weight: 600;
}

.inv-header__sub-unit {
  font-size: 22rpx;
  color: $text-muted;
}

.inv-header__right {
  flex-shrink: 0;
}

.inv-header__logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $gold-soft;
  border: 1rpx solid rgba(201, 180, 138, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 空状态 */
.inv-empty {
  position: relative;
  z-index: 1;
  padding: 100rpx 0;
  text-align: center;
  color: $text-muted;
  font-size: 26rpx;
}

/* 新人专享卡片 */
.inv-card {
  position: relative;
  z-index: 1;
  margin: 0 24rpx 16rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
}

.inv-card--newuser {
  border-color: rgba(201, 180, 138, 0.3);
  background: linear-gradient(145deg, rgba(201, 180, 138, 0.06) 0%, $surface-2 100%);
}

.inv-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.inv-card__badge {
  font-size: 24rpx;
  color: $gold;
  font-weight: 600;
}

.inv-card__name {
  font-size: 30rpx;
  color: $text-highlight;
  font-weight: 600;
}

.inv-card__rate {
  font-size: 28rpx;
  color: $gold;
  font-weight: 600;
}

.inv-card__rate-unit {
  font-size: 20rpx;
  color: $text-muted;
  margin-left: 4rpx;
}

.inv-card__meta {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: $text-secondary;
  margin-bottom: 8rpx;
}

.inv-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid $border-subtle;
  font-size: 26rpx;
  color: $gold;
}

/* 币种分组卡片 */
.inv-group {
  position: relative;
  z-index: 1;
  margin: 0 24rpx 16rpx;
  border-radius: 16rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  overflow: hidden;
}

.inv-group__hairline {
  position: absolute;
  top: 0;
  left: 24rpx;
  right: 24rpx;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(201, 180, 138, 0.28), transparent);
}

.inv-group__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 26rpx 28rpx;
}

.inv-group__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.inv-group__logo {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 1rpx solid $border-subtle;
}

.inv-group__symbol {
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

.inv-group__info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.inv-group__code {
  font-size: 28rpx;
  color: $text-highlight;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.inv-group__count {
  font-size: 22rpx;
  color: $text-muted;
}

.inv-group__right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.inv-group__rate {
  font-size: 28rpx;
  color: $gold;
  font-weight: 600;
  @include amount-text;
}

.inv-group__details {
  border-top: 1rpx solid $border-subtle;
  background: $surface-1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.inv-product {
  display: flex;
  align-items: center;
  border-radius: 12rpx;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  overflow: hidden;
  transition: background 0.2s ease;

  &:active {
    background: $surface-2;
  }
}

.inv-product__accent {
  flex-shrink: 0;
  width: 4rpx;
  align-self: stretch;
  background: linear-gradient(180deg, rgba(201, 180, 138, 0.5) 0%, rgba(201, 180, 138, 0.1) 100%);
}

.inv-product__body {
  flex: 1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.inv-product__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;

  &--meta {
    font-size: 22rpx;
    color: $text-muted;
  }
}

.inv-product__name {
  font-size: 26rpx;
  color: $text-highlight;
  font-weight: 500;
}

.inv-product__rate-wrap {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding-right: 24rpx;
}

.inv-product__rate-tag {
  flex-shrink: 0;
  font-size: 20rpx;
  line-height: 1;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  background: rgba(255, 170, 51, 0.12);
  color: #ffaa33;
  white-space: nowrap;
}

.inv-product__rate {
  @include amount-text;
  font-size: 28rpx;
  color: $gold;
  font-weight: 600;
}

/* 常见问题 */
.inv-faq {
  position: relative;
  z-index: 1;
  margin: 16rpx 24rpx 0;
}

.inv-faq__title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
  font-size: 26rpx;
  color: $text-secondary;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.inv-faq__list {
  border-radius: 16rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  overflow: hidden;
}

.inv-faq__item {
  border-bottom: 1rpx solid $border-subtle;

  &:last-child {
    border-bottom: none;
  }
}

.inv-faq__q {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 500;
}

.inv-faq__a {
  padding: 0 28rpx 24rpx;
  font-size: 24rpx;
  color: $text-muted;
  line-height: 1.7;
}

/* 底部风险提示 */
.inv-footer {
  position: relative;
  z-index: 1;
  margin: 16rpx 24rpx 0;
  padding: 24rpx 28rpx;
  border-radius: 14rpx;
  background: $surface-1;
  border: 1rpx solid $border-subtle;
}

.inv-footer__title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
  font-size: 22rpx;
  color: $text-muted;
  font-weight: 500;
}

.inv-footer__text {
  font-size: 22rpx;
  line-height: 1.7;
  color: $text-muted;
}
</style>
