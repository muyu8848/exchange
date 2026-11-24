import { request } from '@/utils/request';

export interface AdminTodoItem {
  key: string;
  label: string;
  count: number;
  menuUrl: string;
  group: string;
  groupName: string;
  query?: Record<string, string>;
}

export interface AdminTodoList {
  totalCount: number;
  items: AdminTodoItem[];
}

export function findAdminTodoList() {
  return request.post<AdminTodoList>({ url: '/todo/findTodoList' });
}
