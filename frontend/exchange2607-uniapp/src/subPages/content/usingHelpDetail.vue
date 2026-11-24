<script setup lang="ts">
import type { UsingHelpItem } from '@/api/types'
import { usingHelpApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { stripSeconds } from '@/utils/date'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'usingHelpDetail',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const id = ref('')
const record = ref<UsingHelpItem | null>(null)
const pageLoading = ref(true)
const loadError = ref(false)

const { t } = useI18n()
const { currentLang } = useI18nSync()


watch(
  () => currentLang.value,
  () => {
    if (!record.value) {
    }
  },
  { immediate: true },
)

const richContainerStyle = 'color: #c2beb8; font-size: 28rpx; line-height: 1.8; word-break: break-word;'

onLoad((query) => {
  id.value = (query?.id as string) || ''
  loadDetail()
})

async function loadDetail() {
  if (!id.value) {
    pageLoading.value = false
    loadError.value = true
    return
  }
  pageLoading.value = true
  loadError.value = false
  try {
    record.value = await apiData(usingHelpApi.getUsingHelpDetail(id.value))
  }
  catch {
    record.value = null
    loadError.value = true
  }
  finally {
    pageLoading.value = false
  }
}
</script>

<template>
  <view class="uhd-page">
    <AppNavBar :title="t('usingHelpDetail.title')" :nav-title="record?.title || t('usingHelpDetail.title')" />
    <view class="uhd-page__glow" />
    <view class="uhd-page__mesh" />

    <view v-if="pageLoading" class="uhd-skeleton">
      <view class="uhd-skeleton__header" />
      <view class="uhd-skeleton__card" />
    </view>

    <view v-else-if="loadError" class="uhd-state-card">
      <text class="uhd-state-card__title">{{ t('common.loadFail') }}</text>
      <text class="uhd-state-card__desc">{{ t('usingHelpDetail.loadFailDesc') }}</text>
      <view class="uhd-state-card__btn" @click="loadDetail">
        {{ t('common.reload') }}
      </view>
    </view>

    <template v-else-if="record">
      <view class="uhd-header-card">
        <view class="uhd-header-card__hairline" />
        <text class="uhd-header-card__eyebrow">{{ t('usingHelpDetail.eyebrow') }}</text>
        <text class="uhd-header-card__title">{{ record.title }}</text>
        <text v-if="record.latelyUpdateTime" class="uhd-header-card__time">
          {{ t('usingHelpDetail.updated') }} {{ stripSeconds(record.latelyUpdateTime) }}
        </text>
      </view>

      <view class="uhd-content-card">
        <view class="uhd-content-card__hairline" />
        <view v-if="record.content" class="uhd-content-card__body dark-rich-text">
          <mp-html :content="record.content" :container-style="richContainerStyle" />
        </view>
        <text v-else class="uhd-content-card__empty">{{ t('usingHelpDetail.contentEmpty') }}</text>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.uhd-page {
  @include page-shell;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 48rpx;
}

.uhd-page__glow {
  @include page-top-glow;
}

.uhd-page__mesh {
  position: absolute;
  top: 80rpx;
  left: 50%;
  width: 560rpx;
  height: 560rpx;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(201, 180, 138, 0.08) 0%, transparent 68%);
  pointer-events: none;
}

.uhd-skeleton {
  position: relative;
  z-index: 1;
  padding: 16rpx 28rpx 0;
}

.uhd-skeleton__header,
.uhd-skeleton__card {
  border-radius: 20rpx;
  background: linear-gradient(90deg, $surface-2 0%, $surface-3 50%, $surface-2 100%);
  background-size: 200% 100%;
  animation: uhd-shimmer 1.4s ease-in-out infinite;
}

.uhd-skeleton__header {
  height: 200rpx;
  margin-bottom: 16rpx;
}

.uhd-skeleton__card {
  height: 480rpx;
}

@keyframes uhd-shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

.uhd-state-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 16rpx 28rpx 0;
  padding: 72rpx 32rpx;
  border-radius: 20rpx;
  text-align: center;

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

.uhd-header-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 24rpx 28rpx 28rpx;
  border-radius: 20rpx;
  background:
    linear-gradient(155deg, rgba(201, 180, 138, 0.06) 0%, transparent 52%),
    $surface-2;

  &__hairline {
    position: absolute;
    top: 0;
    left: 28rpx;
    right: 28rpx;
    height: 1rpx;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(201, 180, 138, 0.35) 50%,
      transparent 100%
    );
  }

  &__eyebrow {
    display: block;
    font-size: 20rpx;
    font-weight: 500;
    letter-spacing: 0.12em;
    color: $gold;
    text-transform: uppercase;
  }

  &__title {
    display: block;
    margin-top: 14rpx;
    font-size: 34rpx;
    font-weight: 600;
    color: $text-highlight;
    line-height: 1.5;
    letter-spacing: 0.02em;
  }

  &__time {
    display: block;
    margin-top: 16rpx;
    font-size: 22rpx;
    color: $text-muted;
    letter-spacing: 0.04em;
  }
}

.uhd-content-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 24rpx 28rpx 32rpx;
  border-radius: 20rpx;

  &__hairline {
    position: absolute;
    top: 0;
    left: 28rpx;
    right: 28rpx;
    height: 1rpx;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(201, 180, 138, 0.35) 50%,
      transparent 100%
    );
  }

  &__body {
    @include dark-rich-text;

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 8rpx;
    }
  }

  &__empty {
    display: block;
    font-size: 26rpx;
    line-height: 1.6;
    color: $text-muted;
    text-align: center;
    padding: 48rpx 0;
  }
}
</style>
