<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findInvestmentFlexiblePositionByPage } from '@/api/investmentFlexiblePosition';
import type { InvestmentFlexiblePositionRow } from '@/api/investmentFlexiblePosition';
import { positionStatusTheme } from '@/utils/investmentPositionStatus';

import CopyIconButton from '@/components/copy-icon-button/index.vue';
import OrderNoCell from '../order-no-cell.vue';
import { usePaged } from '../use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<InvestmentFlexiblePositionRow>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'assetCode', title: '币种' },
  { colKey: 'flexibleAccountId', title: '活期账户' },
  { colKey: 'principal', title: '持仓金额', align: 'right' },
  { colKey: 'accruedInterest', title: '累计收益', align: 'right' },
  { colKey: 'status', title: '状态' },
  { colKey: 'investTime', title: '申购时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<InvestmentFlexiblePositionRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findInvestmentFlexiblePositionByPage({ memberUid: uid, pageNum: page, pageSize });
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
    <template #flexibleAccountId="{ row }">
      <div v-if="row.flexibleAccountId" class="table-line">
        <span class="table-value table-ellipsis" :title="row.flexibleAccountId">{{ row.flexibleAccountId }}</span>
        <CopyIconButton :text="row.flexibleAccountId" />
      </div>
      <span v-else class="table-muted">—</span>
    </template>
    <template #status="{ row }">
      <t-tag size="small" variant="light" :theme="positionStatusTheme(row.status)">
        {{ row.statusName || row.status || '—' }}
      </t-tag>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
@import '../investment-shared.less';
</style>
