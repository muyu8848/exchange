<script setup lang="ts">
import type { C2cFiatOption } from '@/api/types'
import { computed, ref, watch } from 'vue'
import { commonApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { useAppStore } from '@/store/app'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'displayCurrency',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const { t } = useI18n()
const { currentLang } = useI18nSync()
const appStore = useAppStore()

const fiatList = ref<C2cFiatOption[]>([])
const loading = ref(true)

/** 仅展示启用中的法币（接口返回含禁用态） */
const options = computed(() => fiatList.value.filter(f => !f.status || f.status === '1'))



async function loadList() {
  loading.value = true
  try {
    const list = await apiData(commonApi.findAllC2cFiat()) || []
    fiatList.value = list
  }
  catch {
    fiatList.value = []
  }
  finally {
    loading.value = false
  }
}

onShow(() => {
  loadList()
})

function isActive(code: string) {
  return appStore.effectiveDisplayFiat === code
}

/** 有可用汇率才允许选择，否则折算金额无法计算 */
function isSupported(item: C2cFiatOption) {
  const rate = Number(item.fiatRate)
  return Number.isFinite(rate) && rate > 0
}

function badgeText(item: C2cFiatOption) {
  return (item.fiatCode || '?').charAt(0)
}

function selectFiat(item: C2cFiatOption) {
  if (!isSupported(item)) {
    return
  }
  if (!isActive(item.fiatCode)) {
    appStore.displayFiatCode = item.fiatCode
  }
  uni.navigateBack()
}
</script>

<template>
  <view class="form-page">
    <AppNavBar :title="t('displayCurrency.title')" />
    <view class="form-page__glow" />

    <view class="dc-tip">
      {{ t('displayCurrency.desc') }}
    </view>

    <view class="menu-card">
      <view class="section-label">
        {{ t('displayCurrency.sectionLabel') }}
      </view>

      <view
        v-for="item in options"
        :key="item.fiatCode"
        class="menu-row dc-row"
        :class="{
          'dc-row--active': isActive(item.fiatCode),
          'dc-row--disabled': !isSupported(item),
        }"
        @click="selectFiat(item)"
      >
        <view class="dc-row__left">
          <view class="dc-row__badge">
            <image
              v-if="item.logo"
              :src="item.logo"
              class="dc-row__logo"
              mode="aspectFill"
            />
            <text v-else class="dc-row__badge-text">
              {{ badgeText(item) }}
            </text>
          </view>
          <view class="dc-row__info">
            <text class="dc-row__name">
              {{ item.fiatName || item.fiatCode }}
            </text>
            <text class="dc-row__code">
              {{ item.fiatCode }}
            </text>
          </view>
        </view>

        <view class="dc-row__right">
          <text v-if="!isSupported(item)" class="dc-row__unsupported">
            {{ t('displayCurrency.unsupported') }}
          </text>
          <wd-icon
            v-else-if="isActive(item.fiatCode)"
            name="check"
            size="32rpx"
            color="#C9B48A"
          />
        </view>
      </view>

      <view v-if="!loading && !options.length" class="dc-empty">
        {{ t('displayCurrency.empty') }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.form-page {
  @include form-page;
}

.form-page__glow {
  @include page-top-glow;
}

.section-label {
  @include section-label;
}

.menu-card {
  @include menu-card;
}

.menu-row {
  @include menu-row;
}

.dc-tip {
  margin: 24rpx 28rpx 0;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: $gold-soft;
  color: $text-secondary;
  font-size: 24rpx;
  line-height: 1.6;
}

.dc-row__left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-width: 0;
}

.dc-row__badge {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  overflow: hidden;
  border-radius: 50%;
  background: $surface-2;
}

.dc-row__logo {
  width: 100%;
  height: 100%;
}

.dc-row__badge-text {
  font-size: 28rpx;
  font-weight: 600;
  color: $gold;
}

.dc-row__info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.dc-row__name {
  font-size: 28rpx;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dc-row__code {
  font-size: 22rpx;
  color: $text-muted;
}

.dc-row__right {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8rpx;
}

.dc-row__unsupported {
  font-size: 22rpx;
  color: $text-muted;
}

.dc-row--active {
  .dc-row__name {
    color: $gold-light;
  }
}

.dc-row--disabled {
  opacity: 0.45;
}

.dc-empty {
  @include list-empty;
}
</style>
