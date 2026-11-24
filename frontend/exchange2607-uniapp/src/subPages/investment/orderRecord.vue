<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()
import type { MyInvestmentRecord, MyInterestRecord, MyRedeemRecord } from '@/api/types'
import { investmentApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { currencyLogo } from '@/utils/currency'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import AppTabs from '@/components/AppTabs.vue'

definePage({
  name: 'investmentOrderRecord',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

type TabKey = 'invest' | 'redeem' | 'interest'

const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()



const activeTab = ref<TabKey>('invest')
const tabs = computed<{ key: TabKey; label: string }[]>(() => [
  { key: 'invest', label: t('investmentOrderRecord.tabInvest') },
  { key: 'redeem', label: t('investmentCommon.redeem') },
  { key: 'interest', label: t('investmentCommon.interest') },
])

// 列表数据
const records = ref<(MyInvestmentRecord | MyInterestRecord | MyRedeemRecord)[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')
const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

// 筛选弹窗
const filterVisible = ref(false)
const productTypeFilter = ref<'' | 'FLEXIBLE' | 'FIXED'>('')
const dateStart = ref('')
const dateEnd = ref('')

// 临时筛选值（确认后才同步）
const tempProductType = ref<'' | 'FLEXIBLE' | 'FIXED'>('')
const tempDateStart = ref('')
const tempDateEnd = ref('')

type ProductTypeValue = '' | 'FLEXIBLE' | 'FIXED'

const productTypeOptions = computed<{ label: string; value: ProductTypeValue }[]>(() => [
  { label: t('common.all'), value: '' },
  { label: dictItemText('investmentProductType', 'FLEXIBLE'), value: 'FLEXIBLE' },
  { label: dictItemText('investmentProductType', 'FIXED'), value: 'FIXED' },
])

const hasActiveFilter = computed(() => productTypeFilter.value !== '' || dateStart.value !== '' || dateEnd.value !== '')

onShow(() => {
  refreshData()
})

onReachBottom(() => {
  nextPage()
})

onPullDownRefresh(() => {
  refreshData(true)
})

function switchTab(value: string | number) {
  const next = value as TabKey
  if (activeTab.value === next) return
  activeTab.value = next
  refreshData()
}

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

    // 产品类型筛选
    if (productTypeFilter.value) {
      params.productType = productTypeFilter.value
    }

    // 时间筛选
    if (dateStart.value) {
      if (activeTab.value === 'interest') {
        params.interestDateStart = dateStart.value
      } else if (activeTab.value === 'invest') {
        params.createTimeStart = `${dateStart.value} 00:00:00`
      } else {
        params.createTimeStart = `${dateStart.value} 00:00:00`
      }
    }
    if (dateEnd.value) {
      if (activeTab.value === 'interest') {
        params.interestDateEnd = dateEnd.value
      } else if (activeTab.value === 'invest') {
        params.createTimeEnd = `${dateEnd.value} 23:59:59`
      } else {
        params.createTimeEnd = `${dateEnd.value} 23:59:59`
      }
    }

    let list: (MyInvestmentRecord | MyInterestRecord | MyRedeemRecord)[] = []
    let total = 0

    if (activeTab.value === 'interest') {
      const page = await apiData(investmentApi.findMyInterestRecordByPage(params))
      list = page?.content || []
      total = page?.total || 0
    } else if (activeTab.value === 'redeem') {
      const page = await apiData(investmentApi.findMyRedeemRecordByPage(params))
      list = page?.content || []
      total = page?.total || 0
    } else {
      const page = await apiData(investmentApi.findMyInvestmentRecordByPage(params))
      list = page?.content || []
      total = page?.total || 0
    }

    if (pageNum.value === 1) {
      records.value = list
    } else {
      records.value = records.value.concat(list)
    }

    loadingState.value = records.value.length >= total || list.length < 15 ? 'nomore' : 'loadmore'
  } catch {
    if (pageNum.value === 1) records.value = []
    loadingState.value = 'nomore'
  } finally {
    if (fromPull) uni.stopPullDownRefresh()
  }
}

// 筛选弹窗操作
function openFilter() {
  tempProductType.value = productTypeFilter.value
  tempDateStart.value = dateStart.value
  tempDateEnd.value = dateEnd.value
  filterVisible.value = true
}

function selectProductType(value: '' | 'FLEXIBLE' | 'FIXED') {
  tempProductType.value = value
}

function onStartDateChange(e: { detail: { value: string } }) {
  tempDateStart.value = e.detail.value
}

function onEndDateChange(e: { detail: { value: string } }) {
  tempDateEnd.value = e.detail.value
}

function confirmFilter() {
  productTypeFilter.value = tempProductType.value
  dateStart.value = tempDateStart.value
  dateEnd.value = tempDateEnd.value
  filterVisible.value = false
  refreshData()
}

function resetFilter() {
  tempProductType.value = ''
  tempDateStart.value = ''
  tempDateEnd.value = ''
}

// ===== 展示辅助 =====

function getAssetCode(item: MyInvestmentRecord | MyInterestRecord | MyRedeemRecord): string {
  return (item as MyInvestmentRecord).assetCode || (item as MyInterestRecord).assetCode || (item as MyRedeemRecord).assetCode || '—'
}

function getProductTypeName(item: MyInvestmentRecord | MyInterestRecord | MyRedeemRecord): string {
  return dictItemText('investmentProductType', item.productType, item.productTypeName || '—')
}

function getRedeemTypeName(item: MyRedeemRecord): string {
  return dictItemText('investmentRedeemType', item.redeemType, item.redeemTypeName || '—')
}

function getInterestTypeName(item: MyInterestRecord): string {
  return dictItemText('investmentInterestType', item.interestType, item.interestTypeName || '—')
}

function formatNum(val: string | number | undefined | null) {
  if (val == null) return '0'
  return String(val)
}

</script>

<template>
  <view class="ior-page">
    <AppNavBar :title="t('investmentOrderRecord.title')" />
    <view class="ior-glow" />

    <!-- Tab + 筛选 -->
    <view class="ior-tabbar">
      <AppTabs
        variant="capsule" align="start" item-padding="14rpx 32rpx"
        :model-value="activeTab" :items="tabs" @update:model-value="switchTab"
      />
      <view class="ior-tabbar__filter" @click="openFilter">
        <wd-icon name="filter" size="32rpx" :color="hasActiveFilter ? '#C9B48A' : '#9A9690'" />
        <view v-if="hasActiveFilter" class="ior-tabbar__filter-dot" />
      </view>
    </view>

    <!-- 列表 -->
    <view v-if="noData" class="ior-empty">
      <wd-icon name="list" size="64rpx" color="#C9B48A" />
      <text class="ior-empty__title">{{ t('investmentCommon.empty') }}</text>
    </view>

    <view v-else class="ior-list">
      <view
        v-for="item in records"
        :key="item.id"
        class="ior-item"
      >
        <!-- 头部：币种 logo + 币种 + 产品类型标签 + 已完成状态 -->
        <view class="ior-item__head">
          <view class="ior-item__head-left">
            <image
              v-if="currencyLogo(getAssetCode(item))"
              :src="currencyLogo(getAssetCode(item))"
              class="ior-item__logo"
              mode="aspectFit"
            />
            <text v-else class="ior-item__symbol-fallback">{{ getAssetCode(item).charAt(0) }}</text>
            <text class="ior-item__symbol">{{ getAssetCode(item) }}</text>
            <view class="ior-item__tag">
              <text>{{ getProductTypeName(item) }}</text>
            </view>
          </view>
          <view class="ior-item__head-right">
            <text class="ior-item__status">{{ t('investmentOrderRecord.statusCompleted') }}</text>
          </view>
        </view>

        <!-- 一行三列 -->
        <view class="ior-item__body">
          <!-- 投资 tab -->
          <template v-if="activeTab === 'invest'">
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('common.time') }}</text>
              <text class="ior-item__col-value ior-item__col-value--sm">{{ (item as MyInvestmentRecord).createTime || '—' }}</text>
            </view>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('investmentOrderRecord.colType') }}</text>
              <text class="ior-item__col-value">{{ t('investmentOrderRecord.typeManual') }}</text>
            </view>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('investmentOrderRecord.colAmount') }}</text>
              <text class="ior-item__col-value">{{ formatNum((item as MyInvestmentRecord).principal) }}</text>
            </view>
          </template>

          <!-- 赎回 tab -->
          <template v-else-if="activeTab === 'redeem'">
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('common.time') }}</text>
              <text class="ior-item__col-value ior-item__col-value--sm">{{ (item as MyRedeemRecord).createTime || '—' }}</text>
            </view>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('investmentOrderRecord.colType') }}</text>
              <text class="ior-item__col-value">{{ getRedeemTypeName(item as MyRedeemRecord) }}</text>
            </view>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('investmentOrderRecord.colAmount') }}</text>
              <text class="ior-item__col-value">{{ formatNum((item as MyRedeemRecord).redeemAmount) }}</text>
            </view>
          </template>

          <!-- 收益 tab -->
          <template v-else>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('common.time') }}</text>
              <text class="ior-item__col-value ior-item__col-value--sm">{{ (item as MyInterestRecord).createTime || '—' }}</text>
            </view>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('investmentOrderRecord.colType') }}</text>
              <text class="ior-item__col-value">{{ getInterestTypeName(item as MyInterestRecord) }}</text>
            </view>
            <view class="ior-item__col">
              <text class="ior-item__col-label">{{ t('investmentCommon.interest') }}</text>
              <text class="ior-item__col-value">{{ formatNum((item as MyInterestRecord).interestAmount) }}</text>
            </view>
          </template>
        </view>
      </view>
    </view>

    <view v-if="loadingState === 'loading'" class="ior-footer">
      <text>{{ t('common.loading') }}</text>
    </view>
    <view v-else-if="loadingState === 'loadmore'" class="ior-footer">
      <text>{{ t('common.loadMore') }}</text>
    </view>
    <view v-else-if="loadingState === 'nomore' && records.length > 0" class="ior-footer">
      <text>{{ t('common.noMore') }}</text>
    </view>

    <!-- 筛选弹窗 -->
    <wd-popup
      v-model="filterVisible"
      position="bottom"
      closable
      :root-portal="false"
      :custom-class="`ior-filter-popup theme-${theme}`"
      :z-index="100"
      modal-style="background: rgba(0, 0, 0, 0.6)"
    >
      <view class="ior-filter" @click.stop>
        <text class="ior-filter__title">{{ t('investmentOrderRecord.filterTitle') }}</text>

        <!-- 时间 -->
        <view class="ior-filter__section">
          <text class="ior-filter__label">{{ t('common.time') }}</text>
          <view class="ior-filter__date-row">
            <picker class="ior-filter__date-picker" mode="date" :value="tempDateStart" @change="onStartDateChange">
              <view class="ior-filter__date-box">
                <text :class="{ 'ior-filter__date-placeholder': !tempDateStart }">
                  {{ tempDateStart || t('investmentOrderRecord.startDate') }}
                </text>
              </view>
            </picker>
            <text class="ior-filter__date-sep">{{ t('investmentOrderRecord.dateTo') }}</text>
            <picker class="ior-filter__date-picker" mode="date" :value="tempDateEnd" @change="onEndDateChange">
              <view class="ior-filter__date-box">
                <text :class="{ 'ior-filter__date-placeholder': !tempDateEnd }">
                  {{ tempDateEnd || t('investmentOrderRecord.endDate') }}
                </text>
              </view>
            </picker>
          </view>
        </view>

        <!-- 产品类型 -->
        <view class="ior-filter__section">
          <text class="ior-filter__label">{{ t('investmentCommon.productType') }}</text>
          <view class="ior-filter__pills">
            <view
              v-for="opt in productTypeOptions"
              :key="opt.value"
              class="ior-filter__pill"
              :class="{ 'ior-filter__pill--active': tempProductType === opt.value }"
              @click="selectProductType(opt.value)"
            >
              {{ opt.label }}
            </view>
          </view>
        </view>

        <!-- 按钮 -->
        <view class="ior-filter__btns">
          <view class="ior-filter__btn ior-filter__btn--outline" @click="resetFilter">
            {{ t('investmentOrderRecord.reset') }}
          </view>
          <view class="ior-filter__btn ior-filter__btn--gold" @click="confirmFilter">
            {{ t('investmentOrderRecord.confirm') }}
          </view>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.ior-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding: 0 0 48rpx;
}

.ior-glow {
  @include page-top-glow;
  height: 280rpx;
}

/* Tab + 筛选 */
.ior-tabbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 16rpx 28rpx;
}

.ior-tabbar__filter {
  position: relative;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
}

.ior-tabbar__filter-dot {
  position: absolute;
  top: 10rpx;
  right: 12rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: $gold;
}

/* 空状态 */
.ior-empty {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 28rpx;
  gap: 20rpx;
}

.ior-empty__title {
  font-size: 28rpx;
  color: $text-muted;
}

/* 列表 */
.ior-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 0 28rpx;
}

.ior-item {
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

  &__logo {
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 1rpx solid $border-subtle;
  }

  &__symbol-fallback {
    width: 36rpx;
    height: 36rpx;
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

  &__symbol {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__tag {
    padding: 2rpx 8rpx;
    border-radius: 4rpx;
    font-size: 18rpx;
    color: $gold-light;
    background: $gold-soft;
  }

  &__status {
    font-size: 24rpx;
    color: $success;
  }

  &__body {
    display: flex;
    gap: 16rpx;
  }

  &__col {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }

  &__col-label {
    @include label-caps;
    font-size: 18rpx;
  }

  &__col-value {
    font-size: 26rpx;
    color: $text-primary;
    @include amount-text;

    &--sm {
      font-size: 22rpx;
    }
  }
}

/* 底部加载 */
.ior-footer {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}

/* 筛选弹窗 */
.ior-filter {
  @include dark-popup-panel;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.ior-filter__title {
  font-size: 30rpx;
  font-weight: 600;
  text-align: center;
  color: $text-highlight;
}

.ior-filter__section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ior-filter__label {
  font-size: 24rpx;
  color: $text-secondary;
  letter-spacing: 0.04em;
}

.ior-filter__date-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.ior-filter__date-picker {
  flex: 1;
  min-width: 0;
}

.ior-filter__date-box {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10rpx;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  font-size: 24rpx;
  color: $text-primary;
}

.ior-filter__date-placeholder {
  color: $text-muted;
}

.ior-filter__date-sep {
  font-size: 24rpx;
  color: $text-muted;
}

.ior-filter__pills {
  display: flex;
  gap: 12rpx;
}

.ior-filter__pill {
  flex: 1;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10rpx;
  font-size: 24rpx;
  color: $text-muted;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  transition: all 0.2s ease;
}

.ior-filter__pill--active {
  color: $page-bg;
  background: linear-gradient(135deg, #d1c4a8 0%, #b8a078 100%);
  border-color: rgba(209, 196, 168, 0.24);
  font-weight: 600;
}

.ior-filter__btns {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.ior-filter__btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 26rpx;
}

.ior-filter__btn--outline {
  color: $silver;
  border: 1rpx solid rgba(196, 198, 202, 0.22);
  background: $silver-soft;
}

.ior-filter__btn--gold {
  color: $page-bg;
  background: linear-gradient(135deg, #d1c4a8 0%, #b8a078 100%);
  border: 1rpx solid rgba(209, 196, 168, 0.24);
  font-weight: 600;
}
</style>
