<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findAssetTransferByPage } from '@/api/assetTransfer';
import type { AssetTransferRow } from '@/api/assetTransfer';

import OrderNoCell from './order-no-cell.vue';
import { usePaged } from './use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<AssetTransferRow>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'flow', title: '划转方向' },
  { colKey: 'assetCode', title: '币种' },
  { colKey: 'amount', title: '划转金额', align: 'right' },
  { colKey: 'createTime', title: '划转时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<AssetTransferRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findAssetTransferByPage({ memberUid: uid, pageNum: page, pageSize });
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
    <template #orderNo="{ row }">
      <OrderNoCell :value="row.orderNo" />
    </template>
    <template #flow="{ row }">
      <span class="table-flow">
        <t-tag size="small" variant="light" :theme="row.fromAccountType === 'SPOT' ? 'primary' : 'warning'">
          {{ row.fromAccountTypeName || row.fromAccountType }}
        </t-tag>
        <span class="table-flow__arrow">→</span>
        <t-tag size="small" variant="light" :theme="row.toAccountType === 'SPOT' ? 'primary' : 'warning'">
          {{ row.toAccountTypeName || row.toAccountType }}
        </t-tag>
      </span>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
.table-flow {
  display: inline-flex;
  align-items: center;
  gap: 8px;

  &__arrow {
    color: var(--td-text-color-placeholder);
    font-size: 13px;
  }
}
</style>
