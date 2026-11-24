<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()
import type { AssetTransferRecord } from '@/api/types'
import { assetTransferApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { copyText } from '@/utils/app'
import { getAllCurrencies, loadAllCurrencies } from '@/utils/currency'
import CurrencySelectPopup from '@/components/CurrencySelectPopup.vue'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'assetTransferRecord',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const goldIcon = '#C9B48A'
const silverIcon = '#C4C6CA'
const mutedIcon = '#9A9690'

const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()

/** 弹窗内联背景：App 端 custom-class 的 theme-light 未必落到弹窗节点，改用内联样式兜底 */
const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)



const records = ref<AssetTransferRecord[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')

const currencies = ref(getAllCurrencies())
const filterAssetCode = ref('')
const filterFromType = ref('')
const filterToType = ref('')
const showCoinFilter = ref(false)
const showFromTypeFilter = ref(false)
const showToTypeFilter = ref(false)

const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const accountTypeOptions = computed(() => [
  { value: '', label: t('common.all') },
  { value: 'SPOT', label: dictItemText('accountType', 'SPOT') },
  { value: 'FUTURES', label: dictItemText('accountType', 'FUTURES') },
  { value: 'FIAT', label: dictItemText('accountType', 'FIAT') },
])

const filterCoinLabel = computed(() => {
  if (!filterAssetCode.value) return t('common.allCoin')
  const coin = currencies.value.find(c => c.currencyCode === filterAssetCode.value)
  return coin?.currencyCode || filterAssetCode.value
})

const filterFromTypeLabel = computed(() =>
  accountTypeOptions.value.find(o => o.value === filterFromType.value)?.label || t('common.all'),
)
const filterToTypeLabel = computed(() =>
  accountTypeOptions.value.find(o => o.value === filterToType.value)?.label || t('common.all'),
)

onLoad(() => {
  initPage()
})

onShow(() => {
  refreshData()
})

onReachBottom(() => {
  nextPage()
})

onPullDownRefresh(() => {
  refreshData(true)
})

async function initPage() {
  await loadAllCurrencies()
  currencies.value = getAllCurrencies()
}

function buildQueryParams() {
  const params: Record<string, unknown> = {
    pageNum: pageNum.value,
    pageSize: 10,
  }
  if (filterAssetCode.value) {
    params.assetCode = filterAssetCode.value
  }
  if (filterFromType.value) {
    params.fromAccountType = filterFromType.value
  }
  if (filterToType.value) {
    params.toAccountType = filterToType.value
  }
  return params
}

function refreshData(fromPull = false) {
  pageNum.value = 1
  records.value = []
  loadingState.value = 'loading'
  loadPage(fromPull)
}

function nextPage() {
  if (loadingState.value === 'nomore') return
  pageNum.value += 1
  loadPage()
}

async function loadPage(fromPull = false) {
  loadingState.value = 'loading'
  try {
    const page = await apiData(assetTransferApi.findByPage(buildQueryParams()))
    const list = page.content || []
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
    if (list.length === 0) {
      loadingState.value = 'nomore'
      return
    }
    loadingState.value = pageNum.value >= page.totalPage ? 'nomore' : 'loadmore'
    const ids = new Set(records.value.map(r => r.id))
    records.value.push(...list.filter(r => !ids.has(r.id)))
  }
  catch {
    loadingState.value = 'loadmore'
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

function selectCoin(code: string) {
  filterAssetCode.value = code
  refreshData()
}

function applyAccountType(value: string) {
  if (showFromTypeFilter.value) {
    filterFromType.value = value
  } else if (showToTypeFilter.value) {
    filterToType.value = value
  }
  showFromTypeFilter.value = false
  showToTypeFilter.value = false
  refreshData()
}

function closeAccountFilter() {
  showFromTypeFilter.value = false
  showToTypeFilter.value = false
}

function shortOrderNo(orderNo?: string) {
  if (!orderNo) return ''
  if (orderNo.length <= 12) return orderNo
  return `${orderNo.slice(0, 6)}···${orderNo.slice(-4)}`
}
</script>

<template>
  <view class="atr-page">
    <AppNavBar :title="t('assetTransferRecord.title')" />
    <view class="atr-page__glow" />
    <view class="atr-page__glow atr-page__glow--gold" />

    <view class="atr-body">
      <!-- 筛选栏 -->
      <view class="atr-filter">
        <view class="atr-filter__bar">
          <view class="atr-filter__item" @click="showCoinFilter = true">
            <text class="atr-filter__item-label">{{ filterCoinLabel }}</text>
            <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
          </view>
          <view class="atr-filter__divider" />
          <view class="atr-filter__direction">
            <view class="atr-filter__dt" @click="showFromTypeFilter = true">
              <text class="atr-filter__dt-label">{{ filterFromTypeLabel }}</text>
              <wd-icon name="caret-down" size="18rpx" :color="mutedIcon" />
            </view>
            <wd-icon name="arrow-right" size="20rpx" :color="mutedIcon" />
            <view class="atr-filter__dt" @click="showToTypeFilter = true">
              <text class="atr-filter__dt-label">{{ filterToTypeLabel }}</text>
              <wd-icon name="caret-down" size="18rpx" :color="mutedIcon" />
            </view>
          </view>
        </view>
      </view>

      <!-- 记录列表 -->
      <view class="atr-list">
        <view v-if="loadingState === 'loading' && records.length === 0" class="atr-list__loading">
          <text>{{ t('common.loading') }}</text>
        </view>

        <view v-else-if="noData" class="atr-list__empty">
          <view class="atr-list__empty-icon">
            <wd-icon name="history" size="64rpx" color="#4A4846" />
          </view>
          <text class="atr-list__empty-title">{{ t('assetTransferRecord.emptyTitle') }}</text>
          <text class="atr-list__empty-desc">{{ t('assetTransferRecord.emptyDesc') }}</text>
        </view>

        <view
          v-for="item in records"
          :key="item.id"
          class="atr-card"
        >
          <view class="atr-card__icon">
            <wd-icon name="swap" size="32rpx" :color="goldIcon" />
          </view>

          <view class="atr-card__main">
            <view class="atr-card__title-row">
              <text class="atr-card__from">{{ dictItemText('accountType', item.fromAccountType, item.fromAccountTypeName) }}</text>
              <wd-icon name="arrow-right" size="20rpx" :color="mutedIcon" />
              <text class="atr-card__to">{{ dictItemText('accountType', item.toAccountType, item.toAccountTypeName) }}</text>
            </view>
            <text class="atr-card__time">{{ item.createTime }}</text>
            <view v-if="item.orderNo" class="atr-card__order" @click.stop="copyText(item.orderNo)">
              <text class="atr-card__order-no">{{ shortOrderNo(item.orderNo) }}</text>
              <text class="atr-card__order-copy">{{ t('common.copy') }}</text>
            </view>
          </view>

          <view class="atr-card__side">
            <text class="atr-card__amount">{{ item.amount }}</text>
            <text class="atr-card__unit">{{ item.assetCode }}</text>
          </view>
        </view>

        <view
          v-if="records.length > 0"
          class="atr-list__more"
          @click="nextPage"
        >
          {{ loadingState === 'nomore' ? t('common.noMore') : loadingState === 'loading' ? t('common.loading') : t('common.loadMore') }}
        </view>
      </view>
    </view>

    <!-- 币种筛选弹窗 -->
    <CurrencySelectPopup v-model="showCoinFilter" :current="filterAssetCode" :root-portal="false" @select="selectCoin" />

    <!-- 账户类型筛选弹窗 -->
    <wd-popup
      :model-value="showFromTypeFilter || showToTypeFilter"
      position="bottom"
      :custom-style="popupStyle"
      :z-index="100"
      modal-style="background: rgba(0, 0, 0, 0.6)"
      :custom-class="`atr-account-popup theme-${theme}`"
      @close="closeAccountFilter"
    >
      <view class="popup-panel">
        <view class="popup-panel__head">
          <view class="popup-panel__head-side" />
          <text class="popup-panel__title">{{ showFromTypeFilter ? t('assetTransferRecord.popupFromTitle') : t('assetTransferRecord.popupToTitle') }}</text>
          <wd-icon class="popup-panel__close" name="close" size="32rpx" color="#9A9690" @click="closeAccountFilter" />
        </view>
        <view class="atr-account-list">
          <view
            v-for="opt in accountTypeOptions"
            :key="opt.value"
            class="atr-account-item"
            :class="{ 'atr-account-item--active': (showFromTypeFilter ? filterFromType : filterToType) === opt.value }"
            @click="applyAccountType(opt.value)"
          >
            <text class="atr-account-item__label">{{ opt.label }}</text>
            <wd-icon
              v-if="(showFromTypeFilter ? filterFromType : filterToType) === opt.value"
              name="check"
              size="32rpx"
              :color="goldIcon"
            />
          </view>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.atr-page {
  @include page-shell;
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.atr-page__glow {
  @include page-top-glow;
  height: 300rpx;

  &--gold {
    height: 400rpx;
    background: radial-gradient(ellipse 90% 60% at 50% -10%, rgba(201, 180, 138, 0.08) 0%, transparent 72%);
  }
}

.atr-body {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx 48rpx;
}

/* ===================== 筛选栏 ===================== */
.atr-filter {
  margin-bottom: 20rpx;

  &__bar {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 16rpx 20rpx;
    border-radius: 14rpx;
    background: rgba(var(--surface-2-rgb), 0.8);
    border: 1rpx solid $border-subtle;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 16rpx;
    border-radius: 999rpx;
    background: $surface-3;
    border: 1rpx solid $border-subtle;

    &-label {
      font-size: 24rpx;
      color: $text-secondary;
    }
  }

  &__direction {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10rpx;
    min-width: 0;
  }

  &__dt {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 8rpx 14rpx;
    border-radius: 999rpx;
    background: $surface-3;
    border: 1rpx solid $border-subtle;
    max-width: 50%;

    &-label {
      font-size: 22rpx;
      color: $text-secondary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  &__divider {
    flex-shrink: 0;
    width: 1rpx;
    height: 28rpx;
    background: $border-subtle;
  }

  &__clear {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6rpx;
    margin-top: 12rpx;
    font-size: 22rpx;
    color: $text-muted;
  }
}

/* ===================== 记录列表 ===================== */
.atr-list {
  &__loading,
  &__more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40rpx 0;
    font-size: 24rpx;
    color: $text-muted;
    letter-spacing: 0.04em;
  }

  &__empty {
    @include list-empty;
    padding-top: 160rpx;

    &-icon {
      margin-bottom: 8rpx;
    }

    &-title {
      font-size: 28rpx;
      color: $text-secondary;
    }

    &-desc {
      font-size: 22rpx;
      color: $text-muted;
    }
  }
}

.atr-card {
  @include record-card;
  display: flex;
  align-items: flex-start;
  gap: 20rpx;

  &__icon {
    flex-shrink: 0;
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(201, 180, 138, 0.08);
    border: 1rpx solid rgba(201, 180, 138, 0.14);
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__title-row {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__from,
  &__to {
    font-size: 26rpx;
    color: $text-highlight;
    font-weight: 500;
  }

  &__time {
    display: block;
    margin-top: 10rpx;
    font-size: 22rpx;
    color: $text-muted;
  }

  &__order {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 10rpx;
  }

  &__order-no {
    font-size: 20rpx;
    color: $text-muted;
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    letter-spacing: 0.04em;
  }

  &__order-copy {
    font-size: 20rpx;
    color: $gold;
  }

  &__side {
    flex-shrink: 0;
    text-align: right;
  }

  &__amount {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $gold-light;
    @include amount-text;
  }

  &__unit {
    display: block;
    margin-top: 4rpx;
    font-size: 22rpx;
    color: $text-muted;
  }
}

.atr-account-popup {
  border-radius: 24rpx 24rpx 0 0;
  background: $surface-2;
}

.popup-panel {
  padding: 32rpx 28rpx;
  max-height: 70vh;
  overflow-y: auto;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;

    &-side {
      width: 32rpx;
      flex-shrink: 0;
    }
  }

  &__title {
    flex: 1;
    text-align: center;
    font-size: 30rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__close {
    flex-shrink: 0;
  }
}

.atr-account-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.atr-account-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 12rpx;
  border-radius: 12rpx;

  &--active {
    background: $gold-soft;
  }

  &__label {
    font-size: 26rpx;
    font-weight: 500;
    color: $text-highlight;
  }
}
</style>
