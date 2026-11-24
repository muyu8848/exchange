import type { ApiResult } from './types'
import alovaInstance from './core/instance'

export async function apiGet<T>(url: string, params?: Record<string, unknown>) {
  const method = alovaInstance.Get<ApiResult<T>>(url, { params })
  return method.send() as Promise<ApiResult<T>>
}

export async function apiPost<T>(url: string, data?: Record<string, unknown>) {
  const method = alovaInstance.Post<ApiResult<T>>(url, data || {})
  return method.send() as Promise<ApiResult<T>>
}

export async function apiPostJson<T>(url: string, data?: Record<string, unknown>) {
  const method = alovaInstance.Post<ApiResult<T>>(url, data || {}, {
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
  })
  return method.send() as Promise<ApiResult<T>>
}

export async function apiData<T>(promise: Promise<ApiResult<T>>) {
  const res = await promise
  return res.data as T
}
