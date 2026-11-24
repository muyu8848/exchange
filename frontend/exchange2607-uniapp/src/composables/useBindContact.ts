import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { apiData } from '@/api/http'
import { memberApi } from '@/api/modules'
import { useMemberStore } from '@/store/member'
import { useI18n } from 'vue-i18n'

const CONTACT_TYPE_EMAIL = 'email'
const CONTACT_TYPE_PHONE = 'phone'

/**
 * 绑定 / 换绑 手机号或邮箱的公共逻辑。
 * - 首次绑定：调 bindContact（仅需新联系方式的验证码）。
 * - 换绑（已绑定过）：调 changeBindContact，需额外校验原绑定联系方式的验证码（安全验证）。
 * @param contactType 'phone' 手机，'email' 邮箱
 */
export function useBindContact(contactType: string) {
  const toast = useGlobalToast()
  const memberStore = useMemberStore()
  const { memberInfo } = storeToRefs(memberStore)
  const { t } = useI18n()

  const contact = ref('')
  const code = ref('')
  const phoneAreaCode = ref('86')
  const counting = ref(0)
  let timer: ReturnType<typeof setInterval> | null = null
  const sending = ref(false)
  const submitting = ref(false)

  const isPhone = contactType === CONTACT_TYPE_PHONE
  // 是否已绑定过（决定走首次绑定还是换绑）
  const isRebind = computed(() =>
    isPhone
      ? !!memberInfo.value.phone
      : !!memberInfo.value.email,
  )
  // 原绑定联系方式展示文案（用于安全验证弹窗）
  const oldContactDisplay = computed(() => {
    if (isPhone) {
      return memberInfo.value.phone ? `+${memberInfo.value.phoneAreaCode || '86'} ${memberInfo.value.phone}` : ''
    }
    return memberInfo.value.email || ''
  })

  // 原绑定联系方式验证码相关
  const oldCode = ref('')
  const oldCounting = ref(0)
  let oldTimer: ReturnType<typeof setInterval> | null = null
  const sendingOld = ref(false)

  function validContact() {
    const value = contact.value.trim()
    if (isPhone) {
      return /^\d{6,12}$/.test(value)
    }
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  }

  function startCountdown() {
    counting.value = 60
    timer = setInterval(() => {
      counting.value -= 1
      if (counting.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  }

  function startOldCountdown() {
    oldCounting.value = 60
    oldTimer = setInterval(() => {
      oldCounting.value -= 1
      if (oldCounting.value <= 0 && oldTimer) {
        clearInterval(oldTimer)
        oldTimer = null
      }
    }, 1000)
  }

  function clearTimer() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    if (oldTimer) {
      clearInterval(oldTimer)
      oldTimer = null
    }
  }

  async function sendCode() {
    if (!validContact()) {
      toast.error({ msg: isPhone ? t('bindContact.invalidPhone') : t('bindContact.invalidEmail') })
      return
    }
    if (counting.value > 0) {
      return
    }
    sending.value = true
    try {
      await apiData(memberApi.sendBindVerificationCode({
        contactType,
        contact: contact.value.trim(),
        ...(isPhone ? { phoneAreaCode: phoneAreaCode.value } : {}),
      }))
      toast.success({ msg: t('common.codeSent') })
      startCountdown()
    }
    catch {
      // 错误已由全局 handler 提示
    }
    finally {
      sending.value = false
    }
  }

  // 发送原绑定联系方式的验证码（换绑安全验证用）
  async function sendOldCode() {
    const oldContact = isPhone ? memberInfo.value.phone : memberInfo.value.email
    if (!oldContact) {
      toast.error({ msg: t('bindContact.noOldContact') })
      return
    }
    if (oldCounting.value > 0) {
      return
    }
    sendingOld.value = true
    try {
      await apiData(memberApi.sendRebindVerificationCode({
        contactType,
        contact: oldContact,
        ...(isPhone ? { phoneAreaCode: memberInfo.value.phoneAreaCode || '86' } : {}),
      }))
      toast.success({ msg: t('common.codeSent') })
      startOldCountdown()
    }
    catch {
      // 错误已由全局 handler 提示
    }
    finally {
      sendingOld.value = false
    }
  }

  async function submit(): Promise<boolean> {
    if (!validContact()) {
      toast.error({ msg: isPhone ? t('bindContact.invalidPhone') : t('bindContact.invalidEmail') })
      return false
    }
    if (!/^\d{6}$/.test(code.value.trim())) {
      toast.error({ msg: t('bindContact.invalidCode') })
      return false
    }
    if (isRebind.value && !/^\d{6}$/.test(oldCode.value.trim())) {
      toast.error({ msg: t('bindContact.invalidOldCode') })
      return false
    }
    submitting.value = true
    try {
      if (isRebind.value) {
        await apiData(memberApi.changeBindContact({
          contactType,
          contact: contact.value.trim(),
          ...(isPhone ? { phoneAreaCode: phoneAreaCode.value } : {}),
          verificationCode: code.value.trim(),
          oldVerificationCode: oldCode.value.trim(),
        }))
      }
      else {
        await apiData(memberApi.bindContact({
          contactType,
          contact: contact.value.trim(),
          ...(isPhone ? { phoneAreaCode: phoneAreaCode.value } : {}),
          verificationCode: code.value.trim(),
        }))
      }
      await memberStore.refreshMemberInfo()
      toast.success({ msg: t('common.bindSuccess') })
      return true
    }
    catch {
      return false
    }
    finally {
      submitting.value = false
    }
  }

  return {
    contact,
    code,
    phoneAreaCode,
    counting,
    sending,
    submitting,
    isPhone,
    isRebind,
    oldContactDisplay,
    oldCode,
    oldCounting,
    sendingOld,
    sendCode,
    sendOldCode,
    submit,
    clearTimer,
  }
}
