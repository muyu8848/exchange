<script setup lang="ts">
import type { MyAssetChangeLog } from '@/api/types'
import { useI18n } from 'vue-i18n'
import { useDictItemText } from '@/utils/dictItem'

const { t } = useI18n()
const dictItemText = useDictItemText()

const props = withDefaults(
  defineProps<{
    record: MyAssetChangeLog
    clickable?: boolean
  }>(),
  {
    clickable: true,
  },
)

const emit = defineEmits<{
  (e: 'click', record: MyAssetChangeLog): void
}>()

function isIncome(n: number) {
  return n >= 0
}

function formatAmount(n: number) {
  const sign = n < 0 ? '-' : '+'
  return `${sign}${Math.abs(n)}`
}

function onClick() {
  if (props.clickable) {
    emit('click', props.record)
  }
}
</script>

<template>
  <view
    class="acl-card"
    :class="isIncome(record.balanceChange) ? 'acl-card--in' : 'acl-card--out'"
    @click="onClick"
  >
    <view class="acl-card__main">
      <view class="acl-card__title-row">
        <text class="acl-card__name">{{ dictItemText('memberAssetChangeType', record.changeType, record.changeTypeName) }}</text>
        <text
          v-if="record.accountTypeName || record.accountType"
          class="acl-card__account"
        >{{ dictItemText('accountType', record.accountType, record.accountTypeName) }}</text>
      </view>
      <text class="acl-card__time">{{ record.changeTime }}</text>
      <text v-if="record.balanceAfter != null" class="acl-card__balance">
        {{ t('common.balance', { n: record.balanceAfter }) }}
      </text>
    </view>

    <view class="acl-card__side">
      <text
        class="acl-card__amount"
        :class="isIncome(record.balanceChange) ? 'acl-card__amount--in' : 'acl-card__amount--out'"
      >
        {{ formatAmount(record.balanceChange) }}
      </text>
      <text class="acl-card__asset">{{ record.assetCode }}</text>
      <wd-icon v-if="clickable" name="right" size="24rpx" color="#6A6660" class="acl-card__arrow" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.acl-card {
  @include dark-card;
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  margin-bottom: 14rpx;
  padding: 28rpx;
  background: rgba(var(--surface-2-rgb), 0.82);

  &--in {
    box-shadow:
      inset 0 1rpx 0 $highlight-edge,
      inset 3rpx 0 0 rgba(201, 180, 138, 0.35);
  }

  &--out {
    box-shadow:
      inset 0 1rpx 0 $highlight-edge,
      inset 3rpx 0 0 rgba(196, 198, 202, 0.28);
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__title-row {
    display: flex;
    align-items: center;
    gap: 10rpx;
    flex-wrap: wrap;
  }

  &__account {
    flex-shrink: 0;
    margin-left: 4rpx;
    padding: 4rpx 10rpx;
    border-radius: 8rpx;
    font-size: 20rpx;
    color: $gold-light;
    background: $gold-soft;
    border: 1rpx solid rgba(201, 180, 138, 0.2);
  }

  &__name {
    font-size: 28rpx;
    font-weight: 500;
    color: $text-highlight;
  }

  &__time {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    color: $text-muted;
  }

  &__balance {
    display: block;
    margin-top: 6rpx;
    font-size: 20rpx;
    color: $text-secondary;
    @include amount-text;
  }

  &__side {
    flex-shrink: 0;
    text-align: right;
    padding-top: 2rpx;
  }

  &__amount {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    @include amount-text;

    &--in {
      color: $gold-light;
    }

    &--out {
      color: $silver;
    }
  }

  &__asset {
    display: inline-block;
    margin-top: 8rpx;
    padding: 2rpx 12rpx;
    border-radius: 8rpx;
    font-size: 20rpx;
    color: $text-muted;
    background: rgba(255, 255, 255, 0.04);
    border: 1rpx solid $border-subtle;
  }

  &__arrow {
    display: block;
    margin-top: 10rpx;
    margin-left: auto;
  }
}
</style>
