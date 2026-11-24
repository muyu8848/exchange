import type { Method } from 'alova'
import router from '@/router'
import i18n from '@/locale'
import { useAuthStore } from '@/store/auth'
import { useMemberStore } from '@/store/member'

/** 非组件环境下的翻译（toast 文案） */
function t(key: string, named?: Record<string, unknown>) {
  return named ? i18n.global.t(key, named) : i18n.global.t(key)
}

export class ApiError extends Error {
  code: number
  data?: unknown
  /** 静默错误：游客态下私有接口返回「未登录」时置位，调用方自行决定是否引导登录，全局不弹 toast、不跳转 */
  silent: boolean

  constructor(message: string, code: number, data?: unknown, options?: { silent?: boolean }) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.data = data
    this.silent = options?.silent ?? false
  }
}

interface ApiResponse {
  code: number
  msg?: string
  data?: unknown
}

/**
 * 免登录可访问的路由（游客模式）：
 * 登录/注册/客服/找回密码 + 首页 + 行情。
 * 只放「纯公开数据」的页面；交易页/资产页仍需登录（页面内可看行情，下单与私有数据会走登录引导）。
 */
const AUTH_WHITE_ROUTES = new Set([
  'login',
  'register',
  'customerService',
  'forgetPwd',
  'home',
  'markets',
  // 交易页：游客可看 K 线/盘口/参数，下单与私有数据由页面内 requireLogin 引导
  'spotTrade',
  'perpTrade',
  'secondContractTrade',
  // K 线详情页：纯行情数据（findSymbolInfo + findOrderBook + findKline），无私有接口
  'spotChart',
  'perpChart',
  // 资金费率历史：纯公开行情数据（findFundingRateHistory）
  'fundingRateHistory',
])

function handleSessionExpired(msg?: string) {
  const authStore = useAuthStore()
  const memberStore = useMemberStore()
  authStore.clearAuth()
  // 同步清会员信息并重置缓存时间戳，避免重新登录后 10 秒内命中上一会话的缓存
  memberStore.clearMemberInfo()
  const toast = useGlobalToast()
  toast.error({ msg: msg || t('apiError.sessionExpired'), duration: 2000 })
  setTimeout(() => {
    router.replaceAll({ name: 'login' })
  }, 1500)
}

export async function handleAlovaResponse(
  response: UniApp.RequestSuccessCallbackResult | UniApp.UploadFileSuccessCallbackResult | UniApp.DownloadSuccessData,
) {
  const toast = useGlobalToast()
  const { statusCode, data } = response as UniNamespace.RequestSuccessCallbackResult

  if (statusCode >= 400) {
    toast.error(t('apiError.httpStatus', { status: statusCode }))
    throw new ApiError(t('apiError.httpStatus', { status: statusCode }), statusCode, data)
  }

  const json = data as ApiResponse
  if (import.meta.env.MODE === 'development') {
    console.log('[API Response]', json)
  }

  if (json.code === 200) {
    return json
  }

  if (json.code === 999) {
    const authStore = useAuthStore()
    // 游客态：私有接口返回未登录属于预期，静默失败（不弹 toast、不清状态、不跳登录），
    // 由调用方按 isLoggedIn 决定是否渲染「登录后查看」或引导登录。
    if (!authStore.isLoggedIn) {
      throw new ApiError(json.msg || t('apiError.sessionExpired'), 999, json, { silent: true })
    }
    // 已登录但 token 失效：保持原有行为（清理登录态 + 跳登录）
    handleSessionExpired(json.msg)
    throw new ApiError(json.msg || t('apiError.sessionExpired'), 999, json)
  }

  // 需要谷歌二次验证：属于登录流程状态而非错误，不弹 toast，由登录页展示验证码输入框
  if (json.code === 1002) {
    throw new ApiError(json.msg || '', 1002, json, { silent: true })
  }

  toast.error(json.msg || t('apiError.requestFailed'))
  throw new ApiError(json.msg || t('apiError.requestFailed'), json.code, json)
}

export function handleAlovaError(error: unknown, method: Method) {
  const toast = useGlobalToast()
  if (import.meta.env.MODE === 'development') {
    console.error('[API Error]', error, method)
  }

  if (error instanceof ApiError) {
    throw error
  }

  const err = error as { name?: string, message?: string }
  if (err.name === 'NetworkError') {
    toast.error(t('apiError.network'))
  }
  else if (err.name === 'TimeoutError') {
    toast.error(t('apiError.timeout'))
  }
  else {
    toast.error(err.message || t('apiError.unexpected'))
  }
  throw error
}

export function isAuthRoute(name?: string) {
  return !!name && AUTH_WHITE_ROUTES.has(name)
}
