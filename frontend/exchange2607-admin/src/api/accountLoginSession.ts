import type { PageResult } from '@/api/common';
import { request } from '@/utils/request';

export interface AccountLoginSession {
  id: string;
  accountId: string;
  accountType: string;
  accountUid?: number;
  accountName?: string;
  deviceType?: string;
  deviceTypeName?: string;
  ip?: string;
  loginTime?: string;
  lastActiveTime?: string;
  status?: string;
  statusName?: string;
  logoutTime?: string;
}

export interface AccountLoginSessionQuery {
  accountUid?: number;
  accountName?: string;
  deviceType?: string;
  ip?: string;
  status?: string;
  timeStart?: string;
  timeEnd?: string;
  pageNum: number;
  pageSize: number;
}

export function findMemberAccountLoginSessionByPage(params: AccountLoginSessionQuery) {
  return request.post<PageResult<AccountLoginSession>>({
    url: '/memberAccountLoginSession/findByPage',
    data: params,
  });
}

export function kickoutMemberAccountLoginSession(id: string) {
  return request.post({ url: '/memberAccountLoginSession/kickout', data: { id } });
}

export function findBackgroundAccountLoginSessionByPage(params: AccountLoginSessionQuery) {
  return request.post<PageResult<AccountLoginSession>>({
    url: '/backgroundAccountLoginSession/findByPage',
    data: params,
  });
}

export function kickoutBackgroundAccountLoginSession(id: string) {
  return request.post({ url: '/backgroundAccountLoginSession/kickout', data: { id } });
}
