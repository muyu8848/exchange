<script setup lang="ts">
import type { AssetByCurrency, AssetByAccount } from '@/api/types'
import { memberAssetApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useMemberStore } from '@/store/member'
import { useAppStore } from '@/store/app'
import { loadAllCurrencies, currencyLogo, currencyName } from '@/utils/currency'
import AddFundsSheet from '@/components/AddFundsSheet.vue'
import AppTabs from '@/components/AppTabs.vue'
import type { AddFundOption } from '@/components/AddFundsSheet.vue'
import { useI18n } from 'vue-i18n'
import { useDictItemText } from '@/utils/dictItem'
import { useAddFundsOptions } from '@/composables/useAddFundsOptions'
import { useNavTitle } from '@/composables/useNavTitle'

definePage({
  name: 'assets',
  layout: 'tabbar',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const { t } = useI18n()
const dictItemText = useDictItemText()
const memberStore = useMemberStore()
const appStore = useAppStore()

// 资产页没有导航栏，借助 composable 同步浏览器标题
useNavTitle(() => t('tabbar.assets'))

/** 折算后的法币金额；该法币汇率暂不可用时为 null */
const totalFiat = ref<number | null>(null)
const totalUsdt = ref('0.00')
/** 当前折算展示货币（法币代码，由「折算货币」页设置） */
const fiatCode = ref('CNY')
const hideBalance = ref(false)
const loading = ref(false)
const activeTab = ref<'currency' | 'account'>('currency')
const subTabs = computed<{ key: 'currency' | 'account', label: string }[]>(() => [
  { key: 'currency', label: t('assets.tabCurrency') },
  { key: 'account', label: t('assets.tabAccount') },
])
const currencyAssets = ref<AssetByCurrency[]>([])
const accountAssets = ref<AssetByAccount[]>([])

onShow(() => {
  memberStore.ensureMemberInfo()
  loadAllCurrencies()
  loadData()
})

onPullDownRefresh(() => {
  loadData(true)
})

async function loadData(fromPull = false) {
  loading.value = true
  try {
    const [totalRes, currencyRes, accountRes] = await Promise.all([
      apiData(memberAssetApi.getTotalValue(appStore.effectiveDisplayFiat)),
      apiData(memberAssetApi.findByCurrencySummary()),
      apiData(memberAssetApi.findByAccountTypeSummary()),
    ])
    // 后端已按法币精度格式化，直接展示
    totalFiat.value = totalRes?.totalFiat ?? null
    fiatCode.value = totalRes?.fiatCode || appStore.effectiveDisplayFiat
    totalUsdt.value = String(totalRes?.totalUsdt ?? '0.00')
    currencyAssets.value = currencyRes || []
    accountAssets.value = accountRes || []
  }
  catch {
    // ignore
  }
  finally {
    loading.value = false
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

function switchTab(value: string | number) {
  activeTab.value = value as 'currency' | 'account'
}

function toggleHideBalance() {
  hideBalance.value = !hideBalance.value
}

function formatNum(val: number | string | undefined, decimals?: number) {
  if (val == null) return '0'
  const n = Number(val)
  if (isNaN(n)) return '0'
  if (decimals != null) {
    return n.toFixed(decimals)
  }
  return String(val)
}

function maskVal(val: string | number) {
  return hideBalance.value ? '****' : String(val)
}

/** 折算金额展示：汇率不可用时显示占位符（不显示 0，避免误解为资产归零） */
const totalFiatText = computed(() => (totalFiat.value == null ? '—' : maskVal(totalFiat.value)))

function goDisplayCurrency() {
  router.push({ name: 'displayCurrency' })
}

function goWithdraw() {
  router.push({ name: 'withdraw' })
}

function goTransfer() {
  router.push({ name: 'assetTransfer' })
}

function goConvert() {
  router.push({ name: 'assetChangeLog' })
}

const showAddFunds = ref(false)
const addFundsOptions = useAddFundsOptions()

function onAddFundsSelect(option: AddFundOption) {
  router.push(option.route)
}

function goAssetDetail(currencyCode: string) {
  router.push({ name: 'assetDetail', query: { assetCode: currencyCode } })
}

function goInvestmentOverview() {
  router.push({ name: 'investmentOverview' })
}

</script>

<template>
  <view class="assets-page">
    <view class="assets-page__glow" />

    <view class="assets-body">
      <view class="assets-total-card">
        <view class="assets-total-card__hairline" />
        <view class="assets-total-card__label-row">
          <text class="assets-total-card__label">{{ t('common.totalAssetsUsdt') }}</text>
          <view class="assets-total-card__eye" @click="toggleHideBalance">
            <wd-icon :name="hideBalance ? 'eye' : 'eye-invisible'" size="36rpx" color="#AEAAA4" />
          </view>
        </view>
        <view class="assets-total-card__amount-row">
          <text class="assets-total-card__amount">{{ maskVal(totalUsdt) }}</text>
        </view>
        <view class="assets-total-card__sub">
          <text class="assets-total-card__sub-amount">≈ {{ totalFiatText }}</text>
          <view
            class="assets-total-card__fiat"
            hover-class="assets-total-card__fiat--active"
            @click="goDisplayCurrency"
          >
            <text>{{ fiatCode }}</text>
            <wd-icon name="right" size="20rpx" color="#C9B48A" />
          </view>
        </view>
      </view>

      <view class="assets-actions">
        <view class="assets-action-btn" @click="showAddFunds = true">
          <wd-icon name="plus" size="40rpx" color="#C9B48A" />
          <text>{{ t('common.addFund') }}</text>
        </view>
        <view class="assets-action-btn" @click="goWithdraw">
          <wd-icon name="upload" size="40rpx" color="#C9B48A" />
          <text>{{ t('withdraw.title') }}</text>
        </view>
        <view class="assets-action-btn" @click="goTransfer">
          <wd-icon name="swap" size="40rpx" color="#C9B48A" />
          <text>{{ t('common.transfer') }}</text>
        </view>
        <view class="assets-action-btn" @click="goConvert">
          <wd-icon name="list" size="40rpx" color="#C9B48A" />
          <text>{{ t('assets.bill') }}</text>
        </view>
      </view>

      <view class="assets-sub-tabs-wrap">
        <AppTabs
          variant="underline" size="md" :gap="8" item-padding="8rpx 24rpx 14rpx"
          :model-value="activeTab" :items="subTabs" @update:model-value="switchTab"
        />
      </view>

      <view class="assets-list">
        <view v-if="loading" class="assets-list__loading">
          <wd-icon name="loading" size="48rpx" color="#4A4846" />
        </view>
        <view
          v-else-if="(activeTab === 'currency' && currencyAssets.length === 0) || (activeTab === 'account' && accountAssets.length === 0)"
          class="assets-list__empty"
        >
          <wd-icon name="safe" size="72rpx" color="#4A4846" />
          <text>{{ t('assets.empty') }}</text>
        </view>
        <template v-else>
          <template v-if="activeTab === 'currency'">
            <view
              v-for="item in currencyAssets"
              :key="item.currencyCode"
              class="asset-row"
              @click="goAssetDetail(item.currencyCode)"
            >
              <view class="asset-row__icon">
                <image
                  v-if="currencyLogo(item.currencyCode)"
                  :src="currencyLogo(item.currencyCode)"
                  class="asset-row__logo"
                  mode="aspectFit"
                />
                <text v-else class="asset-row__symbol">{{ item.currencyCode?.charAt(0) || '?' }}</text>
              </view>
              <view class="asset-row__main">
                <text class="asset-row__code">{{ item.currencyCode }}</text>
                <text class="asset-row__name">{{ currencyName(item.currencyCode, item.currencyName) }}</text>
              </view>
              <view class="asset-row__right">
                <text class="asset-row__avail">{{ maskVal(formatNum(item.total)) }}</text>
                <text v-if="item.usdtValue != null" class="asset-row__usdt">{{ maskVal(formatNum(item.usdtValue)) }} USDT</text>
              </view>
            </view>
          </template>
          <template v-else>
            <view
              v-for="item in accountAssets"
              :key="item.accountType"
              class="asset-row"
              @click="item.accountType === 'INVEST' ? goInvestmentOverview() : undefined"
            >
              <view class="asset-row__icon asset-row__icon--account">
                <wd-icon name="safe" size="32rpx" color="#C9B48A" />
              </view>
              <view class="asset-row__main">
                <text class="asset-row__code">{{ dictItemText('accountType', item.accountType, item.accountTypeName) }}</text>
              </view>
              <view class="asset-row__right">
                <text class="asset-row__usdt">{{ maskVal(formatNum(item.usdtValue)) }} USDT</text>
              </view>
            </view>
          </template>
        </template>
      </view>
    </view>

    <AddFundsSheet
      v-model="showAddFunds"
      :options="addFundsOptions"
      @select="onAddFundsSelect"
    />
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.assets-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding-bottom: 140rpx;
}

.assets-page__glow {
  @include page-top-glow;
}

.assets-body {
  position: relative;
  z-index: 1;
  padding: calc(env(safe-area-inset-top) + 24rpx) 28rpx 0;
}

.assets-total-card {
  @include dark-card;
  position: relative;
  padding: 36rpx 32rpx 32rpx;
  background:
    linear-gradient(155deg, rgba(201, 180, 138, 0.06) 0%, transparent 34%),
    $surface-2;
}

.assets-total-card__hairline {
  position: absolute;
  top: 0;
  left: 40rpx;
  right: 40rpx;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(184, 160, 120, 0.42), transparent);
}

.assets-total-card__label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.assets-total-card__label {
  @include label-caps;
  font-size: 20rpx;
}

.assets-total-card__eye {
  display: flex;
  align-items: center;
  justify-content: center;
}

.assets-total-card__amount-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.assets-total-card__amount {
  @include amount-text;
  font-size: 56rpx;
  font-weight: 500;
  color: $gold-light;
  line-height: 1;
}

.assets-total-card__sub {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.assets-total-card__sub-amount {
  font-size: 24rpx;
  color: $text-secondary;
}

/* 仅折算货币代码可点击，热区放大便于点按 */
.assets-total-card__fiat {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 12rpx;
  margin-left: -6rpx;
  border-radius: 8rpx;
  background: $gold-soft;
  font-size: 22rpx;
  color: $gold;
  line-height: 1.3;
}

.assets-total-card__fiat--active {
  opacity: 0.6;
}

.assets-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 24rpx;
}

.assets-action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 24rpx 0;
  border-radius: 12rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
  font-size: 22rpx;
  color: $text-secondary;
}

/* 子 tab 外层：视觉交给 AppTabs，这里只保留分隔线与间距 */
.assets-sub-tabs-wrap {
  margin-top: 28rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid $border-subtle;
}

.assets-list {
  margin-top: 16rpx;
}

.assets-list__loading,
.assets-list__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 80rpx 24rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.asset-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 12rpx;
  border-radius: 12rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
}

.asset-row__icon {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $surface-3;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  &--account {
    background: $surface-3;
  }
}

.asset-row__logo {
  width: 100%;
  height: 100%;
}

.asset-row__symbol {
  font-size: 28rpx;
  font-weight: 600;
  color: $gold-light;
}

.asset-row__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.asset-row__code {
  font-size: 28rpx;
  color: $text-primary;
  font-weight: 500;
}

.asset-row__name {
  font-size: 20rpx;
  color: $text-muted;
}

.asset-row__right {
  text-align: right;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.asset-row__avail {
  font-size: 22rpx;
  color: $text-muted;
}

.asset-row__usdt {
  font-size: 26rpx;
  color: $text-highlight;
  font-weight: 500;
}


</style>
