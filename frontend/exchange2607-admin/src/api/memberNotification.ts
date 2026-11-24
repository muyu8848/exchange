import type { PageResult } from '@/api/common';
import { request } from '@/utils/request';

export interface MemberNotificationRow {
  id: string;
  memberUid?: number;
  memberAccountId?: string;
  type?: string;
  typeName?: string;
  title?: string;
  content?: string;
  bizId?: string;
  readFlag?: boolean;
  readTime?: string;
  createTime?: string;
}

export interface MemberNotificationQuery {
  memberUid?: number;
  type?: string;
  readFlag?: boolean;
  createTimeStart?: string;
  createTimeEnd?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface SendMemberNotificationForm {
  memberUid: number;
  title: string;
  content: string;
}

export function findMemberNotificationByPage(params: MemberNotificationQuery) {
  return request.post<PageResult<MemberNotificationRow>>({ url: '/memberNotification/findByPage', data: params });
}

export function sendMemberSystemMessage(data: SendMemberNotificationForm) {
  return request.post({ url: '/memberNotification/sendSystemMessage', data });
}
