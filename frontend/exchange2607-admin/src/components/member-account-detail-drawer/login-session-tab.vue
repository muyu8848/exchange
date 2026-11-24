<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findMemberAccountLoginSessionByPage } from '@/api/accountLoginSession';
import type { AccountLoginSession } from '@/api/accountLoginSession';

import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<AccountLoginSession>[] = [
  { colKey: 'deviceTypeName', title: '设备类型' },
  { colKey: 'ip', title: 'IP地址' },
  { colKey: 'loginTime', title: '登录时间' },
  { colKey: 'lastActiveTime', title: '最后活跃' },
  { colKey: 'logoutTime', title: '结束时间' },
  { colKey: 'status', title: '状态' },
];

function statusTheme(status?: string): 'success' | 'default' | 'warning' | 'danger' {
  if (status === 'ONLINE') {
    return 'success';
  }
  if (status === 'KICKED' || status === 'REPLACED') {
    return 'warning';
  }
  if (status === 'EXPIRED') {
    return 'default';
  }
  return 'danger';
}

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<AccountLoginSession>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findMemberAccountLoginSessionByPage({
      accountUid: Number(uid),
      pageNum: page,
      pageSize,
    });
  });

const loaded = ref(false);
watch(
  () => props.memberUid,
  () => {
    reset();
    loaded.value = false;
    load();
    loaded.value = true;
  },
  { immediate: true },
);
</script>

<template>
  <t-table
    :data="rows"
    :columns="columns"
    row-key="id"
    size="small"
    table-layout="auto"
    bordered
    :loading="loading"
    :pagination="pagination"
    @page-change="onPageChange"
  >
    <template #status="{ row }">
      <t-tag size="small" variant="light" :theme="statusTheme(row.status)">
        {{ row.statusName || row.status || '—' }}
      </t-tag>
    </template>
  </t-table>
</template>
