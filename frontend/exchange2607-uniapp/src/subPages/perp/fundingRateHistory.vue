<script setup lang="ts">
import type { FundingRateHistoryItem } from '@/api/types'
import { apiData } from '@/api/http'
import { perpetualApi } from '@/api/modules'
import { formatSymbol } from '@/utils/symbol'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'fundingRateHistory',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const { t } = useI18n()
const { currentLang } = useI18nSync()


onShow(() => {
})


const symbolCode = ref('')
const list = ref<FundingRateHistoryItem[]>([])
const pageNum = ref(1)
const total = ref(0)
const hasMore = ref(true)
const loading = ref(false)
const loaded = ref(false)

const PAGE_SIZE = 20

onLoad((query) => {
  symbolCode.value = (query?.symbolCode as string) || ''
  loadList(true)
})

async function loadList(reset = false) {
  if (loading.value)
    return
  if (reset) {
    pageNum.value = 1
    hasMore.value = true
    list.value = []
  }
  if (!hasMore.value)
    return
  loading.value = true
  try {
    const page = await apiData(perpetualApi.findFundingRateHistory({
      symbolCode: symbolCode.value,
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
    }))
    const content = page?.content || []
    total.value = page?.totalElements ?? 0
    list.value = reset ? content : [...list.value, ...content]
    hasMore.value = list.value.length < total.value
    pageNum.value += 1
  }
  finally {
    loading.value = false
    loaded.value = true
  }
}

onReachBottom(() => loadList())

onPullDownRefresh(async () => {
  await loadList(true)
  uni.stopPullDownRefresh()
})

/** 资金费率小数 → 百分比文本（保留 4 位，正数带 +） */
function formatFundingRate(rate?: number) {
  if (rate == null)
    return '—'
  const pct = rate * 100
  return `${pct >= 0 ? '+' : ''}${pct.toFixed(4)}%`
}
</script>

<template>
  <view class="frh">
    <AppNavBar :title="t('perpTrade.fundingHistoryTitle')" />
    <view class="frh__header">
      <text class="frh__symbol">
        {{ formatSymbol(symbolCode) }}
      </text>
    </view>

    <view class="frh__row frh__row--head">
      <text class="frh__cell">
        {{ t('perpTrade.fundingTime') }}
      </text>
      <text class="frh__cell frh__cell--right">
        {{ t('perpTrade.fundingRateLabel') }}
      </text>
    </view>

    <view v-for="item in list" :key="item.id" class="frh__row">
      <text class="frh__cell">
        {{ item.fundingTime || '—' }}
      </text>
      <text
        class="frh__cell frh__cell--right"
        :class="(item.fundingRate ?? 0) >= 0 ? 'frh--up' : 'frh--down'"
      >
        {{ formatFundingRate(item.fundingRate) }}
      </text>
    </view>

    <view v-if="loaded && !list.length" class="frh__tip">
      {{ t('perpTrade.noFundingHistory') }}
    </view>
    <view v-else-if="hasMore" class="frh__tip frh__tip--link" @click="loadList()">
      {{ loading ? t('common.loading') : t('common.loadMore') }}
    </view>
    <view v-else class="frh__tip frh__tip--dim">
      {{ t('common.noMore') }}
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.frh {
  min-height: 100vh;
  padding: 0 0 calc(40rpx + env(safe-area-inset-bottom));
  background: $page-bg;
}

.frh__header {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin: 24rpx 28rpx 20rpx;
}

.frh__symbol {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-highlight;
}

.frh__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 28rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid $border-subtle;
}

.frh__row--head {
  padding: 12rpx 0;
  border-bottom-color: $border-subtle;

  .frh__cell {
    font-size: 22rpx;
    color: $text-muted;
  }
}

.frh__cell {
  font-size: 26rpx;
  color: $text-primary;
  font-variant-numeric: tabular-nums;

  &--right {
    text-align: right;
  }
}

.frh--up {
  color: $success;
}

.frh--down {
  color: #d08080;
}

.frh__tip {
  padding: 40rpx 28rpx;
  font-size: 24rpx;
  text-align: center;
  color: $text-muted;
}

.frh__tip--link {
  color: $gold-light;
}

.frh__tip--dim {
  color: rgba(154, 150, 144, 0.6);
}
</style>
