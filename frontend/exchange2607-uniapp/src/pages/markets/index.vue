<script setup lang="ts">
import MarketsPanel from '@/components/MarketsPanel.vue'
import { useI18n } from 'vue-i18n'

definePage({
  name: 'markets',
  layout: 'tabbar',
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '',
    backgroundColor: '#f5f6f8',
    enablePullDownRefresh: true,
  },
})

const { t } = useI18n()
const panelRef = ref<InstanceType<typeof MarketsPanel>>()

onPullDownRefresh(async () => {
  await panelRef.value?.reload()
  uni.stopPullDownRefresh()
})
</script>

<template>
  <view class="markets-page">
    <AppNavBar :title="t('tabbar.markets')" :show-back="false" />
    <view class="markets-page__glow" />

    <view class="markets-panel-wrap">
      <MarketsPanel ref="panelRef" show-search />
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.markets-page {
  @include page-shell;
  position: relative;
  overflow: hidden;
  padding-bottom: 140rpx;
}

.markets-page__glow {
  @include page-top-glow;
}

.markets-panel-wrap {
  position: relative;
  z-index: 1;
  margin: 8rpx 28rpx 0;
}
</style>
