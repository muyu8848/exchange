<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()

const popupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { LANGUAGES } from '@/locale/languages'

const props = withDefaults(
  defineProps<{ modelValue: boolean; mode?: 'ui' | 'notify'; current?: string }>(),
  {
    mode: 'ui',
    current: '',
  },
)
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'select': [value: string]
}>()

const { t } = useI18n()
const { currentLang, setLocale } = useI18nSync()

const langs = LANGUAGES
const activeLang = computed(() => (props.mode === 'notify' ? props.current : currentLang.value))

function choose(value: string) {
  if (props.mode === 'notify') {
    emit('select', value)
  }
  else {
    setLocale(value)
  }
  emit('update:modelValue', false)
}

function onVisibleChange(visible: boolean) {
  emit('update:modelValue', visible)
}
</script>

<template>
  <wd-popup
    :model-value="modelValue"
    position="bottom"
    closable
    root-portal
    :z-index="100"
    modal-style="background: rgba(0, 0, 0, 0.6)"
    :custom-style="popupStyle"
    :custom-class="`lang-picker-popup theme-${theme}`"
    @update:model-value="onVisibleChange"
  >
    <view class="lang-picker">
      <view class="lang-picker__header">
        <text class="lang-picker__title">{{ t('my.languageTitle') }}</text>
      </view>
      <view
        v-for="item in langs"
        :key="item.value"
        class="lang-picker__item"
        :class="{ 'lang-picker__item--active': activeLang === item.value }"
        @click="choose(item.value)"
      >
        <text class="lang-picker__item-label">{{ item.label }}</text>
        <text v-if="activeLang === item.value" class="lang-picker__check">✓</text>
      </view>
    </view>
  </wd-popup>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.lang-picker {
  background: $surface-2;
  border-radius: 24rpx 24rpx 0 0;
  padding: 8rpx 0 calc(24rpx + env(safe-area-inset-bottom));
}

.lang-picker__header {
  padding: 28rpx 32rpx 12rpx;
}

.lang-picker__title {
  font-size: 26rpx;
  color: $text-muted;
  letter-spacing: 0.04em;
}

.lang-picker__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 32rpx;
  transition: background-color 0.18s ease;

  &--active {
    background: $surface-3;
  }

  &:active {
    background: $surface-3;
  }
}

.lang-picker__item-label {
  font-size: 30rpx;
  color: $text-primary;
}

.lang-picker__check {
  font-size: 30rpx;
  color: $gold;
}

:global(.lang-picker-popup) {
  background: $surface-2 !important;
}

:global(.wd-popup__close) {
  color: $text-muted !important;
}
</style>
