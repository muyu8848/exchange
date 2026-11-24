import { defineStore } from 'pinia';
import { store } from '@/store';

import type { AdminTodoItem, AdminTodoList } from '@/api/adminTodo';
import { findAdminTodoList } from '@/api/adminTodo';

const POLL_INTERVAL_MS = 10_000;

export const useAdminTodoStore = defineStore('adminTodo', {
  state: () => ({
    list: null as AdminTodoList | null,
    loading: false,
    pollTimer: null as ReturnType<typeof setInterval> | null,
  }),
  getters: {
    totalCount: (state) => state.list?.totalCount ?? 0,
    items: (state) => state.list?.items ?? [],
    pendingItems: (state) => (state.list?.items ?? []).filter((item) => item.count > 0),
    groupedItems(): Array<{ group: string; groupName: string; items: AdminTodoItem[] }> {
      const groups = new Map<string, { group: string; groupName: string; items: AdminTodoItem[] }>();
      for (const item of this.items) {
        if (!groups.has(item.group)) {
          groups.set(item.group, { group: item.group, groupName: item.groupName, items: [] });
        }
        groups.get(item.group)?.items.push(item);
      }
      return Array.from(groups.values());
    },
  },
  actions: {
    async fetchTodoList() {
      this.loading = true;
      try {
        this.list = await findAdminTodoList();
      } catch {
        // 待办拉取失败不阻断页面，等待下一次轮询重试
      } finally {
        this.loading = false;
      }
    },
    startPolling() {
      this.stopPolling();
      this.pollTimer = setInterval(() => {
        this.fetchTodoList();
      }, POLL_INTERVAL_MS);
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer);
        this.pollTimer = null;
      }
    },
    async init() {
      await this.fetchTodoList();
      this.startPolling();
    },
  },
});

export function getAdminTodoStore() {
  return useAdminTodoStore(store);
}
