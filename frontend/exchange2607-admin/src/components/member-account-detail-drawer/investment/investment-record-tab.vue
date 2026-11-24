<script setup lang="ts">
import { ref, watch } from 'vue';

import type { PrimaryTableCol } from 'tdesign-vue-next';
import { findInvestmentRecordByPage } from '@/api/investmentRecord';
import type { InvestmentRecordRow } from '@/api/investmentRecord';

import OrderNoCell from '../order-no-cell.vue';
import { usePaged } from '../use-paged';

const props = defineProps<{
  memberUid?: number | string;
}>();

const columns: PrimaryTableCol<InvestmentRecordRow>[] = [
  { colKey: 'orderNo', title: '订单号', width: 180, ellipsis: true },
  { colKey: 'productInfo', title: '产品' },
  { colKey: 'principal', title: '申购本金', align: 'right' },
  { colKey: 'rateInfo', title: '利率快照' },
  { colKey: 'createTime', title: '申购时间' },
];

const { rows, loading, pagination, load, reset, onPageChange } =
  usePaged<InvestmentRecordRow>(async (page, pageSize) => {
    const uid = String(props.memberUid ?? '');
    if (!uid) {
      return { content: [], total: 0 };
    }
    return findInvestmentRecordByPage({ memberUid: uid, pageNum: page, pageSize });
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
          <t-tag size="small" variant="light-outline">{{ row.productTypeName }}</t-tag>
        </div>
        <div v-if="row.productType === 'FIXED'" class="table-line table-muted">
          锁{{ row.lockDays }}天
        </div>
      </div>
    </template>
    <template #rateInfo="{ row }">
      <div class="table-stack">
        <div class="table-muted">年化{{ row.annualRate }}%</div>
        <div v-if="row.productType === 'FLEXIBLE' && row.subsidizedRate" class="table-muted">
          补贴{{ row.subsidizedRate }}%<template v-if="row.rewardLimit"> · 奖励限额{{ row.rewardLimit }}</template>
        </div>
      </div>
    </template>
  </t-table>
</template>

<style lang="less" scoped>
@import '../investment-shared.less';
</style>
