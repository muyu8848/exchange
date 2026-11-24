<script setup lang="ts">
import { watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findByPage } from '@/api/memberContactBindLog';
import type { MemberContactBindPageRow } from '@/api/memberContactBindLog';

import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<MemberContactBindPageRow>[] = [
  { colKey: 'createTime', title: '时间' },
  {
    colKey: 'contactType',
    title: '类型',
    cell: (_h, { row }) => row.contactTypeName || row.contactType || '—',
  },
  {
    colKey: 'contact',
    title: '联系方式',
    width: 220,
    ellipsis: true,
    cell: (_h, { row }) => {
      if (!row.contact) {
        return '—';
      }
      return row.phoneAreaCode ? `+${row.phoneAreaCode} ${row.contact}` : row.contact;
    },
  },
  {
    colKey: 'action',
    title: '动作',
    cell: (_h, { row }) => row.actionName || row.action || '—',
  },
];

const { rows, loading, pagination, load, onPageChange, reset } = usePaged<MemberContactBindPageRow>(async (page, pageSize) => {
  const uid = Number(props.memberUid);
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findByPage({ memberUid: uid, pageNum: page, pageSize });
});

watch(
  () => props.memberUid,
  () => {
    reset();
    load();
  },
  { immediate: true },
);
</script>

<template>
  <div class="tab-body">
    <div class="hint">展示该会员邮箱 / 手机的注册、绑定、解绑记录</div>
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
    />
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

.hint {
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--td-text-color-secondary);
}
</style>
