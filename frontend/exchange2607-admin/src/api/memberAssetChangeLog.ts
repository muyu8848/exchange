import type { PageResult } from '@/api/common';
import { request } from '@/utils/request';

export interface MemberAssetChangeLogRow {
  id: string;
  memberAccountId?: string;
  memberUid: number;
  realName?: string;
  bizOrderNo: string;
  changeTime: string;
  changeType: string;
  changeTypeName: string;
  note?: string;
  balanceChange: number;
  balanceBefore: number;
  balanceAfter: number;
  accountType?: string;
  assetCode?: string;
}

export interface AssetSubtotal {
  assetCode: string;
  income: number;
  expenditure: number;
  recordCount: number;
}

export interface MemberAssetChangeSubtotal {
  items: AssetSubtotal[];
}

export interface MemberAssetChangeLogQuery {
  memberUid?: number;
  realName?: string;
  bizOrderNo?: string;
  changeType?: string;
  direction?: string;
  accountType?: string;
  assetCode?: string;
  timeStart?: string;
  timeEnd?: string;
  pageNum?: number;
  pageSize?: number;
}

export function findMemberAssetChangeLogByPage(params: MemberAssetChangeLogQuery) {
  return request.post<PageResult<MemberAssetChangeLogRow>>({
    url: '/memberAssetChangeLog/findByPage',
    data: params,
  });
}

export function memberAssetChangeLogSubtotal(params: MemberAssetChangeLogQuery) {
  return request.post<MemberAssetChangeSubtotal>({
    url: '/memberAssetChangeLog/subtotal',
    data: params,
  });
}
