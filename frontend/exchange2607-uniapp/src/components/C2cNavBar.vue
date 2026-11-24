<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute, useRouter } from '@wot-ui/router'
import { useI18n } from 'vue-i18n'
import { TABBAR_ICON_ACTIVE, TABBAR_ICON_INACTIVE } from '@/composables/useTabbar'

interface C2cNavItem {
  name: string
  title: string
  icon: string
}

const { t } = useI18n()

const items: C2cNavItem[] = [
  { name: 'tradeHall', title: 'c2c.tabbar.tradeHall', icon: 'store' },
  { name: 'c2cOrders', title: 'c2c.tabbar.orders', icon: 'list' },
  { name: 'myPreOrders', title: 'c2c.tabbar.ad', icon: 'tag' },
  { name: 'c2cMine', title: 'c2c.tabbar.mine', icon: 'user' },
]

const route = useRoute()
const router = useRouter()

const currentName = computed(() => String(route.name ?? ''))

function handleClick(name: string) {
  if (name === currentName.value) {
    return
  }
  router.replace({ name })
}
</script>

<template>
  <view class="c2c-nav">
    <view
      v-for="item in items"
      :key="item.name"
      class="c2c-nav__item"
      :class="{ 'is-active': currentName === item.name }"
      @click="handleClick(item.name)"
    >
      <wd-icon
        :name="item.icon"
        size="44rpx"
        :color="currentName === item.name ? TABBAR_ICON_ACTIVE : TABBAR_ICON_INACTIVE"
      />
      <text class="c2c-nav__title">{{ t(item.title) }}</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use '@/styles/theme.scss' as *;

.c2c-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  display: flex;
  align-items: stretch;
  min-height: 100rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: $surface-1;
  border-top: 1rpx solid $border-subtle;
}

.c2c-nav__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 10rpx 0 8rpx;
  min-width: 0;

  &.is-active {
    .c2c-nav__title {
      color: $gold;
    }
  }
}

.c2c-nav__title {
  font-size: 20rpx;
  line-height: 1.2;
  letter-spacing: 0.06em;
  color: $text-muted;
  white-space: nowrap;
}
</style>
