<script setup lang="ts">
import { watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findRebateRecordByPage } from '@/api/rebateRecord';
import type { RebateRecordRow } from '@/api/rebateRecord';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

/** 与 rebate-record 页面一致：来源会员 / 层级标签 / 金额带币种 / 业务类型标签 / 来源订单号（可复制） */
const columns: PrimaryTableCol<RebateRecordRow>[] = [
  { colKey: 'sourceMemberUid', title: '来源会员' },
  { colKey: 'level', title: '层级' },
  { colKey: 'amount', title: '返佣金额', align: 'right' },
  { colKey: 'bizType', title: '业务类型' },
  { colKey: 'sourceOrderId', title: '来源订单号' },
  { colKey: 'createTime', title: '返佣时间' },
];

const { rows, loading, pagination, load, onPageChange, reset } = usePaged<RebateRecordRow>(async (page, pageSize) => {
  const uid = String(props.memberUid ?? '');
  if (!uid) {
    return { content: [], total: 0 };
  }
  return findRebateRecordByPage({ beneficiaryUid: uid, pageNum: page, pageSize });
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
    <div class="hint">仅展示该会员作为受益人收到的返佣</div>
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
      <template #sourceMemberUid="{ row }">
        <span v-if="row.sourceMemberUid != null">{{ row.sourceMemberUid }}</span>
        <span v-else class="table-muted">—</span>
      </template>
      <template #level="{ row }">
        <t-tag size="small" theme="primary" variant="light">第{{ row.level ?? '—' }}级</t-tag>
      </template>
      <template #amount="{ row }">
        <span class="amount-main">{{ row.amount ?? '—' }}</span>
        <span class="amount-asset">{{ row.assetCode || '' }}</span>
      </template>
      <template #bizType="{ row }">
        <t-tag size="small" variant="light">{{ row.bizTypeName || '—' }}</t-tag>
      </template>
      <template #sourceOrderId="{ row }">
        <div v-if="row.sourceOrderId" class="order-no">
          <span class="order-no__text" :title="row.sourceOrderId">{{ row.sourceOrderId }}</span>
          <CopyIconButton :text="row.sourceOrderId" />
        </div>
        <span v-else class="table-muted">—</span>
      </template>
    </t-table>
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

/* 返佣金额 + 币种 */
.amount-main {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.amount-asset {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--td-text-color-secondary);
}

/* 来源订单号 + 复制 */
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

.table-muted {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}
</style>
