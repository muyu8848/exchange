import type { PageResult } from '@/api/common';
import { request } from '@/utils/request';

export interface MemberAssetRow {
  id: string;
  memberAccountId?: string;
  memberUid?: number;
  accountType?: string;
  accountTypeName?: string;
  assetCode?: string;
  available?: number;
  frozen?: number;
  createTime?: string;
}

export interface MemberAssetQuery {
  memberUid?: string;
  memberAccountStatus?: string;
  assetCode?: string;
  accountType?: string;
  availableStart?: number;
  availableEnd?: number;
  pageNum: number;
  pageSize: number;
}

export interface MemberAssetAdjustParam {
  id: string;
  amount: number;
  note?: string;
}

export interface MemberAssetOpenAccountParam {
  memberUid: number;
  accountType: string;
  assetCode: string;
}

export interface MemberAssetSubtotalItem {
  assetCode: string;
  accountCount: number;
  totalAvailable: number;
  totalFrozen: number;
}

export interface MemberAssetSubtotal {
  items: MemberAssetSubtotalItem[];
}

export function findMemberAssetByPage(params: MemberAssetQuery) {
  return request.post<PageResult<MemberAssetRow>>({ url: '/memberAsset/findByPage', data: params });
}

export function memberAssetSubtotal(params: Omit<MemberAssetQuery, 'pageNum' | 'pageSize'>) {
  return request.post<MemberAssetSubtotal>({ url: '/memberAsset/subtotal', data: params });
}

export function addAvailableBalance(params: MemberAssetAdjustParam) {
  return request.post({ url: '/memberAsset/addAvailableBalance', data: params });
}

export function reduceAvailableBalance(params: MemberAssetAdjustParam) {
  return request.post({ url: '/memberAsset/reduceAvailableBalance', data: params });
}

export function freezeBalance(params: MemberAssetAdjustParam) {
  return request.post({ url: '/memberAsset/freezeBalance', data: params });
}

export function unfreezeBalance(params: MemberAssetAdjustParam) {
  return request.post({ url: '/memberAsset/unfreezeBalance', data: params });
}

export function openMemberAssetAccount(params: MemberAssetOpenAccountParam) {
  return request.post({ url: '/memberAsset/openAssetAccount', data: params });
}
