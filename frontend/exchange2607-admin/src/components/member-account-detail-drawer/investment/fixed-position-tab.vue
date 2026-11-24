<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findInvestmentFixedPositionByPage } from '@/api/investmentFixedPosition';
import type { InvestmentFixedPositionRow } from '@/api/investmentFixedPosition';
import { positionStatusTheme } from '@/utils/investmentPositionStatus';

import OrderNoCell from '../order-no-cell.vue';
import { usePaged } from '../use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<InvestmentFixedPositionRow>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'productInfo', title: '产品' },
  { colKey: 'principal', title: '本金', align: 'right' },
  { colKey: 'estimatedTotalInterest', title: '估算总收益', align: 'right' },
  { colKey: 'confirmedInterest', title: '已确认收益', align: 'right' },
  { colKey: 'status', title: '状态' },
  { colKey: 'dateInfo', title: '时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<InvestmentFixedPositionRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findInvestmentFixedPositionByPage({ memberUid: uid, pageNum: page, pageSize });
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
    <template #productInfo="{ row }">
      <div class="table-stack">
        <div class="table-line">
          <span class="table-value">{{ row.assetCode }}</span>
        </div>
        <div class="table-line table-muted">
          锁{{ row.lockDays }}天 · 年化{{ row.annualRate }}%
        </div>
      </div>
    </template>
    <template #status="{ row }">
      <t-tag size="small" variant="light" :theme="positionStatusTheme(row.status)">
        {{ row.statusName || row.status || '—' }}
      </t-tag>
    </template>
    <template #dateInfo="{ row }">
      <div class="table-stack">
        <div class="table-muted">申购：{{ row.investTime || '—' }}</div>
        <div class="table-value">到期：{{ row.maturityDate || '—' }}</div>
      </div>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
@import '../investment-shared.less';
</style>
