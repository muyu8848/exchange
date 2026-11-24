<template>
  <view class="area-code-select">
    <slot name="trigger">
      <view class="area-code-select__trigger" @click="open">+{{ modelValue || '86' }}</view>
    </slot>
    <wd-popup
      v-model="visible"
      position="bottom"
      closable
      root-portal
      round
      :z-index="100"
      :modal-style="areaCodeMaskStyle"
    :custom-style="popupStyle"
      :custom-class="`area-code-popup theme-${theme}`"
    >
      <view class="popup-panel area-code-panel">
        <text class="popup-panel__title">{{ t('areaCode.title') }}</text>
        <view class="area-pop__search">
          <input v-model="keyword" class="area-pop__input" :placeholder="t('areaCode.searchPlaceholder')" />
        </view>
        <scroll-view scroll-y="true" class="area-pop__list">
          <view
            v-for="item in filtered"
            :key="item.areaCode"
            class="area-pop__item"
            @click="choose(item)"
          >
            <view class="area-pop__name">
              <image v-if="item.flagImage" :src="item.flagImage" class="area-pop__flag" mode="aspectFit" />
              <text>{{ displayName(item) }}</text>
            </view>
            <text class="area-pop__code">+{{ item.areaCode }}</text>
          </view>
          <view v-if="filtered.length === 0" class="area-pop__empty">{{ t('areaCode.empty') }}</view>
        </scroll-view>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()

const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)
import { ref, computed, watch } from 'vue'
import type { CountryAreaCode } from '@/api/types'
import { apiData, apiPost } from '@/api/http'
import { dictApi } from '@/api/modules'
import { useI18n } from 'vue-i18n'

const props = withDefaults(defineProps<{ modelValue?: string }>(), { modelValue: '86' })
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'open'): void
  (e: 'close'): void
}>()

const { t, locale } = useI18n()

/** 中文语境（简/繁）显示中文名，其余语言显示英文名（英文缺失时回落中文名） */
const displayName = (item: CountryAreaCode) =>
  locale.value.startsWith('zh') ? item.countryName : item.countryNameEn || item.countryName

const visible = ref(false)

// 遮罩样式：用官方 modal-style 属性设置，独立于组件内部 class，确保半透明遮罩生效
const areaCodeMaskStyle = 'background: rgba(0, 0, 0, 0.6);'

watch(visible, (val) => {
  emit(val ? 'open' : 'close')
})
const keyword = ref('')
const list = ref<CountryAreaCode[]>([])
const loading = ref(false)

const filtered = computed(() => {
  const raw = keyword.value.trim()
  const k = raw.toLowerCase()
  if (!k) return list.value
  return list.value.filter(
    (i) =>
      i.countryName.includes(raw) ||
      (i.countryNameEn || '').toLowerCase().includes(k) ||
      i.areaCode.includes(k),
  )
})

const load = async () => {
  if (list.value.length || loading.value) return
  loading.value = true
  try {
    list.value = await apiData<CountryAreaCode[]>(apiPost<CountryAreaCode[]>('/common/findAllCountryAreaCode'))
  } finally {
    loading.value = false
  }
}

const open = () => {
  visible.value = true
  keyword.value = ''
  load()
}

const choose = (item: CountryAreaCode) => {
  emit('update:modelValue', item.areaCode)
  visible.value = false
}
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.area-code-select {
  display: inline-block;
}

.area-code-select__trigger {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 18rpx;
  font-size: 28rpx;
  color: $text-primary;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
  border-radius: 12rpx;
  box-sizing: border-box;
  white-space: nowrap;
}

.popup-panel {
  @include dark-popup-panel;

  box-sizing: border-box;
  width: 100%;
}

.popup-panel__title {
  display: block;
  margin-bottom: 24rpx;
  font-size: 30rpx;
  font-weight: 500;
  color: $text-highlight;
}

.area-code-panel {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  width: 100%;
  max-height: 70vh;
  overflow: hidden;
}

.area-pop__search {
  box-sizing: border-box;
  width: 100%;
  padding-bottom: 16rpx;
}

.area-pop__input {
  box-sizing: border-box;
  width: 100%;
  height: 72rpx;
  padding: 0 20rpx;
  background: $surface-3;
  border-radius: 12rpx;
  color: $text-highlight;
  font-size: 28rpx;
}

.area-pop__list {
  box-sizing: border-box;
  flex: 1;
  width: 100%;
  min-height: 0;
  overflow-x: hidden;
}

.area-pop__item {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
  padding: 22rpx 12rpx;
  border-bottom: 1rpx solid $border-subtle;
}

.area-pop__name {
  display: flex;
  align-items: center;
  min-width: 0;
  overflow: hidden;
  font-size: 28rpx;
  color: $text-primary;

  text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.area-pop__flag {
  flex-shrink: 0;
  width: 40rpx;
  height: 28rpx;
  margin-right: 10rpx;
  border-radius: 4rpx;
}

.area-pop__code {
  flex-shrink: 0;
  margin-left: 12rpx;
  font-size: 26rpx;
  color: $gold;
}

.area-pop__empty {
  text-align: center;
  color: $text-muted;
  padding: 40rpx;
  font-size: 26rpx;
}

:global(.area-code-popup) {
  background: $surface-2 !important;
  border-top: 1rpx solid $border-subtle;
}

:global(.area-code-popup .wd-popup__close) {
  color: $text-muted !important;
}
</style>
