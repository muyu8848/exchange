import { request } from '@/utils/request';
import type { PageResult } from '@/api/common';

export interface MemberContactBindPageRow {
  id: string;
  memberAccountId: string;
  contactType: string;
  contact?: string;
  phoneAreaCode?: string;
  action?: string;
  memberUid?: number | string;
  contactTypeName?: string;
  actionName?: string;
  createTime?: string;
}

/** 联系方式绑定流水：明细列表（逐条） */
export function findByPage(params: Record<string, unknown>) {
  return request.post<PageResult<MemberContactBindPageRow>>({
    url: '/memberContactBindLog/findByPage',
    data: params,
  });
}
