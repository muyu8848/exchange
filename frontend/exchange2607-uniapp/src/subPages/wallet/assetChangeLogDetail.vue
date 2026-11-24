<script setup lang="ts">
import type { MyAssetChangeLogDetail } from '@/api/types'
import { balanceLogApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { formatSymbol } from '@/utils/symbol'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useDictItemText } from '@/utils/dictItem'

definePage({
  name: 'assetChangeLogDetail',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const { t } = useI18n()
const { currentLang } = useI18nSync()
const dictItemText = useDictItemText()

const goldIcon = '#C9B48A'
const silverIcon = '#C4C6CA'

const id = ref('')
const detail = ref<MyAssetChangeLogDetail | null>(null)
const loading = ref(true)

/** 导航标题优先展示变动类型（本地化），未命中回落「记录详情」 */
const pageTitle = computed(() => {
  const detailValue = detail.value
  const typeName = detailValue?.changeType
    ? dictItemText('memberAssetChangeType', detailValue.changeType, detailValue.changeTypeName)
    : ''
  return typeName || t('assetChangeLogDetail.title')
})

const isIncome = computed(() => (detail.value?.balanceChange ?? 0) >= 0)

const formattedAmount = computed(() => {
  const n = detail.value?.balanceChange ?? 0
  const sign = n >= 0 ? '+' : ''
  return `${sign}${n}`
})

onLoad((query) => {
  id.value = (query?.id as string) || ''
  loadDetail()
})

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await apiData(balanceLogApi.getDetail(id.value))
  }
  catch {
    detail.value = null
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="wcd-page">
    <AppNavBar :title="pageTitle" />
    <view class="wcd-page__glow" />
    <view class="wcd-page__glow wcd-page__glow--gold" />

    <view v-if="loading" class="wcd-loading">
      <text>{{ t('common.loading') }}</text>
    </view>

    <template v-else-if="detail">
      <view class="wcd-banner">
        <view class="wcd-banner__hairline" />
        <view class="wcd-banner__amount-row">
          <text
            class="wcd-banner__amount"
            :class="isIncome ? 'wcd-banner__amount--in' : 'wcd-banner__amount--out'"
          >
            {{ formattedAmount }}
          </text>
          <text class="wcd-banner__unit">{{ detail.assetCode }}</text>
        </view>
        <text class="wcd-banner__type">{{ dictItemText('memberAssetChangeType', detail.changeType, detail.changeTypeName) }}</text>
      </view>

      <view class="wcd-card">
        <text class="wcd-card__title">{{ t('assetChangeLogDetail.infoTitle') }}</text>

        <view class="wcd-info-row">
          <text class="wcd-info-row__label">{{ t('assetChangeLogDetail.balanceAfter') }}</text>
          <text class="wcd-info-row__value wcd-info-row__value--amount">
            {{ detail.balanceAfter }} {{ detail.assetCode }}
          </text>
        </view>

        <view class="wcd-info-row">
          <text class="wcd-info-row__label">{{ t('assetChangeLogDetail.changeTime') }}</text>
          <text class="wcd-info-row__value">{{ detail.changeTime }}</text>
        </view>

        <view v-if="detail.accountTypeName || detail.accountType" class="wcd-info-row">
          <text class="wcd-info-row__label">{{ t('assetChangeLogDetail.accountType') }}</text>
          <text class="wcd-info-row__value">{{ dictItemText('accountType', detail.accountType, detail.accountTypeName) }}</text>
        </view>

        <view v-if="detail.symbolCode" class="wcd-info-row">
          <text class="wcd-info-row__label">{{ t('assetChangeLogDetail.contract') }}</text>
          <text class="wcd-info-row__value">{{ formatSymbol(detail.symbolCode) }}</text>
        </view>

        <view v-if="detail.note" class="wcd-info-row wcd-info-row--note">
          <text class="wcd-info-row__label">{{ t('assetChangeLogDetail.note') }}</text>
          <text class="wcd-info-row__value wcd-info-row__value--note">{{ detail.note }}</text>
        </view>
      </view>
    </template>

    <view v-else class="wcd-empty">
      <wd-icon name="exclamation-circle" size="56rpx" color="#6A6660" />
      <text>{{ t('assetChangeLogDetail.notFound') }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.wcd-page {
  @include form-page;
  position: relative;
  min-height: 100vh;
  padding-bottom: 48rpx;
  overflow: hidden;
}

.wcd-page__glow {
  @include page-top-glow;
  height: 280rpx;

  &--gold {
    height: 360rpx;
    background: radial-gradient(ellipse 90% 60% at 50% -10%, rgba(201, 180, 138, 0.1) 0%, transparent 72%);
  }
}

.wcd-loading,
.wcd-empty {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 160rpx 40rpx;
  font-size: 26rpx;
  color: $text-muted;
}

.wcd-banner {
  position: relative;
  z-index: 1;
  margin: 8rpx 28rpx 0;
  padding: 40rpx 32rpx 36rpx;
  border-radius: 20rpx;
  text-align: center;
  background:
    linear-gradient(155deg, rgba(201, 180, 138, 0.1) 0%, transparent 46%),
    linear-gradient(180deg, $surface-2 0%, rgba(var(--surface-2-rgb), 0.96) 100%);
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  box-shadow:
    inset 0 1rpx 0 $highlight-edge,
    0 16rpx 48rpx rgba(0, 0, 0, 0.2);
  overflow: hidden;

  &__hairline {
    position: absolute;
    top: 0;
    left: 48rpx;
    right: 48rpx;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(184, 160, 120, 0.45), transparent);
  }

  &__amount-row {
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 12rpx;
    margin-bottom: 12rpx;
  }

  &__amount {
    font-size: 64rpx;
    font-weight: 500;
    line-height: 1.1;
    @include amount-text;

    &--in {
      color: $gold-light;
    }

    &--out {
      color: $silver;
    }
  }

  &__unit {
    font-size: 24rpx;
    color: $text-muted;
    letter-spacing: 0.06em;
  }

  &__type {
    display: block;
    font-size: 26rpx;
    color: $text-secondary;
    letter-spacing: 0.04em;
  }
}

.wcd-card {
  @include detail-info-card;
  margin-top: 20rpx;
  padding: 28rpx;

  &__title {
    @include detail-section-title;
    margin-bottom: 8rpx;
  }
}

.wcd-info-row {
  @include info-row;
  padding: 18rpx 0;
  border-bottom: 1rpx solid $border-subtle;

  &:last-child {
    border-bottom: none;
  }

  &--note {
    align-items: flex-start;
  }

  &__label {
    @include info-row-label;
    width: 168rpx;
  }

  &__value {
    @include info-row-value;
    text-align: right;

    &--amount {
      font-size: 30rpx;
      font-weight: 500;
      color: $text-highlight;
      @include amount-text;
    }

    &--note {
      line-height: 1.7;
      color: $text-secondary;
    }
  }
}
</style>
