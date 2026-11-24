<script setup lang="ts">
import { authApi } from '@/api/modules'
import AreaCodeSelect from '@/components/AreaCodeSelect/index.vue'
import LanguageEntry from '@/components/language-entry.vue'
import { apiData } from '@/api/http'
import { useAuthStore } from '@/store/auth'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'

definePage({
  name: 'register',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const authStore = useAuthStore()
const toast = useGlobalToast()

const { t } = useI18n()
const { currentLang } = useI18nSync()



const CONTACT_TYPE_EMAIL = 'email'
const CONTACT_TYPE_PHONE = 'phone'

const contactType = ref(CONTACT_TYPE_PHONE)
const phoneAreaCode = ref('86')
const contact = ref('')
const verificationCode = ref('')
const loginPwd = ref('')
const confirmPwd = ref('')
const inviteCode = ref('')
const codeCountdown = ref(0)
const loading = ref(false)
const activeInput = ref('')
let codeTimer: ReturnType<typeof setInterval> | null = null

// 智能识别联系方式：账号输入含 @ 视为邮箱，否则手机号
watch(contact, (val) => {
  contactType.value = val.includes('@') ? CONTACT_TYPE_EMAIL : CONTACT_TYPE_PHONE
})

// 用户输入后且识别为手机号时才显示区号选择器
const showAreaCode = computed(() => !!contact.value && !contact.value.includes('@'))

// 通过邀请链接 /subPages/auth/register?inviteCode=xxx 打开时，自动带入邀请码
onLoad((options) => {
  const fromQuery = useRoute().query.inviteCode
  const code = options?.inviteCode || (fromQuery ? String(fromQuery) : '')
  if (code) {
    inviteCode.value = code.trim()
  }
})



onUnmounted(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
  }
})

function validForm() {
  return contact.value.trim()
    && verificationCode.value.trim()
    && loginPwd.value.trim()
    && confirmPwd.value.trim()
}

function canSendCode() {
  if (codeCountdown.value > 0) {
    return false
  }
  const value = contact.value.trim()
  if (contactType.value === CONTACT_TYPE_EMAIL) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  }
  return /^\d{6,12}$/.test(value)
}

async function handleSendCode() {
  if (!canSendCode()) {
    toast.error({ msg: t('authCommon.invalidAccount') })
    return
  }
  try {
    await apiData(authApi.sendRegisterVerificationCode({
      contactType: contactType.value,
      contact: contact.value.trim(),
      ...(contactType.value === CONTACT_TYPE_PHONE ? { phoneAreaCode: phoneAreaCode.value } : {}),
    }))
    toast.success({ msg: t('common.codeSent') })
    codeCountdown.value = 60
    codeTimer = setInterval(() => {
      if (codeCountdown.value > 0) {
        codeCountdown.value -= 1
      }
      else if (codeTimer) {
        clearInterval(codeTimer)
        codeTimer = null
      }
    }, 1000)
  }
  catch (e) {
    // 错误由全局拦截统一提示
  }
}

async function handleRegister() {
  if (!validForm()) {
    toast.error({ msg: t('register.incomplete') })
    return
  }
  if (loginPwd.value !== confirmPwd.value) {
    toast.error({ msg: t('common.pwdMismatch') })
    return
  }
  loading.value = true
  try {
    await apiData(authApi.register({
      contactType: contactType.value,
      contact: contact.value.trim(),
      ...(contactType.value === CONTACT_TYPE_PHONE ? { phoneAreaCode: phoneAreaCode.value } : {}),
      verificationCode: verificationCode.value.trim(),
      loginPwd: loginPwd.value,
      inviteCode: inviteCode.value.trim(),
    }))
    const token = await apiData(authApi.login({
      contactType: contactType.value,
      contact: contact.value.trim(),
      loginType: '1',
      loginPwd: loginPwd.value,
      ...(contactType.value === CONTACT_TYPE_PHONE ? { phoneAreaCode: phoneAreaCode.value } : {}),
    }))
    authStore.setToken(token)
    toast.success({ msg: t('register.success') })
    router.replaceAll({ name: 'home' })
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="auth-page">
    <AppNavBar :title="t('register.title')">
      <template #right>
        <language-entry inline />
      </template>
    </AppNavBar>
    <view class="auth-page__glow" />

    <view class="auth-logo">
      <wd-img src="/static/img/logo.png" width="160rpx" height="160rpx" radius="32rpx" />
    </view>

    <view class="form-card">
      <view class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'contact' }">
        <text class="form-field__label">{{ t('authCommon.account') }}</text>
        <view class="contact-input">
          <AreaCodeSelect v-if="showAreaCode" v-model="phoneAreaCode" />
          <wd-input
            v-model="contact"
            :placeholder="t('authCommon.accountPlaceholder')"
            clearable
            @focus="activeInput = 'contact'"
            @blur="activeInput = ''"
          />
        </view>
      </view>
      <view class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'code' }">
        <text class="form-field__label">{{ t('authCommon.code') }}</text>
        <wd-input v-model="verificationCode" :placeholder="t('authCommon.codePlaceholder')" clearable @focus="activeInput = 'code'" @blur="activeInput = ''">
          <template #suffix>
            <view
              class="code-send-btn"
              :class="{ 'code-send-btn--disabled': !canSendCode() }"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? t('authCommon.retryCountdown', { n: codeCountdown }) : t('common.getCode') }}
            </view>
          </template>
        </wd-input>
      </view>
      <view class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'loginPwd' }">
        <text class="form-field__label">{{ t('authCommon.loginPwd') }}</text>
        <wd-input v-model="loginPwd" :placeholder="t('register.pwdPlaceholder')" show-password clearable @focus="activeInput = 'loginPwd'" @blur="activeInput = ''" />
      </view>
      <view class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'confirmPwd' }">
        <text class="form-field__label">{{ t('authCommon.confirmPwd') }}</text>
        <wd-input v-model="confirmPwd" :placeholder="t('register.confirmPwdPlaceholder')" show-password clearable @focus="activeInput = 'confirmPwd'" @blur="activeInput = ''" />
      </view>
      <view class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'inviteCode' }">
        <text class="form-field__label">{{ t('register.inviteCodeLabel') }}</text>
        <wd-input v-model="inviteCode" :placeholder="t('register.inviteCodePlaceholder')" clearable @focus="activeInput = 'inviteCode'" @blur="activeInput = ''" />
      </view>
    </view>

    <view class="auth-actions">
      <view
        class="auth-submit-btn"
        :class="{ 'auth-submit-btn--disabled': !validForm() || loading }"
        @click="handleRegister"
      >
        {{ loading ? t('register.registering') : t('common.register') }}
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@use '@/styles/theme.scss' as *;

.auth-page {
  @include form-page;
  padding: 0 0 48rpx;
}

.auth-page__glow {
  @include page-top-glow;
}

.auth-logo {
  @include auth-logo-wrap;
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  padding: 40rpx 28rpx 0;
  margin-bottom: 32rpx;
}

.form-card {
  @include form-card;
  margin: 0 28rpx;
}

.form-field {
  @include form-field;

  &--input {
    @include form-input-deep;
    @include input-focus-gold;
  }
}

.form-field__label {
  @include form-field-label;
}

.auth-actions {
  position: relative;
  z-index: 1;
  margin: 32rpx 28rpx 0;
}

.auth-submit-btn {
  @include btn-gold-block;
  transition: transform 0.12s ease;

  &:active {
    transform: scale(0.98);
  }
}

.auth-submit-btn--disabled {
  opacity: 0.42;
  pointer-events: none;
}

.contact-input {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.area-picker {
  flex-shrink: 0;
}

.area-picker__trigger {
  display: flex;
  align-items: center;
  height: 72rpx;
  padding: 0 18rpx;
  font-size: 28rpx;
  color: $text-secondary;
  background: $surface-1;
  border-radius: 12rpx;
  white-space: nowrap;
}

.code-send-btn {
  flex-shrink: 0;
  padding-left: 16rpx;
  font-size: 26rpx;
  color: $gold-light;
  white-space: nowrap;

  &--disabled {
    color: $text-muted;
    pointer-events: none;
  }
}
</style>
