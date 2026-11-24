<script setup lang="ts">
import type { ConvertOrderItem } from '@/api/types'
import { convertApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { loadAllCurrencies, getAllCurrencies } from '@/utils/currency'
import CurrencySelectPopup from '@/components/CurrencySelectPopup.vue'
import DateRangeFilterPopup from '@/components/DateRangeFilterPopup.vue'
import { lastNDays } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'convertRecord',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const { t } = useI18n()
const { currentLang } = useI18nSync()



const goldIcon = '#C9B48A'
const mutedIcon = '#9A9690'

const currencies = ref(getAllCurrencies())
const records = ref<ConvertOrderItem[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')

const filterAssetCode = ref('')
const showCoinFilter = ref(false)
const dateStart = ref('')
const dateEnd = ref('')
const dateQuickLabel = ref('')
const showDateFilter = ref(false)

const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const filterCoinLabel = computed(() => (filterAssetCode.value ? filterAssetCode.value : t('common.allCoin')))
const filterDateLabel = computed(() => {
  if (dateQuickLabel.value) return dateQuickLabel.value
  if (dateStart.value && dateEnd.value) return `${dateStart.value} / ${dateEnd.value}`
  if (dateStart.value) return `${dateStart.value} /`
  if (dateEnd.value) return `/ ${dateEnd.value}`
  return t('common.allTime')
})

/** 默认近30天 */
const last30 = lastNDays(30)
dateStart.value = last30.start
dateEnd.value = last30.end
dateQuickLabel.value = t('dateRange.last30')

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
    pageSize: 20,
  }
  if (filterAssetCode.value) {
    params.fromAssetCode = filterAssetCode.value
  }
  if (dateStart.value) params.timeStart = `${dateStart.value} 00:00:00`
  if (dateEnd.value) params.timeEnd = `${dateEnd.value} 23:59:59`
  return params
}

function refreshData(fromPull = false) {
  pageNum.value = 1
  records.value = []
  loadingState.value = 'loading'
  loadRecords(fromPull)
}

function nextPage() {
  if (loadingState.value === 'nomore' || loadingState.value === 'loading') return
  pageNum.value += 1
  loadRecords()
}

async function loadRecords(fromPull = false) {
  loadingState.value = 'loading'
  try {
    const page = await apiData(convertApi.findByPage(buildQueryParams()))
    const list = page.content || []
    if (fromPull) uni.stopPullDownRefresh()
    if (list.length === 0 && pageNum.value === 1) {
      loadingState.value = 'nomore'
      return
    }
    if (pageNum.value >= page.totalPage) {
      loadingState.value = 'nomore'
    } else {
      loadingState.value = 'loadmore'
    }
    const ids = new Set(records.value.map(r => r.id))
    records.value.push(...list.filter(r => !ids.has(r.id)))
  } catch {
    loadingState.value = 'loadmore'
    if (fromPull) uni.stopPullDownRefresh()
  }
}

function selectCoin(code: string) {
  filterAssetCode.value = code
  refreshData()
}

function onDateConfirm(payload: { start: string; end: string; quickLabel?: string }) {
  dateStart.value = payload.start
  dateEnd.value = payload.end
  dateQuickLabel.value = payload.quickLabel || ''
  refreshData()
}

function fmtNum(val: number | string | undefined | null): string {
  if (val == null) return '0'
  const n = Number(val)
  if (isNaN(n)) return '0'
  if (n === 0) return '0'
  return n.toFixed(8).replace(/\.?0+$/, '')
}

</script>

<template>
  <view class="cr-page">
    <AppNavBar :title="t('convertRecord.title')" />
    <view class="cr-page__glow" />
    <view class="cr-page__glow cr-page__glow--gold" />

    <view class="cr-body">
      <!-- 筛选栏 -->
      <view class="cr-filter">
        <view class="cr-filter__item" @click="showCoinFilter = true">
          <wd-icon name="qrcode" size="24rpx" :color="goldIcon" />
          <text class="cr-filter__label">{{ filterCoinLabel }}</text>
          <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
        </view>
        <view class="cr-filter__item" @click="showDateFilter = true">
          <wd-icon name="clock-circle" size="24rpx" :color="goldIcon" />
          <text class="cr-filter__label">{{ filterDateLabel }}</text>
          <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
        </view>
      </view>

      <!-- 列表 -->
      <view v-if="noData" class="cr-empty">
        <view class="cr-empty__icon">
          <wd-icon name="history" size="64rpx" :color="goldIcon" />
        </view>
        <text class="cr-empty__title">{{ t('convertRecord.emptyTitle') }}</text>
        <text class="cr-empty__desc">{{ t('convertRecord.emptyDesc') }}</text>
      </view>

      <view v-else class="cr-list">
        <view v-for="item in records" :key="item.id" class="cr-card">
          <view class="cr-card__head">
            <view class="cr-card__head-left">
              <view class="cr-card__pair">
                <text class="cr-card__from">{{ item.fromAssetCode }}</text>
                <wd-icon name="arrow-right" size="20rpx" :color="mutedIcon" />
                <text class="cr-card__to">{{ item.toAssetCode }}</text>
              </view>
            </view>
            <view class="cr-card__head-right">
              <text class="cr-card__time">{{ item.createTime }}</text>
            </view>
          </view>
          <view class="cr-card__body">
            <view class="cr-card__row">
              <text class="cr-card__col-label">{{ t('convert.payRole') }}</text>
              <view class="cr-card__row-value">
                <text class="cr-card__col-value">{{ fmtNum(item.fromAmount) }}</text>
                <text class="cr-card__col-unit">{{ item.fromAssetCode }}</text>
              </view>
            </view>
            <view class="cr-card__row">
              <text class="cr-card__col-label">{{ t('convert.receiveRole') }}</text>
              <view class="cr-card__row-value">
                <text class="cr-card__col-value cr-card__col-value--gold">{{ fmtNum(item.toAmount) }}</text>
                <text class="cr-card__col-unit">{{ item.toAssetCode }}</text>
              </view>
            </view>
            <view class="cr-card__row">
              <text class="cr-card__col-label">{{ t('convertRecord.colRate') }}</text>
              <view class="cr-card__row-value">
                <text class="cr-card__col-value cr-card__col-value--sm">{{ t('convert.rateLine', { from: item.fromAssetCode, rate: fmtNum(item.exchangeRate), to: item.toAssetCode }) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="records.length > 0" class="cr-footer">
          <text v-if="loadingState === 'loading'">{{ t('common.loading') }}</text>
          <text v-else-if="loadingState === 'nomore'">{{ t('common.noMore') }}</text>
        </view>
      </view>
    </view>

    <!-- 币种筛选弹窗 -->
    <CurrencySelectPopup v-model="showCoinFilter" :current="filterAssetCode" @select="selectCoin" />

    <!-- 时间范围筛选弹窗 -->
    <DateRangeFilterPopup v-model="showDateFilter" :start="dateStart" :end="dateEnd" :default-days="30" @confirm="onDateConfirm" />
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.cr-page {
  @include page-shell;
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.cr-page__glow {
  @include page-top-glow;
  height: 300rpx;

  &--gold {
    height: 400rpx;
    background: radial-gradient(ellipse 90% 60% at 50% -10%, rgba(201, 180, 138, 0.08) 0%, transparent 72%);
  }
}

.cr-body {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx 48rpx;
}

/* ===================== 筛选栏 ===================== */
.cr-filter {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 16rpx;
  margin: 20rpx 0 8rpx;

  &__item {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 14rpx 22rpx;
    background: $surface-2;
    border: 1rpx solid $border-subtle;
    border-radius: 36rpx;
  }

  &__label {
    font-size: 24rpx;
    color: $text-secondary;
    max-width: 280rpx;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}

/* ===================== 空状态 ===================== */
.cr-empty {
  @include list-empty;
  padding-top: 160rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;

  &__icon {
    margin-bottom: 4rpx;
  }

  &__title {
    font-size: 28rpx;
    color: $text-secondary;
  }

  &__desc {
    font-size: 22rpx;
    color: $text-muted;
  }
}

/* ===================== 记录列表 ===================== */
.cr-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.cr-card {
  position: relative;
  padding: 28rpx 24rpx 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(160deg, $surface-2 0%, $surface-1 100%);
  border: 1rpx solid $border-subtle;
  box-shadow: inset 0 1rpx 0 $highlight-edge;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4rpx;
    background: $gold;
    opacity: 0.5;
  }

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }

  &__head-left {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  &__head-right {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__pair {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__from,
  &__to {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__time {
    font-size: 22rpx;
    color: $text-muted;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 14rpx;
  }

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__row-value {
    display: flex;
    align-items: baseline;
    gap: 6rpx;
  }

  &__col-label {
    @include label-caps;
    font-size: 20rpx;
  }

  &__col-value {
    font-size: 26rpx;
    color: $text-primary;
    @include amount-text;

    &--gold {
      color: $gold-light;
    }

    &--sm {
      font-size: 22rpx;
    }
  }

  &__col-unit {
    font-size: 20rpx;
    color: $text-muted;
  }
}

.cr-footer {
  position: relative;
  z-index: 1;
  padding: 24rpx 0;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}
</style>
