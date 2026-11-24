<script setup lang="ts">
import { watch } from 'vue'
import { apiData } from '@/api/http'
import type { TokenInfo } from '@/api/types'
import { ApiError } from '@/api/core/handlers'
import { authApi } from '@/api/modules'
import AreaCodeSelect from '@/components/AreaCodeSelect/index.vue'
import LanguageEntry from '@/components/language-entry.vue'
import { useAuthStore } from '@/store/auth'
import { openCustomerServiceAsync } from '@/utils/customerService'
import { useI18n } from 'vue-i18n'
import { useI18nSync } from '@/hooks/useI18nSync'
import { useAppTheme } from '@/composables/useAppTheme'

definePage({
  name: 'login',
  layout: 'default',
  style: {
    navigationStyle: 'custom',
    backgroundColor: '#f5f6f8',
  },
})

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

/** tabBar 页（与 pages.json 的 tabBar 保持同步），登录后回跳必须用 pushTab */
const TAB_ROUTE_NAMES = new Set(['home', 'markets', 'spotTrade', 'perpTrade', 'assets'])

/**
 * 解析登录后的回跳目标（由 checkAuthAndGo / 路由守卫写入 redirect）。
 * 只接受合法的路由 name，防止被篡改成外部地址；不接受登录/注册类页面，避免回跳死循环。
 */
function resolveRedirectTarget(raw?: unknown): string {
  if (typeof raw !== 'string' || !raw) {
    return ''
  }
  let name = ''
  try {
    name = decodeURIComponent(raw)
  }
  catch {
    return ''
  }
  if (!/^[A-Za-z][A-Za-z0-9]{0,40}$/.test(name)) {
    return ''
  }
  if (name === 'login' || name === 'register' || name === 'forgetPwd') {
    return ''
  }
  return name
}
const toast = useGlobalToast()

const { t } = useI18n()
const { currentLang } = useI18nSync()
const { theme } = useAppTheme()
// 弹窗通过 root-portal 挂到 body，脱离页面主题作用域，需要显式设置背景
const googlePopupStyle = computed(() => `background: ${theme.value === 'dark' ? '#101012' : '#ffffff'};`)



const CONTACT_TYPE_EMAIL = 'email'
const CONTACT_TYPE_PHONE = 'phone'
const LOGIN_TYPE_PWD = '1'
const LOGIN_TYPE_CODE = '2'

const contactType = ref(CONTACT_TYPE_PHONE)
const phoneAreaCode = ref('86')
const contact = ref('')
const loginType = ref(LOGIN_TYPE_PWD)
const loginTypeOptions = computed(() => [
  { label: t('login.tabPwd'), value: LOGIN_TYPE_PWD },
  { label: t('login.tabCode'), value: LOGIN_TYPE_CODE },
])
const loginPwd = ref('')
const verificationCode = ref('')
const counting = ref(0)
let codeTimer: ReturnType<typeof setInterval> | null = null
const loading = ref(false)
const activeInput = ref('')

// 谷歌二次验证（账号已绑定谷歌验证器时，密码通过后弹出）
const googleDialogVisible = ref(false)
const googleVerCode = ref('')
const googleSubmitting = ref(false)

// 用户输入后且识别为手机号时才显示区号选择器
const showAreaCode = computed(() => !!contact.value && !contact.value.includes('@'))

onUnload(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})

// 智能识别联系方式：账号输入含 @ 视为邮箱，否则手机号（隐藏/显示区号）
watch(contact, (val) => {
  contactType.value = val.includes('@') ? CONTACT_TYPE_EMAIL : CONTACT_TYPE_PHONE
})

function validContact() {
  const value = contact.value.trim()
  if (contactType.value === CONTACT_TYPE_EMAIL) {
    return /^[^\s@]+@[^\s@][^\s.@]*\.[^\s@]+$/.test(value)
  }
  return /^\d{6,12}$/.test(value)
}

function validForm() {
  if (!validContact()) {
    return false
  }
  if (loginType.value === LOGIN_TYPE_PWD) {
    return !!loginPwd.value.trim()
  }
  return /^\d{6}$/.test(verificationCode.value.trim())
}

function startCountdown() {
  counting.value = 60
  codeTimer = setInterval(() => {
    counting.value -= 1
    if (counting.value <= 0 && codeTimer) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

async function handleSendCode() {
  if (!validContact()) {
    toast.error({ msg: t('authCommon.invalidAccount') })
    return
  }
  if (counting.value > 0) {
    return
  }
  try {
    await apiData(authApi.sendLoginVerificationCode({
      contactType: contactType.value,
      contact: contact.value.trim(),
      ...(contactType.value === CONTACT_TYPE_PHONE ? { phoneAreaCode: phoneAreaCode.value } : {}),
    }))
    toast.success({ msg: t('common.codeSent') })
    startCountdown()
  }
  catch {
    // 错误已由全局 handler 提示
  }
}

function buildLoginParams() {
  const base = {
    contactType: contactType.value,
    contact: contact.value.trim(),
    loginType: loginType.value,
    ...(contactType.value === CONTACT_TYPE_PHONE ? { phoneAreaCode: phoneAreaCode.value } : {}),
  }
  return loginType.value === LOGIN_TYPE_PWD
    ? { ...base, loginPwd: loginPwd.value }
    : { ...base, verificationCode: verificationCode.value.trim() }
}

function onLoginSuccess(token: TokenInfo) {
  authStore.setToken(token)
  toast.success({ msg: t('login.success') })
  const redirectName = resolveRedirectTarget(route.query?.redirect)
  if (!redirectName) {
    router.replaceAll({ name: 'home' })
  }
  else if (TAB_ROUTE_NAMES.has(redirectName)) {
    // tabBar 页必须用 pushTab，普通 replace 对 tab 页无效
    router.pushTab({ name: redirectName })
  }
  else {
    router.replaceAll({ name: redirectName })
  }
}

async function handleLogin() {
  if (!validForm()) {
    toast.error({ msg: loginType.value === LOGIN_TYPE_PWD ? t('login.invalidAccountPwd') : t('login.invalidAccountCode') })
    return
  }
  loading.value = true
  try {
    const token = await apiData(authApi.login(buildLoginParams()))
    onLoginSuccess(token)
  }
  catch (e) {
    // 账号已绑定谷歌验证器：后端返回 1002，弹出动态验证码输入框
    if (e instanceof ApiError && e.code === 1002) {
      googleVerCode.value = ''
      googleDialogVisible.value = true
    }
  }
  finally {
    loading.value = false
  }
}

async function submitGoogleCode() {
  const code = googleVerCode.value.trim()
  if (!/^\d{6}$/.test(code)) {
    toast.error({ msg: t('login.googleCodeInvalid') })
    return
  }
  googleSubmitting.value = true
  try {
    const token = await apiData(authApi.login({ ...buildLoginParams(), googleVerCode: code }))
    googleDialogVisible.value = false
    onLoginSuccess(token)
  }
  catch {
    // 验证码错误等：保持弹窗，错误提示已由全局 handler 处理
  }
  finally {
    googleSubmitting.value = false
  }
}

function goRegister() {
  router.push({ name: 'register' })
}

function goForgetPwd() {
  router.push({ name: 'forgetPwd' })
}

async function contactService() {
  const ok = await openCustomerServiceAsync()
  if (!ok) {
    toast.error({ msg: t('login.serviceBusy') })
  }
}
</script>

<template>
  <view class="auth-page">
    <AppNavBar :title="t('login.title')" :show-back="false">
      <template #right>
        <language-entry inline />
      </template>
    </AppNavBar>
    <view class="auth-page__glow" />

    <view class="auth-logo">
      <wd-img src="/static/img/logo.png" width="200rpx" height="200rpx" radius="34rpx" />
    </view>

    <view class="form-card">
      <view class="form-field form-field--segmented">
        <wd-segmented
          v-model:value="loginType"
          :options="loginTypeOptions"
          theme="outline"
        >
          <template #label="{ option }">
            {{ option.label }}
          </template>
        </wd-segmented>
      </view>
      <view class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'contact' }">
        <text class="form-field__label">
          {{ t('authCommon.account') }}
        </text>
        <view class="contact-input">
          <AreaCodeSelect v-if="showAreaCode" v-model="phoneAreaCode" />
          <wd-input
            v-model="contact"
            :placeholder="t('authCommon.accountPlaceholder')"
            clearable
            prefix-icon="user"
            @focus="activeInput = 'contact'"
            @blur="activeInput = ''"
          />
        </view>
      </view>
      <view v-if="loginType === '1'" class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'loginPwd' }">
        <text class="form-field__label">
          {{ t('authCommon.password') }}
        </text>
        <wd-input v-model="loginPwd" :placeholder="t('login.pwdPlaceholder')" clearable show-password prefix-icon="lock" @focus="activeInput = 'loginPwd'" @blur="activeInput = ''" />
      </view>
      <view v-else class="form-field form-field--input" :class="{ 'is-focused': activeInput === 'code' }">
        <text class="form-field__label">
          {{ t('authCommon.code') }}
        </text>
        <wd-input
          v-model="verificationCode"
          :placeholder="t('authCommon.codePlaceholder')"
          clearable
          @focus="activeInput = 'code'"
          @blur="activeInput = ''"
        >
          <template #suffix>
            <view
              class="code-send-btn"
              :class="{ 'code-send-btn--disabled': counting > 0 }"
              @click="handleSendCode"
            >
              {{ counting > 0 ? t('authCommon.countdown', { n: counting }) : t('common.getCode') }}
            </view>
          </template>
        </wd-input>
      </view>
    </view>

    <view class="auth-links">
      <text @click="contactService">
        {{ t('login.onlineService') }}
      </text>
      <text @click="goForgetPwd">
        {{ t('authCommon.forgotPwd') }}
      </text>
    </view>

    <view class="auth-actions">
      <view
        class="auth-submit-btn"
        :class="{ 'auth-submit-btn--disabled': !validForm() || loading }"
        @click="handleLogin"
      >
        {{ loading ? t('login.loggingIn') : t('common.login') }}
      </view>
      <text class="auth-register-link" @click="goRegister">
        {{ t('common.register') }}
      </text>
    </view>

    <wd-popup
      v-model="googleDialogVisible"
      position="bottom"
      root-portal
      :z-index="100"
      modal-style="background: rgba(0, 0, 0, 0.6)"
      :custom-style="googlePopupStyle"
      :custom-class="`google-auth-popup theme-${theme}`"
      @close="googleDialogVisible = false"
    >
      <view class="google-auth">
        <text class="google-auth__title">{{ t('login.googleTitle') }}</text>
        <text class="google-auth__tip">{{ t('login.googleTip') }}</text>
        <view class="google-auth__field">
          <wd-input v-model="googleVerCode" :placeholder="t('login.googlePlaceholder')" :maxlength="6" clearable />
        </view>
        <view
          class="google-auth__btn"
          :class="{ 'google-auth__btn--disabled': googleSubmitting }"
          @click="submitGoogleCode"
        >
          {{ googleSubmitting ? t('login.loggingIn') : t('login.googleConfirm') }}
        </view>
      </view>
    </wd-popup>
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
  padding: 80rpx 28rpx 0;
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

.auth-links {
  @include auth-links-row;
  position: relative;
  z-index: 1;
  padding: 0 36rpx;
  box-sizing: border-box;
}

.auth-actions {
  position: relative;
  z-index: 1;
  margin: 8rpx 28rpx 0;
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

.auth-register-link {
  display: block;
  margin-top: 40rpx;
  text-align: center;
  font-size: 28rpx;
  @include auth-link-text;
}

.form-field--segmented {
  margin-bottom: 28rpx;
  @include gold-segmented;
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
  color: $gold;
  white-space: nowrap;

  &--disabled {
    color: $text-muted;
    pointer-events: none;
  }
}

.google-auth {
  padding: 32rpx 32rpx calc(40rpx + env(safe-area-inset-bottom));
  background: $surface-2;
  border-radius: 24rpx 24rpx 0 0;

  &__title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: $text-highlight;
    text-align: center;
  }

  &__tip {
    display: block;
    margin-top: 16rpx;
    font-size: 24rpx;
    line-height: 1.5;
    color: $text-secondary;
    text-align: center;
  }

  &__field {
    margin-top: 32rpx;
    @include form-input-deep;
  }

  &__btn {
    @include btn-gold-block;
    margin-top: 32rpx;
    transition: transform 0.12s ease;

    &:active {
      transform: scale(0.98);
    }
  }
}
</style>
