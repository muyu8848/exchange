<script setup lang="ts">
import type { LoginLogItem } from '@/api/types'
import { memberApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'
import { stripSeconds } from '@/utils/date'
import AppTabs from '@/components/AppTabs.vue'

definePage({
  name: 'loginRecord',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

type StatusFilter = '' | '1' | '0'

const records = ref<LoginLogItem[]>([])
const pageNum = ref(1)
const statusFilter = ref<StatusFilter>('')
const loadingState = ref<'loading' | 'loadmore' | 'nomore'>('loadmore')
const noData = computed(() => records.value.length === 0 && loadingState.value === 'nomore')

const { t } = useI18n()
const dictItemText = useDictItemText()
const { currentLang } = useI18nSync()

const tabs = computed<{ key: StatusFilter, label: string }[]>(() => [
  { key: '', label: t('common.all') },
  { key: '1', label: dictItemText('loginRecord', '1') },
  { key: '0', label: dictItemText('loginRecord', '0') },
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

function switchTab(value: string | number) {
  const next = value as StatusFilter
  if (statusFilter.value === next) {
    return
  }
  statusFilter.value = next
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
    const page = await apiData(memberApi.findLoginLogByPage({
      pageNum: pageNum.value,
      pageSize: 10,
      status: statusFilter.value || undefined,
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
    loadingState.value = records.value.length > 0 ? 'loadmore' : 'nomore'
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

function isFailed(record: LoginLogItem) {
  return record.status === '0'
}

function deviceText(record: LoginLogItem) {
  const parts = [record.os, record.browser].filter(Boolean)
  return parts.join(' · ') || t('loginRecord.unknownDevice')
}
</script>

<template>
  <view class="list-page">
    <AppNavBar :title="t('loginRecord.title')" />
    <view class="list-page__glow" />

    <view class="list-page__body">
      <view class="lr-tabs-wrap">
        <AppTabs
          variant="capsule" :model-value="statusFilter" :items="tabs"
          @update:model-value="switchTab"
        />
      </view>

      <view v-if="loadingState === 'loading' && records.length === 0" class="lr-skeleton">
        <view v-for="i in 3" :key="i" class="lr-skeleton__item" />
      </view>

      <view v-else-if="noData" class="list-page__empty">
        <wd-icon name="history" size="72rpx" color="#4A4846" />
        <text>{{ statusFilter === '0' ? t('loginRecord.emptyFail') : t('loginRecord.emptyAll') }}</text>
      </view>

      <view
        v-for="record in records"
        :key="record.id"
        class="record-card"
        :class="{ 'record-card--fail': isFailed(record) }"
      >
        <view class="record-card__head">
          <view class="record-card__time-wrap">
            <text class="record-card__time">{{ stripSeconds(record.loginTime) }}</text>
          </view>
          <text
            class="record-card__badge"
            :class="isFailed(record) ? 'record-card__badge--fail' : 'record-card__badge--success'"
          >
            {{ dictItemText('loginRecord', record.status, record.statusName) }}
          </text>
        </view>

        <view class="record-card__row">
          <text class="record-card__label">{{ t('loginRecord.device') }}</text>
          <text class="record-card__value">{{ deviceText(record) }}</text>
        </view>
        <view class="record-card__row">
          <text class="record-card__label">IP</text>
          <text class="record-card__value record-card__value--mono">{{ record.ip || '—' }}</text>
        </view>
        <view v-if="isFailed(record) && record.msg" class="record-card__fail-msg">
          {{ record.msg }}
        </view>
      </view>

      <view
        v-if="records.length > 0"
        class="list-page__more"
        @click="nextPage"
      >
        {{ loadingState === 'nomore' ? t('loginRecord.noMore') : loadingState === 'loading' ? t('common.loading') : t('common.loadMore') }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.list-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding-bottom: 48rpx;
}

.list-page__glow {
  @include page-top-glow;
}

.list-page__body {
  position: relative;
  z-index: 1;
  padding: 24rpx 28rpx 0;
}

/* Tab 外层：视觉交给 AppTabs，这里只负责间距 */
.lr-tabs-wrap {
  margin-bottom: 20rpx;
}

.list-page__more {
  padding: 28rpx 0 16rpx;
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
}

.record-card {
  @include record-card;

  &--fail {
    border-color: rgba(208, 128, 128, 0.28);
    background:
      linear-gradient(90deg, rgba(208, 128, 128, 0.08) 0%, transparent 28%),
      $surface-2;
    box-shadow:
      inset 6rpx 0 0 #d08080,
      inset 0 1rpx 0 $highlight-edge;
  }
}

.record-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid $border-subtle;
}

.record-card__time-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.record-card__time {
  font-size: 28rpx;
  font-weight: 500;
  color: $text-primary;
}

.record-card__time-sub {
  font-size: 22rpx;
  color: $text-muted;
  letter-spacing: 0.02em;
}

.record-card__badge {
  flex-shrink: 0;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;

  &--success {
    color: #8ab88a;
    background: rgba(138, 184, 138, 0.12);
    border: 1rpx solid rgba(138, 184, 138, 0.24);
  }

  &--fail {
    color: #d08080;
    background: rgba(208, 128, 128, 0.12);
    border: 1rpx solid rgba(208, 128, 128, 0.24);
  }
}

.record-card__row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  padding: 8rpx 0;

  & + & {
    margin-top: 2rpx;
  }
}

.record-card__label {
  flex-shrink: 0;
  font-size: 24rpx;
  color: $text-muted;
  letter-spacing: 0.04em;
}

.record-card__value {
  flex: 1;
  min-width: 0;
  font-size: 26rpx;
  color: $text-primary;
  text-align: right;
  word-break: break-all;

  &--mono {
    @include amount-text;
    font-size: 24rpx;
    color: $text-secondary;
  }
}

.record-card__fail-msg {
  margin-top: 12rpx;
  padding: 14rpx 16rpx;
  border-radius: 10rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: #d08080;
  background: rgba(208, 128, 128, 0.08);
  border: 1rpx solid rgba(208, 128, 128, 0.16);
}
</style>
