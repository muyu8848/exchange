<script setup lang="ts">
import type { ContractOrderItem } from '@/api/types'
import { contractApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import AppTabs from '@/components/AppTabs.vue'

definePage({
  name: 'secondContractOrderRecord',
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
const records = ref<ContractOrderItem[]>([])
const pageNum = ref(1)
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')
const activeTab = ref('')

const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const tabs = computed(() => [
  { key: '', label: t('common.all') },
  { key: '1', label: dictItemText('secondContractOrderStatus', '1') },
  { key: '2', label: dictItemText('secondContractOrderStatus', '2') },
])

onShow(() => {
  refreshData()
})

onReachBottom(() => {
  nextPage()
})

onPullDownRefresh(() => {
  refreshData(true)
})

function onTabChange(value: string | number) {
  activeTab.value = value as string
  refreshData()
}

function refreshData(fromPull = false) {
  pageNum.value = 1
  records.value = []
  loadingState.value = 'loading'
  loadPage(fromPull)
}

function nextPage() {
  if (loadingState.value !== 'loadmore') {
    return
  }
  pageNum.value += 1
  loadPage()
}

async function loadPage(fromPull = false) {
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value,
      pageSize: 15,
    }
    if (activeTab.value) {
      params.status = activeTab.value
    }
    const page = await apiData(contractApi.findMyOrderByPage(params))
    const list = page?.content || []
    if (pageNum.value === 1) {
      records.value = list
    }
    else {
      records.value = records.value.concat(list)
    }
    const total = page?.total || 0
    loadingState.value = records.value.length >= total ? 'nomore' : 'loadmore'
  }
  catch {
    if (pageNum.value === 1) {
      records.value = []
    }
    loadingState.value = 'nomore'
  }
  finally {
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

function directionClass(direction?: string) {
  if (direction === 'long') {
    return 'cor-item__dir--long'
  }
  return 'cor-item__dir--short'
}


function pnlClass(pnl?: number) {
  if (pnl == null) {
    return ''
  }
  if (pnl > 0) {
    return 'cor-item__pnl--win'
  }
  if (pnl < 0) {
    return 'cor-item__pnl--loss'
  }
  return ''
}

function openDetail(item: ContractOrderItem) {
  router.push({ name: 'secondContractOrderDetail', query: { id: item.id } })
}
</script>

<template>
  <view class="cor-page">
    <AppNavBar :title="t('secondContractOrderRecord.title')" />
    <view class="cor-page__glow" />

    <view class="cor-tabs-wrap">
      <AppTabs
        variant="capsule" :model-value="activeTab" :items="tabs"
        @update:model-value="onTabChange"
      />
    </view>

    <view v-if="noData" class="cor-empty">
      <wd-icon name="list" size="64rpx" :color="goldIcon" />
      <text class="cor-empty__title">{{ t('secondContractOrderRecord.empty') }}</text>
    </view>

    <view v-else class="cor-list">
      <view
        v-for="item in records"
        :key="item.id"
        class="cor-item"
        @click="openDetail(item)"
      >
        <view class="cor-item__head">
          <view class="cor-item__head-left">
            <view class="cor-item__dir" :class="directionClass(item.direction)">
              {{ dictItemText('secondContractDirection', item.direction, item.directionName) }}
            </view>
            <text class="cor-item__symbol">{{ item.symbolCode }}</text>
          </view>
          <view class="cor-item__head-right">
            <text class="cor-item__status">{{ dictItemText('secondContractOrderStatus', item.status, item.statusName) }}</text>
            <wd-icon name="right" size="28rpx" color="#5a5650" />
          </view>
        </view>
        <view class="cor-item__body">
          <view class="cor-item__col">
            <text class="cor-item__col-label">{{ t('secondContractCommon.openPrice') }}</text>
            <text class="cor-item__col-value">{{ item.openPrice }}</text>
          </view>
          <view class="cor-item__col">
            <text class="cor-item__col-label">{{ t('secondContractCommon.amount') }}</text>
            <text class="cor-item__col-value">{{ item.amount }} USDT</text>
          </view>
          <view class="cor-item__col">
            <text class="cor-item__col-label">{{ t('secondContractCommon.period') }}</text>
            <text class="cor-item__col-value">{{ item.period }}s</text>
          </view>
        </view>
        <view class="cor-item__foot">
          <view class="cor-item__foot-left">
            <text class="cor-item__time">{{ item.openTime }}</text>
            <text v-if="item.closePrice" class="cor-item__close">{{ t('secondContractCommon.closePrice') }} {{ item.closePrice }}</text>
          </view>
          <text
            v-if="item.pnl != null && item.status === '2'"
            class="cor-item__pnl"
            :class="pnlClass(item.pnl)"
          >
            {{ item.pnl > 0 ? '+' : '' }}{{ item.pnl }}
          </text>
          <text v-else-if="item.status === '1'" class="cor-item__pnl cor-item__pnl--running">
            {{ t('secondContractOrderRecord.waiting') }}
          </text>
        </view>
      </view>
      <view class="cor-footer">
        <text v-if="loadingState === 'loading'">{{ t('common.loading') }}</text>
        <text v-else-if="loadingState === 'nomore'">{{ t('common.noMore') }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.cor-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding: 0 0 48rpx;
}

.cor-page__glow {
  @include page-top-glow;
  height: 280rpx;
}

/* tab 外层：视觉交给 AppTabs，这里只负责页面级定位与间距 */
.cor-tabs-wrap {
  position: relative;
  z-index: 1;
  margin: 24rpx 28rpx;
}

.cor-empty {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 28rpx;
  gap: 20rpx;

  &__title {
    font-size: 28rpx;
    color: $text-muted;
  }
}

.cor-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 0 28rpx;
}

.cor-item {
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

  &__dir {
    padding: 4rpx 16rpx;
    border-radius: 6rpx;
    font-size: 22rpx;
    font-weight: 500;

    &--long {
      color: $success;
      background: $success-soft;
    }

    &--short {
      color: #c97a7a;
      background: rgba(201, 122, 122, 0.1);
    }
  }

  &__symbol {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__status {
    font-size: 24rpx;
    color: $text-secondary;
  }

  &__body {
    display: flex;
    gap: 16rpx;
    margin-bottom: 16rpx;
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
  }

  &__foot {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid $border-subtle;
  }

  &__foot-left {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  &__time {
    font-size: 22rpx;
    color: $text-muted;
  }

  &__close {
    font-size: 22rpx;
    color: $text-muted;
  }

  &__pnl {
    font-size: 30rpx;
    font-weight: 600;
    @include amount-text;

    &--win {
      color: $success;
    }

    &--loss {
      color: #c97a7a;
    }

    &--running {
      font-size: 22rpx;
      font-weight: 400;
      color: $gold;
    }
  }
}

.cor-footer {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx;
  text-align: center;
  font-size: 24rpx;
  color: $text-muted;
}
</style>
