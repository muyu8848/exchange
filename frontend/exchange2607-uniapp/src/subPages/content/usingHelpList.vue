<script setup lang="ts">
import type { UsingHelpAbstract } from '@/api/types'
import { usingHelpApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { stripSeconds } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'usingHelpList',
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



const records = ref<UsingHelpAbstract[]>([])
const pageLoading = ref(true)
const loadError = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

const noData = computed(() => !pageLoading.value && !loadError.value && records.value.length === 0)
const hasMore = computed(() => records.value.length < total.value)

onShow(() => {
  if (!records.value.length) {
    loadList()
  }
})

onPullDownRefresh(() => {
  pageNum.value = 1
  loadList(true)
})

onReachBottom(() => {
  if (pageLoading.value || loadingMore.value || !hasMore.value) {
    return
  }
  pageNum.value += 1
  loadingMore.value = true
  loadList()
})

async function loadList(fromPull = false) {
  const reset = fromPull || pageNum.value === 1
  const silent = !fromPull && records.value.length > 0
  if (!silent) {
    pageLoading.value = true
  }
  loadError.value = false
  try {
    const page = await apiData(usingHelpApi.findUsingHelpByPage({ pageNum: pageNum.value, pageSize }))
    const list = page?.content || []
    records.value = reset ? list : [...records.value, ...list]
    total.value = page?.total ?? 0
  }
  catch {
    if (reset) {
      records.value = []
    }
    loadError.value = true
  }
  finally {
    pageLoading.value = false
    loadingMore.value = false
    if (fromPull) {
      uni.stopPullDownRefresh()
    }
  }
}

function openDetail(item: UsingHelpItem) {
  if (!item.id) {
    return
  }
  router.push({ name: 'usingHelpDetail', query: { id: item.id } })
}
</script>

<template>
  <view class="uh-page">
    <AppNavBar :title="t('usingHelpList.title')" />
    <view class="uh-page__glow" />
    <view class="uh-page__mesh" />

    <view class="uh-hero uh-hero--compact">
      <view class="uh-hero__glow" />
      <view class="uh-hero__head">
        <view class="uh-hero__icon-ring">
          <view class="uh-help-icon" />
        </view>
        <view class="uh-hero__head-text">
          <text class="uh-hero__title">{{ t('usingHelpList.heroTitle') }}</text>
          <text class="uh-hero__desc">{{ t('usingHelpList.heroDesc') }}</text>
        </view>
      </view>
    </view>

    <view class="uh-body">
      <view v-if="pageLoading" class="uh-skeleton">
        <view v-for="i in 4" :key="i" class="uh-skeleton__item" />
      </view>

      <view v-else-if="loadError" class="uh-state-card">
        <text class="uh-state-card__title">{{ t('common.loadFail') }}</text>
        <text class="uh-state-card__desc">{{ t('common.networkError') }}</text>
        <view class="uh-state-card__btn" @click="loadList()">
          {{ t('common.reload') }}
        </view>
      </view>

      <view v-else-if="noData" class="uh-state-card uh-state-card--empty">
        <view class="uh-empty-icon" />
        <text class="uh-state-card__title">{{ t('usingHelpList.empty') }}</text>
        <text class="uh-state-card__desc">{{ t('usingHelpList.emptyDesc') }}</text>
      </view>

      <view v-else class="uh-list">
        <view
          v-for="(item, index) in records"
          :key="item.id"
          class="uh-item"
          :class="{ 'uh-item--latest': index === 0 }"
          @click="openDetail(item)"
        >
          <view class="uh-item__hairline" />
          <view class="uh-item__head">
            <view v-if="index === 0" class="uh-item__badge uh-item__badge--latest">{{ t('common.latest') }}</view>
            <text class="uh-item__time">{{ stripSeconds(item.latelyUpdateTime) }}</text>
          </view>
          <text class="uh-item__title">{{ item.title }}</text>
          <view class="uh-item__foot">
            <text class="uh-item__link">{{ t('common.viewDetail') }}</text>
            <view class="uh-item__arrow" />
          </view>
        </view>

        <view class="uh-more">
          <text v-if="loadingMore">{{ t('common.loading') }}</text>
          <text v-else-if="!hasMore">{{ t('common.noMore') }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.uh-page {
  @include page-shell;
  position: relative;
  overflow-x: hidden;
}

.uh-page__glow {
  @include page-top-glow;
}

.uh-page__mesh {
  position: absolute;
  top: 80rpx;
  left: 50%;
  width: 560rpx;
  height: 560rpx;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(201, 180, 138, 0.08) 0%, transparent 68%);
  pointer-events: none;
}

.uh-hero {
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(165deg, rgba(var(--surface-3-rgb), 0.96) 0%, rgba(var(--surface-1-rgb), 0.98) 100%);
  border: 1rpx solid $border-subtle;
  box-shadow:
    inset 0 1rpx 0 $highlight-edge,
    0 16rpx 48rpx rgba(0, 0, 0, 0.28);
  overflow: hidden;

  &__glow {
    position: absolute;
    top: -40rpx;
    right: -20rpx;
    width: 240rpx;
    height: 240rpx;
    background: radial-gradient(circle, rgba(201, 180, 138, 0.14) 0%, transparent 70%);
    pointer-events: none;
  }

  &__head {
    display: flex;
    align-items: flex-start;
    gap: 20rpx;
  }

  &__icon-ring {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: linear-gradient(145deg, rgba(201, 180, 138, 0.16) 0%, rgba(201, 180, 138, 0.04) 100%);
    border: 1rpx solid rgba(201, 180, 138, 0.22);
    box-shadow: 0 0 24rpx rgba(201, 180, 138, 0.12);
  }

  &__head-text {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
    letter-spacing: 0.04em;
    line-height: 1.35;
  }

  &__desc {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    line-height: 1.55;
    color: $text-secondary;
  }
}

.uh-help-icon {
  position: relative;
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid $gold-light;
  border-radius: 50%;
  box-sizing: border-box;

  &::after {
    content: '?';
    position: absolute;
    left: 50%;
    top: 50%;
    font-size: 18rpx;
    font-weight: 600;
    line-height: 1;
    color: $gold-light;
    transform: translate(-50%, -52%);
  }
}

.uh-body {
  position: relative;
  z-index: 1;
  padding: 16rpx 28rpx 32rpx;
}

.uh-skeleton__item {
  height: 168rpx;
  margin-bottom: 16rpx;
  border-radius: 20rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: uh-shimmer 1.4s ease-in-out infinite;
}

@keyframes uh-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.uh-state-card {
  @include dark-card;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 56rpx 32rpx;
  border-radius: 20rpx;
  text-align: center;

  &--empty {
    padding-top: 72rpx;
    padding-bottom: 72rpx;
  }

  &__title {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-primary;
  }

  &__desc {
    margin-top: 12rpx;
    font-size: 22rpx;
    line-height: 1.6;
    color: $text-muted;
  }

  &__btn {
    margin-top: 28rpx;
    padding: 16rpx 40rpx;
    border-radius: 999rpx;
    font-size: 24rpx;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.22);
  }
}

.uh-empty-icon {
  width: 72rpx;
  height: 72rpx;
  margin-bottom: 20rpx;
  border-radius: 50%;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  position: relative;

  &::before {
    content: '?';
    position: absolute;
    left: 50%;
    top: 50%;
    font-size: 32rpx;
    font-weight: 500;
    color: $text-muted;
    transform: translate(-50%, -52%);
  }
}

.uh-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.uh-more {
  display: flex;
  justify-content: center;
  padding: 24rpx 0 8rpx;

  text {
    font-size: 24rpx;
    color: $text-muted;
  }
}

.uh-item {
  @include dark-card;
  position: relative;
  padding: 22rpx 24rpx 20rpx;
  border-radius: 20rpx;
  overflow: hidden;

  &--latest {
    background:
      linear-gradient(155deg, rgba(201, 180, 138, 0.08) 0%, transparent 52%),
      $surface-2;
    border-color: rgba(201, 180, 138, 0.16);
  }

  &__hairline {
    position: absolute;
    top: 0;
    left: 24rpx;
    right: 24rpx;
    height: 1rpx;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(201, 180, 138, 0.35) 50%,
      transparent 100%
    );
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 12rpx;
  }

  &__badge {
    flex-shrink: 0;
    padding: 4rpx 12rpx;
    border-radius: 999rpx;
    font-size: 18rpx;
    font-weight: 500;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.2);
    letter-spacing: 0.06em;

    &--latest {
      color: $page-bg;
      background: linear-gradient(135deg, #e5d9c0 0%, #c9b48a 100%);
      border-color: transparent;
    }
  }

  &__time {
    font-size: 20rpx;
    color: $text-muted;
    letter-spacing: 0.04em;
  }

  &__title {
    display: -webkit-box;
    overflow: hidden;
    font-size: 28rpx;
    font-weight: 500;
    line-height: 1.55;
    color: $text-primary;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  &__foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;
  }

  &__link {
    font-size: 22rpx;
    color: $gold;
    letter-spacing: 0.04em;
  }

  &__arrow {
    width: 12rpx;
    height: 12rpx;
    border-top: 2rpx solid $text-muted;
    border-right: 2rpx solid $text-muted;
    transform: rotate(45deg);
  }
}
</style>
