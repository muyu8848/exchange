<script setup lang="ts">
import type { MerchantProfileVO, PreOrderItem, TradablePreOrder } from '@/api/types'
import { c2cApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { currencyLogo } from '@/utils/currency'
import { c2cFiatSymbolOf } from '@/utils/c2cOption'
import { DEFAULT_LOGO } from '@/utils/paymentAccountType'
import MerchantProfileCard from '@/components/MerchantProfileCard.vue'
import AppTabs from '@/components/AppTabs.vue'

definePage({
  name: 'merchantProfile',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const { t } = useI18n()
const { currentLang } = useI18nSync()

const userId = ref('')
const profile = ref<MerchantProfileVO | null>(null)
const activeAds = ref<PreOrderItem[]>([])
const currentTab = ref(0)
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')


onLoad((options) => {
  userId.value = options?.userId || ''
  loadProfile()
  loadAds()
})

onReachBottom(() => {
  nextPage()
})

async function loadProfile() {
  if (!userId.value) {
    return
  }
  try {
    profile.value = await apiData(c2cApi.getMerchantProfile(userId.value))
  }
  catch {
    profile.value = null
  }
}

function adType(): string {
  return currentTab.value === 0 ? 'sell' : 'buy'
}

async function loadAds(fromPull = false) {
  if (!userId.value) {
    return
  }
  loadingState.value = 'loading'
  try {
    const page = await apiData(c2cApi.findTradablePreOrderByPage({
      memberAccountId: userId.value,
      orderType: adType(),
      pageNum: pageNum.value,
      pageSize: 10,
    }))
    const rawList: TradablePreOrder[] = page.content || []
    const list: PreOrderItem[] = rawList.map(ad => ({
      id: ad.id,
      amount: ad.availableAmount,
      availableAmount: ad.availableAmount,
      assetCode: ad.assetCode,
      singleMinFiatAmount: ad.singleMinFiatAmount,
      singleMaxFiatAmount: ad.singleMaxFiatAmount,
      status: '1',
      statusName: '',
      createTime: '',
      supportedPaymentAccountTypes: ad.supportedPaymentAccountTypes,
      price: ad.price,
      priceType: ad.priceType,
      premiumRate: ad.premiumRate,
      orderType: ad.orderType,
      fiatCode: ad.fiatCode,
      fiatAmount: ad.fiatAmount,
      remark: ad.remark,
    }))
    if (fromPull) {
      activeAds.value = list
      uni.stopPullDownRefresh()
    }
    else {
      const ids = new Set(activeAds.value.map(a => a.id))
      activeAds.value.push(...list.filter(a => !ids.has(a.id)))
    }
    if (pageNum.value >= page.totalPage) {
      loadingState.value = 'nomore'
    }
    else {
      loadingState.value = 'loadmore'
    }
  }
  catch {
    loadingState.value = 'loadmore'
  }
}

function nextPage() {
  if (loadingState.value === 'nomore') {
    return
  }
  pageNum.value += 1
  loadAds()
}

const tabs = computed<{ key: number, label: string }[]>(() => [
  { key: 0, label: t('c2cMerchantProfile.tabSell') },
  { key: 1, label: t('c2cMerchantProfile.tabBuy') },
])

function onTabChange(value: string | number) {
  const index = Number(value)
  if (currentTab.value === index)
    return
  currentTab.value = index
  pageNum.value = 1
  activeAds.value = []
  loadingState.value = 'loadmore'
  loadAds()
}

/** 跳转下单 */
function goOrderCreate(ad: PreOrderItem | TradablePreOrder) {
  router.push({ name: 'orderCreate', query: { id: ad.id } })
}

</script>

<template>
  <view class="profile-page">
    <AppNavBar :title="t('c2cMerchantProfile.title')" />
    <view v-if="profile" class="profile-page__inner">
      <MerchantProfileCard :profile="profile" />

      <!-- Tab：TA 在卖 / TA 在买（AppTabs bar：等分 + 通栏底线 + 短线指示条） -->
      <view class="profile-page__tabs-wrap">
        <AppTabs
          variant="bar" size="md"
          :model-value="currentTab" :items="tabs" @update:model-value="onTabChange"
        />
      </view>

      <!-- 广告列表 -->
      <view v-if="activeAds.length" class="profile-page__ads">
        <view
          v-for="ad in activeAds"
          :key="ad.id"
          class="merchant-ad"
          @click="goOrderCreate(ad)"
        >
          <view class="merchant-ad__hairline" />

          <!-- Row 1: 币种 logo + 币种 -->
          <view class="merchant-ad__asset-row">
            <wd-img
              :src="currencyLogo(ad.assetCode) || '/static/img/coin-default.svg'"
              width="40rpx"
              height="40rpx"
            />
            <text class="merchant-ad__asset-code">{{ ad.assetCode }}</text>
          </view>

          <!-- Row 2: 法币符号 + 单价 -->
          <view class="merchant-ad__price-row">
            <text class="merchant-ad__price-symbol">{{ c2cFiatSymbolOf(ad.fiatCode) }}</text>
            <text class="merchant-ad__price">{{ ad.price != null ? ad.price : '—' }}</text>
          </view>

          <!-- Row 3: 数量 + 按钮 -->
          <view class="merchant-ad__qty-row">
            <view class="merchant-ad__qty">
              <text class="merchant-ad__qty-label">{{ t('common.amount') }}</text>
              <text class="merchant-ad__qty-value">{{ ad.availableAmount }}</text>
              <text class="merchant-ad__qty-unit">{{ ad.assetCode }}</text>
            </view>
            <view
              class="merchant-ad__action"
              :class="ad.orderType === 'sell' ? 'merchant-ad__action--buy' : 'merchant-ad__action--sell'"
            >
              {{ ad.orderType === 'sell' ? t('c2cCommon.buy') : t('c2cCommon.sell') }}
            </view>
          </view>

          <!-- Row 4: 限额 -->
          <view class="merchant-ad__limit-row">
            <text class="merchant-ad__limit-label">{{ t('c2cCommon.limit') }}</text>
            <text class="merchant-ad__limit-value">
              {{ c2cFiatSymbolOf(ad.fiatCode) }}{{ ad.singleMinFiatAmount != null ? ad.singleMinFiatAmount : '0' }}
              ~
              {{ c2cFiatSymbolOf(ad.fiatCode) }}{{ ad.singleMaxFiatAmount != null ? ad.singleMaxFiatAmount : '∞' }}
            </text>
          </view>

          <!-- Row 5: 支付方式 -->
          <scroll-view
            v-if="ad.supportedPaymentAccountTypes && ad.supportedPaymentAccountTypes.length"
            class="merchant-ad__pay-scroll"
            scroll-x
            :show-scrollbar="false"
          >
            <view class="merchant-ad__pay-list">
              <view
                v-for="pt in ad.supportedPaymentAccountTypes"
                :key="pt.code"
                class="merchant-ad__pay-chip"
              >
                <wd-img
                  :src="pt.logo || DEFAULT_LOGO"
                  width="28rpx"
                  height="28rpx"
                />
                <text>{{ pt.name }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
      <view v-else class="profile-page__empty">{{ t('c2cMerchantProfile.empty') }}</view>
      <view v-if="activeAds.length" class="profile-page__more" @click="nextPage">
        {{ loadingState === 'nomore' ? t('common.noMore') : t('common.loadMore') }}
      </view>
    </view>

    <!-- 加载中 -->
    <view v-else class="profile-page__loading">
      <wd-loading />
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.profile-page {
  @include page-shell;
  min-height: 100vh;

  &__inner {
    padding: 24rpx 28rpx 80rpx;
  }

  &__loading {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 200rpx 0;
  }
}

/* ===== Tabs 外层：视觉交给 AppTabs，这里只负责间距与底线 ===== */
.profile-page__tabs-wrap {
  margin-top: 28rpx;
  margin-bottom: 20rpx;
}

/* ===== Ads ===== */
.profile-page__ads {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* --- Merchant Ad Card --- */
.merchant-ad {
  position: relative;
  padding: 24rpx 28rpx;
  border-radius: 16rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
}

.merchant-ad__hairline {
  position: absolute;
  top: 0;
  left: 28rpx;
  right: 28rpx;
  height: 1rpx;
  background: $border-subtle;
}

/* Row 1: Asset */
.merchant-ad__asset-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.merchant-ad__asset-code {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-highlight;
}

/* Row 2: Price */
.merchant-ad__price-row {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin-bottom: 12rpx;
}

.merchant-ad__price-symbol {
  font-size: 28rpx;
  color: $text-secondary;
}

.merchant-ad__price {
  font-size: 36rpx;
  font-weight: 600;
  color: $text-highlight;
}

/* Row 3: Qty + Action */
.merchant-ad__qty-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.merchant-ad__qty {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.merchant-ad__qty-label {
  font-size: 24rpx;
  color: $text-muted;
}

.merchant-ad__qty-value {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 500;
}

.merchant-ad__qty-unit {
  font-size: 24rpx;
  color: $text-primary;
  margin-left: 4rpx;
}

.merchant-ad__action {
  padding: 12rpx 32rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  font-weight: 500;
}

.merchant-ad__action--buy {
  background: #9cc9a8;
  color: #121511;
}

.merchant-ad__action--sell {
  background: #d89090;
  color: #1a0e0e;
}

/* Row 4: Limit */
.merchant-ad__limit-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.merchant-ad__limit-label {
  font-size: 24rpx;
  color: $text-muted;
  flex-shrink: 0;
}

.merchant-ad__limit-value {
  font-size: 24rpx;
  color: $text-secondary;
}

/* Row 5: Payment */
.merchant-ad__pay-scroll {
  width: 100%;
  white-space: nowrap;
}

.merchant-ad__pay-list {
  display: flex;
  gap: 10rpx;
}

.merchant-ad__pay-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  background: $surface-3;
  font-size: 22rpx;
  color: $text-secondary;
}

.profile-page__empty {
  padding: 60rpx 0;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}

.profile-page__more {
  padding: 32rpx 0;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}
</style>
