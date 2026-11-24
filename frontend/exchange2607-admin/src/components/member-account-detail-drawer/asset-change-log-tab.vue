<script setup lang="ts">
import { watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findMemberAssetChangeLogByPage } from '@/api/memberAssetChangeLog';
import type { MemberAssetChangeLogRow } from '@/api/memberAssetChangeLog';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const ACCOUNT_TYPE_LABEL: Record<string, string> = {
  SPOT: '现货',
  FUTURES: '合约',
  FIAT: '法币',
};

/** 变动类型标签配色：收入绿、支出红（与 member-asset-change-log 页面一致） */
function balanceChangeTheme(change?: number | null): 'success' | 'danger' | 'default' {
  if (change == null) {
    return 'default';
  }
  if (change > 0) {
    return 'success';
  }
  if (change < 0) {
    return 'danger';
  }
  return 'default';
}

function balanceDeltaClass(change?: number | null): string {
  if (change == null) {
    return '';
  }
  if (change > 0) {
    return 'balance-change__delta--in';
  }
  if (change < 0) {
    return 'balance-change__delta--out';
  }
  return '';
}

function formatBalanceDelta(change?: number | null): string {
  if (change == null) {
    return '变化 —';
  }
  const prefix = change > 0 ? '+' : '';
  return `变化 ${prefix}${change}`;
}

/** 与 member-asset-change-log 页面一致：业务订单号 / 变动类型（含备注）/ 金额变化（变动前后） */
const columns: PrimaryTableCol<MemberAssetChangeLogRow>[] = [
  {
    colKey: 'accountType',
    title: '账户',
    cell: (_h, { row }) => ACCOUNT_TYPE_LABEL[row.accountType || ''] || row.accountType || '—',
  },
  { colKey: 'assetCode', title: '币种' },
  { colKey: 'bizOrderNo', title: '业务订单号' },
  { colKey: 'changeType', title: '变动类型' },
  { colKey: 'changeTime', title: '变动时间' },
  { colKey: 'balanceChange', title: '金额变化', align: 'right' },
];

const { rows, loading, pagination, load, onPageChange, reset } = usePaged<MemberAssetChangeLogRow>(async (page, pageSize) => {
  const uid = Number(props.memberUid);
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findMemberAssetChangeLogByPage({ memberUid: uid, pageNum: page, pageSize });
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
      <template #bizOrderNo="{ row }">
        <div v-if="row.bizOrderNo" class="order-no">
          <span class="order-no__text" :title="row.bizOrderNo">{{ row.bizOrderNo }}</span>
          <CopyIconButton :text="row.bizOrderNo" />
        </div>
        <span v-else class="table-muted">—</span>
      </template>
      <template #changeType="{ row }">
        <div class="table-stack change-type-cell">
          <t-tag size="small" :theme="balanceChangeTheme(row.balanceChange)">
            {{ row.changeTypeName || '—' }}
          </t-tag>
          <div v-if="row.note" class="table-muted">{{ row.note }}</div>
        </div>
      </template>
      <template #balanceChange="{ row }">
        <div class="table-stack balance-change">
          <div>
            <span class="balance-change__before">变动前 {{ row.balanceBefore }}</span>
            <span class="balance-change__delta" :class="balanceDeltaClass(row.balanceChange)">
              {{ formatBalanceDelta(row.balanceChange) }}
            </span>
          </div>
          <div class="balance-change__after">变动后 {{ row.balanceAfter }}</div>
        </div>
      </template>
    </t-table>
  </div>
</template>

<style lang="less" scoped>
.tab-body {
  min-height: 200px;
}

/* 业务订单号 + 复制 */
.order-no {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.order-no__text {
  overflow: hidden;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.table-muted {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.change-type-cell {
  align-items: flex-start;
}

.change-type-cell :deep(.t-tag) {
  width: fit-content;
  max-width: 100%;
}

/* 金额变化：变动前 + 变化值 / 变动后 */
.balance-change {
  align-items: flex-end;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.balance-change__before {
  color: var(--td-text-color-secondary);
  font-weight: 400;
}

.balance-change__delta {
  padding-left: 8px;
}

.balance-change__delta--in {
  color: var(--td-success-color);
}

.balance-change__delta--out {
  color: var(--td-error-color);
}

.balance-change__after {
  color: var(--td-text-color-primary);
  font-weight: 500;
}
</style>
