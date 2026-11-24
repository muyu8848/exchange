<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()
import type { MyAssetChangeLog } from '@/api/types'
import { balanceLogApi, dictApi } from '@/api/modules'
import { apiData } from '@/api/http'
import CurrencySelectPopup from '@/components/CurrencySelectPopup.vue'
import DateRangeFilterPopup from '@/components/DateRangeFilterPopup.vue'
import AssetChangeLogCard from '@/components/asset/AssetChangeLogCard.vue'
import { lastNDays } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'assetChangeLog',
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

/** 弹窗内联背景：root-portal 在 App 端会把弹窗移出页面主题作用域，仅靠 CSS 变量取色会回落到夜间值 */
const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)



const goldIcon = '#C9B48A'
const silverIcon = '#C4C6CA'
const mutedIcon = '#9A9690'

const records = ref<MyAssetChangeLog[]>([])
const pageNum = ref(1)
const changeType = ref('')
const assetCode = ref('')
/** 字典原始项：name 为后端中文，仅作本地化未命中时的兜底 */
const changeTypeItems = ref<{ code: string, name: string }[]>([])
const changeTypeOptions = computed(() => changeTypeItems.value.map(item => ({
  code: item.code,
  name: dictItemText('memberAssetChangeType', item.code, item.name),
})))
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')

const showCoinFilter = ref(false)
const showDateFilter = ref(false)
const dateStart = ref('')
const dateEnd = ref('')
const dateQuickLabel = ref('')

/** 默认近30天 */
const last30 = lastNDays(30)
dateStart.value = last30.start
dateEnd.value = last30.end

const accountTypeOptions = computed(() => [
  { value: '', label: t('assetChangeLog.allAccounts') },
  { value: 'SPOT', label: dictItemText('accountType', 'SPOT') },
  { value: 'FUTURES', label: dictItemText('accountType', 'FUTURES') },
  { value: 'FIAT', label: dictItemText('accountType', 'FIAT') },
])
const accountType = ref('')
const showAccountTypeFilter = ref(false)

const filterAccountTypeLabel = computed(
  () => accountTypeOptions.value.find(o => o.value === accountType.value)?.label || t('assetChangeLog.allAccounts'),
)

const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const filterCoinLabel = computed(() => (assetCode.value ? assetCode.value : t('common.allCoin')))
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

onLoad((query) => {
  if (query?.changeType) {
    changeType.value = String(query.changeType)
  }
  if (query?.assetCode) {
    assetCode.value = String(query.assetCode)
  }
})

onShow(() => {
  loadChangeTypes()
  refreshData()
})

function queryParams() {
  const params: Record<string, unknown> = {}
  if (changeType.value) {
    params.changeType = changeType.value
  }
  if (assetCode.value) {
    params.assetCode = assetCode.value
  }
  if (accountType.value) {
    params.accountType = accountType.value
  }
  if (dateStart.value) {
    params.timeStart = `${dateStart.value} 00:00:00`
  }
  if (dateEnd.value) {
    params.timeEnd = `${dateEnd.value} 23:59:59`
  }
  return params
}



async function loadChangeTypes() {
  if (changeTypeItems.value.length) {
    return
  }
  try {
    const items = await apiData(dictApi.findDictItemInCache('memberAssetChangeType')) || []
    changeTypeItems.value = items.map(item => ({
      code: item.dictItemCode,
      name: item.dictItemName,
    }))
  }
  catch {
    changeTypeItems.value = []
  }
}

onReachBottom(() => {
  nextPage()
})

onPullDownRefresh(() => {
  refreshData(true)
})

function selectChangeType(code: string) {
  const next = changeType.value === code ? '' : code
  if (changeType.value === next) {
    return
  }
  changeType.value = next
  refreshData()
}

function selectAssetCode(code: string) {
  assetCode.value = code
  refreshData()
}

function applyAccountType(value: string) {
  accountType.value = value
  showAccountTypeFilter.value = false
  refreshData()
}

function onDateConfirm(payload: { start: string; end: string; quickLabel?: string }) {
  dateStart.value = payload.start
  dateEnd.value = payload.end
  dateQuickLabel.value = payload.quickLabel || ''
  refreshData()
}

function refreshData(fromPull = false) {
  pageNum.value = 1
  records.value = []
  loadingState.value = 'loading'
  loadPage(fromPull)
}

function nextPage() {
  if (loadingState.value === 'nomore' || loadingState.value === 'loading') {
    return
  }
  pageNum.value += 1
  loadPage()
}

async function loadPage(fromPull = false) {
  loadingState.value = 'loading'
  try {
    const page = await apiData(balanceLogApi.findByPage({
      ...queryParams(),
      pageNum: pageNum.value,
      pageSize: 10,
    }))
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

function openDetail(record: MyAssetChangeLog) {
  router.push({ name: 'assetChangeLogDetail', query: { id: record.id } })
}
</script>

<template>
  <view class="wcl-page">
    <AppNavBar :title="t('assetChangeLog.title')" />
    <view class="wcl-page__glow" />
    <view class="wcl-page__glow wcl-page__glow--gold" />

    <view class="wcl-body">
      <view class="wcl-filter">
        <view class="wcl-filter__item" @click="showAccountTypeFilter = true">
          <wd-icon name="safe" size="24rpx" :color="goldIcon" />
          <text class="wcl-filter__label">{{ filterAccountTypeLabel }}</text>
          <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
        </view>
        <view class="wcl-filter__item" @click="showCoinFilter = true">
          <wd-icon name="qrcode" size="24rpx" :color="goldIcon" />
          <text class="wcl-filter__label">{{ filterCoinLabel }}</text>
          <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
        </view>
        <view class="wcl-filter__item" @click="showDateFilter = true">
          <wd-icon name="clock-circle" size="24rpx" :color="goldIcon" />
          <text class="wcl-filter__label">{{ filterDateLabel }}</text>
          <wd-icon name="caret-down" size="20rpx" :color="mutedIcon" />
        </view>
      </view>

      <scroll-view v-if="changeTypeOptions.length" scroll-x class="wcl-filters" :show-scrollbar="false">
        <view class="wcl-filters__inner">
          <view
            class="wcl-filters__chip"
            :class="{ 'wcl-filters__chip--active': !changeType }"
            @click="selectChangeType('')"
          >
            {{ t('common.allType') }}
          </view>
          <view
            v-for="item in changeTypeOptions"
            :key="item.code"
            class="wcl-filters__chip"
            :class="{ 'wcl-filters__chip--active': changeType === item.code }"
            @click="selectChangeType(item.code)"
          >
            {{ item.name }}
          </view>
        </view>
      </scroll-view>

      <view class="wcl-list">
        <view v-if="loadingState === 'loading' && records.length === 0" class="wcl-list__loading">
          <text>{{ t('common.loading') }}</text>
        </view>

        <view v-else-if="noData" class="wcl-list__empty">
          <view class="wcl-list__empty-icon">
            <wd-icon name="history" size="56rpx" :color="goldIcon" />
          </view>
          <text class="wcl-list__empty-title">{{ t('assetChangeLog.empty') }}</text>
        </view>

        <AssetChangeLogCard
          v-for="record in records"
          :key="record.id"
          :record="record"
          @click="openDetail"
        />

        <view
          v-if="records.length > 0"
          class="wcl-list__more"
          @click="nextPage"
        >
          {{ loadingState === 'nomore' ? t('common.noMore') : loadingState === 'loading' ? t('common.loading') : t('common.loadMore') }}
        </view>
      </view>

      <CurrencySelectPopup v-model="showCoinFilter" :current="assetCode" @select="selectAssetCode" />
      <DateRangeFilterPopup v-model="showDateFilter" :start="dateStart" :end="dateEnd" :default-days="30" @confirm="onDateConfirm" />

      <!-- 账户类型筛选弹层 -->
      <wd-popup
        :model-value="showAccountTypeFilter"
        position="bottom"
        root-portal
        :z-index="100"
        modal-style="background: rgba(0, 0, 0, 0.6)"
        :custom-style="popupStyle"
        :custom-class="`wcl-account-popup theme-${theme}`"
        @close="showAccountTypeFilter = false"
      >
        <view class="popup-panel">
          <text class="popup-panel__title">{{ t('assetChangeLog.selectAccountType') }}</text>
          <view class="wcl-account-list">
            <view
              v-for="opt in accountTypeOptions"
              :key="opt.value"
              class="wcl-account-item"
              :class="{ 'wcl-account-item--active': accountType === opt.value }"
              @click="applyAccountType(opt.value)"
            >
              <text class="wcl-account-item__label">{{ opt.label }}</text>
              <wd-icon
                v-if="accountType === opt.value"
                name="check"
                size="32rpx"
                :color="goldIcon"
              />
            </view>
          </view>
        </view>
      </wd-popup>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.wcl-page {
  @include page-shell;
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.wcl-page__glow {
  @include page-top-glow;
  height: 300rpx;

  &--gold {
    height: 400rpx;
    background: radial-gradient(ellipse 90% 60% at 50% -10%, rgba(201, 180, 138, 0.08) 0%, transparent 72%);
  }
}

.wcl-body {
  position: relative;
  z-index: 1;
  padding: 8rpx 28rpx 48rpx;
}

.wcl-filter {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;

  &__item {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 14rpx 16rpx;
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

.wcl-filters {
  width: 100%;
  margin-top: 16rpx;
  white-space: nowrap;

  &__inner {
    display: inline-flex;
    gap: 12rpx;
    padding: 4rpx 2rpx 8rpx;
  }

  &__chip {
    @include chip-item;
    flex-shrink: 0;
    padding: 12rpx 22rpx;
    font-size: 22rpx;
    white-space: nowrap;
  }
}

.wcl-list {
  margin-top: 20rpx;

  &__loading,
  &__more {
    padding: 32rpx 0 16rpx;
    text-align: center;
    font-size: 24rpx;
    color: $text-muted;
    letter-spacing: 0.04em;
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 160rpx 24rpx;
    text-align: center;
  }

  &__empty-icon {
    width: 96rpx;
    height: 96rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24rpx;
    border-radius: 50%;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.16);
  }

  &__empty-title {
    font-size: 28rpx;
    color: $text-muted;
  }
}

/* ===================== 账户类型筛选弹层 ===================== */
.wcl-account-popup {
  border-radius: 24rpx 24rpx 0 0;
  background: $surface-2;
}

.popup-panel {
  padding: 32rpx 28rpx;
  max-height: 70vh;
  overflow-y: auto;

  &__title {
    display: block;
    font-size: 30rpx;
    font-weight: 500;
    color: $text-highlight;
    text-align: center;
    margin-bottom: 24rpx;
  }
}

.wcl-account-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.wcl-account-item {
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
