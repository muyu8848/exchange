<script setup lang="ts">
import { useAppTheme } from '@/composables/useAppTheme'

const { theme } = useAppTheme()
import { watch } from 'vue'
import { apiData } from '@/api/http'
import { memberApi } from '@/api/modules'
import { useMemberStore } from '@/store/member'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'tradeSettings',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const NICKNAME_MAX = 20

const router = useRouter()
const toast = useGlobalToast()
const { t } = useI18n()
const { currentLang } = useI18nSync()
const memberStore = useMemberStore()

const { memberInfo } = storeToRefs(memberStore)
const showNickNamePopup = ref(false)
const nickName = ref('')
const savingNickName = ref(false)
const nickNameFocus = ref(false)

const originalNickName = computed(() => (memberInfo.value.nickName || '').trim())

const previewNickName = computed(() => nickName.value.trim() || t('settings.defaultName'))

const canSaveNickName = computed(() => {
  const next = nickName.value.trim()
  if (!next || savingNickName.value) {
    return false
  }
  return next !== originalNickName.value
})

onShow(() => {
  memberStore.ensureMemberInfo()
})

function openNickNamePopup() {
  nickName.value = memberInfo.value.nickName || ''
  showNickNamePopup.value = true
  nickNameFocus.value = false
  nextTick(() => {
    nickNameFocus.value = true
  })
}

function onNickNamePopupVisibleChange(visible: boolean) {
  if (visible) {
    showNickNamePopup.value = true
    return
  }
  closeNickNamePopup()
}

function closeNickNamePopup() {
  showNickNamePopup.value = false
  nickNameFocus.value = false
}

async function saveNickName() {
  if (!canSaveNickName.value) {
    return
  }
  const nextNickName = nickName.value.trim()
  savingNickName.value = true
  try {
    await apiData(memberApi.updateNickName(nextNickName))
    await memberStore.refreshMemberInfo()
    closeNickNamePopup()
    toast.success({ msg: t('settings.toast.nicknameSaved') })
  }
  finally {
    savingNickName.value = false
  }
}
</script>

<template>
  <view class="form-page">
    <AppNavBar :title="t('settings.tradeSettings')" />
    <view class="form-page__glow" />

    <view class="menu-card">
      <view class="section-label">
        {{ t('tradeSettings.section.c2c') }}
      </view>
      <view class="menu-row" @click="openNickNamePopup">
        <text class="menu-row__title">
          {{ t('settings.nickname') }}
        </text>
        <view class="menu-row__right">
          <text class="menu-row__value">
            {{ memberInfo.nickName || '—' }}
          </text>
          <wd-icon name="right" size="28rpx" color="#9A9690" />
        </view>
      </view>
      <view class="menu-row" @click="router.push({ name: 'updateFundPwd' })">
        <text class="menu-row__title">
          {{ t('tradeSettings.fundPwd') }}
        </text>
        <view class="menu-row__right">
          <text class="menu-row__value">
            {{ memberInfo.hasFundPwdFlag ? t('common.modify') : t('tradeSettings.setNow') }}
          </text>
          <wd-icon name="right" size="28rpx" color="#9A9690" />
        </view>
      </view>
      <view class="menu-row" @click="router.push({ name: 'paymentAccountList' })">
        <text class="menu-row__title">
          {{ t('tradeSettings.paymentAccount') }}
        </text>
        <view class="menu-row__right">
          <wd-icon name="right" size="28rpx" color="#9A9690" />
        </view>
      </view>
    </view>

    <wd-popup
      :model-value="showNickNamePopup"
      position="bottom"
      closable
      root-portal
      :z-index="100"
      modal-style="background: rgba(0, 0, 0, 0.6)"
      :custom-class="`settings-popup theme-${theme}`"
      @update:model-value="onNickNamePopupVisibleChange"
    >
      <view class="popup-panel popup-panel--input">
        <text class="popup-panel__title">
          {{ t('settings.editNickname') }}
        </text>
        <view class="nickname-preview">
          <view class="nickname-preview__avatar">
            <text class="nickname-preview__avatar-text">
              {{ previewNickName.charAt(0) }}
            </text>
          </view>
          <view class="nickname-preview__info">
            <text class="nickname-preview__label">
              {{ t('settings.preview') }}
            </text>
            <text class="nickname-preview__name">
              {{ previewNickName }}
            </text>
          </view>
        </view>

        <text class="popup-panel__hint">
          {{ t('settings.nicknameHint') }}
        </text>

        <view class="nickname-input-wrap">
          <wd-input
            v-model="nickName"
            :maxlength="NICKNAME_MAX"
            :focus="nickNameFocus"
            adjust-position
            :placeholder="t('settings.nicknamePlaceholder')"
            clearable
            @blur="nickNameFocus = false"
          />
          <text class="nickname-input-wrap__count">
            {{ nickName.length }}/{{ NICKNAME_MAX }}
          </text>
        </view>

        <view
          class="popup-panel__btn"
          :class="{ 'popup-panel__btn--disabled': !canSaveNickName }"
          @click="saveNickName"
        >
          {{ savingNickName ? t('settings.saving') : t('settings.save') }}
        </view>
      </view>
    </wd-popup>
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

.menu-row__title {
  font-size: 28rpx;
  color: $text-primary;
}

.menu-row__right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  min-width: 0;
}

.menu-row__value {
  max-width: 360rpx;
  font-size: 24rpx;
  color: $text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.popup-panel {
  @include dark-popup-panel;

  &--input {
    @include form-input-deep;
  }
}

.popup-panel__title {
  display: block;
  margin-bottom: 24rpx;
  font-size: 30rpx;
  font-weight: 500;
  color: $text-highlight;
  letter-spacing: 0.04em;
}

.popup-panel__hint {
  display: block;
  margin-bottom: 20rpx;
  font-size: 22rpx;
  line-height: 1.5;
  color: $text-muted;
}

.nickname-preview {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: $surface-3;
  border: 1rpx solid $border-subtle;
}

.nickname-preview__avatar {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  border: 1rpx solid rgba(255, 255, 255, 0.14);
  background: $surface-3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nickname-preview__avatar-text {
  font-size: 34rpx;
  font-weight: 600;
  color: $gold-light;
}

.nickname-preview__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.nickname-preview__label {
  font-size: 22rpx;
  color: $text-muted;
  letter-spacing: 0.04em;
}

.nickname-preview__name {
  font-size: 32rpx;
  font-weight: 500;
  color: $text-highlight;
  word-break: break-all;
}

.nickname-input-wrap {
  position: relative;
}

.nickname-input-wrap__count {
  display: block;
  margin-top: 12rpx;
  text-align: right;
  font-size: 22rpx;
  color: $text-muted;
}

.popup-panel__btn {
  @include btn-gold-block;
  margin-top: 32rpx;

  &--disabled {
    opacity: 0.42;
    pointer-events: none;
  }
}
</style>
