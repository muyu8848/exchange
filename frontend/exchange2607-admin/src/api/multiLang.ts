import { request } from '@/utils/request';

export interface MultiLangRow {
  id: string;
  langCode: string;
  langName: string;
  nativeName: string;
  status: string;
  statusName?: string;
  sortNum?: number;
}

/** 全量语言（传 { status: '1' } 只取启用中的） */
export function findAllMultiLang(params: { status?: string } = {}) {
  return request.post<MultiLangRow[]>({ url: '/multiLang/findAll', data: params });
}
