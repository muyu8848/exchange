import type { PaymentAccountType } from '@/api/types'
import { apiData } from '@/api/http'
import { paymentAccountTypeApi } from '@/api/modules'

export const DEFAULT_LOGO = '/static/img/payment/default.svg'

/** 拉取全部启用的支付方式类型（无缓存，调用方自行持有列表） */
export async function loadPaymentAccountTypes(): Promise<PaymentAccountType[]> {
  return await apiData(paymentAccountTypeApi.findAllEnabled())
}

export function getEnabledPaymentAccountTypes(types: PaymentAccountType[]) {
  return types || []
}

export function paymentAccountTypeLogo(code: string, types: PaymentAccountType[]) {
  const matched = (types || []).find(item => item.code === code)
  return matched?.logo || DEFAULT_LOGO
}

export function paymentAccountTypeLabel(code: string, types: PaymentAccountType[], fallback = '') {
  const matched = (types || []).find(item => item.code === code)
  return matched?.name || fallback || code
}

export function findPaymentAccountType(code: string, types: PaymentAccountType[]) {
  return (types || []).find(item => item.code === code)
}
