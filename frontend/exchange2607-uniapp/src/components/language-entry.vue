<template>
  <view class="lang-entry" :class="{ 'lang-entry--inline': inline }" @click="visible = true">
    <text class="lang-entry__label">{{ currentLabel }}</text>
    <text class="lang-entry__arrow">▾</text>
  </view>
  <language-picker v-model="visible" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import LanguagePicker from '@/components/language-picker.vue'
import { LANGUAGES } from '@/locale/languages'
import { useI18nSync } from '@/hooks/useI18nSync'

withDefaults(defineProps<{
  /** 内联模式：用于放入 AppNavBar 右侧插槽，不脱离文档流 */
  inline?: boolean
}>(), {
  inline: false,
})

const visible = ref(false)
const { currentLang } = useI18nSync()

/** 当前语言名称，直接展示在入口按钮上 */
const currentLabel = computed(() => LANGUAGES.find((l) => l.value === currentLang.value)?.label ?? '')
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.lang-entry {
  position: absolute;
  top: 20rpx;
  right: 32rpx;
  z-index: 10;
  display: inline-flex;
  align-items: center;
  padding: 8rpx 18rpx;
  line-height: 1.4;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 999rpx;

  &:active {
    background: rgba(255, 255, 255, 0.12);
  }

  &--inline {
    position: static;
    top: auto;
    right: auto;
    padding: 4rpx 12rpx;
    background: $gold-soft;

    &:active {
      background: $gold-soft;
    }
  }
}

.lang-entry__label {
  font-size: 24rpx;
  color: $text-muted;
}

.lang-entry__arrow {
  margin-left: 6rpx;
  font-size: 18rpx;
  color: $text-muted;
  opacity: 0.7;
}
</style>
