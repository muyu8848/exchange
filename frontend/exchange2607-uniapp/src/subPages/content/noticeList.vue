<script setup lang="ts">
import type { NoticeAbstract } from '@/api/types'
import { noticeApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { stripSeconds } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'noticeList',
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



const records = ref<NoticeAbstract[]>([])
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
    const page = await apiData(noticeApi.findNoticeByPage({ pageNum: pageNum.value, pageSize }))
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

function openDetail(item: NoticeAbstract) {
  if (!item.id) {
    return
  }
  router.push({ name: 'noticeDetail', query: { id: item.id } })
}
</script>

<template>
  <view class="nc-page">
    <AppNavBar :title="t('noticeList.title')" />
    <view class="nc-page__glow" />
    <view class="nc-page__mesh" />

    <view class="nc-hero nc-hero--compact">
      <view class="nc-hero__glow" />
      <view class="nc-hero__head">
        <view class="nc-hero__icon-ring">
          <view class="nc-bell-icon" />
        </view>
        <view class="nc-hero__head-text">
          <text class="nc-hero__title">{{ t('noticeList.heroTitle') }}</text>
          <text class="nc-hero__desc">{{ t('noticeList.heroDesc') }}</text>
        </view>
      </view>
    </view>

    <view class="nc-body">
      <view v-if="pageLoading" class="nc-skeleton">
        <view v-for="i in 4" :key="i" class="nc-skeleton__item" />
      </view>

      <view v-else-if="loadError" class="nc-state-card">
        <text class="nc-state-card__title">{{ t('common.loadFail') }}</text>
        <text class="nc-state-card__desc">{{ t('common.networkError') }}</text>
        <view class="nc-state-card__btn" @click="loadList()">
          {{ t('common.reload') }}
        </view>
      </view>

      <view v-else-if="noData" class="nc-state-card nc-state-card--empty">
        <view class="nc-empty-icon" />
        <text class="nc-state-card__title">{{ t('noticeList.empty') }}</text>
        <text class="nc-state-card__desc">{{ t('noticeList.emptyDesc') }}</text>
      </view>

      <view v-else class="nc-list">
        <view
          v-for="(item, index) in records"
          :key="item.id"
          class="nc-item"
          :class="{ 'nc-item--latest': index === 0 }"
          @click="openDetail(item)"
        >
          <view class="nc-item__hairline" />
          <view class="nc-item__head">
            <view v-if="index === 0" class="nc-item__badge nc-item__badge--latest">{{ t('common.latest') }}</view>
            <text class="nc-item__time">{{ stripSeconds(item.publishTime) }}</text>
          </view>
          <text class="nc-item__title">{{ item.title }}</text>
          <view class="nc-item__foot">
            <text class="nc-item__link">{{ t('common.viewDetail') }}</text>
            <view class="nc-item__arrow" />
          </view>
        </view>

        <view class="nc-more">
          <text v-if="loadingMore">{{ t('common.loading') }}</text>
          <text v-else-if="!hasMore">{{ t('common.noMore') }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.nc-page {
  @include page-shell;
  position: relative;
  overflow-x: hidden;
}

.nc-page__glow {
  @include page-top-glow;
}

.nc-page__mesh {
  position: absolute;
  top: 80rpx;
  left: 50%;
  width: 560rpx;
  height: 560rpx;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(201, 180, 138, 0.08) 0%, transparent 68%);
  pointer-events: none;
}

.nc-hero {
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

.nc-bell-icon {
  position: relative;
  width: 26rpx;
  height: 28rpx;
  border: 3rpx solid $gold-light;
  border-radius: 14rpx 14rpx 8rpx 8rpx;
  box-sizing: border-box;

  &::before {
    content: '';
    position: absolute;
    top: -10rpx;
    left: 50%;
    width: 8rpx;
    height: 8rpx;
    border: 3rpx solid $gold-light;
    border-bottom: none;
    border-radius: 50% 50% 0 0;
    box-sizing: border-box;
    transform: translateX(-50%);
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -8rpx;
    left: 50%;
    width: 10rpx;
    height: 4rpx;
    border-radius: 999rpx;
    background: $gold-light;
    transform: translateX(-50%);
  }
}

.nc-body {
  position: relative;
  z-index: 1;
  padding: 16rpx 28rpx 32rpx;
}

.nc-skeleton__item {
  height: 168rpx;
  margin-bottom: 16rpx;
  border-radius: 20rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: nc-shimmer 1.4s ease-in-out infinite;
}

@keyframes nc-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.nc-state-card {
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

.nc-empty-icon {
  width: 72rpx;
  height: 72rpx;
  margin-bottom: 20rpx;
  border-radius: 50%;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 22rpx;
    width: 28rpx;
    height: 20rpx;
    border: 3rpx solid $text-muted;
    border-radius: 12rpx 12rpx 6rpx 6rpx;
    box-sizing: border-box;
    transform: translateX(-50%);
  }

  &::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: 18rpx;
    width: 12rpx;
    height: 4rpx;
    border-radius: 999rpx;
    background: $text-muted;
    transform: translateX(-50%);
  }
}

.nc-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.nc-more {
  display: flex;
  justify-content: center;
  padding: 24rpx 0 8rpx;

  text {
    font-size: 24rpx;
    color: $text-muted;
  }
}

.nc-item {
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
