<script setup lang="ts">
import type { RebateRecordItem } from '@/api/types'
import { rebateRecordApi, dictApi } from '@/api/modules'
import { apiData } from '@/api/http'
import CurrencySelectPopup from '@/components/CurrencySelectPopup.vue'
import DateRangeFilterPopup from '@/components/DateRangeFilterPopup.vue'
import DictSelectPopup from '@/components/DictSelectPopup.vue'
import { lastNDays } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'rebateRecord',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()

const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()



const goldIcon = '#C9B48A'
const mutedIcon = '#9A9690'

const records = ref<RebateRecordItem[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')
const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const filterAssetCode = ref('')
const dateStart = ref('')
const dateEnd = ref('')
const dateQuickLabel = ref('')
const filterBizType = ref('')
const showCoinFilter = ref(false)
const showDateFilter = ref(false)
const showBizFilter = ref(false)
/** 字典原始项：name 为后端中文，仅作本地化未命中时的兜底 */
const bizTypeItems = ref<{ code: string; name: string }[]>([])
const bizTypeOptions = computed(() => bizTypeItems.value.map(it => ({
  code: it.code,
  name: dictItemText('rebateBizType', it.code, it.name),
})))

/** 默认近30天 */
const last30 = lastNDays(30)
dateStart.value = last30.start
dateEnd.value = last30.end

const filterCoinLabel = computed(() => (filterAssetCode.value ? filterAssetCode.value : t('common.allCoin')))
const filterDateLabel = computed(() => {
  if (dateQuickLabel.value) return dateQuickLabel.value
  if (dateStart.value && dateEnd.value) {
    const def = lastNDays(30)
    if (dateStart.value === def.start && dateEnd.value === def.end) return t('dateRange.last30')
    return `${dateStart.value} / ${dateEnd.value}`
  }
  if (dateStart.value) return `${dateStart.value} /`
  if (dateEnd.value) return `/ ${dateEnd.value}`
  return t('common.allTime')
})
const filterBizLabel = computed(() => {
  if (!filterBizType.value) return t('common.allType')
  return dictItemText('rebateBizType', filterBizType.value, filterBizType.value)
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
  loadingState.value = 'loading'
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value,
      pageSize: 15,
    }
    if (filterAssetCode.value) params.assetCode = filterAssetCode.value
    if (filterBizType.value) params.bizType = filterBizType.value
    if (dateStart.value) params.timeStart = `${dateStart.value} 00:00:00`
    if (dateEnd.value) params.timeEnd = `${dateEnd.value} 23:59:59`
    const page = await apiData(rebateRecordApi.findByPage(params))
    const list = page?.content || []
    if (pageNum.value === 1) {
      records.value = list
    }
    else {
      records.value = records.value.concat(list)
    }
    loadingState.value = records.value.length >= (page?.total || 0) ? 'nomore' : 'loadmore'
  }
  catch {
    if (pageNum.value === 1) records.value = []
    loadingState.value = 'nomore'
  }
  finally {
    if (fromPull) uni.stopPullDownRefresh()
  }
}

function sourceText(item: RebateRecordItem) {
  return item.sourceMemberUid != null ? String(item.sourceMemberUid) : '—'
}

function selectAssetCode(code: string) {
  filterAssetCode.value = code
  refreshData()
}

function selectBizType(code: string) {
  filterBizType.value = code
  refreshData()
}

function onDateConfirm(payload: { start: string; end: string; quickLabel?: string }) {
  dateStart.value = payload.start
  dateEnd.value = payload.end
  dateQuickLabel.value = payload.quickLabel || ''
  refreshData()
}

function goInvite() {
  uni.navigateBack({
    fail: () => router.push({ name: 'inviteRebate' }),
  })
}

onMounted(async () => {
  const items = (await apiData(dictApi.findDictItemInCache('rebateBizType'))) || []
  bizTypeItems.value = (items as Array<{ dictItemCode: string; dictItemName: string }>).map(it => ({
    code: it.dictItemCode,
    name: it.dictItemName,
  }))
})
</script>

<template>
  <view class="list-page">
    <AppNavBar :title="t('rebateRecord.title')" />
    <view class="list-page__glow" />
    <view class="list-page__glow list-page__glow--gold" />

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-bar__item" @click="showCoinFilter = true">
        <wd-icon name="qrcode" size="24rpx" :color="goldIcon" />
        <text class="filter-bar__label">{{ filterCoinLabel }}</text>
        <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
      </view>
      <view class="filter-bar__item" @click="showBizFilter = true">
        <wd-icon name="apps" size="24rpx" :color="goldIcon" />
        <text class="filter-bar__label">{{ filterBizLabel }}</text>
        <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
      </view>
      <view class="filter-bar__item" @click="showDateFilter = true">
        <wd-icon name="clock-circle" size="24rpx" :color="goldIcon" />
        <text class="filter-bar__label">{{ filterDateLabel }}</text>
        <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
      </view>
    </view>

    <view class="list-page__body">
      <view v-if="loadingState === 'loading' && records.length === 0" class="lr-skeleton">
        <view v-for="i in 3" :key="i" class="lr-skeleton__item" />
      </view>

      <view v-else-if="noData" class="list-page__empty">
        <wd-icon name="history" size="72rpx" color="#4A4846" />
        <text>{{ t('rebateRecord.empty') }}</text>
        <view class="list-page__empty-btn" @click="goInvite">
          <text>{{ t('rebateCommon.goInvite') }}</text>
        </view>
      </view>

      <view
        v-for="item in records"
        :key="item.id"
        class="record-card"
      >
        <view class="record-card__top">
          <text class="record-card__symbol">{{ item.assetCode }}</text>
          <text class="record-card__amount">+{{ item.amount }}</text>
        </view>

        <view class="record-card__body">
          <view class="record-card__col">
            <text class="record-card__col-label">{{ t('rebateRecord.sourceMember') }}</text>
            <text class="record-card__col-value">{{ sourceText(item) }}</text>
          </view>
          <view class="record-card__col">
            <text class="record-card__col-label">{{ t('rebateRecord.bizType') }}</text>
            <text class="record-card__col-value">{{ dictItemText('rebateBizType', item.bizType, item.bizTypeName || item.bizType) || '—' }}</text>
          </view>
          <view class="record-card__col">
            <text class="record-card__col-label">{{ t('common.time') }}</text>
            <text class="record-card__col-value">{{ item.createTimeShort }}</text>
          </view>
        </view>
      </view>

      <view
        v-if="records.length > 0"
        class="list-page__more"
        @click="nextPage"
      >
        {{ loadingState === 'nomore' ? `— ${t('common.noMore')} —` : loadingState === 'loading' ? t('common.loading') : t('common.loadMore') }}
      </view>
    </view>

    <!-- 币种选择弹窗 -->
    <CurrencySelectPopup v-model="showCoinFilter" :current="filterAssetCode" @select="selectAssetCode" />

    <!-- 时间范围筛选弹窗 -->
    <DateRangeFilterPopup v-model="showDateFilter" :start="dateStart" :end="dateEnd" :default-days="30" @confirm="onDateConfirm" />

    <!-- 业务类型筛选弹窗 -->
    <DictSelectPopup v-model="showBizFilter" :current="filterBizType" :options="bizTypeOptions" :title="t('rebateRecord.bizType')" @select="selectBizType" />
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.list-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding: 0 0 48rpx;
}

.list-page__glow {
  @include page-top-glow;
  height: 280rpx;

  &--gold {
    height: 360rpx;
    background: radial-gradient(ellipse 80% 50% at 50% -8%, rgba(201, 180, 138, 0.08) 0%, transparent 70%);
  }
}

.filter-bar {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 16rpx;
  margin: 20rpx 28rpx 8rpx;

  &__item {
    flex: 1;
    min-width: 0;
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

.list-page__body {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx 0;
}

.list-page__more {
  padding: 28rpx 28rpx 16rpx;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
  letter-spacing: 0.04em;
}

.lr-skeleton__item {
  height: 196rpx;
  margin-bottom: 16rpx;
  border-radius: 20rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: lr-shimmer 1.4s ease-in-out infinite;
}

@keyframes lr-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.list-page__empty {
  @include list-empty;
  padding: 120rpx 28rpx 0;

  &-btn {
    margin-top: 16rpx;
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

.record-card {
  @include record-card;
  padding: 28rpx;
  border-radius: 20rpx;
  margin-bottom: 16rpx;
}

.record-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 18rpx;
}

.record-card__symbol {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-highlight;
  letter-spacing: 0.04em;
}

.record-card__amount {
  flex-shrink: 0;
  font-size: 32rpx;
  font-weight: 600;
  color: #8ab88a;
  @include amount-text;
}

.record-card__body {
  display: flex;
  gap: 16rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid $border-subtle;
}

.record-card__col {
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

.record-card__col-label {
  font-size: 20rpx;
  letter-spacing: 0.06em;
  color: $text-muted;
}

.record-card__col-value {
  font-size: 26rpx;
  color: $text-primary;
  @include amount-text;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
