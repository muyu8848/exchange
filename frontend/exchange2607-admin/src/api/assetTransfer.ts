import { request } from '@/utils/request';
import type { PageResult } from '@/api/common';

export interface AssetTransferRow {
  id: string;
  orderNo: string;
  memberAccountId?: string;
  memberUid?: number;
  fromAccountType: string;
  fromAccountTypeName?: string;
  toAccountType: string;
  toAccountTypeName?: string;
  assetCode: string;
  amount: number;
  createTime: string;
}

export interface AssetTransferQuery {
  pageNum: number;
  pageSize: number;
  memberUid?: string;
  orderNo?: string;
  fromAccountType?: string;
  toAccountType?: string;
  assetCode?: string;
  timeStart?: string;
  timeEnd?: string;
}

export function findAssetTransferByPage(params: AssetTransferQuery) {
  return request.post<PageResult<AssetTransferRow>>({ url: '/assetTransfer/findByPage', data: params });
}
