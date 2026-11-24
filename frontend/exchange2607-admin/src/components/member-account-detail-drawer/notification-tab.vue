<script setup lang="ts">
import { watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findMemberNotificationByPage } from '@/api/memberNotification';
import type { MemberNotificationRow } from '@/api/memberNotification';

import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

/** 与 member-notification 页面一致：消息类型 / 标题 / 内容（换行）/ 已读状态（标签）/ 已读时间 / 创建时间 */
const columns: PrimaryTableCol<MemberNotificationRow>[] = [
  {
    colKey: 'type',
    title: '消息类型',
    cell: (_h, { row }) => row.typeName || row.type || '—',
  },
  { colKey: 'title', title: '标题', ellipsis: true },
  /* 内容列允许换行，不用 ellipsis（与 member-notification 页面一致） */
  { colKey: 'content', title: '内容', width: 260 },
  { colKey: 'readFlag', title: '已读状态' },
  { colKey: 'readTime', title: '已读时间' },
  { colKey: 'createTime', title: '创建时间' },
];

const { rows, loading, pagination, load, onPageChange, reset } = usePaged<MemberNotificationRow>(async (page, pageSize) => {
  const uid = Number(props.memberUid);
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findMemberNotificationByPage({ memberUid: uid, pageNum: page, pageSize });
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
      <template #content="{ row }">
        <div class="table-content">{{ row.content || '—' }}</div>
      </template>
      <template #readFlag="{ row }">
        <t-tag size="small" :theme="row.readFlag ? 'default' : 'warning'">
          {{ row.readFlag ? '已读' : '未读' }}
        </t-tag>
      </template>
      <template #readTime="{ row }">
        <span :class="{ 'table-muted': !row.readTime }">{{ row.readTime || '—' }}</span>
      </template>
    </t-table>
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

/* 内容列自动换行（与 member-notification 页面一致） */
.table-content {
  white-space: normal;
  word-break: break-word;
  line-height: 1.6;
  font-size: 13px;
  color: var(--td-text-color-primary);
}

.table-muted {
  color: var(--td-text-color-placeholder);
}
</style>
