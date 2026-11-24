import AdapterUniapp from '@alova/adapter-uniapp'
import { createAlova } from 'alova'
import vueHook from 'alova/vue'
import mockAdapter from '../mock/mockAdapter'
import { toFormBody } from '@/utils/form'
import { handleAlovaError, handleAlovaResponse } from './handlers'
import { API_BASE_URL } from '@/config'
import i18n from '@/locale'

const enableMock = import.meta.env.VITE_ENABLE_MOCK === 'true'

export { API_BASE_URL }

export const alovaInstance = createAlova({
  baseURL: API_BASE_URL,
  ...AdapterUniapp({
    mockRequest: mockAdapter,
  }),
  statesHook: vueHook,
  beforeRequest: (method) => {
    const tokenName = uni.getStorageSync('tokenName')
    const tokenValue = uni.getStorageSync('tokenValue')
    method.config.headers = method.config.headers || {}
    if (tokenName) {
      method.config.headers[tokenName] = tokenValue
    }

    // 后端多语言：Lang 为业务真源（值为后端下发的语言码），Accept-Language 作为标准兜底
    method.config.headers.Lang = i18n.global.locale.value
    method.config.headers['Accept-Language'] = i18n.global.locale.value

    const isJson = method.config.headers['Content-Type']?.includes('application/json')
    if (['POST', 'PUT', 'PATCH'].includes(method.type) && !isJson) {
      method.config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
      if (method.data && typeof method.data === 'object' && !(method.data instanceof ArrayBuffer)) {
        method.data = toFormBody(method.data as Record<string, unknown>)
      }
    }

    if (method.type === 'GET' && CommonUtil.isObj(method.config.params)) {
      method.config.params._t = Date.now()
    }

    if (import.meta.env.MODE === 'development' && !enableMock) {
      console.log(`[API] ${method.type} ${method.url}`, method.data || method.config.params)
    }
  },
  responded: {
    onSuccess: handleAlovaResponse,
    onError: handleAlovaError,
    onComplete: async () => {},
  },
  timeout: 60000,
  cacheFor: null,
})

export default alovaInstance
