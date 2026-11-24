import i18n from '@/locale'

export function copyText(text: string) {
  if (!text) {
    return
  }
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: i18n.global.t('common.copied'), icon: 'success' })
    },
  })
}

export function isValidRealName(name: string) {
  const trimmed = name.trim()
  return trimmed.length >= 2 && trimmed.length <= 20 && /^[\u4E00-\u9FA5·]+$/.test(trimmed)
}

/** 与后端 Constant.会员登录密码强度校验 一致 */
export function isValidLoginPwd(pwd: string | number) {
  return /^[A-Z0-9]{5,15}$/i.test(String(pwd ?? ''))
}

/** 与后端 Constant.会员资金密码强度校验 一致 */
export function isValidFundPwd(pwd: string | number) {
  return /^\d{6}$/.test(String(pwd ?? ''))
}
