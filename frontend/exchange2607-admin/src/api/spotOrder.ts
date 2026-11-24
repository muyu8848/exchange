import type { PageResult } from '@/api/common';
import { request } from '@/utils/request';

export interface SpotOrderRow {
  id: string;
  memberAccountId?: string;
  orderNo: string;
  memberUid?: number;
  symbolCode?: string;
  baseCurrencyCode?: string;
  quoteCurrencyCode?: string;
  side?: string;
  sideName?: string;
  price?: number;
  amount?: number;
  total?: number;
  dealPrice?: number;
  dealAmount?: number;
  dealTotal?: number;
  fee?: number;
  feeUsdtAmount?: number;
  feeAssetCode?: string;
  status?: string;
  statusName?: string;
  entrustType?: string;
  entrustTypeName?: string;
  createTime?: string;
  dealTime?: string;
}

export interface SpotOrderSubtotal {
  orderCount: number;
  buyDealCount: number;
  sellDealCount: number;
  buyDealTotal: number;
  sellDealTotal: number;
  totalFeeUsdtAmount: number;
}

export interface SpotOrderQueryParam {
  orderNo?: string;
  memberUid?: number;
  symbolCode?: string;
  side?: string;
  status?: string;
  timeStart?: string;
  timeEnd?: string;
  pageNum?: number;
  pageSize?: number;
}

export function findSpotOrderByPage(params: SpotOrderQueryParam) {
  return request.post<PageResult<SpotOrderRow>>({ url: '/spotOrder/findByPage', data: params });
}

export function findSpotOrderById(id: string) {
  return request.post<SpotOrderRow>({ url: '/spotOrder/findById', data: { id } });
}

export function spotOrderSubtotal(params: SpotOrderQueryParam) {
  return request.post<SpotOrderSubtotal>({ url: '/spotOrder/subtotal', data: params });
}
