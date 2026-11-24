<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMarkets } from '@/composables/useMarkets'
import { loadAllCurrencies } from '@/utils/currency'
import MarketPairList from '@/components/MarketPairList.vue'
import AppTabs from '@/components/AppTabs.vue'
import type { MarketCategory, MarketPair } from '@/composables/useMarkets'

const props = withDefaults(
  defineProps<{
    showSearch?: boolean
    /** 弹窗/外部指定默认 tab（如合约页打开时自动切到合约） */
    category?: MarketCategory
    /** 透传给行：true 点击跳转 / false 仅 emit select */
    navigate?: boolean
  }>(),
  { showSearch: false, navigate: true },
)

const emit = defineEmits<{
  (e: 'select', item: MarketPair, category: MarketCategory): void
}>()

const {
  activeTab,
  keyword,
  loading,
  displayList,
  activeSubTab,
  switchTab,
  switchSubTab,
  load,
  startPolling,
  stopPolling,
} = useMarkets()

const { t } = useI18n()

const tabs = computed(() => [
  { key: 'spot' as const, label: t('tabbar.spot') },
  { key: 'perp' as const, label: t('tabbar.perp') },
  { key: 'second' as const, label: t('markets.secondContract') },
])

const subTabs = computed(() => [
  { key: 'hot' as const, label: t('markets.hot') },
  { key: 'gain' as const, label: t('markets.gainers') },
  { key: 'loss' as const, label: t('markets.losers') },
])

/** AppTabs 事件适配（组件透传 string | number） */
function onTabChange(value: string | number) {
  switchTab(value as MarketCategory)
}

function onSubTabChange(value: string | number) {
  switchSubTab(value as Parameters<typeof switchSubTab>[0])
}

// 外部指定默认 tab 时自动切换（弹窗打开即定位到对应类）
watch(
  () => props.category,
  (c) => { if (c) switchTab(c) },
  { immediate: true },
)

// 图标数据兜底：确保首屏即使 onShow 未触发也有币种图标
loadAllCurrencies()

// 弹窗内以 v-if 挂载时也要拉数据 + 轮询
onMounted(() => {
  load(activeTab.value)
  startPolling()
})

onShow(() => {
  loadAllCurrencies()
  load(activeTab.value)
  startPolling()
})

onHide(() => {
  stopPolling()
})

onUnload(() => {
  stopPolling()
})

onUnmounted(() => {
  stopPolling()
})

function reload() {
  return load(activeTab.value, true)
}

defineExpose({ reload })
</script>

<template>
  <view class="markets-panel">
    <view v-if="showSearch" class="markets-panel__search">
      <view class="markets-panel__search-icon" />
      <input
        v-model="keyword"
        class="markets-panel__search-input"
        type="text"
        :placeholder="t('markets.searchPlaceholder')"
        placeholder-class="markets-panel__search-placeholder"
        confirm-type="search"
      />
      <view v-if="keyword" class="markets-panel__search-clear" @click="keyword = ''">
        <wd-icon name="close" size="28rpx" color="#9A9690" />
      </view>
    </view>

    <view class="markets-panel__tabs-wrap">
      <AppTabs
        variant="underline" size="lg"
        :model-value="activeTab" :items="tabs" @update:model-value="onTabChange"
      />
    </view>

    <view class="markets-panel__subtabs-wrap">
      <AppTabs
        variant="capsule" size="sm" item-padding="12rpx 0" item-radius="10rpx"
        :model-value="activeSubTab" :items="subTabs" @update:model-value="onSubTabChange"
      />
    </view>

    <view class="markets-panel__list">
      <view class="markets-panel__col-head">
        <text class="col-pair">{{ t('markets.colPair') }}</text>
        <text class="col-static">{{ t('markets.colPrice') }}</text>
        <text class="col-static">{{ t('markets.colChg') }}</text>
      </view>

      <MarketPairList
        :items="displayList"
        :category="activeTab"
        :loading="loading"
        :navigate="navigate"
        @select="(item, cat) => emit('select', item, cat)"
      />
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.markets-panel {
  position: relative;
  width: 100%;
  box-sizing: border-box;

  // 统一盒模型，避免带横向 padding/border 的元素（搜索框、子 tab）在 100% 宽度下撑出横向滚动
  *,
  *::before,
  *::after {
    box-sizing: border-box;
  }
}

.markets-panel__search {
  display: flex;
  align-items: center;
  margin: 0 0 8rpx;
  padding: 0 24rpx;
  height: 72rpx;
  border-radius: 14rpx;
  background: $surface-2;
  border: 1rpx solid $border-subtle;
}

.markets-panel__search-icon {
  flex-shrink: 0;
  width: 26rpx;
  height: 26rpx;
  margin-right: 16rpx;
  border: 3rpx solid #6a6660;
  border-radius: 50%;
  position: relative;
}

.markets-panel__search-icon::after {
  content: '';
  position: absolute;
  right: -7rpx;
  bottom: -5rpx;
  width: 11rpx;
  height: 3rpx;
  background: #6a6660;
  transform: rotate(45deg);
}

.markets-panel__search-input {
  flex: 1;
  min-width: 0;
  height: 72rpx;
  font-size: 26rpx;
  color: $text-primary;
  background: transparent;
}

.markets-panel__search-placeholder {
  color: $text-muted;
}

.markets-panel__search-clear {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 8rpx;
}

/* 一级 tab 外层：视觉交给 AppTabs，这里只负责页面级间距 */
.markets-panel__tabs-wrap {
  margin: 8rpx 0;
}

.markets-panel__list {
  position: relative;
  width: 100%;
  overflow: hidden;
}

.markets-panel__col-head {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 16rpx 4rpx;
  font-size: 22rpx;
  color: $text-muted;
}

.col-pair {
  flex: 1;
  min-width: 0;
}

.col-static {
  text-align: right;
  width: 200rpx;

  &:last-child {
    width: 170rpx;
  }
}

/* 二级 tab 外层：视觉交给 AppTabs，这里只负责间距 */
.markets-panel__subtabs-wrap {
  margin: 20rpx 0 12rpx;
}
</style>
