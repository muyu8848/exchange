<script setup lang="ts">
import { memberApi } from '@/api/modules'
import { apiData } from '@/api/http'
import { isValidLoginPwd } from '@/utils/app'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useAuthStore } from '@/store/auth'
import { useMemberStore } from '@/store/member'

definePage({
  name: 'updateLoginPwd',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const toast = useGlobalToast()
const authStore = useAuthStore()
const memberStore = useMemberStore()

const { t } = useI18n()
const { currentLang } = useI18nSync()



const oldPwd = ref('')
const newPwd = ref('')
const confirmPwd = ref('')
const loading = ref(false)

const pwdMismatch = computed(() => {
  const newVal = String(newPwd.value ?? '')
  const confirmVal = String(confirmPwd.value ?? '')
  return !!confirmVal && newVal !== confirmVal
})

const canSubmit = computed(() => {
  if (loading.value) {
    return false
  }
  const oldVal = String(oldPwd.value ?? '')
  const newVal = String(newPwd.value ?? '')
  const confirmVal = String(confirmPwd.value ?? '')
  if (!oldVal || !newVal || !confirmVal) {
    return false
  }
  if (!isValidLoginPwd(newVal)) {
    return false
  }
  if (newVal !== confirmVal) {
    return false
  }
  if (newVal === oldVal) {
    return false
  }
  return true
})

async function submit() {
  if (!canSubmit.value) {
    return
  }
  if (!isValidLoginPwd(newPwd.value)) {
    toast.error({ msg: t('updateLoginPwd.errPwdFormat') })
    return
  }
  if (newPwd.value === oldPwd.value) {
    toast.error({ msg: t('common.pwdSameAsOld') })
    return
  }
  if (newPwd.value !== confirmPwd.value) {
    toast.error({ msg: t('common.pwdMismatch') })
    return
  }
  loading.value = true
  try {
    await apiData(memberApi.updateLoginPwd({
      oldPwd: String(oldPwd.value),
      newPwd: String(newPwd.value),
    }))
    toast.success({ msg: t('updateLoginPwd.success') })
    // 对齐「退出登录」的本地清理：后端已强制下线所有会话，无需再调 /logout
    authStore.clearAuth()
    memberStore.clearMemberInfo()
    setTimeout(() => router.replaceAll({ name: 'login' }), 1500)
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="mlp-page">
    <AppNavBar :title="t('updateLoginPwd.title')" />
    <view class="mlp-page__glow" />
    <view class="mlp-page__mesh" />

    <view class="mlp-hero mlp-hero--compact">
      <view class="mlp-hero__glow" />
      <view class="mlp-hero__head">
        <view class="mlp-hero__icon-ring">
          <view class="mlp-lock-icon-wrap">
            <view class="mlp-lock-icon" />
          </view>
        </view>
        <view class="mlp-hero__head-text">
          <text class="mlp-hero__title">{{ t('updateLoginPwd.title') }}</text>
          <text class="mlp-hero__desc">{{ t('updateLoginPwd.heroDesc') }}</text>
        </view>
      </view>
    </view>

    <view class="mlp-main-card">
      <view class="mlp-main-card__hairline" />

      <view class="mlp-field mlp-field--input">
        <text class="mlp-field__label">{{ t('updateLoginPwd.oldPwdLabel') }}</text>
        <wd-input
          v-model="oldPwd"
          :placeholder="t('updateLoginPwd.oldPwdPlaceholder')"
          show-password
          clearable
        />
      </view>

      <view class="mlp-field mlp-field--input">
        <text class="mlp-field__label">{{ t('updateLoginPwd.newPwdLabel') }}</text>
        <wd-input
          v-model="newPwd"
          :placeholder="t('updateLoginPwd.newPwdPlaceholder')"
          :maxlength="15"
          show-password
          clearable
        />
      </view>

      <view class="mlp-field mlp-field--input">
        <text class="mlp-field__label">{{ t('updateLoginPwd.confirmPwdLabel') }}</text>
        <wd-input
          v-model="confirmPwd"
          :placeholder="t('updateLoginPwd.confirmPwdPlaceholder')"
          :maxlength="15"
          show-password
          clearable
        />
        <text v-if="pwdMismatch" class="mlp-field__error">{{ t('common.pwdMismatch') }}</text>
      </view>

      <text class="mlp-main-card__tip">{{ t('updateLoginPwd.tip') }}</text>
    </view>

    <view class="mlp-submit-bar mlp-submit-bar--fixed">
      <view
        class="mlp-submit-btn"
        :class="{ 'mlp-submit-btn--disabled': !canSubmit }"
        @click="submit"
      >
        {{ loading ? t('common.submitting') : t('updateLoginPwd.submit') }}
      </view>
    </view>
  </view>
</template>

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
</style>
