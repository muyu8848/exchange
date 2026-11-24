<script setup lang="ts">
import type { DepositOrder } from '@/api/types'
import { depositApi } from '@/api/modules'
import { apiData } from '@/api/http'
import CurrencySelectPopup from '@/components/CurrencySelectPopup.vue'
import DateRangeFilterPopup from '@/components/DateRangeFilterPopup.vue'
import { lastNDays } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'depositRecord',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()

const { t } = useI18n()
const dictItemText = useDictItemText()
const { currentLang } = useI18nSync()



const goldIcon = '#C9B48A'
const mutedIcon = '#9A9690'
const records = ref<DepositOrder[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')

const filterAssetCode = ref('')
const dateStart = ref('')
const dateEnd = ref('')
const dateQuickLabel = ref('')
const showCoinFilter = ref(false)
const showDateFilter = ref(false)

/** 默认近30天 */
const last30 = lastNDays(30)
dateStart.value = last30.start
dateEnd.value = last30.end
dateQuickLabel.value = t('dateRange.last30')

const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const filterCoinLabel = computed(() => (filterAssetCode.value ? filterAssetCode.value : t('common.allCoin')))
const filterDateLabel = computed(() => {
  if (dateQuickLabel.value) return dateQuickLabel.value
  if (dateStart.value && dateEnd.value) return `${dateStart.value} / ${dateEnd.value}`
  if (dateStart.value) return `${dateStart.value} /`
  if (dateEnd.value) return `/ ${dateEnd.value}`
  return t('common.allTime')
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

function refreshData(fromPull = false) {
  pageNum.value = 1
  records.value = []
  loadingState.value = 'loading'
  loadPage(fromPull)
}

function nextPage() {
  if (loadingState.value !== 'loadmore') return
  pageNum.value += 1
  loadPage()
}

async function loadPage(fromPull = false) {
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value,
      pageSize: 15,
    }
    if (filterAssetCode.value) params.assetCode = filterAssetCode.value
    if (dateStart.value) params.timeStart = `${dateStart.value} 00:00:00`
    if (dateEnd.value) params.timeEnd = `${dateEnd.value} 23:59:59`
    const page = await apiData(depositApi.findByPage(params))
    const list = page?.content || []
    if (pageNum.value === 1) {
      records.value = list
    } else {
      records.value = records.value.concat(list)
    }
    const total = page?.total || 0
    loadingState.value = records.value.length >= total ? 'nomore' : 'loadmore'
  } catch {
    if (pageNum.value === 1) records.value = []
    loadingState.value = 'nomore'
  } finally {
    if (fromPull) uni.stopPullDownRefresh()
  }
}

function statusClass(status?: string) {
  if (status === '3') return 'dr-card__status--done'
  if (status === '1') return 'dr-card__status--pending'
  if (status === '2') return 'dr-card__status--confirm'
  if (status === '4') return 'dr-card__status--expired'
  return 'dr-card__status--closed'
}

function statusDot(status?: string) {
  if (status === '3') return 'dr-card__dot--done'
  if (status === '1') return 'dr-card__dot--pending'
  if (status === '2') return 'dr-card__dot--confirm'
  if (status === '4') return 'dr-card__dot--expired'
  return 'dr-card__dot--closed'
}

function openDetail(item: DepositOrder) {
  router.push({ name: 'depositDetail', query: { id: item.id } })
}

function selectAssetCode(code: string) {
  filterAssetCode.value = code
  refreshData()
}

function onDateConfirm(payload: { start: string; end: string; quickLabel?: string }) {
  dateStart.value = payload.start
  dateEnd.value = payload.end
  dateQuickLabel.value = payload.quickLabel || ''
  refreshData()
}
</script>

<template>
  <view class="dr-page">
    <AppNavBar :title="t('depositRecord.title')" />
    <view class="dr-page__glow" />
    <view class="dr-page__glow dr-page__glow--gold" />

    <!-- 筛选栏 -->
    <view class="dr-filter">
      <view class="dr-filter__item" @click="showCoinFilter = true">
        <wd-icon name="qrcode" size="24rpx" :color="goldIcon" />
        <text class="dr-filter__label">{{ filterCoinLabel }}</text>
        <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
      </view>
      <view class="dr-filter__item" @click="showDateFilter = true">
        <wd-icon name="clock-circle" size="24rpx" :color="goldIcon" />
        <text class="dr-filter__label">{{ filterDateLabel }}</text>
        <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
      </view>
    </view>

    <view v-if="noData" class="dr-empty">
      <view class="dr-empty__icon">
        <wd-icon name="download" size="56rpx" :color="goldIcon" />
      </view>
      <text class="dr-empty__title">{{ t('depositRecord.emptyTitle') }}</text>
      <view class="dr-empty__btn" @click="router.push({ name: 'deposit' })">
        <text>{{ t('depositRecord.emptyBtn') }}</text>
      </view>
    </view>

    <view v-else class="dr-list">
      <view
        v-for="item in records"
        :key="item.id"
        class="dr-card"
        @click="openDetail(item)"
      >
        <view class="dr-card__top">
          <text class="dr-card__symbol">{{ item.assetCode }}</text>
          <view class="dr-card__status-wrap">
            <view class="dr-card__dot" :class="statusDot(item.status)" />
            <text class="dr-card__status" :class="statusClass(item.status)">{{ dictItemText('deposit', item.status, item.statusName) }}</text>
            <wd-icon name="right" size="28rpx" color="#5a5650" class="dr-card__arrow" />
          </view>
        </view>

        <view class="dr-card__body">
          <view class="dr-card__col">
            <text class="dr-card__col-label">{{ t('common.amount') }}</text>
            <text class="dr-card__col-value">{{ item.amount }} {{ item.assetCode }}</text>
          </view>
          <view class="dr-card__col">
            <text class="dr-card__col-label">{{ t('depositRecord.colNetwork') }}</text>
            <text class="dr-card__col-value">{{ item.chainType }}</text>
          </view>
          <view class="dr-card__col">
            <text class="dr-card__col-label">{{ t('common.time') }}</text>
            <text class="dr-card__col-value">{{ item.createTimeShort }}</text>
          </view>
        </view>
      </view>

      <view class="dr-footer">
        <text v-if="loadingState === 'loading'">{{ t('common.loading') }}</text>
        <text v-else-if="loadingState === 'nomore'">{{ t('common.noMore') }}</text>
      </view>
    </view>

    <!-- 币种选择弹窗 -->
    <CurrencySelectPopup
      v-model="showCoinFilter"
      :current="filterAssetCode"
      :root-portal="false"
      @select="selectAssetCode"
    />

    <!-- 时间范围筛选弹窗 -->
    <DateRangeFilterPopup
      v-model="showDateFilter"
      :start="dateStart"
      :end="dateEnd"
      :default-days="30"
      :root-portal="false"
      @confirm="onDateConfirm"
    />
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.dr-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding: 0 0 48rpx;
}

.dr-page__glow {
  @include page-top-glow;
  height: 280rpx;

  &--gold {
    height: 360rpx;
    background: radial-gradient(ellipse 80% 50% at 50% -8%, rgba(201, 180, 138, 0.08) 0%, transparent 70%);
  }
}

.dr-empty {
  position: relative;
  z-index: 1;
  @include list-empty;
  padding: 160rpx 28rpx 0;

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.16);
    margin-bottom: 8rpx;
  }

  &__title {
    font-size: 28rpx;
    color: $text-muted;
  }

  &__btn {
    margin-top: 12rpx;
    padding: 0 56rpx;
    height: 76rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12rpx;
    background: linear-gradient(135deg, #d1c4a8 0%, #b8a078 100%);
    border: 1rpx solid rgba(209, 196, 168, 0.24);
    font-size: 28rpx;
    color: $page-bg;
    letter-spacing: 0.08em;
  }
}

.dr-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 0 28rpx;
}

.dr-card {
  @include dark-card;
  padding: 28rpx;
  border-radius: 20rpx;

  &:active {
    border-color: rgba(201, 180, 138, 0.16);
  }

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 18rpx;
  }

  &__symbol {
    font-size: 30rpx;
    font-weight: 600;
    color: $text-highlight;
    letter-spacing: 0.04em;
  }

  &__status-wrap {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;

    &--pending { background: #e8b86d; }
    &--confirm { background: #7eb8da; }
    &--done { background: #8bc49a; }
    &--expired { background: #c97a7a; }
    &--closed { background: #5a5650; }
  }

  &__status {
    font-size: 24rpx;

    &--pending { color: #e8b86d; }
    &--confirm { color: #7eb8da; }
    &--done { color: #8bc49a; }
    &--expired { color: #c97a7a; }
    &--closed { color: #7a7670; }
  }

  &__arrow {
    margin-left: 4rpx;
  }

  &__body {
    display: flex;
    gap: 16rpx;
    padding-top: 18rpx;
    border-top: 1rpx solid $border-subtle;
  }

  &__col {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    min-width: 0;

    &:last-child {
      align-items: flex-end;
      text-align: right;
    }
  }

  &__col-label {
    font-size: 20rpx;
    letter-spacing: 0.06em;
    color: $text-muted;
  }

  &__col-value {
    font-size: 26rpx;
    color: $text-primary;
    @include amount-text;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.dr-footer {
  padding: 32rpx 28rpx 16rpx;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}

.dr-filter {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 16rpx;
  margin: 20rpx 28rpx 8rpx;

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
</style>
