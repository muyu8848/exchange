<template>
  <view class="mlp-page">
    <AppNavBar :title="t('bindContact.phoneTitle')" />
    <view class="mlp-page__glow" />
    <view class="mlp-page__mesh" />

    <view class="mlp-hero mlp-hero--compact">
      <view class="mlp-hero__glow" />
      <view class="mlp-hero__head">
        <view class="mlp-hero__icon-ring">
          <wd-icon name="phone" size="32rpx" color="#e5d9c0" />
        </view>
        <view class="mlp-hero__head-text">
          <text class="mlp-hero__title">{{ isRebind ? t('bindContact.phoneTitleRebind') : t('bindContact.phoneTitle') }}</text>
          <text class="mlp-hero__desc">{{ t('bindContact.desc') }}</text>
        </view>
      </view>
    </view>

    <view class="mlp-main-card">
      <view class="mlp-main-card__hairline" />

      <text class="mlp-current" v-if="memberInfo.phone">
        {{ t('bindContact.currentBound') }}+{{ memberInfo.phoneAreaCode || '86' }} {{ memberInfo.phone }}
      </text>

      <view class="mlp-field mlp-field--input">
        <text class="mlp-field__label">{{ isRebind ? t('bindContact.phoneLabelNew') : t('bindContact.phoneLabel') }}</text>
        <wd-input v-model="contact" type="number" :placeholder="t('bindContact.placeholderPhone')" clearable>
          <template #prefix>
            <AreaCodeSelect v-model="phoneAreaCode" />
          </template>
        </wd-input>
      </view>

      <view class="mlp-field mlp-field--input">
        <text class="mlp-field__label">{{ isRebind ? t('bindContact.codeLabelNewPhone') : t('bindContact.codeLabel') }}</text>
        <wd-input v-model="code" :placeholder="t('bindContact.placeholderCode')" clearable>
          <template #suffix>
            <view
              class="code-send-btn"
              :class="{ 'code-send-btn--disabled': counting > 0 }"
              @click="sendCode"
            >
              {{ counting > 0 ? counting + 's' : t('common.getCode') }}
            </view>
          </template>
        </wd-input>
      </view>
    </view>

    <view class="mlp-submit-bar mlp-submit-bar--fixed">
      <view
        class="mlp-submit-btn"
        :class="{ 'mlp-submit-btn--disabled': !canSubmit }"
        @click="onConfirm"
      >
        {{ submitting ? t('common.submitting') : t('common.confirmBind') }}
      </view>
    </view>

    <wd-popup
      v-model="showSecurityPopup"
      position="bottom"
      closable
      root-portal
      :z-index="100"
      :custom-class="`bind-security-popup theme-${theme}`"
      modal-style="background: rgba(0, 0, 0, 0.6)"
      @close="onSecurityClose"
    >
      <view class="popup-panel">
        <text class="popup-panel__title">{{ t('bindContact.securityTitle') }}</text>
        <view class="mlp-field mlp-field--input">
          <text class="mlp-field__label">{{ oldContactDisplay }}</text>
          <wd-input v-model="oldCode" :placeholder="isPhone ? t('bindContact.placeholderOldPhoneCode') : t('bindContact.placeholderOldEmailCode')" clearable>
            <template #suffix>
              <view
                class="code-send-btn"
                :class="{ 'code-send-btn--disabled': oldCounting > 0 }"
                @click="sendOldCode"
              >
                {{ oldCounting > 0 ? oldCounting + 's' : t('common.getCode') }}
              </view>
            </template>
          </wd-input>
        </view>
        <view
          class="popup-panel__btn"
          :class="{ 'popup-panel__btn--disabled': submitting || !oldCode.trim() }"
          @click="handleSecurityConfirm"
        >
          {{ submitting ? t('common.verifying') : t('bindContact.confirmRebind') }}
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()
import { ref, computed, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import AreaCodeSelect from '@/components/AreaCodeSelect/index.vue'
import { useBindContact } from '@/composables/useBindContact'
import { useMemberStore } from '@/store/member'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'bindPhone',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const memberStore = useMemberStore()
const { memberInfo } = storeToRefs(memberStore)

const { t } = useI18n()
const { currentLang } = useI18nSync()


const {
  contact,
  code,
  phoneAreaCode,
  counting,
  submitting,
  isRebind,
  isPhone,
  oldContactDisplay,
  oldCode,
  oldCounting,
  sendCode,
  sendOldCode,
  submit,
  clearTimer,
} = useBindContact('phone')

const showSecurityPopup = ref(false)
const leaving = ref(false)

const canSubmit = computed(() => {
  if (submitting.value) return false
  return !!contact.value.trim() && !!code.value.trim() && !leaving.value
})

function onConfirm() {
  if (!canSubmit.value) return
  if (isRebind.value) {
    showSecurityPopup.value = true
    return
  }
  handleSubmit()
}

async function handleSubmit() {
  if (leaving.value) return
  const ok = await submit()
  if (ok) {
    leaving.value = true
    setTimeout(() => router.back(), 1200)
  }
}

async function handleSecurityConfirm() {
  if (leaving.value) return
  const ok = await submit()
  if (ok) {
    showSecurityPopup.value = false
    leaving.value = true
    setTimeout(() => router.back(), 1200)
  }
}

function onSecurityClose() {
  oldCode.value = ''
}

onShow(() => {
  memberStore.ensureMemberInfo()
})

onUnmounted(() => clearTimer())
</script>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.mlp-page {
  @include page-shell;
  position: relative;
  overflow-x: hidden;
  min-height: 100vh;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.mlp-page__glow {
  @include page-top-glow;
}

.mlp-page__mesh {
  position: absolute;
  top: 80rpx;
  left: 50%;
  width: 560rpx;
  height: 560rpx;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(201, 180, 138, 0.08) 0%, transparent 68%);
  pointer-events: none;
}

.mlp-hero {
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  background: linear-gradient(165deg, rgba(var(--surface-3-rgb), 0.96) 0%, rgba(var(--surface-1-rgb), 0.98) 100%);
  border: 1rpx solid $border-subtle;
  box-shadow:
    inset 0 1rpx 0 $highlight-edge,
    0 16rpx 48rpx rgba(0, 0, 0, 0.28);
  overflow: hidden;

  &__glow {
    position: absolute;
    top: -40rpx;
    right: -20rpx;
    width: 240rpx;
    height: 240rpx;
    background: radial-gradient(circle, rgba(201, 180, 138, 0.14) 0%, transparent 70%);
    pointer-events: none;
  }

  &__head {
    display: flex;
    align-items: flex-start;
    gap: 20rpx;
  }

  &__icon-ring {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: linear-gradient(145deg, rgba(201, 180, 138, 0.16) 0%, rgba(201, 180, 138, 0.04) 100%);
    border: 1rpx solid rgba(201, 180, 138, 0.22);
    box-shadow: 0 0 24rpx rgba(201, 180, 138, 0.12);
  }

  &__head-text {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
    letter-spacing: 0.04em;
    line-height: 1.35;
  }

  &__desc {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    line-height: 1.55;
    color: $text-secondary;
  }
}

.mlp-lock-icon-wrap {
  position: relative;
  width: 24rpx;
  height: 44rpx;
  flex-shrink: 0;
}

.mlp-lock-icon {
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 24rpx;
  height: 28rpx;
  border: 3rpx solid $gold-light;
  border-radius: 6rpx;
  box-sizing: border-box;
  transform: translateX(-50%);

  &::before {
    content: '';
    position: absolute;
    top: -16rpx;
    left: 50%;
    width: 20rpx;
    height: 16rpx;
    border: 3rpx solid $gold-light;
    border-bottom: none;
    border-radius: 12rpx 12rpx 0 0;
    box-sizing: border-box;
    transform: translateX(-50%);
  }
}

.mlp-main-card {
  @include dark-card;
  position: relative;
  z-index: 1;
  margin: 16rpx 28rpx 0;
  padding: 20rpx 24rpx 24rpx;
  border-radius: 20rpx;

  &__hairline {
    position: absolute;
    top: 0;
    left: 24rpx;
    right: 24rpx;
    height: 1rpx;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(201, 180, 138, 0.35) 50%,
      transparent 100%
    );
  }

  &__tip {
    display: block;
    margin-top: 8rpx;
    font-size: 20rpx;
    line-height: 1.5;
    color: $text-muted;
    text-align: center;
  }
}

.mlp-field {
  margin-bottom: 20rpx;

  &:last-of-type {
    margin-bottom: 0;
  }

  &--input {
    @include form-input-deep;

    /* 让 prefix 里的区号选择器融入输入框，避免撑高、与验证码输入框等高对齐 */
    :deep(.area-code-select__trigger) {
      height: auto;
      padding: 0 16rpx 0 4rpx;
      margin-right: 12rpx;
      background: transparent;
      border-radius: 0;
      color: $gold;
      border-right: 1rpx solid $border-subtle;
    }
  }

  &__label {
    display: block;
    margin-bottom: 12rpx;
    font-size: 24rpx;
    color: $text-secondary;
    letter-spacing: 0.04em;
  }

  &__error {
    display: block;
    margin-top: 10rpx;
    font-size: 20rpx;
    color: #c97a7a;
    line-height: 1.4;
  }
}

.mlp-submit-bar {
  position: relative;
  z-index: 1;
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));

  &--fixed {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 20;
    padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
    background: linear-gradient(
      180deg,
      rgba(var(--page-bg-rgb), 0) 0%,
      rgba(var(--page-bg-rgb), 0.94) 28%,
      $page-bg 100%
    );
    border-top: 1rpx solid $border-subtle;
    backdrop-filter: blur(12px);
  }
}

.mlp-submit-btn {
  @include btn-gold-block;

  &--disabled {
    opacity: 0.42;
    pointer-events: none;
  }
}

/* 绑定页专属：当前已绑定提示 / 获取验证码 / 换绑弹窗按钮 */
.mlp-current {
  display: block;
  margin-bottom: 24rpx;
  font-size: 24rpx;
  color: $gold;
}

.code-send-btn {
  flex-shrink: 0;
  padding-left: 16rpx;
  font-size: 26rpx;
  color: $gold;
  white-space: nowrap;

  &--disabled {
    color: $text-secondary;
    opacity: 0.5;
    pointer-events: none;
  }
}

.popup-panel {
  @include dark-popup-panel;
}

.popup-panel__title {
  display: block;
  margin-bottom: 24rpx;
  font-size: 30rpx;
  font-weight: 500;
  color: $text-highlight;
}

.popup-panel__btn {
  @include btn-gold-block;
  margin-top: 32rpx;

  &--disabled {
    opacity: 0.6;
  }
}

:global(.bind-security-popup) {
  background: $surface-2 !important;
}

:global(.bind-security-popup .wd-popup__close) {
  color: $text-muted !important;
}
</style>
