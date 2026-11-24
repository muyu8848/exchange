<script setup lang="ts">
import { computed, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findLoginLogByPage } from '@/api/loginLog';
import type { LoginLogRow } from '@/api/loginLog';

import { usePaged } from './use-paged';

const props = defineProps<{
  /** 会员账号 ID（memberAccountId）：登录日志的 accountId 存的就是它 */
  memberId?: string;
}>();

const columns: PrimaryTableCol<LoginLogRow>[] = [
  { colKey: 'userName', title: '登录账号', width: 180, ellipsis: true },
  { colKey: 'ip', title: 'IP 地址', width: 140 },
  { colKey: 'browser', title: '浏览器' },
  { colKey: 'os', title: '操作系统' },
  { colKey: 'login_state', title: '登录状态', width: 150 },
  { colKey: 'loginTime', title: '登录时间', width: 170 },
];

const { rows, loading, pagination, load, onPageChange, reset } = usePaged<LoginLogRow>(async (page, pageSize) => {
  if (!props.memberId) {
    return { content: [], total: 0 };
  }
  return findLoginLogByPage({
    accountId: props.memberId,
    // 只看客户端登录，避免把后台管理登录也带出来
    subSystem: 'client',
    pageNum: page,
    pageSize,
  });
});

watch(
  () => props.memberId,
  () => {
    reset();
    load();
  },
  { immediate: true },
);
</script>

<template>
  <div class="tab-body">
    <template v-if="memberId">
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
        <template #login_state="{ row }">
          <div class="state">
            <t-tag size="small" variant="light" :theme="row.status === '1' ? 'success' : 'danger'">
              {{ row.statusName || (row.status === '1' ? '成功' : '失败') }}
            </t-tag>
            <div v-if="row.status === '0' && row.msg" class="state__msg">{{ row.msg }}</div>
          </div>
        </template>
      </t-table>
    </template>

    <div v-else class="empty">
      暂无登录记录
    </div>
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

.state {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.state__msg {
  font-size: 12px;
  color: var(--td-text-color-secondary);
}

.empty {
  padding: 40px 0;
  text-align: center;
  color: var(--td-text-color-placeholder);
}
</style>
