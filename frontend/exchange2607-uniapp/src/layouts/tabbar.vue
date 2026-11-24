<script lang="ts" setup>
import { nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { applyNavigationBar, useAppTheme } from '@/composables/useAppTheme'
import { useI18nSync } from '@/hooks/useI18nSync'

const TABBAR_HEIGHT = 'var(--wot-tabbar-height, 50px)'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const { activeTabbar, getTabbarItemValue, setTabbarItemActive, tabbarList } = useTabbar()
const { theme } = useAppTheme()

function isTabActive(name: string) {
  return activeTabbar.value.name === name
}

function handleTabItemClick(name: string) {
  setTabbarItemActive(name)
  router.pushTab({ name })
}

onMounted(() => {
  uni.hideTabBar({ animation: false })
  applyNavigationBar(theme.value)
})

watch(theme, value => applyNavigationBar(value))

/**
 * tab 页标题兜底：tab 页会被缓存，从其他 tab 切回时 AppNavBar 不会重新挂载，
 * 其内部 watch 也不会触发，需在这里补设一次浏览器标题。
 */
const TAB_NAV_TITLE_KEYS: Record<string, string> = {
  home: 'tabbar.home',
  markets: 'tabbar.markets',
  spotTrade: 'spot.title',
  perpTrade: 'perpTrade.title',
  assets: 'tabbar.assets',
}

// 路由切换到某个 tab 页时同步高亮；切到非 tab 子页则保持上一个高亮
watch(
  () => route.name,
  (name) => {
    if (!name) {
      return
    }
    const item = tabbarList.value.find(it => it.name === name)
    if (!item) {
      return
    }
    setTabbarItemActive(name)
    const titleKey = TAB_NAV_TITLE_KEYS[name]
    if (titleKey) {
      nextTick(() => uni.setNavigationBarTitle({ title: t(titleKey) }))
    }
  },
  { immediate: true },
)

// 语言切换兜底：tab 页的 AppNavBar 可能尚未挂载（首次进入前），这里按当前路由补设一次
watch(currentLang, () => {
  const name = route.name
  if (!name) {
    return
  }
  const titleKey = TAB_NAV_TITLE_KEYS[String(name)]
  if (titleKey) {
    nextTick(() => uni.setNavigationBarTitle({ title: t(titleKey) }))
  }
})
</script>

<script lang="ts">
export default {
  options: {
    addGlobalClass: true,
    virtualHost: true,
    styleIsolation: 'shared',
  },
}
</script>

<template>
  <view class="app-layout" :class="`theme-${theme}`">
    <slot />
    <wd-gap safe-area-bottom :height="TABBAR_HEIGHT" />
    <view class="tabbar-wrap fixed left-0 right-0 bottom-0 z-100">
      <view class="tabbar">
        <view
          v-for="item in tabbarList"
          :key="item.name"
          class="tabbar-item"
          @click="handleTabItemClick(item.name)"
        >
          <view class="tabbar-item__body">
            <view class="tabbar-item__icon">
              <wd-badge
                :model-value="getTabbarItemValue(item.name)"
                :hidden="getTabbarItemValue(item.name) == null"
                :max="99"
              >
                <wd-icon
                  :name="item.icon"
                  size="44rpx"
                  :color="isTabActive(item.name) ? TABBAR_ICON_ACTIVE : TABBAR_ICON_INACTIVE"
                />
              </wd-badge>
            </view>
            <text class="tabbar-item__title" :class="{ 'is-active': isTabActive(item.name) }">
              {{ t(item.title) }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use '@/styles/theme.scss' as *;

.app-layout {
  min-height: 100vh;
  background: $page-bg;
  color: $text-primary;
}

.tabbar-wrap {
  pointer-events: none;
}

.tabbar {
  pointer-events: auto;
  display: flex;
  align-items: stretch;
  min-height: 100rpx;
  background: $surface-1;
  border-top: 1rpx solid $border-subtle;
}

.tabbar-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  padding: 10rpx 0 8rpx;
}

.tabbar-item__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}

.tabbar-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
}

.tabbar-item__title {
  font-size: 20rpx;
  line-height: 1.2;
  letter-spacing: 0.06em;
  color: $text-muted;
  white-space: nowrap;

  &.is-active {
    color: $gold;
  }
}
</style>
