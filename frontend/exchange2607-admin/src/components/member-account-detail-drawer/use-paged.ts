import type { Ref } from 'vue';
import { computed, ref } from 'vue';

export const DEFAULT_PAGE_SIZE = 10;

/**
 * 会员详情弹窗内各 tab 的通用分页状态。
 * 传入一个「按页码取数」的函数，返回表格所需的 data / loading / pagination。
 */
export function usePaged<T>(
  fetcher: (page: number, pageSize: number) => Promise<{ content?: T[]; total?: number }>,
  pageSize = DEFAULT_PAGE_SIZE,
) {
  const rows = ref([]) as Ref<T[]>;
  const loading = ref(false);
  const pageNum = ref(1);
  const total = ref(0);

  async function load() {
    loading.value = true;
    try {
      const res = await fetcher(pageNum.value, pageSize);
      rows.value = res?.content || [];
      total.value = res?.total || 0;
    }
    finally {
      loading.value = false;
    }
  }

  function onPageChange(info: { current: number }) {
    pageNum.value = info.current;
    load();
  }

  function reset() {
    pageNum.value = 1;
    rows.value = [];
    total.value = 0;
  }

  const pagination = computed(() => ({
    current: pageNum.value,
    pageSize,
    total: total.value,
    showJumper: false,
  }));

  return { rows, loading, pageNum, total, load, onPageChange, reset, pagination };
}
